package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_EXP;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_IAT;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_ISS;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_RFT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TokenIssuer {
  private final Vertx vertx;
  private final JWTEncoder encoder = JWT.getEncoder();
  private final Registry registry;

  public Single<String> generateIdToken(JsonObject user, Long iat, TenantConfig config) {
    JWT jwt =
        getBaseJwt(
            user.getString(USERID),
            iat,
            iat + config.getTokenConfig().getIdTokenExpiry(),
            config.getTokenConfig().getIssuer());
    config
        .getTokenConfig()
        .getIdTokenClaims()
        .forEach(
            claim -> {
              Object value = user.getValue(claim);
              if (value != null) {
                jwt.addClaim(claim, value);
              }
            });

    return signToken(jwt, config.getTenantId());
  }

  public Single<String> generateAccessToken(
      String sub, Long iat, String rftId, TenantConfig config) {
    JWT jwt =
        getBaseJwt(
                sub,
                iat,
                iat + config.getTokenConfig().getAccessTokenExpiry(),
                config.getTokenConfig().getIssuer())
            .addClaim(JWT_CLAIMS_RFT_ID, rftId);

    return signToken(jwt, config.getTenantId());
  }

  public String generateRefreshToken() {
    return RandomStringUtils.randomAlphanumeric(32);
  }

  private JWT getBaseJwt(String sub, Long iat, Long exp, String issuer) {
    return new JWT()
        .addClaim(JWT_CLAIMS_IAT, iat)
        .addClaim(JWT_CLAIMS_SUB, sub)
        .addClaim(JWT_CLAIMS_ISS, issuer)
        .addClaim(JWT_CLAIMS_EXP, exp);
  }

  private Single<String> signToken(JWT jwt, String tenantId) {
    return vertx
        .rxExecuteBlocking(
            future -> {
              RSASigner signer = registry.get(tenantId, RSASigner.class);
              future.complete(encoder.encode(jwt, signer));
            },
            false)
        .switchIfEmpty(Single.error(INTERNAL_SERVER_ERROR.getException()))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(String.class::cast);
  }
}
