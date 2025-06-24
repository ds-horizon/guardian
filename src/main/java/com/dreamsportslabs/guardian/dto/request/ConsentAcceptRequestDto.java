package com.dreamsportslabs.guardian.dto.request;

import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ConsentAcceptRequestDto {
  private String consentChallenge;
  private List<String> consentedScopes;
  private String refreshToken;

  public void validate() {
    if (StringUtils.isBlank(consentChallenge)) {
      throw OidcErrorEnum.INVALID_REQUEST.getCustomException(
          "consent_challenge is required", null, null);
    }

    if (consentedScopes == null || consentedScopes.isEmpty()) {
      throw OidcErrorEnum.INVALID_REQUEST.getCustomException(
          "consented_scopes is required", null, null);
    }

    if (StringUtils.isBlank(refreshToken)) {
      throw OidcErrorEnum.INVALID_REQUEST.getCustomException(
          "refresh_token is required", null, null);
    }
  }
}
