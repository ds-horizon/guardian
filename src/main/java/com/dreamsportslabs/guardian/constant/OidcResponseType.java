package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OidcResponseType {
  CODE("code");

  private final String type;

  OidcResponseType(String type) {
    this.type = type;
  }

  @JsonValue
  public String getValue() {
    return type;
  }
}
