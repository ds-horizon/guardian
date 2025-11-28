package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum Channel {
  EMAIL("email"),
  SMS("sms");

  private final String name;

  Channel(String name) {
    this.name = name;
  }

  @JsonCreator
  public static Channel fromValue(String value) {
    for (Channel channel : Channel.values()) {
      if (channel.name.equalsIgnoreCase(value)) {
        return channel;
      }
    }
    return null;
  }
}
