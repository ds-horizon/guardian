package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_SERVICE_ERROR;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.config.tenant.UserConfig;
import com.dreamsportslabs.guardian.dto.Provider;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import io.vertx.rxjava3.ext.web.client.WebClient;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserService {
  private final WebClient webClient;
  private final Registry registry;

  public Single<JsonObject> getUser(
      Map<String, String> userFilters, MultivaluedMap<String, String> headers, String tenantId) {
    UserConfig userConfig = registry.get(tenantId, TenantConfig.class).getUserConfig();

    HttpRequest<Buffer> request =
        webClient.get(userConfig.getPort(), userConfig.getHost(), userConfig.getGetUserPath());
    userFilters.forEach(request::addQueryParam);
    return request
        .putHeaders(Utils.getForwardingHeaders(headers))
        .ssl(userConfig.getIsSslEnabled())
        .rxSend()
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(
            res -> {
              JsonObject resBody = res.bodyAsJsonObject();
              if (res.statusCode() / 100 != 2) {
                throw USER_SERVICE_ERROR.getCustomException(resBody.getMap());
              }
              return resBody;
            });
  }

  public Single<JsonObject> createUser(
      UserDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    UserConfig userConfig = registry.get(tenantId, TenantConfig.class).getUserConfig();
    return webClient
        .post(userConfig.getPort(), userConfig.getHost(), userConfig.getCreateUserPath())
        .ssl(userConfig.getIsSslEnabled())
        .putHeaders(Utils.getForwardingHeaders(headers))
        .rxSendJson(dto)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(
            res -> {
              JsonObject resBody = res.bodyAsJsonObject();
              if (res.statusCode() / 100 != 2 || !resBody.containsKey(USERID)) {
                throw USER_SERVICE_ERROR.getCustomException(resBody.getMap());
              } else if (!resBody.containsKey(USERID)) {
                throw USER_SERVICE_ERROR.getException();
              }
              return resBody.put("isNewUser", true);
            });
  }

  public Single<JsonObject> authenticate(
      UserDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    UserConfig userConfig = registry.get(tenantId, TenantConfig.class).getUserConfig();
    return webClient
        .post(userConfig.getPort(), userConfig.getHost(), userConfig.getAuthenticateUserPath())
        .ssl(userConfig.getIsSslEnabled())
        .putHeaders(Utils.getForwardingHeaders(headers))
        .rxSendJson(dto)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(
            res -> {
              JsonObject resBody = res.bodyAsJsonObject();
              if (res.statusCode() != 200) {
                throw USER_SERVICE_ERROR.getCustomException(resBody.getMap());
              } else if (!resBody.containsKey(USERID)) {
                throw USER_SERVICE_ERROR.getException();
              }
              return res.bodyAsJsonObject();
            });
  }

  public Single<JsonObject> addProvider(
      String userId, MultivaluedMap<String, String> headers, Provider provider, String tenantId) {
    UserConfig userConfig = registry.get(tenantId, TenantConfig.class).getUserConfig();
    return webClient
        .post(userConfig.getPort(), userConfig.getHost(), userConfig.getAddProviderPath())
        .ssl(userConfig.getIsSslEnabled())
        .putHeaders(Utils.getForwardingHeaders(headers))
        .rxSendJson(new JsonObject().put(USERID, userId).put("provider", provider))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(
            res -> {
              JsonObject resBody = res.bodyAsJsonObject();
              if (res.statusCode() / 100 != 2) {
                throw USER_SERVICE_ERROR.getCustomException(resBody.getMap());
              }
              return resBody;
            });
  }
}
