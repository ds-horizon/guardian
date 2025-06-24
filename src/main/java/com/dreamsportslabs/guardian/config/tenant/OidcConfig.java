package com.dreamsportslabs.guardian.config.tenant;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import java.util.List;
import lombok.Data;

@Data
public class OidcConfig {
  private String tenantId;
  private String issuer;
  private String authorizationEndpoint;
  private String tokenEndpoint;
  private String userinfoEndpoint;
  private String revocationEndpoint;
  private String jwksUri;
  private String loginPageUri;
  private String consentPageUri;
  private Integer authorizeTtl;
  private List<OidcGrantType> grantTypesSupported;
  private List<OidcResponseType> responseTypesSupported;
  private List<String> subjectTypesSupported;
  private List<String> idTokenSigningAlgValuesSupported;
  private List<String> tokenEndpointAuthMethodsSupported;
}
