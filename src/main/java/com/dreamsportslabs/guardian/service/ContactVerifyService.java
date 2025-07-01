package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.STATIC_OTP_NUMBER;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FLOW_BLOCKED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INCORRECT_OTP;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_STATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.RESENDS_EXHAUSTED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.RESEND_NOT_ALLOWED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.RETRIES_EXHAUSTED;

import com.dreamsportslabs.guardian.config.tenant.ContactVerifyConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.dao.ContactVerifyDao;
import com.dreamsportslabs.guardian.dao.model.OtpGenerateModel;
import com.dreamsportslabs.guardian.dto.request.V1SendOtpRequestDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.OtpUtils;
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
  private final ContactFlowBlockService contactFlowBlockService;

  public Single<OtpGenerateModel> initOtp(
      V1SendOtpRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {
    // Check if OTP flow is blocked for the contact
    return getContactIdentifier(requestDto, tenantId)
        .flatMap(
            contactIdentifier ->
                contactFlowBlockService
                    .isApiBlocked(tenantId, contactIdentifier, "/v1/otp/send")
                    .flatMap(
                        isBlocked -> {
                          if (isBlocked) {
                            log.warn(
                                "OTP send API is blocked for contact: {} in tenant: {}",
                                contactIdentifier,
                                tenantId);
                            return Single.error(
                                FLOW_BLOCKED.getCustomException(
                                    "OTP verify flow is blocked for this contact"));
                          }
                          return Single.just(requestDto);
                        })
                    .flatMap(
                        dto -> {
                          String state = dto.getState();
                          Single<OtpGenerateModel> otpGenerateModel;

                          if (state != null) {
                            otpGenerateModel = this.getOtpGenerateModel(tenantId, state);
                          } else {
                            state = OtpUtils.generateState();
                            TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);

                            OtpUtils.updateContactTemplate(
                                tenantConfig.getSmsConfig(),
                                tenantConfig.getEmailConfig(),
                                dto.getContact());

                            otpGenerateModel =
                                this.createOtpGenerateModel(dto, headers, tenantId, state);
                          }

                          return otpGenerateModel
                              .map(
                                  model -> {
                                    if (model.getResends() >= model.getMaxResends()) {
                                      contactVerifyDao.deleteOtpGenerateModel(
                                          tenantId, model.getState());
                                      throw RESENDS_EXHAUSTED.getException();
                                    }

                                    if ((System.currentTimeMillis() / 1000)
                                        < model.getResendAfter()) {
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
                                        .sendOtp(
                                            List.of(model.getContact()),
                                            model.getOtp(),
                                            headers,
                                            tenantId)
                                        .andThen(Single.just(model));
                                  })
                              .map(OtpGenerateModel::updateResend)
                              .flatMap(
                                  model ->
                                      contactVerifyDao.setOtpGenerateModel(
                                          model, tenantId, model.getState()));
                        }));
  }

  private Single<String> getContactIdentifier(V1SendOtpRequestDto requestDto, String tenantId) {
    if (requestDto.getContact() != null) {
      return Single.just(requestDto.getContact().getIdentifier());
    } else {
      return getOtpGenerateModel(tenantId, requestDto.getState())
          .map(model -> model.getContact().getIdentifier());
    }
  }

  private Single<OtpGenerateModel> createOtpGenerateModel(
      V1SendOtpRequestDto dto,
      MultivaluedMap<String, String> headers,
      String tenantId,
      String state) {
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);

    OtpUtils.updateContactTemplate(
        tenantConfig.getSmsConfig(), tenantConfig.getEmailConfig(), dto.getContact());

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
            .expiry(System.currentTimeMillis() / 1000 + config.getOtpValidity())
            .build());
  }

  public Single<Boolean> verifyOtp(String state, String otp, String tenantId) {
    return getOtpGenerateModel(tenantId, state)
        .flatMap(
            model -> {
              // Check if OTP flow is blocked for the contact
              String contactIdentifier = model.getContact().getIdentifier();
              return contactFlowBlockService
                  .isApiBlocked(tenantId, contactIdentifier, "/v1/otp/verify")
                  .flatMap(
                      isBlocked -> {
                        if (isBlocked) {
                          log.warn(
                              "OTP verify API is blocked for contact: {} in tenant: {}",
                              contactIdentifier,
                              tenantId);
                          return Single.error(
                              FLOW_BLOCKED.getCustomException(
                                  "OTP verify flow is blocked for this contact"));
                        }
                        return Single.just(model);
                      })
                  .flatMap(
                      otpModel -> {
                        if (otpModel.getOtp().equals(otp)) {
                          contactVerifyDao.deleteOtpGenerateModel(tenantId, state);
                          return Single.just(true);
                        }

                        otpModel.incRetry();

                        if (otpModel.getTries() >= otpModel.getMaxTries()) {
                          contactVerifyDao.deleteOtpGenerateModel(tenantId, state);
                          return Single.error(RETRIES_EXHAUSTED.getException());
                        }

                        return contactVerifyDao
                            .setOtpGenerateModel(otpModel, tenantId, state)
                            .flatMap(
                                m ->
                                    Single.error(
                                        INCORRECT_OTP.getCustomException(
                                            Map.of(
                                                "retriesLeft",
                                                otpModel.getMaxTries() - otpModel.getTries()))));
                      });
            });
  }

  private Single<OtpGenerateModel> getOtpGenerateModel(String tenantId, String state) {
    return contactVerifyDao
        .getOtpGenerateModel(tenantId, state)
        .switchIfEmpty(Single.error(INVALID_STATE.getException()))
        .map(
            model -> {
              if (System.currentTimeMillis() / 1000 > model.getExpiry()) {
                contactVerifyDao.deleteOtpGenerateModel(tenantId, state);
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
