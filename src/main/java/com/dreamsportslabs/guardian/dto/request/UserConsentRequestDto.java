package com.dreamsportslabs.guardian.dto.request;

import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UserConsentRequestDto {

  @QueryParam("consent_challenge")
  private String consentChallenge;

  private String refreshToken;

  public void validate() {
    if (StringUtils.isBlank(consentChallenge)) {
      throw OidcErrorEnum.INVALID_REQUEST.getJsonCustomException("consent_challenge is required");
    }
  }

  public void setRefreshTokenFromCookie(String cookieRefreshToken) {
    if (StringUtils.isBlank(refreshToken) && StringUtils.isNotBlank(cookieRefreshToken)) {
      this.refreshToken = cookieRefreshToken;
    }
  }
}
