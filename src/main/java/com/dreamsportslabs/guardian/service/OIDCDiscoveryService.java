package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.config.tenant.OidcConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.dto.response.OIDCDiscoveryResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.stream.Collectors;
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
        getSupportedScopes(tenantId),
        getSupportedClaims(tenantId),
        OIDCDiscoveryResponseDto::from);
  }

  public Single<List<String>> getSupportedScopes(String tenantId) {
    return scopeDao
        .getScopesWithPagination(tenantId, 0, Integer.MAX_VALUE)
        .map(scopes -> scopes.stream().map(ScopeModel::getName).collect(Collectors.toList()));
  }

  public Single<List<String>> getSupportedClaims(String tenantId) {
    return scopeDao
        .getScopesWithPagination(tenantId, 0, Integer.MAX_VALUE)
        .map(
            scopes ->
                scopes.stream()
                    .flatMap(scope -> scope.getClaims().stream())
                    .distinct()
                    .collect(Collectors.toList()));
  }
}
