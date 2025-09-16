package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.validation.annotation.ValidV2AuthGoogleRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@ValidV2AuthGoogleRequest
public class V2AuthGoogleRequestDto extends V2AbstractAuthenticationRequestDto {
  @JsonProperty("id_token")
  private String idToken;

  @JsonProperty("response_type")
  private String responseType;
}
