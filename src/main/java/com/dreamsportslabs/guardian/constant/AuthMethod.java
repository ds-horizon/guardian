package com.dreamsportslabs.guardian.constant;

import lombok.Getter;

@Getter
public enum AuthMethod {

  // Reference: https://www.rfc-editor.org/rfc/rfc8176.html
  // Do not change the values, these are as per RFC

  FACE("face", AuthMethodCategory.INHERENCE),
  FINGERPRINT("fpt", AuthMethodCategory.INHERENCE),
  GEOLOCATION("geo", AuthMethodCategory.POSSESSION),
  HARDWARE_KEY_PROOF("hwk", AuthMethodCategory.POSSESSION),
  IRIS_SCAN("iris", AuthMethodCategory.INHERENCE),
  KNOWLEDGE_BASED_AUTHENTICATION("kba", AuthMethodCategory.KNOWLEDGE),
  MULTIPLE_CHANNEL_AUTHENTICATION("mca", AuthMethodCategory.POSSESSION),
  MULTIPLE_FACTOR_AUTHENTICATION("mfa", AuthMethodCategory.KNOWLEDGE),
  ONE_TIME_PASSWORD("otp", AuthMethodCategory.POSSESSION),
  PIN_OR_PATTERN("pin", AuthMethodCategory.KNOWLEDGE),
  PASSWORD("pwd", AuthMethodCategory.KNOWLEDGE),
  RISK_BASED_AUTHENTICATION("rba", AuthMethodCategory.KNOWLEDGE),
  RETINA_SCAN("retina", AuthMethodCategory.INHERENCE),
  SMART_CARD("sc", AuthMethodCategory.POSSESSION),
  SMS_CONFIRMATION("sms", AuthMethodCategory.POSSESSION),
  SOFTWARE_KEY_PROOF("swk", AuthMethodCategory.POSSESSION),
  TELEPHONE_CONFIRMATION("tel", AuthMethodCategory.POSSESSION),
  USER_PRESENCE_TEST("user", AuthMethodCategory.POSSESSION),
  VOICEPRINT("vbm", AuthMethodCategory.INHERENCE),
  WINDOWS_INTEGRATED_AUTHENTICATION("wia", AuthMethodCategory.KNOWLEDGE),
  THIRD_PARTY_OIDC(
      "oidc", AuthMethodCategory.POSSESSION); // Adding this custom as RFC doesn't support this

  private final String value;
  private final AuthMethodCategory category;

  AuthMethod(String authMethod, AuthMethodCategory category) {
    this.value = authMethod;
    this.category = category;
  }
}
