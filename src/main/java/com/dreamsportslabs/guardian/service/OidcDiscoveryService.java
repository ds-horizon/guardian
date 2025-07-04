package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_NOT_EXISTS;

import com.dreamsportslabs.guardian.config.tenant.OidcConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.dto.response.OidcDiscoveryResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcDiscoveryService {

  private final ScopeDao scopeDao;
  private final Registry registry;

  public Single<OidcDiscoveryResponseDto> getOidcDiscovery(String tenantId) {
    OidcConfig oidcConfig = registry.get(tenantId, TenantConfig.class).getOidcConfig();
    if (oidcConfig == null) {
      return Single.error(
          OIDC_CONFIG_NOT_EXISTS.getCustomException(
              "oidc Config not found for the tenant: " + tenantId));
    }

    return scopeDao
        .oidcScopes(tenantId)
        .map(
            list -> {
              List<String> scopeNames =
                  list.stream().map(ScopeModel::getName).collect(Collectors.toList());
              List<String> claims =
                  list.stream().map(ScopeModel::getClaims).flatMap(List::stream).toList();
              return OidcDiscoveryResponseDto.from(oidcConfig, scopeNames, claims);
            });
  }
}
