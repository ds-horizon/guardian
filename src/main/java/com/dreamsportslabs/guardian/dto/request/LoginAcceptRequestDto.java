package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REQUEST;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class LoginAcceptRequestDto {
  private String loginChallenge;
  private String refreshToken;

  public void validate() {
    if (StringUtils.isBlank(loginChallenge)) {
      throw INVALID_REQUEST.getJsonCustomException("loginChallenge is required");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw INVALID_REQUEST.getJsonCustomException("refreshToken is required");
    }
  }

  public void setRefreshTokenFromCookie(String cookieRefreshToken) {
    if (StringUtils.isBlank(this.refreshToken) && StringUtils.isNotBlank(cookieRefreshToken)) {
      this.refreshToken = cookieRefreshToken;
    }
  }
}
