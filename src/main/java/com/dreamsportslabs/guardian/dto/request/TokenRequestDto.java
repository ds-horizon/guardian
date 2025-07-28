package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CODE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_GRANT_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_REDIRECT_URI;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_CLIENT;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.UNSUPPORTED_GRANT_TYPE;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcTokenEndpointAuthMethod;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class TokenRequestDto {
  @FormParam("grant_type")
  private String grantType;

  @FormParam("code")
  private String code;

  @FormParam("refresh_token")
  private String refreshToken;

  @FormParam("redirect_uri")
  private String redirectUri;

  @FormParam("client_id")
  private String clientId;

  @FormParam("client_secret")
  private String clientSecret;

  @FormParam("code_verifier")
  private String codeVerifier;

  @FormParam("scope")
  private String scope;

  @JsonIgnore private String ip;

  @JsonIgnore private String deviceName;

  @JsonIgnore private OidcGrantType oidcGrantType;

  public void validate() {
    validateGrantType();
    validateClientCredentials();

    switch (oidcGrantType) {
      case AUTHORIZATION_CODE -> validateAuthCodeFlow();
      case REFRESH_TOKEN -> validateRefreshTokenFlow();
      case CLIENT_CREDENTIALS -> {}
      default -> throw UNSUPPORTED_GRANT_TYPE.getException();
    }
  }

  public void setDataFromHeaders(MultivaluedMap<String, String> headers) {
    this.ip = Utils.getIpFromHeaders(headers);
    this.deviceName = Utils.getDeviceNameFromHeaders(headers);
  }

  public void validateAuth(String authorizationHeader, String tenantId, Registry registry) {
    if (StringUtils.isBlank(authorizationHeader) && StringUtils.isBlank(clientId)) {
      throw INVALID_REQUEST.getJsonCustomException(
          "Both 'Authorization' header and 'client_id' parameter are missing");
    }

    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    List<OidcTokenEndpointAuthMethod> endpointAuthMethods =
        tenantConfig.getOidcConfig().getTokenEndpointAuthMethodsSupported();

    if (endpointAuthMethods.contains(OidcTokenEndpointAuthMethod.CLIENT_SECRET_BASIC)
        && endpointAuthMethods.contains(OidcTokenEndpointAuthMethod.CLIENT_SECRET_POST)) {
      if (StringUtils.isNotBlank(authorizationHeader) && StringUtils.isNotBlank(clientId)) {
        throw INVALID_REQUEST.getJsonCustomException(
            "Only one of 'Authorization' header or 'client_id' parameter should be provided");
      }
    } else if (endpointAuthMethods.contains(OidcTokenEndpointAuthMethod.CLIENT_SECRET_BASIC)) {
      if (StringUtils.isBlank(authorizationHeader)) {
        throw INVALID_REQUEST.getJsonCustomException("Authorization header is required");
      }
    } else if (endpointAuthMethods.contains(OidcTokenEndpointAuthMethod.CLIENT_SECRET_POST)) {
      if (StringUtils.isBlank(clientId)) {
        throw INVALID_REQUEST.getJsonCustomException("client_id is required");
      }
    }
  }

  private void validateGrantType() {
    requireNonBlank(grantType, OIDC_GRANT_TYPE);
    oidcGrantType = OidcGrantType.fromString(grantType);
  }

  /** Ensure both client_id and client_secret are either both provided or both missing */
  private void validateClientCredentials() {
    if (StringUtils.isBlank(clientId) ^ StringUtils.isBlank(clientSecret)) {
      throw INVALID_CLIENT.getException();
    }
  }

  private void validateAuthCodeFlow() {
    requireNonBlank(code, OIDC_CODE);
    requireNonBlank(redirectUri, OIDC_REDIRECT_URI);
  }

  private void validateRefreshTokenFlow() {
    requireNonBlank(refreshToken, OIDC_REFRESH_TOKEN);
  }

  private void requireNonBlank(String value, String field) {
    if (StringUtils.isBlank(value)) {
      throw INVALID_REQUEST.getJsonCustomException(field + " is required");
    }
  }
}
