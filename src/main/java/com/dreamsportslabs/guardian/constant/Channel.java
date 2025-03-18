package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum Channel {
  EMAIL("email"),
  SMS("sms");

  private final String name;

  Channel(String name) {
    this.name = name;
  }
}
