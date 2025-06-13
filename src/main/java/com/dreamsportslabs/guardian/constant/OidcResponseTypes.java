package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum OidcResponseTypes {
  CODE("code");

  private final String type;

  OidcResponseTypes(String type) {
    this.type = type;
  }

  public String getValue() {
    return type;
  }
}
