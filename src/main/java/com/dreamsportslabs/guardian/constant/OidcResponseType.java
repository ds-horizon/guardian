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

  public static boolean isValid(String value) {
    if (value == null) {
      return false;
    }
    for (OidcResponseType type : values()) {
      if (type.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }
}
