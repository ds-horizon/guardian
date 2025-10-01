package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum LogoutType {
  TOKEN("token"),
  CLIENT("client"),
  TENANT("tenant");

  private final String value;

  LogoutType(String value) {
    this.value = value;
  }
}
