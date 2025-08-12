package com.dreamsportslabs.guardian.dto.request;

import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UserConsentRequestDto {

  @QueryParam("consent_challenge")
  private String consentChallenge;

  public void validate() {
    if (StringUtils.isBlank(consentChallenge)) {
      throw OidcErrorEnum.INVALID_REQUEST.getJsonCustomException("consent_challenge is required");
    }
  }
}
