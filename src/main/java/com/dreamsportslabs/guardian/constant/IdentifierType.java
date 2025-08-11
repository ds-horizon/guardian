package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum IdentifierType {
  CODE("code"),
  ID_TOKEN("id_token");

  private final String value;

  IdentifierType(String value) {
    this.value = value;
  }
}
