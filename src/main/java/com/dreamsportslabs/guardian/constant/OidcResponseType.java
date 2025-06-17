package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum OidcResponseType {
  CODE("code");

  private final String type;

  OidcResponseType(String type) {
    this.type = type;
  }

  public String getValue() {
    return type;
  }
}
