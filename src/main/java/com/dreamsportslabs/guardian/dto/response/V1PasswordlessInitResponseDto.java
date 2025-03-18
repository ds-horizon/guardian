package com.dreamsportslabs.guardian.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
public class V1PasswordlessInitResponseDto {
  private Integer tries;
  private Integer retriesLeft;

  private Integer resends;
  private Integer resendsLeft;
  private Long resendAfter;

  private Boolean isNewUser;
  private String state;
}
