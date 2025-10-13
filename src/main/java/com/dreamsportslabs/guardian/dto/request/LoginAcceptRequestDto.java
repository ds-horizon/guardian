package com.dreamsportslabs.guardian.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class LoginAcceptRequestDto {

  @JsonProperty("login_challenge")
  @NotBlank(message = "loginChallenge is required")
  private String loginChallenge;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("sso_token")
  private String ssoToken;

  public void setRefreshTokenFromCookie(String cookieRefreshToken) {
    if (StringUtils.isBlank(this.refreshToken) && StringUtils.isNotBlank(cookieRefreshToken)) {
      this.refreshToken = cookieRefreshToken;
    }
  }

  public void setSsoTokenFromCookie(String ssoTokenCookie) {
    if (StringUtils.isBlank(this.ssoToken) && StringUtils.isNotBlank(ssoTokenCookie)) {
      this.ssoToken = ssoTokenCookie;
    }
  }
}
