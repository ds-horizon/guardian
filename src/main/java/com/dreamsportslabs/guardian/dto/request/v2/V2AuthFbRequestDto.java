package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.validation.annotation.ValidV2AuthFbRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@ValidV2AuthFbRequest
public class V2AuthFbRequestDto extends V2AbstractAuthenticationRequestDto {
  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("response_type")
  private String responseType;
}
