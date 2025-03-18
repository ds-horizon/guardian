package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import jakarta.ws.rs.core.Cookie;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class V1LogoutRequestDto {
  private String refreshToken;
  private Cookie cookie;
  private Boolean isUniversalLogout = false;

  public void validate() {
    if ((refreshToken == null && cookie == null) || (refreshToken != null && cookie != null)) {
      throw INVALID_REQUEST.getException();
    }
  }
}
