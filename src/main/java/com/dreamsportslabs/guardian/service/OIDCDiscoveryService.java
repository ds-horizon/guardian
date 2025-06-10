package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.dao.OIDCConfigDao;
import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dto.response.OIDCDiscoveryResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OIDCDiscoveryService {

  private final OIDCConfigDao oidcConfigDao;
  private final ScopeDao scopeDao;

  public Single<OIDCDiscoveryResponseDto> getOIDCDiscovery(String tenantId) {
    return Single.zip(
        oidcConfigDao.getOIDCConfig(tenantId),
        scopeDao.getSupportedScopes(tenantId),
        scopeDao.getSupportedClaims(tenantId),
        (config, scopes, claims) -> {
          if (config == null) {
            throw new IllegalStateException("OIDC config not found for tenant: " + tenantId);
          }

          return OIDCDiscoveryResponseDto.builder()
              .issuer(config.getIssuer())
              .authorizationEndpoint(config.getAuthorizationEndpoint())
              .tokenEndpoint(config.getTokenEndpoint())
              .userinfoEndpoint(config.getUserinfoEndpoint())
              .revocationEndpoint(config.getRevocationEndpoint())
              .jwksUri(config.getJwksUri())
              .grantTypesSupported(config.getGrantTypesSupported())
              .responseTypesSupported(config.getResponseTypesSupported())
              .subjectTypesSupported(config.getSubjectTypesSupported())
              .idTokenSigningAlgValuesSupported(config.getIdTokenSigningAlgValuesSupported())
              .userinfoSigningAlgValuesSupported(config.getUserinfoSigningAlgValuesSupported())
              .tokenEndpointAuthMethodsSupported(config.getTokenEndpointAuthMethodsSupported())
              .scopesSupported(scopes)
              .claimsSupported(claims)
              .build();
        });
  }
}
