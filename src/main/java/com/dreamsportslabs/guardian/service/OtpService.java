package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_SERVICE_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_SERVICE_ERROR;

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
      contact.getTemplate().getParams().put("otp", otp);
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
                .put("channel", contact.getChannel().getName())
                .put("to", contact.getIdentifier())
                .put("templateName", contact.getTemplate().getName())
                .put("templateParams", contact.getTemplate().getParams()))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(
            res -> {
              if (res.statusCode() / 100 != 2) {
                throw SMS_SERVICE_ERROR.getCustomException(res.bodyAsJsonObject().getMap());
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
                .put("channel", contact.getChannel().getName())
                .put("to", contact.getIdentifier())
                .put("templateName", contact.getTemplate().getName())
                .put("templateParams", contact.getTemplate().getParams()))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(
            res -> {
              if (res.statusCode() / 100 != 2) {
                throw EMAIL_SERVICE_ERROR.getCustomException(res.bodyAsJsonObject().getMap());
              }
              return res;
            })
        .ignoreElement();
  }
}
