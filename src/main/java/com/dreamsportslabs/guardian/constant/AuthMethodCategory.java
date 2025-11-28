package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum AuthMethodCategory {
  KNOWLEDGE("knowledge"),
  POSSESSION("possession"),
  INHERENCE("inherence");

  private final String value;

  AuthMethodCategory(String value) {
    this.value = value;
  }
}
