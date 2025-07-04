package com.dreamsportslabs.guardian.dto.response;

import com.dreamsportslabs.guardian.config.tenant.OidcConfig;
import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcIdTokenSigningAlgValue;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.dreamsportslabs.guardian.constant.OidcSubjectType;
import com.dreamsportslabs.guardian.constant.OidcTokenEndpointAuthMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OidcDiscoveryResponseDto {
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
  private List<OidcResponseType> responseTypesSupported;

  @JsonProperty("subject_types_supported")
  private List<OidcSubjectType> subjectTypesSupported;

  @JsonProperty("id_token_signing_alg_values_supported")
  private List<OidcIdTokenSigningAlgValue> idTokenSigningAlgValuesSupported;

  @JsonProperty("grant_types_supported")
  private List<OidcGrantType> grantTypesSupported;

  @JsonProperty("scopes_supported")
  private List<String> scopesSupported;

  @JsonProperty("token_endpoint_auth_methods_supported")
  private List<OidcTokenEndpointAuthMethod> tokenEndpointAuthMethodsSupported;

  @JsonProperty("claims_supported")
  private List<String> claimsSupported;

  public static OidcDiscoveryResponseDto from(
      OidcConfig config, List<String> scopes, List<String> claims) {
    return OidcDiscoveryResponseDto.builder()
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
        .tokenEndpointAuthMethodsSupported(config.getTokenEndpointAuthMethodsSupported())
        .scopesSupported(scopes)
        .claimsSupported(claims)
        .build();
  }
}
