package com.dreamsportslabs.guardian.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OidcTokenResponseDto {
  @JsonProperty(value = "access_token")
  private String accessToken;

  @JsonProperty(value = "refresh_token")
  private String refreshToken;

  @JsonProperty(value = "id_token")
  private String idToken;

  @JsonProperty(value = "token_type")
  private String tokenType;

  @JsonProperty(value = "expires_in")
  private Integer expiresIn;

  private String scope;
}
