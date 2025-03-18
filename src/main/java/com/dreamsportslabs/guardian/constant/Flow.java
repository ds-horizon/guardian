package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum Flow {
  SIGNINUP("signinup"),
  SIGNIN("signin"),
  SIGNUP("signup");

  private final String flow;

  Flow(String flow) {
    this.flow = flow;
  }
}
