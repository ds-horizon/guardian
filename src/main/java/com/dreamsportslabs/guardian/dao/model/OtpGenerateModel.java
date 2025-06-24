package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.constant.Contact;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Builder
@Getter
@Slf4j
@Jacksonized
public class OtpGenerateModel {
  private String state;
  private String otp;
  @Builder.Default private Boolean isOtpMocked = false;
  @Builder.Default private Integer tries = 0;
  @Builder.Default private Integer resends = 0;
  private Long resendAfter;
  private Integer resendInterval;
  private Integer maxTries;
  private Integer maxResends;

  private Map<String, String> headers;

  private Contact contact;
  @Builder.Default private Long createdAtEpoch = Instant.now().toEpochMilli();
  private Long expiry;

  public OtpGenerateModel incRetry() {
    this.tries += 1;
    return this;
  }

  public OtpGenerateModel updateResend() {
    this.resendAfter = System.currentTimeMillis() / 1000 + resendInterval;
    this.resends += 1;
    return this;
  }
}
