package com.dreamsportslabs.guardian.registry;

import com.dreamsportslabs.guardian.config.tenant.RsaKey;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.config.tenant.TokenConfig;
import com.dreamsportslabs.guardian.service.impl.idproviders.FacebookIdProvider;
import com.dreamsportslabs.guardian.service.impl.idproviders.GoogleIdProvider;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.rsa.RSASigner;
import lombok.SneakyThrows;

public class RegistryInit {

  @SneakyThrows
  public static TenantConfig initializeRegistry(Registry registry, TenantConfig tenantConfig) {
    String tenantId = tenantConfig.getTenantId();
    registry.put(tenantId, tenantConfig);
    registry.put(tenantId, getTokenSigner(tenantConfig.getTokenConfig()));
    registry.put(tenantId, new FacebookIdProvider(tenantConfig.getFbConfig()));
    registry.put(tenantId, new GoogleIdProvider(tenantConfig.getGoogleConfig()));
    return tenantConfig;
  }

  private static Signer getTokenSigner(TokenConfig config) {
    RsaKey currentKey = config.getRsaKeys().stream().filter(RsaKey::getCurrent).toList().get(0);
    if ("RS512".equals(config.getAlgorithm())) {
      return RSASigner.newSHA512Signer(currentKey.getPrivateKey(), currentKey.getKid());
    } else if ("RS256".equals(config.getAlgorithm())) {
      return RSASigner.newSHA256Signer(currentKey.getPrivateKey(), currentKey.getKid());
    }
    throw new Error("Invalid configuration, only RS256 and RS512 are supported");
  }
}
