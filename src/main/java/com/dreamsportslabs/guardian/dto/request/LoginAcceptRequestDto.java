package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import org.apache.commons.lang3.StringUtils;
import lombok.Data;

@Data
public class LoginAcceptRequestDto {
  private String loginChallenge;
  private String refreshToken;

  public void validate() {
    if (loginChallenge == null || StringUtils.isBlank(loginChallenge)) {
      throw INVALID_REQUEST.getCustomException("login_challenge is required");
    }
    if (refreshToken == null || StringUtils.isBlank(refreshToken)) {
      throw INVALID_REQUEST.getCustomException("refresh_token is required");
    }
  }
}
