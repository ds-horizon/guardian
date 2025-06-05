package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.config.tenant.TokenConfig;
import com.dreamsportslabs.guardian.utils.KeyUtils;
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
                  .map(key -> KeyUtils.getKeysInJwksFormat(key.getPublicKey(), key.getKid(), alg))
                  .collect(Collectors.toList());
            });
  }
}
