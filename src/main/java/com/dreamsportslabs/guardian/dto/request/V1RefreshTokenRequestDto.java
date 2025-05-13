package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
public class V1RefreshTokenRequestDto {
  private String refreshToken;

  public void validate() {
    if (StringUtils.isEmpty(refreshToken)) {
      throw UNAUTHORIZED.getException();
    }
  }
}
