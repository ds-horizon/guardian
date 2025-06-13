package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum OidcGrantTypes {
  AUTHORIZATION_CODE("authorization_code"),
  CLIENT_CREDENTIALS("client_credentials"),
  REFRESH_TOKEN("refresh_token");

  private final String type;

  OidcGrantTypes(String type) {
    this.type = type;
  }

  public String getValue() {
    return type;
  }
}
