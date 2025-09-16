package com.dreamsportslabs.guardian.dto.request.v2;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor
@Getter
@Setter
public class V2AuthGoogleRequestDto extends V2AbstractAuthenticationRequestDto {
  @JsonProperty("id_token")
  private String idToken;

  @JsonProperty("response_type")
  private String responseType;

  public void validate() {
    if (StringUtils.isEmpty(this.responseType)) {
      throw INVALID_REQUEST.getCustomException("Invalid response type");
    }

    if (StringUtils.isEmpty(this.idToken)) {
      throw INVALID_REQUEST.getCustomException("Invalid id token");
    }

    if (!Constants.googleAuthResponseTypes.contains(this.responseType)) {
      throw INVALID_REQUEST.getCustomException("Invalid response type");
    }
  }
}
