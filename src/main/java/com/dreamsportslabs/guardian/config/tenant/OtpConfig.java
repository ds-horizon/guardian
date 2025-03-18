package com.dreamsportslabs.guardian.config.tenant;

import java.util.HashMap;
import lombok.Data;

@Data
public class OtpConfig {
  private Integer otpLength;
  private Integer tryLimit;
  private Boolean isOtpMocked;
  private Integer resendLimit;
  private Integer otpResendInterval;
  private Integer otpValidity;
  private HashMap<String, String> whitelistedInputs;
}
