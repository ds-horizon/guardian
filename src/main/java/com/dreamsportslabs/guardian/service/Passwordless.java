package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.OTP_RESEND_AFTER;
import static com.dreamsportslabs.guardian.constant.Constants.OTP_RETRIES_LEFT;
import static com.dreamsportslabs.guardian.constant.Constants.STATIC_OTP_NUMBER;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_PHONE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FLOW_BLOCKED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INCORRECT_OTP;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_STATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.RESENDS_EXHAUSTED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.RESEND_NOT_ALLOWED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.RETRIES_EXHAUSTED;

import com.dreamsportslabs.guardian.config.tenant.OtpConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.Channel;
import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.dao.PasswordlessDao;
import com.dreamsportslabs.guardian.dao.model.PasswordlessModel;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.V1PasswordlessCompleteRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1PasswordlessInitRequestDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.OtpUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MultivaluedHashMap;
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
public class Passwordless {
  private final UserService userService;
  private final OtpService otpService;
  private final PasswordlessDao passwordlessDao;
  private final AuthorizationService authorizationService;
  private final Registry registry;
  private final UserFlowBlockService userFlowBlockService;

  public Single<PasswordlessModel> init(
      V1PasswordlessInitRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String tenantId) {
    String state = requestDto.getState();
    Single<PasswordlessModel> passwordlessModel;

    if (state != null) {
      passwordlessModel = this.getPasswordlessModel(state, tenantId);
    } else {
      updateDefaultTemplate(requestDto, tenantId);
      passwordlessModel = this.createPasswordlessModel(requestDto, headers, tenantId);
    }

    return passwordlessModel
        .flatMap(
            model ->
                userFlowBlockService
                    .isUserBlocked(model, tenantId)
                    .map(
                        blockedResult -> {
                          if (blockedResult.isBlocked()) {
                            throw FLOW_BLOCKED.getCustomException(blockedResult.getReason());
                          }

                          if (model.getResends() >= model.getMaxResends()) {
                            passwordlessDao.deletePasswordlessModel(state, tenantId);
                            throw RESENDS_EXHAUSTED.getException();
                          }

                          if ((System.currentTimeMillis() / 1000) < model.getResendAfter()) {
                            throw RESEND_NOT_ALLOWED.getCustomException(
                                Map.of(OTP_RESEND_AFTER, model.getResendAfter()));
                          }

                          return model;
                        }))
        .flatMap(
            model -> {
              if (Boolean.TRUE.equals(model.getIsOtpMocked())) {
                return Single.just(model);
              }
              return otpService
                  .sendOtp(model.getContacts(), model.getOtp(), headers, tenantId)
                  .andThen(Single.just(model));
            })
        .map(PasswordlessModel::updateResend)
        .flatMap(model -> passwordlessDao.setPasswordlessModel(model, tenantId));
  }

  private void updateDefaultTemplate(V1PasswordlessInitRequestDto requestDto, String tenantId) {
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    for (Contact contact : requestDto.getContacts()) {
      OtpUtils.updateContactTemplate(
          tenantConfig.getSmsConfig(), tenantConfig.getEmailConfig(), contact);
    }
  }

  private Single<PasswordlessModel> getPasswordlessModel(String state, String tenantId) {
    return passwordlessDao
        .getPasswordlessModel(state, tenantId)
        .switchIfEmpty(Single.error(INVALID_STATE.getException()))
        .map(
            model -> {
              if (System.currentTimeMillis() / 1000 > model.getExpiry()) {
                passwordlessDao.deletePasswordlessModel(state, tenantId);
                throw INVALID_STATE.getException();
              }
              return model;
            });
  }

  private Single<PasswordlessModel> createPasswordlessModel(
      V1PasswordlessInitRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    Map<String, String> userFilters = new HashMap<>();
    for (Contact contact : dto.getContacts()) {
      if (contact.getChannel() == Channel.EMAIL) {
        userFilters.put(USER_FILTERS_EMAIL, contact.getIdentifier());
      }

      if (contact.getChannel() == Channel.SMS) {
        userFilters.put(USER_FILTERS_PHONE, contact.getIdentifier());
      }
    }
    return userService
        .getUser(userFilters, headers, tenantId)
        .map(
            user -> {
              OtpConfig config = registry.get(tenantId, TenantConfig.class).getOtpConfig();
              Map<String, String> h = new HashMap<>();
              headers.forEach((key, val) -> h.put(key, val.get(0)));
              return PasswordlessModel.builder()
                  .state(generateState())
                  .otp(generateOtp(config, dto.getContacts()))
                  .isOtpMocked(config.getIsOtpMocked())
                  .resends(-1)
                  .resendAfter(0L)
                  .resendInterval(config.getOtpResendInterval())
                  .maxTries(config.getTryLimit())
                  .maxResends(config.getResendLimit())
                  .user(user.getMap())
                  .headers(h)
                  .contacts(dto.getContacts())
                  .flow(dto.getFlow())
                  .responseType(dto.getResponseType())
                  .metaInfo(dto.getMetaInfo())
                  .additionalInfo(dto.getAdditionalInfo())
                  .expiry(System.currentTimeMillis() / 1000 + config.getOtpValidity())
                  .build();
            });
  }

  private String generateState() {
    return RandomStringUtils.randomAlphanumeric(10);
  }

  private String generateOtp(OtpConfig config, List<Contact> contacts) {
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

  public Single<Object> complete(V1PasswordlessCompleteRequestDto dto, String tenantId) {

    return getPasswordlessModel(dto.getState(), tenantId)
        .flatMap(
            model ->
                userFlowBlockService
                    .isUserBlocked(model, tenantId)
                    .map(
                        blockedResult -> {
                          if (blockedResult.isBlocked()) {
                            throw FLOW_BLOCKED.getCustomException(blockedResult.getReason());
                          }
                          return model;
                        }))
        .flatMap(model -> validateOtp(model, dto.getOtp(), tenantId))
        .flatMap(
            model -> {
              if (model.getUser().get(USERID) != null) {
                return Single.just(model);
              }

              UserDto.UserDtoBuilder builder = UserDto.builder();
              Contact contact = model.getContacts().get(0);
              if (contact.getChannel() == Channel.EMAIL) {
                builder.email(contact.getIdentifier());
              } else {
                builder.phoneNumber(contact.getIdentifier());
              }
              builder.additionalInfo(model.getAdditionalInfo());

              MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
              model.getHeaders().forEach(headers::add);

              return userService
                  .createUser(builder.build(), headers, tenantId)
                  .map(
                      user -> {
                        model.setUser(user.getMap());
                        return model;
                      });
            })
        .flatMap(
            model ->
                authorizationService.generate(
                    new JsonObject(model.getUser()),
                    model.getResponseType(),
                    model.getMetaInfo(),
                    tenantId))
        .doOnSuccess(res -> passwordlessDao.deletePasswordlessModel(dto.getState(), tenantId));
  }

  private Single<PasswordlessModel> validateOtp(
      PasswordlessModel model, String otp, String tenantId) {
    if (model.getOtp().equals(otp)) {
      return Single.just(model);
    }

    model.incRetry();

    if (model.getTries() >= model.getMaxTries()) {
      passwordlessDao.deletePasswordlessModel(model.getState(), tenantId);
      throw RETRIES_EXHAUSTED.getException();
    }

    return passwordlessDao
        .setPasswordlessModel(model, tenantId)
        .map(
            m -> {
              throw INCORRECT_OTP.getCustomException(
                  Map.of(OTP_RETRIES_LEFT, model.getMaxTries() - model.getTries()));
            });
  }
}
