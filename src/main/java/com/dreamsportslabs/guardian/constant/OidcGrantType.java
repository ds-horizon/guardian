package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonValue;
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

  @JsonValue
  public String getValue() {
    return type;
  }
}
