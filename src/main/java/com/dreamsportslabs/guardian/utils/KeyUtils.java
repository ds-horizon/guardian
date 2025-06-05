package com.dreamsportslabs.guardian.utils;

import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.jwt.domain.KeyType;
import io.vertx.core.json.JsonObject;

public class KeyUtils {

  public static JsonObject getKeysInJwksFormat(String publicKey, String kid, String alg) {
    JSONWebKey jwk = JSONWebKey.build(publicKey);
    jwk.kid = kid;
    jwk.kty = KeyType.RSA;
    jwk.alg = Algorithm.fromName(alg);
    return new JsonObject(jwk.toJSON());
  }
}
