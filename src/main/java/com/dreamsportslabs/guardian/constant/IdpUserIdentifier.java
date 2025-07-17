package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum IdpUserIdentifier {
  EMAIL("email"),
  PHONE("phone"),
  SUB("sub");

  private final String value;

  IdpUserIdentifier(String value) {
    this.value = value;
  }

  public static IdpUserIdentifier fromString(String value) {
    for (IdpUserIdentifier identifier : IdpUserIdentifier.values()) {
      if (identifier.value.equals(value)) {
        return identifier;
      }
    }
    return EMAIL;
  }
}
