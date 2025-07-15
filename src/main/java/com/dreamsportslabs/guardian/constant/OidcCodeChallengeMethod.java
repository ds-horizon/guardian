package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum OidcCodeChallengeMethod {
  PLAIN("plain"),
  S256("S256");

  private final String value;

  OidcCodeChallengeMethod(String value) {
    this.value = value;
  }

  @JsonCreator
  public static OidcCodeChallengeMethod fromString(String value) {
    if (value == null) return null;
    return OidcCodeChallengeMethod.valueOf(value.toUpperCase());
  }
}
