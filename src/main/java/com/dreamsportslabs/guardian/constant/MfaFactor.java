package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum MfaFactor {
  PASSWORD("password", AuthMethod.PASSWORD),
  PIN("pin", AuthMethod.PIN_OR_PATTERN),
  SMS_OTP("sms-otp", AuthMethod.ONE_TIME_PASSWORD),
  EMAIL_OTP("email-otp", AuthMethod.ONE_TIME_PASSWORD);

  private final String value;
  private final AuthMethod authMethod;

  MfaFactor(String value, AuthMethod authMethod) {
    this.value = value;
    this.authMethod = authMethod;
  }

  public static MfaFactor fromValue(String value) {
    for (MfaFactor factor : values()) {
      if (factor.value.equalsIgnoreCase(value)) {
        return factor;
      }
    }
    throw new IllegalArgumentException("Unknown MFA factor: " + value);
  }
}
