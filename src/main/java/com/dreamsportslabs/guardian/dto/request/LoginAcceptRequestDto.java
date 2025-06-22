package com.dreamsportslabs.guardian.dto.request;

import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import lombok.Data;

@Data
public class LoginAcceptRequestDto {
  private String loginChallenge;
  private String refreshToken;

  public void validate() {
    if (loginChallenge == null || loginChallenge.trim().isEmpty()) {
      throw OidcErrorEnum.INVALID_REQUEST.getCustomException(
          "login_challenge is required", null, null);
    }
    if (refreshToken == null || refreshToken.trim().isEmpty()) {
      throw OidcErrorEnum.INVALID_REQUEST.getCustomException(
          "refresh_token is required", null, null);
    }
  }
}
