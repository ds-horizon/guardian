package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.JWT_HEADERS_KID;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_HEADERS_TYP;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_TENANT_ID_CLAIM;
import static com.dreamsportslabs.guardian.constant.Constants.TYP_JWT_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.fusionauth.jwt.JWTEncoder;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSASigner;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TokenIssuer {
  private final Vertx vertx;
  private final JWTEncoder encoder = JWT.getEncoder();
  private final Registry registry;

  public Single<String> generateIdToken(
      Map<String, Object> claims, JsonObject user, String tenantId) {
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    return generateIdToken(
        claims, user, tenantId, tenantConfig.getTokenConfig().getIdTokenClaims());
  }

  public Single<String> generateIdToken(
      Map<String, Object> claims, JsonObject user, String tenantId, List<String> idTokenClaims) {
    JWT jwt = new JWT();
    for (Map.Entry<String, Object> claim : claims.entrySet()) {
      jwt.addClaim(claim.getKey(), claim.getValue());
    }
    for (String claim : idTokenClaims) {
      Object value = user.getValue(claim);
      if (value != null) {
        jwt.addClaim(claim, value);
      }
    }
    return signToken(jwt, tenantId);
  }

  public Single<String> generateAccessToken(Map<String, Object> claims, String tenantId) {
    JWT jwt = new JWT();
    for (Map.Entry<String, Object> claim : claims.entrySet()) {
      jwt.addClaim(claim.getKey(), claim.getValue());
    }

    Map<String, String> tokenHeaders = new HashMap<>();
    tokenHeaders.put(JWT_HEADERS_TYP, TYP_JWT_ACCESS_TOKEN);
    return signToken(jwt, tenantId, tokenHeaders);
  }

  public String generateRefreshToken() {
    return RandomStringUtils.randomAlphanumeric(32);
  }

  private Single<String> signToken(JWT jwt, String tenantId) {
    return signToken(jwt, tenantId, new HashMap<>());
  }

  private Single<String> signToken(JWT jwt, String tenantId, Map<String, String> headers) {
    return vertx
        .rxExecuteBlocking(
            future -> {
              RSASigner signer = registry.get(tenantId, RSASigner.class);
              future.complete(
                  encoder.encode(
                      jwt,
                      signer,
                      header -> {
                        for (Map.Entry<String, String> entry : headers.entrySet()) {
                          header.set(entry.getKey(), entry.getValue());
                        }
                        header.set(JWT_HEADERS_KID, signer.getKid());
                      }));
            },
            false)
        .switchIfEmpty(Single.error(INTERNAL_SERVER_ERROR.getException()))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(String.class::cast);
  }
}
