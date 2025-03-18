package com.dreamsportslabs.guardian.service.impl.idproviders;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.config.tenant.FbConfig;
import com.dreamsportslabs.guardian.injection.GuiceInjector;
import com.dreamsportslabs.guardian.service.IdProvider;
import com.google.common.hash.Hashing;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.WebClient;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class FacebookIdProvider implements IdProvider {
  private final WebClient webClient;
  private final String appSecret;
  private final String fields = "id,name,first_name,middle_name,last_name,email,picture";

  public FacebookIdProvider(FbConfig fbConfigDto) {
    this.webClient = GuiceInjector.getGuiceInjector().getInstance(WebClient.class);
    this.appSecret = fbConfigDto.getAppSecret();
  }

  @Override
  public Single<JsonObject> getUserIdentity(String accessToken) {
    String appSecret =
        Hashing.hmacSha256(this.appSecret.getBytes())
            .hashString(accessToken, StandardCharsets.UTF_8)
            .toString();
    return webClient
        .get(443, "graph.facebook.com", "/me")
        .ssl(true)
        .addQueryParam("access_token", accessToken)
        .addQueryParam("appsecret_proof", appSecret)
        .addQueryParam("fields", this.fields)
        .rxSend()
        .map(
            res -> {
              if (res.statusCode() == 200) {
                JsonObject jsonBody = res.bodyAsJsonObject();
                if (StringUtils.isNotBlank(jsonBody.getString("email"))) {
                  return jsonBody;
                } else {
                  throw INVALID_REQUEST.getCustomException("Email unavailable");
                }
              } else if (res.statusCode() == 400) {
                throw INVALID_REQUEST.getCustomException("Invalid access token");
              } else {
                throw INTERNAL_SERVER_ERROR.getException();
              }
            });
  }
}
