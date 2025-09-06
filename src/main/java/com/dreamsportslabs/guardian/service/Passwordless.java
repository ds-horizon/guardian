package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.OTP_RESEND_AFTER;
import static com.dreamsportslabs.guardian.constant.Constants.OTP_RETRIES_LEFT;
import static com.dreamsportslabs.guardian.constant.Constants.STATIC_OTP_NUMBER;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_PHONE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INCORRECT_OTP;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_STATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.RESENDS_EXHAUSTED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.RESEND_NOT_ALLOWED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.RETRIES_EXHAUSTED;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;

import com.dreamsportslabs.guardian.cache.DefaultClientScopesCache;
import com.dreamsportslabs.guardian.config.tenant.OtpConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.constant.Channel;
import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.dao.PasswordlessDao;
import com.dreamsportslabs.guardian.dao.model.PasswordlessModel;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.v1.V1PasswordlessCompleteRequestDto;
import com.dreamsportslabs.guardian.dto.request.v1.V1PasswordlessInitRequestDto;
import com.dreamsportslabs.guardian.dto.request.v2.V2PasswordlessInitRequestDto;
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
  private final ClientService clientService;
  private final DefaultClientScopesCache defaultClientScopesCache;

  public Single<PasswordlessModel> initV1(
      V1PasswordlessInitRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String tenantId) {
    return defaultClientScopesCache
        .getDefaultClientScopes(tenantId)
        .map(
            pair -> {
              V2PasswordlessInitRequestDto v2PasswordlessInitRequestDto =
                  new V2PasswordlessInitRequestDto();
              v2PasswordlessInitRequestDto.setClientId(pair.getLeft());
              v2PasswordlessInitRequestDto.setScopes(pair.getRight());
              v2PasswordlessInitRequestDto.setMetaInfo(requestDto.getMetaInfo());
              v2PasswordlessInitRequestDto.setAdditionalInfo(requestDto.getAdditionalInfo());
              v2PasswordlessInitRequestDto.setFlow(requestDto.getFlow());
              v2PasswordlessInitRequestDto.setContacts(requestDto.getContacts());
              v2PasswordlessInitRequestDto.setState(requestDto.getState());
              v2PasswordlessInitRequestDto.setResponseType(requestDto.getResponseType());
              return v2PasswordlessInitRequestDto;
            })
        .flatMap(
            v2PasswordlessInitRequestDto -> init(v2PasswordlessInitRequestDto, headers, tenantId));
  }

  public Single<PasswordlessModel> init(
      V2PasswordlessInitRequestDto requestDto,
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

    return clientService
        .validateFirstPartyClientAndClientScopes(
            tenantId, requestDto.getClientId(), requestDto.getScopes())
        .andThen(passwordlessModel)
        .flatMap(
            model ->
                userFlowBlockService
                    .isUserBlocked(model, tenantId)
                    .andThen(
                        Single.fromCallable(
                            () -> {
                              if (model.getResends() >= model.getMaxResends()) {
                                passwordlessDao.deletePasswordlessModel(state, tenantId);
                                throw RESENDS_EXHAUSTED.getException();
                              }

                              if ((getCurrentTimeInSeconds()) < model.getResendAfter()) {
                                throw RESEND_NOT_ALLOWED.getCustomException(
                                    Map.of(OTP_RESEND_AFTER, model.getResendAfter()));
                              }

                              return model;
                            })))
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

  private void updateDefaultTemplate(V2PasswordlessInitRequestDto requestDto, String tenantId) {
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
              if (getCurrentTimeInSeconds() > model.getExpiry()) {
                passwordlessDao.deletePasswordlessModel(state, tenantId);
                throw INVALID_STATE.getException();
              }
              return model;
            });
  }

  private Single<PasswordlessModel> createPasswordlessModel(
      V2PasswordlessInitRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
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
                  .expiry(getCurrentTimeInSeconds() + config.getOtpValidity())
                  .scopes(dto.getScopes())
                  .clientId(dto.getClientId())
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
                userFlowBlockService.isUserBlocked(model, tenantId).andThen(Single.just(model)))
        .flatMap(model -> validateOtp(model, dto.getOtp(), tenantId))
        .flatMap(
            model -> {
              if (model.getUser().get(USERID) != null) {
                return Single.just(model);
              }

              UserDto.UserDtoBuilder builder = UserDto.builder();

              for (Map.Entry<String, Object> entry : dto.getAdditionalInfo().entrySet()) {
                model.getAdditionalInfo().putIfAbsent(entry.getKey(), entry.getValue());
              }

              Contact contact = model.getContacts().get(0);
              if (contact.getChannel() == Channel.EMAIL) {
                builder.email(contact.getIdentifier());
              } else {
                builder.phoneNumber(contact.getIdentifier());
              }
              builder.clientId(model.getClientId());

              MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
              model.getHeaders().forEach(headers::add);
              UserDto userDto = builder.build();
              userDto.setAdditionalInfo(model.getAdditionalInfo());

              return userService
                  .createUser(userDto, headers, tenantId)
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
                    String.join(" ", model.getScopes()),
                    List.of(AuthMethod.ONE_TIME_PASSWORD),
                    model.getMetaInfo(),
                    model.getClientId(),
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
