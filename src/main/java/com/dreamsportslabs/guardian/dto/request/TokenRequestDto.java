package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.constant.OidcGrantType.AUTHORIZATION_CODE;
import static com.dreamsportslabs.guardian.constant.OidcGrantType.REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_CLIENT;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.UNSUPPORTED_GRANT_TYPE;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.ws.rs.FormParam;
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
    if (StringUtils.isBlank(grantType)) {
      throw INVALID_REQUEST.getJsonCustomException("grant_type is required");
    }
    if (StringUtils.isBlank(clientId) ^ StringUtils.isBlank(clientSecret)) {
      throw INVALID_CLIENT.getException();
    }
    oidcGrantType = OidcGrantType.fromString(grantType);
    switch (oidcGrantType) {
      case AUTHORIZATION_CODE -> {
        if (StringUtils.isBlank(code)) {
          throw INVALID_REQUEST.getJsonCustomException("code is required");
        }
        if (StringUtils.isBlank(redirectUri)) {
          throw INVALID_REQUEST.getJsonCustomException("redirect_uri is required");
        }
      }
      case REFRESH_TOKEN -> {
        if (StringUtils.isBlank(refreshToken)) {
          throw INVALID_REQUEST.getJsonCustomException("refresh_token is required");
        }
      }
      case CLIENT_CREDENTIALS -> {}
      default -> throw UNSUPPORTED_GRANT_TYPE.getException();
    }
  }
}
