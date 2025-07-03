package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum OidcIdTokenSigningAlgValue {
  RS256("RS256");

  private final String value;

  OidcIdTokenSigningAlgValue(String value) {
    this.value = value;
  }
}
