package com.dreamsportslabs.guardian.dao.model;

import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_NOT_EXISTS;

import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.constant.Flow;
import com.dreamsportslabs.guardian.dto.request.MetaInfo;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Builder
@Getter
@Slf4j
@Jacksonized
public class PasswordlessModel {
  private String state;
  private String otp;
  @Builder.Default private Boolean isOtpMocked = false;
  @Builder.Default private Integer tries = 0;
  @Builder.Default private Integer resends = 0;
  private Long resendAfter;
  private Integer resendInterval;
  private Integer maxTries;
  private Integer maxResends;

  @Setter private Map<String, Object> user;
  private Map<String, String> headers;

  private List<Contact> contacts;
  private Flow flow;
  private String responseType;
  @Builder.Default private MetaInfo metaInfo = new MetaInfo();
  @Builder.Default private Long createdAtEpoch = Instant.now().toEpochMilli();
  private Long expiry;

  private Map<String, Object> additionalInfo;

  PasswordlessModel(
      String state,
      String otp,
      Boolean isOtpMocked,
      Integer tries,
      Integer resends,
      Long resendAfter,
      Integer resendInterval,
      Integer maxTries,
      Integer maxResends,
      Map<String, Object> user,
      Map<String, String> headers,
      List<Contact> contacts,
      Flow flow,
      String responseType,
      MetaInfo metaInfo,
      Long createdAtEpoch,
      Long expiry,
      Map<String, Object> additionalInfo) {
    if (flow.equals(Flow.SIGNUP) && user.get(USERID) != null) {
      throw USER_EXISTS.getException();
    }

    if (flow.equals(Flow.SIGNIN) && user.get(USERID) == null) {
      throw USER_NOT_EXISTS.getException();
    }
    this.state = state;
    this.otp = otp;
    this.isOtpMocked = isOtpMocked;
    this.tries = tries;
    this.resends = resends;
    this.resendAfter = resendAfter;
    this.resendInterval = resendInterval;
    this.maxTries = maxTries;
    this.maxResends = maxResends;
    this.user = user;
    this.headers = headers;
    this.contacts = contacts;
    this.flow = flow;
    this.responseType = responseType;
    this.metaInfo = metaInfo;
    this.createdAtEpoch = createdAtEpoch;
    this.expiry = expiry;
    this.additionalInfo = additionalInfo;
  }

  public PasswordlessModel incRetry() {
    this.tries += 1;
    return this;
  }

  public PasswordlessModel updateResend() {
    this.resendAfter = System.currentTimeMillis() / 1000 + resendInterval;
    this.resends += 1;
    return this;
  }
}
