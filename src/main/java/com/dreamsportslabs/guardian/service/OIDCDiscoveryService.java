package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.config.tenant.OIDCConfig;
import com.dreamsportslabs.guardian.dao.OIDCConfigDao;
import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dto.response.OIDCDiscoveryResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OIDCDiscoveryService {

  @Inject private OIDCConfigDao oidcConfigDao;
  @Inject private ScopeDao scopeDao;

  public Single<OIDCDiscoveryResponseDto> getOIDCDiscovery(String tenantId) {
    return Single.zip(
            oidcConfigDao.getOIDCConfig(tenantId),
            scopeDao.getSupportedScopes(tenantId),
            scopeDao.getSupportedClaims(tenantId),
            (config, supportedScopes, supportedClaims) -> {
              // Set the derived scopes and claims from the scope table
              config.setScopesSupported(supportedScopes);
              config.setClaimsSupported(supportedClaims);
              return mapToDiscoveryResponse(config);
            })
        .doOnSuccess(config -> log.debug("Generated OIDC configuration for tenant: {}", tenantId))
        .doOnError(
            error ->
                log.error("Error generating OIDC configuration for tenant: {}", tenantId, error));
  }

  private OIDCDiscoveryResponseDto mapToDiscoveryResponse(OIDCConfig config) {
    if (config == null) {
      return null;
    }

    return OIDCDiscoveryResponseDto.builder()
        .issuer(config.getIssuer())
        .authorizationEndpoint(config.getAuthorizationEndpoint())
        .tokenEndpoint(config.getTokenEndpoint())
        .userinfoEndpoint(config.getUserinfoEndpoint())
        .revocationEndpoint(config.getRevocationEndpoint())
        .jwksUri(config.getJwksUri())
        .responseTypesSupported(config.getResponseTypesSupported())
        .subjectTypesSupported(config.getSubjectTypesSupported())
        .idTokenSigningAlgValuesSupported(config.getIdTokenSigningAlgValuesSupported())
        .userinfoSigningAlgValuesSupported(config.getUserinfoSigningAlgValuesSupported())
        .tokenEndpointAuthMethodsSupported(config.getTokenEndpointAuthMethodsSupported())
        .grantTypesSupported(config.getGrantTypesSupported())
        .scopesSupported(config.getScopesSupported())
        .claimsSupported(config.getClaimsSupported())
        .build();
  }
}
