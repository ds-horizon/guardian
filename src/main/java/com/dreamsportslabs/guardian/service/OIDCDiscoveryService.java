package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.config.tenant.OIDCConfig;
import com.dreamsportslabs.guardian.dao.OIDCConfigDao;
import com.dreamsportslabs.guardian.dto.response.OIDCDiscoveryResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OIDCDiscoveryService {

  @Inject private OIDCConfigDao oidcConfigDao;

  public Single<OIDCDiscoveryResponseDto> getOIDCDiscovery(String tenantId) {
    return oidcConfigDao.getOIDCConfig(tenantId).map(this::convertToDiscoveryResponse);
  }

  private OIDCDiscoveryResponseDto convertToDiscoveryResponse(OIDCConfig config) {
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
        .grantTypesSupported(config.getGrantTypesSupported())
        .scopesSupported(config.getScopesSupported())
        .tokenEndpointAuthMethodsSupported(config.getTokenEndpointAuthMethodsSupported())
        .claimsSupported(config.getClaimsSupported())
        .build();
  }
}
