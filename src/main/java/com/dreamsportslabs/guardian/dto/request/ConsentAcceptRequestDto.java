package com.dreamsportslabs.guardian.dto.request;

import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@RequiredArgsConstructor
public class ConsentAcceptRequestDto {

  @JsonProperty("consent_challenge")
  private String consentChallenge;

  @JsonProperty("consented_scopes")
  private List<String> consentedScopes;

  @JsonProperty("refresh_token")
  private String refreshToken;

  public void setConsentedScopes(List<String> consentedScopes) {
    if (consentedScopes != null) {
      this.consentedScopes = new ArrayList<>(new LinkedHashSet<>(consentedScopes));
    }
  }

  public void setRefreshTokenFromCookie(String cookieRefreshToken) {
    if (StringUtils.isBlank(refreshToken) && StringUtils.isNotBlank(cookieRefreshToken)) {
      this.refreshToken = cookieRefreshToken;
    }
  }

  public void validate() {
    if (StringUtils.isBlank(consentChallenge)) {
      throw OidcErrorEnum.INVALID_REQUEST.getJsonCustomException("consentChallenge is required");
    }

    if (StringUtils.isBlank(refreshToken)) {
      throw OidcErrorEnum.INVALID_REQUEST.getJsonCustomException("refreshToken is required");
    }
  }
}
