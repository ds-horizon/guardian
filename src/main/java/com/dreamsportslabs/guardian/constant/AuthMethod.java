package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum AuthMethod {

  // Reference: https://www.rfc-editor.org/rfc/rfc8176.html
  // Do not change the values, these are as per RFC

  FACE("face"),
  FINGERPRINT("fpt"),
  GEOLOCATION("geo"),
  HARDWARE_KEY_PROOF("hwk"),
  IRIS_SCAN("iris"),
  KNOWLEDGE_BASED_AUTHENTICATION("kba"),
  MULTIPLE_CHANNEL_AUTHENTICATION("mca"),
  MULTIPLE_FACTOR_AUTHENTICATION("mfa"),
  ONE_TIME_PASSWORD("otp"),
  PIN_OR_PATTERN("pin"),
  PASSWORD("pwd"),
  RISK_BASED_AUTHENTICATION("rba"),
  RETINA_SCAN("retina"),
  SMART_CARD("sc"),
  SMS_CONFIRMATION("sms"),
  SOFTWARE_KEY_PROOF("swk"),
  TELEPHONE_CONFIRMATION("tel"),
  USER_PRESENCE_TEST("user"),
  VOICEPRINT("vbm"),
  WINDOWS_INTEGRATED_AUTHENTICATION("wia"),
  THIRD_PARTY_OIDC("oidc"); // Adding this custom as RFC doesn't support this

  private final String value;

  AuthMethod(String authMethod) {
    this.value = authMethod;
  }
}
