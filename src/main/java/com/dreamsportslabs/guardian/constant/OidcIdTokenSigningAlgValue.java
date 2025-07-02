package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OidcIdTokenSigningAlgValue {
  RS256("RS256"),
  NONE("none");

  private final String value;

  OidcIdTokenSigningAlgValue(String value) {
    this.value = value;
  }

  @JsonValue
  public String toString() {
    return value;
  }
}
