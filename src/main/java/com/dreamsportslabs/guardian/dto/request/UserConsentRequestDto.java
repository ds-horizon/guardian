package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UserConsentRequestDto {

  @QueryParam("consent_challenge")
  private String consentChallenge;

  public void validate() {
    if (StringUtils.isBlank(consentChallenge)) {
      throw INVALID_REQUEST.getCustomException("consent_challenge is required");
    }
  }
}
