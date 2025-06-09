package com.dreamsportslabs.guardian.config.tenant;

import java.util.List;
import lombok.Data;

@Data
public class OIDCConfig {
  private String tenantId;
  private String issuer;
  private String authorizationEndpoint;
  private String tokenEndpoint;
  private String userinfoEndpoint;
  private String revocationEndpoint;
  private String jwksUri;
  private List<String> grantTypesSupported;
  private List<String> responseTypesSupported;
  private List<String> subjectTypesSupported;
  private List<String> idTokenSigningAlgValuesSupported;
  private List<String> userinfoSigningAlgValuesSupported;
  private List<String> tokenEndpointAuthMethodsSupported;
  private List<String> scopesSupported;
  private List<String> claimsSupported;
}
