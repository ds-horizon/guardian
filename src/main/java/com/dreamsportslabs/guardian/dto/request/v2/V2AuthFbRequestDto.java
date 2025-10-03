package com.dreamsportslabs.guardian.dto.request.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class V2AuthFbRequestDto extends V2AbstractAuthenticationRequestDto {
  @JsonProperty("access_token")
  @NotBlank(message = "access_token is required")
  private String accessToken;

  @JsonProperty("response_type")
  @NotBlank(message = "response_type is required")
  private String responseType;
}
