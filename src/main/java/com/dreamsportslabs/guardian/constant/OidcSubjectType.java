package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OidcSubjectType {
  PUBLIC("public");

  private final String type;

  OidcSubjectType(String type) {
    this.type = type;
  }

  @JsonValue
  public String toString() {
    return type;
  }
}
