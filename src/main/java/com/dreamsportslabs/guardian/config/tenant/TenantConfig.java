package com.dreamsportslabs.guardian.config.tenant;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class TenantConfig {
  private String tenantId;
  private AuthCodeConfig authCodeConfig;
  private EmailConfig emailConfig;
  private FbConfig fbConfig;
  private GoogleConfig googleConfig;
  private SmsConfig smsConfig;
  private OtpConfig otpConfig;
  private UserConfig userConfig;
  private TokenConfig tokenConfig;
}
