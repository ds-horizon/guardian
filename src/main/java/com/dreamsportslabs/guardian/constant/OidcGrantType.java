package com.dreamsportslabs.guardian.constant;

import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.UNSUPPORTED_GRANT_TYPE;

import lombok.Getter;

@Getter
public enum OidcGrantType {
  AUTHORIZATION_CODE("authorization_code"),
  CLIENT_CREDENTIALS("client_credentials"),
  REFRESH_TOKEN("refresh_token");

  private final String type;

  OidcGrantType(String type) {
    this.type = type;
  }

  public static OidcGrantType fromString(String type) {
    for (OidcGrantType grantType : values()) {
      if (grantType.type.equals(type)) {
        return grantType;
      }
    }
    throw UNSUPPORTED_GRANT_TYPE.getJsonCustomException(
        "The grant type '" + type + "' is not supported");
  }
}
