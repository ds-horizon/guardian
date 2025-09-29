package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.IS_NEW_USER;
import static com.dreamsportslabs.guardian.constant.Constants.PROVIDER;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_SERVICE_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_SERVICE_ERROR_400;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.config.tenant.UserConfig;
import com.dreamsportslabs.guardian.dto.Provider;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
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

              if (res.statusCode() / 100 == 2) {
                resBody.put(IS_NEW_USER, resBody.getString(USERID) == null);
                return resBody;
              } else if (res.statusCode() / 100 == 4) {
                throw USER_SERVICE_ERROR_400.getCustomException(resBody.getMap());
              } else {
                throw USER_SERVICE_ERROR.getCustomException(resBody.getMap());
              }
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
              resBody.put(IS_NEW_USER, true);
              return resBody;
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

  public Completable addProvider(
      String userId, MultivaluedMap<String, String> headers, Provider provider, String tenantId) {
    UserConfig userConfig = registry.get(tenantId, TenantConfig.class).getUserConfig();
    return webClient
        .post(userConfig.getPort(), userConfig.getHost(), userConfig.getAddProviderPath())
        .ssl(userConfig.getIsSslEnabled())
        .putHeaders(Utils.getForwardingHeaders(headers))
        .rxSendJson(new JsonObject().put(USERID, userId).put(PROVIDER, provider))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(
            res -> {
              if (res.statusCode() / 100 != 2) {
                JsonObject resBody = res.bodyAsJsonObject();
                throw USER_SERVICE_ERROR.getCustomException(resBody.getMap());
              }
              return res;
            })
        .ignoreElement();
  }

  public Single<JsonObject> getOidcUser(
      Map<String, String> userFilters, MultivaluedMap<String, String> headers, String tenantId) {
    UserConfig userConfig = registry.get(tenantId, TenantConfig.class).getUserConfig();

    HttpRequest<Buffer> request =
        webClient.get(userConfig.getPort(), userConfig.getHost(), userConfig.getGetUserPath());
    userFilters.forEach(request::addQueryParam);

    return request
        .putHeaders(Utils.getForwardingHeaders(headers))
        .ssl(userConfig.getIsSslEnabled())
        .rxSend()
        .onErrorResumeNext(
            err -> {
              log.error("Error in fetching OIDC user info :: {}", err.getMessage());
              return Single.error(
                  OidcErrorEnum.INTERNAL_SERVER_ERROR.getJsonCustomException(err.getMessage()));
            })
        .map(
            res -> {
              JsonObject resBody = res.bodyAsJsonObject();
              if (res.statusCode() / 100 != 2) {
                log.error(
                    "Error fetching OIDC user details. Status: {} Response Body: {}",
                    res.statusCode(),
                    resBody.toString());
                throw OidcErrorEnum.USER_SERVICE_ERROR.getJsonException();
              }
              return Utils.convertKeysToSnakeCase(resBody);
            });
  }
}
