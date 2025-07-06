package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum OidcCodeChallengeMethod {
  PLAIN("Plain"),
  S256("S256");

  private final String value;

  OidcCodeChallengeMethod(String value) {
    this.value = value;
  }

  public static boolean isValid(String value) {
    if (value == null) {
      return false;
    }
    for (OidcCodeChallengeMethod method : values()) {
      if (method.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }
}
