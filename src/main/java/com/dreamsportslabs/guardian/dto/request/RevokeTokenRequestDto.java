package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.constant.Constants.BASIC_AUTHENTICATION_SCHEME;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REQUEST;

import jakarta.ws.rs.FormParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class RevokeTokenRequestDto {
  @FormParam("token")
  String token;

  public void validate() {
    if (StringUtils.isBlank(token)) {
      throw INVALID_REQUEST.getJsonException();
    }
  }

  public void validateAuth(String authorizationHeader) {
    if (StringUtils.isBlank(authorizationHeader)
        || !authorizationHeader.startsWith(BASIC_AUTHENTICATION_SCHEME)) {
      throw INVALID_REQUEST.getJsonCustomException(
          "Authorization header parameter is missing or malformed");
    }
  }
}
