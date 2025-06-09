package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.STATIC_OTP_NUMBER;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.*;

import com.dreamsportslabs.guardian.config.tenant.ContactVerifyConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.dao.ContactVerifyDao;
import com.dreamsportslabs.guardian.dao.model.OtpGenerateModel;
import com.dreamsportslabs.guardian.dto.request.SendOtpRequestDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ContactVerifyService {
  private final ContactVerifyDao contactVerifyDao;
  private final Registry registry;
  private final OtpService otpService;

  public Single<OtpGenerateModel> initOtpOnly(
      SendOtpRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {
    String state = requestDto.getState();
    Single<OtpGenerateModel> otpGenerateModel;

    if (state != null) {
      otpGenerateModel =
          this.getOtpGenerateModel(contactVerifyDao.getCacheKeyForOtp(tenantId, state));
    } else {
      state = Utils.generateState();
      TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
      Utils.updateContactTemplate(tenantConfig, requestDto.getContact());
      otpGenerateModel = this.createOtpGenerateModel(requestDto, headers, tenantId, state);
    }

    String cacheKey = contactVerifyDao.getCacheKeyForOtp(tenantId, state);
    return otpGenerateModel
        .map(
            model -> {
              if (model.getResends() >= model.getMaxResends()) {
                contactVerifyDao.deleteOtpGenerateModel(cacheKey);
                throw RESENDS_EXHAUSTED.getException();
              }

              if ((System.currentTimeMillis() / 1000) < model.getResendAfter()) {
                throw RESEND_NOT_ALLOWED.getCustomException(
                    Map.of("resendAfter", model.getResendAfter()));
              }

              return model;
            })
        .flatMap(
            model -> {
              if (Boolean.TRUE.equals(model.getIsOtpMocked())) {
                return Single.just(model);
              }
              return otpService
                  .sendOtp(List.of(model.getContact()), model.getOtp(), headers, tenantId)
                  .andThen(Single.just(model));
            })
        .map(OtpGenerateModel::updateResend)
        .flatMap(model -> contactVerifyDao.setOtpGenerateModel(model, cacheKey));
  }

  private Single<OtpGenerateModel> createOtpGenerateModel(
      SendOtpRequestDto dto,
      MultivaluedMap<String, String> headers,
      String tenantId,
      String state) {
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    Utils.updateContactTemplate(tenantConfig, dto.getContact());

    ContactVerifyConfig config = tenantConfig.getContactVerifyConfig();

    Map<String, String> h = new HashMap<>();
    headers.forEach((key, val) -> h.put(key, val.get(0)));

    return Single.just(
        OtpGenerateModel.builder()
            .state(state)
            .otp(generateOtp(config, List.of(dto.getContact())))
            .isOtpMocked(config.getIsOtpMocked())
            .resends(-1)
            .resendAfter(0L)
            .resendInterval(config.getOtpResendInterval())
            .maxTries(config.getTryLimit())
            .maxResends(config.getResendLimit())
            .headers(h)
            .contact(dto.getContact())
            .metaInfo(dto.getMetaInfo())
            .expiry(System.currentTimeMillis() / 1000 + config.getOtpValidity())
            .build());
  }

  public Single<Boolean> verifyOtpOnly(String state, String otp, String tenantId) {
    String cacheKey = contactVerifyDao.getCacheKeyForOtp(tenantId, state);
    return getOtpGenerateModel(cacheKey)
        .flatMap(
            model -> {
              if (model.getOtp().equals(otp)) {
                contactVerifyDao.deleteOtpGenerateModel(cacheKey);
                return Single.just(true);
              }

              model.incRetry();

              if (model.getTries() >= model.getMaxTries()) {
                contactVerifyDao.deleteOtpGenerateModel(cacheKey);
                return Single.error(RETRIES_EXHAUSTED.getException());
              }

              return contactVerifyDao
                  .setOtpGenerateModel(model, cacheKey)
                  .flatMap(
                      m ->
                          Single.error(
                              INCORRECT_OTP.getCustomException(
                                  Map.of("retriesLeft", model.getMaxTries() - model.getTries()))));
            });
  }

  private Single<OtpGenerateModel> getOtpGenerateModel(String cacheKey) {
    return contactVerifyDao
        .getOtpGenerateModel(cacheKey)
        .switchIfEmpty(Single.error(INVALID_STATE.getException()))
        .map(
            model -> {
              if (System.currentTimeMillis() / 1000 > model.getExpiry()) {
                contactVerifyDao.deleteOtpGenerateModel(cacheKey);
                throw INVALID_STATE.getException();
              }
              return model;
            });
  }

  private static String generateOtp(ContactVerifyConfig config, List<Contact> contacts) {
    if (Boolean.TRUE.equals(config.getIsOtpMocked())) {
      return StringUtils.repeat(STATIC_OTP_NUMBER, config.getOtpLength());
    }

    for (Contact contact : contacts) {
      String whitelistedOtp = config.getWhitelistedInputs().get(contact.getIdentifier());
      if (whitelistedOtp != null) {
        return whitelistedOtp;
      }
    }
    return RandomStringUtils.random(config.getOtpLength(), false, true);
  }
}
