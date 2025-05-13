package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class V1LogoutRequestDto {
  private String refreshToken;
  private Boolean isUniversalLogout = false;

  public void validate() {
    if (refreshToken == null) {
      throw INVALID_REQUEST.getException();
    }
  }
}
