package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.dao.ConfigDao;
import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dto.response.OIDCDiscoveryResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OIDCDiscoveryService {

  private final ConfigDao configDao;
  private final ScopeDao scopeDao;

  public Single<OIDCDiscoveryResponseDto> getOIDCDiscovery(String tenantId) {
    return Single.zip(
        configDao.getOidcConfig(tenantId),
        scopeDao.getSupportedScopes(tenantId),
        scopeDao.getSupportedClaims(tenantId),
        (config, scopes, claims) -> {
          if (config == null) {
            throw new IllegalStateException("OIDC config not found for tenant: " + tenantId);
          }
          return OIDCDiscoveryResponseDto.from(config, scopes, claims);
        });
  }
}
