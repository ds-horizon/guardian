package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.MESSAGE_CHANNEL;
import static com.dreamsportslabs.guardian.constant.Constants.MESSAGE_TEMPLATE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.MESSAGE_TEMPLATE_PARAMS;
import static com.dreamsportslabs.guardian.constant.Constants.MESSAGE_TEMPLATE_PARAMS_OTP;
import static com.dreamsportslabs.guardian.constant.Constants.MESSAGE_TO;
import static com.dreamsportslabs.guardian.constant.Constants.RESPONSE_BODY_STATUS_CODE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_SERVICE_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_SERVICE_ERROR_400;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_SERVICE_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_SERVICE_ERROR_400;

import com.dreamsportslabs.guardian.config.tenant.EmailConfig;
import com.dreamsportslabs.guardian.config.tenant.SmsConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.Channel;
import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.WebClient;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OtpService {
  private final WebClient webClient;
  private final Registry registry;

  public Completable sendOtp(
      List<Contact> contacts, String otp, MultivaluedMap<String, String> headers, String tenantId) {
    List<Completable> completables = new ArrayList<>();
    for (Contact contact : contacts) {
      contact.getTemplate().getParams().put(MESSAGE_TEMPLATE_PARAMS_OTP, otp);
      if (contact.getChannel().equals(Channel.EMAIL)) {
        completables.add(sendOtpViaEmail(contact, headers, tenantId));
      } else {
        completables.add(sendOtpViaSms(contact, headers, tenantId));
      }
    }
    return Completable.merge(completables);
  }

  public Completable sendOtpViaSms(
      Contact contact, MultivaluedMap<String, String> headers, String tenantId) {
    SmsConfig config = registry.get(tenantId, TenantConfig.class).getSmsConfig();
    return webClient
        .post(config.getPort(), config.getHost(), config.getSendSmsPath())
        .ssl(config.isSslEnabled())
        .putHeaders(Utils.getForwardingHeaders(headers))
        .rxSendJson(
            new JsonObject()
                .put(MESSAGE_CHANNEL, contact.getChannel().getName())
                .put(MESSAGE_TO, contact.getIdentifier())
                .put(MESSAGE_TEMPLATE_NAME, contact.getTemplate().getName())
                .put(MESSAGE_TEMPLATE_PARAMS, contact.getTemplate().getParams()))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(
            res -> {
              int statusCode = res.statusCode();
              JsonObject resBody;

              if (statusCode / 100 != 2) {
                try {
                  resBody = new JsonObject(res.bodyAsString());
                } catch (Exception e) {
                  resBody = new JsonObject();
                }
                resBody.put(RESPONSE_BODY_STATUS_CODE, statusCode);
                if (statusCode / 100 == 4) {
                  throw SMS_SERVICE_ERROR_400.getCustomException(resBody.getMap());
                }
                throw SMS_SERVICE_ERROR.getCustomException(resBody.getMap());
              }
              return res;
            })
        .ignoreElement();
  }

  public Completable sendOtpViaEmail(
      Contact contact, MultivaluedMap<String, String> headers, String tenantId) {
    EmailConfig config = registry.get(tenantId, TenantConfig.class).getEmailConfig();
    return webClient
        .post(config.getPort(), config.getHost(), config.getSendEmailPath())
        .ssl(config.isSslEnabled())
        .putHeaders(Utils.getForwardingHeaders(headers))
        .rxSendJson(
            new JsonObject()
                .put(MESSAGE_CHANNEL, contact.getChannel().getName())
                .put(MESSAGE_TO, contact.getIdentifier())
                .put(MESSAGE_TEMPLATE_NAME, contact.getTemplate().getName())
                .put(MESSAGE_TEMPLATE_PARAMS, contact.getTemplate().getParams()))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(
            res -> {
              int statusCode = res.statusCode();
              JsonObject resBody;

              if (statusCode / 100 != 2) {
                try {
                  resBody = new JsonObject(res.bodyAsString());
                } catch (Exception e) {
                  resBody = new JsonObject();
                }
                resBody.put(RESPONSE_BODY_STATUS_CODE, statusCode);
                if (statusCode / 100 == 4) {
                  throw EMAIL_SERVICE_ERROR_400.getCustomException(resBody.getMap());
                }
                throw EMAIL_SERVICE_ERROR.getCustomException(resBody.getMap());
              }
              return res;
            })
        .ignoreElement();
  }
}
