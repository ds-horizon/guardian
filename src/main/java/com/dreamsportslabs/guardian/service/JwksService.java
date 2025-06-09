package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.config.tenant.TokenConfig;
import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.jwt.domain.KeyType;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class JwksService {

  private final TenantCache tenantCache;

  public Single<List<JsonObject>> getJwks(String tenantId) {
    return tenantCache
        .getTenantConfig(tenantId)
        .map(
            tenantConfig -> {
              TokenConfig tokenConfig = tenantConfig.getTokenConfig();
              String alg = tokenConfig.getAlgorithm();
              return tokenConfig.getRsaKeys().stream()
                  .map(key -> getKeysInJwksFormat(key.getPublicKey(), key.getKid(), alg))
                  .collect(Collectors.toList());
            });
  }

  public static JsonObject getKeysInJwksFormat(String publicKey, String kid, String alg) {
    JSONWebKey jwk = JSONWebKey.build(publicKey);
    jwk.kid = kid;
    jwk.kty = KeyType.RSA;
    jwk.alg = Algorithm.fromName(alg);
    return new JsonObject(jwk.toJSON());
  }
}
