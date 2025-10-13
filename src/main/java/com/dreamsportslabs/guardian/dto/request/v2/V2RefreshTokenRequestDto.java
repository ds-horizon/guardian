package com.dreamsportslabs.guardian.dto.request.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
public class V2RefreshTokenRequestDto {
  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("client_id")
  private String clientId;

  public void setRefreshTokenFromCookie(String cookieRefreshToken) {
    if (StringUtils.isBlank(this.refreshToken) && StringUtils.isNotBlank(cookieRefreshToken)) {
      this.refreshToken = cookieRefreshToken;
    }
  }
}
