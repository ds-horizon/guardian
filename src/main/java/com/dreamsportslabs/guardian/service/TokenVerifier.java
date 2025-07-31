package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_TOKEN;
import static com.dreamsportslabs.guardian.utils.Utils.decodeJwtHeaders;

import com.dreamsportslabs.guardian.config.tenant.RsaKey;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.fusionauth.jwt.InvalidJWTSignatureException;
import io.fusionauth.jwt.JWTDecoder;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TokenVerifier {
  private final Registry registry;
  private final JWTDecoder decoder = JWT.getDecoder();

  public Map<String, Object> verifyAccessToken(String accessToken, String tenantId) {

    Map<String, Object> jwtHeaders = decodeJwtHeaders(accessToken);

    String kid = (String) jwtHeaders.get("kid");
    if (StringUtils.isBlank(kid)) {
      throw INVALID_TOKEN.getBearerAuthHeaderException("Invalid token: missing kid in headers");
    }

    String typ = (String) jwtHeaders.get("typ");
    if (typ == null || !typ.equals("at+jwt")) {
      throw INVALID_TOKEN.getBearerAuthHeaderException("Invalid token type");
    }

    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);

    RsaKey rsaKey =
        tenantConfig.getTokenConfig().getRsaKeys().stream()
            .filter(RsaKey -> kid.equals(RsaKey.getKid()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No RSA key found"));

    try {
      JWT jwt = decoder.decode(accessToken, RSAVerifier.newVerifier(rsaKey.getPublicKey()));
      return jwt.getAllClaims();
    } catch (InvalidJWTSignatureException e) {
      throw INVALID_TOKEN.getBearerAuthHeaderException("Token Signature is invalid");
    } catch (JWTExpiredException e) {
      throw INVALID_TOKEN.getBearerAuthHeaderException("Token has expired");
    } catch (Exception e) {
      throw INVALID_TOKEN.getBearerAuthHeaderException();
    }
  }
}
