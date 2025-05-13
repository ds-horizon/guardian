package com.dreamsportslabs.guardian.dto.response;

import lombok.Builder;
import lombok.Getter;

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
