package com.dreamsportslabs.guardian.jwtVerifier;

import static com.dreamsportslabs.guardian.jwtVerifier.constants.Constants.*;

import com.dreamsportslabs.guardian.jwtVerifier.exception.InvalidTokenException;
import io.fusionauth.jwt.JWTDecoder;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.ec.ECVerifier;
import io.fusionauth.jwt.rsa.RSAVerifier;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TokenVerifier {
  private final JWTDecoder decoder;
  private final PublicKeysManager publicKeysManager;
  private final String issuer;

  public TokenVerifier(String publicCertsEncodedUrl, String issuer) {
    this(publicCertsEncodedUrl, issuer, DEFAULT_REFRESH_TIMEOUT);
  }

  public TokenVerifier(String publicCertsEncodedUrl, String issuer, Long refreshTimeout) {
    this.issuer = issuer;
    this.decoder = JWT.getDecoder();
    this.publicKeysManager =
        PublicKeysManager.builder()
            .publicCertsEncodedUrl(publicCertsEncodedUrl)
            .refreshTimeout(refreshTimeout)
            .build();
  }

  public TokenVerifier(Map<String, String> publicKeys, String issuer) {
    this.issuer = issuer;
    this.decoder = JWT.getDecoder();
    this.publicKeysManager = PublicKeysManager.builder().publicKeys(publicKeys).build();
  }

  public Map<String, Object> verify(String token) {
    JWT jwt = this.decoder.decode(token, this::getVerifier);

    Map<String, Object> claims = jwt.getAllClaims();

    verifyIssuerClaim(claims);

    return claims;
  }

  public Map<String, Object> verify(String token, String audience) {
    Map<String, Object> claims = verify(token);
    verifyAudienceClaim(claims, audience);

    return claims;
  }

  private Verifier getVerifier(String keyId) {
    PublicKey publicKey = this.publicKeysManager.getPublicKeyMap().get(keyId);
    if (publicKey == null) {
      throw new InvalidTokenException("Invalid Signature");
    }
    if (publicKey instanceof RSAPublicKey) {
      return RSAVerifier.newVerifier(publicKey);
    } else if (publicKey instanceof ECPublicKey) {
      return ECVerifier.newVerifier(publicKey);
    } else {
      throw new InvalidTokenException("Unsupported Algorithm");
    }
  }

  private void verifyIssuerClaim(Map<String, Object> claims) {
    if (!claims.get(JWT_CLAIMS_ISSUER).equals(this.issuer)) {
      throw new InvalidTokenException("Invalid issuer");
    }
  }

  private void verifyAudienceClaim(Map<String, Object> claims, String audience) {
    List<String> audienceList = getAudienceList(claims);

    if (audienceList == null || !audienceList.contains(audience)) {
      throw new InvalidTokenException("Invalid audience");
    }
  }

  private List<String> getAudienceList(Map<String, Object> claims) {
    Object audienceClaim = claims.get(JWT_CLAIMS_AUDIENCE);
    List<String> audienceList = null;
    if (audienceClaim instanceof String) {
      audienceList = Arrays.asList(((String) audienceClaim).split(","));
    } else if (audienceClaim instanceof List) {
      audienceList = (List<String>) audienceClaim;
    }
    return audienceList;
  }
}
