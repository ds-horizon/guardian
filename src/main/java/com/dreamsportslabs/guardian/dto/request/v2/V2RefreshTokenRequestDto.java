package com.dreamsportslabs.guardian.dto.request.v2;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Setter
@Getter
@NoArgsConstructor
public class V2RefreshTokenRequestDto extends V2AbstractAuthenticationRequestDto {

  @JsonProperty("refresh_token")
  private String refreshToken;

  public void validate() {
    if (StringUtils.isEmpty(refreshToken)) {
      throw UNAUTHORIZED.getException();
    }
  }
}
