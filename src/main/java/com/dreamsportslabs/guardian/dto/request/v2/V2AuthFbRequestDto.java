package com.dreamsportslabs.guardian.dto.request.v2;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.Constants;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class V2AuthFbRequestDto extends V2AbstractAuthenticationRequestDto {
  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("response_type")
  private String responseType;

  public void validate() {
    if (responseType == null) {
      throw INVALID_REQUEST.getCustomException("Invalid response type");
    }

    if (accessToken == null) {
      throw INVALID_REQUEST.getCustomException("Invalid access token");
    }

    if (!Constants.fbAuthResponseTypes.contains(responseType)) {
      throw INVALID_REQUEST.getCustomException("Invalid response type");
    }
  }
}
