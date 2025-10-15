package com.dreamsportslabs.guardian.constant;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_IDENTIFIER_TYPE;

import lombok.Getter;

@Getter
public enum IdentifierType {
  CODE("code"),
  ID_TOKEN("id_token");

  private final String value;

  IdentifierType(String value) {
    this.value = value;
  }

  public static IdentifierType fromString(String value) {
    for (IdentifierType type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw INVALID_IDENTIFIER_TYPE.getCustomException("Invalid identifier type: " + value);
  }
}
