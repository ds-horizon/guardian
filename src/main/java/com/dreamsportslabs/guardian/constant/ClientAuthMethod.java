package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum ClientAuthMethod {
  BASIC("client_secret_basic"),
  POST("client_secret_post");

  private final String value;

  ClientAuthMethod(String clientAuthMethod) {
    this.value = clientAuthMethod;
  }
}
