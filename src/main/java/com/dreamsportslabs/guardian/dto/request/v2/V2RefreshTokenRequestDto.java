package com.dreamsportslabs.guardian.dto.request.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class V2RefreshTokenRequestDto {
  @JsonProperty("refresh_token")
  @NotBlank(message = "Refresh token is invalid")
  private String refreshToken;

  @JsonProperty("client_id")
  private String clientId;
}
