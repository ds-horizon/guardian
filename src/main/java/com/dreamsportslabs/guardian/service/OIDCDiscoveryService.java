package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.config.tenant.OidcConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dto.response.OIDCDiscoveryResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OIDCDiscoveryService {

  private final ScopeDao scopeDao;
  private final Registry registry;

  public Single<OIDCDiscoveryResponseDto> getOIDCDiscovery(String tenantId) {
    OidcConfig oidcConfig = registry.get(tenantId, TenantConfig.class).getOidcConfig();
    if (oidcConfig == null) {
      throw new IllegalStateException("OIDC config not found for tenant: " + tenantId);
    }
    return Single.zip(
        Single.just(oidcConfig),
        scopeDao.getSupportedScopes(tenantId),
        scopeDao.getSupportedClaims(tenantId),
        OIDCDiscoveryResponseDto::from);
  }
}
