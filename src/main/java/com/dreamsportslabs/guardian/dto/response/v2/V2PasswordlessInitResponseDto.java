package com.dreamsportslabs.guardian.dto.response.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class V2PasswordlessInitResponseDto {
  @JsonProperty("tries")
  private Integer tries;

  @JsonProperty("retries_left")
  private Integer retriesLeft;

  @JsonProperty("resends")
  private Integer resends;

  @JsonProperty("resends_left")
  private Integer resendsLeft;

  @JsonProperty("resend_after")
  private Long resendAfter;

  @JsonProperty("is_new_user")
  private Boolean isNewUser;

  @JsonProperty("state")
  private String state;
}
