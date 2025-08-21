package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum OidcResponseType {
  CODE("code");

  private final String value;

  OidcResponseType(String value) {
    this.value = value;
  }

  @JsonCreator
  public static OidcResponseType fromString(String value) {
    if (value == null) return null;
    return OidcResponseType.valueOf(value.toUpperCase());
  }
}
