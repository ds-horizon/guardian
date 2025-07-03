package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OidcTokenEndpointAuthMethod {
  CLIENT_SECRET_BASIC("client_secret_basic"),
  CLIENT_SECRET_POST("client_secret_post");

  private final String value;

  OidcTokenEndpointAuthMethod(String value) {
    this.value = value;
  }

  @JsonValue
  public String toString() {
    return value;
  }
}
