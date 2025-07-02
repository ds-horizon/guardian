package com.dreamsportslabs.guardian.config.tenant;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcIdTokenSigningAlgValue;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.dreamsportslabs.guardian.constant.OidcSubjectType;
import com.dreamsportslabs.guardian.constant.OidcTokenEndpointAuthMethod;
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
  private List<OidcSubjectType> subjectTypesSupported;
  private List<OidcIdTokenSigningAlgValue> idTokenSigningAlgValuesSupported;
  private List<OidcTokenEndpointAuthMethod> tokenEndpointAuthMethodsSupported;
}
