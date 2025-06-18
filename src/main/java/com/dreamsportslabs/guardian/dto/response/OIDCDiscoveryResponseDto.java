package com.dreamsportslabs.guardian.dto.response;

import com.dreamsportslabs.guardian.config.tenant.OidcConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OIDCDiscoveryResponseDto {
  @JsonProperty("issuer")
  private String issuer;

  @JsonProperty("authorization_endpoint")
  private String authorizationEndpoint;

  @JsonProperty("token_endpoint")
  private String tokenEndpoint;

  @JsonProperty("userinfo_endpoint")
  private String userinfoEndpoint;

  @JsonProperty("revocation_endpoint")
  private String revocationEndpoint;

  @JsonProperty("jwks_uri")
  private String jwksUri;

  @JsonProperty("response_types_supported")
  private List<String> responseTypesSupported;

  @JsonProperty("subject_types_supported")
  private List<String> subjectTypesSupported;

  @JsonProperty("id_token_signing_alg_values_supported")
  private List<String> idTokenSigningAlgValuesSupported;

  @JsonProperty("userinfo_signing_alg_values_supported")
  private List<String> userinfoSigningAlgValuesSupported;

  @JsonProperty("grant_types_supported")
  private List<String> grantTypesSupported;

  @JsonProperty("scopes_supported")
  private List<String> scopesSupported;

  @JsonProperty("token_endpoint_auth_methods_supported")
  private List<String> tokenEndpointAuthMethodsSupported;

  @JsonProperty("claims_supported")
  private List<String> claimsSupported;

  public static OIDCDiscoveryResponseDto from(
      OidcConfig config, List<String> scopes, List<String> claims) {
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
  }
}
