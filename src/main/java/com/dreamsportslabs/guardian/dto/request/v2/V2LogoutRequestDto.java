package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.constant.LogoutType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
public class V2LogoutRequestDto {

  @JsonProperty("refresh_token")
  @NotBlank(message = "Refresh token is invalid")
  private String refreshToken;

  @JsonProperty("logout_type")
  private LogoutType logoutType = LogoutType.TOKEN;

  @JsonProperty("client_id")
  private String clientId;

  public void setRefreshTokenFromCookie(String cookieRefreshToken) {
    if (StringUtils.isBlank(this.refreshToken) && StringUtils.isNotBlank(cookieRefreshToken)) {
      this.refreshToken = cookieRefreshToken;
    }
  }
}
