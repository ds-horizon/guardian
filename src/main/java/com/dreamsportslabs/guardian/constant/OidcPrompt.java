package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
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

  @JsonCreator
  public static OidcPrompt fromString(String value) {
    if (value == null) return null;
    return OidcPrompt.valueOf(value.toUpperCase());
  }
}
