package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum ClientType {
  FIRST_PARTY("first_party"),
  THIRD_PARTY("third_party");

  private final String value;

  ClientType(String clientType) {
    this.value = clientType;
  }
}
