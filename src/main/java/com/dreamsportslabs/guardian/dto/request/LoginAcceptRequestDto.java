package com.dreamsportslabs.guardian.dto.request;

import com.dreamsportslabs.guardian.exception.OidcErrorEnum;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class LoginAcceptRequestDto {
  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("login_challenge")
  private String loginChallenge;

  public void validate() {
    if (StringUtils.isBlank(loginChallenge)) {
      throw OidcErrorEnum.INVALID_REQUEST.getCustomException("login_challenge is required", null, null);
    }

    if (StringUtils.isBlank(refreshToken)) {
      throw OidcErrorEnum.INVALID_REQUEST.getCustomException("refresh_token is required", null, null);
    }
  }
} 