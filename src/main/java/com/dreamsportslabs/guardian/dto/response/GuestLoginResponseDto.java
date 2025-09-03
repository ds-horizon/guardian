package com.dreamsportslabs.guardian.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GuestLoginResponseDto {
  private String access_token;
  private String token_type;
  private Integer expires_in;
}
