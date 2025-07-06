package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum OidcPrompt {
  LOGIN("login"),
  CONSENT("consent"),
  NONE("none"),
  SELECT_ACCOUNT("select_account");

  private final String value;

  OidcPrompt(String value) {
    this.value = value;
  }

  public static boolean isValid(String value) {
    if (value == null) {
      return false;
    }
    for (OidcPrompt prompt : values()) {
      if (prompt.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }
}
