package com.dreamsportslabs.guardian.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RefreshTokenResponseDto {
  private String accessToken;
  private String tokenType;
  private long expiresIn;
}
