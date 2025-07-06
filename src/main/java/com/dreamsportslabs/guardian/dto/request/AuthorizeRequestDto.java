package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.OidcCodeChallengeMethod;
import com.dreamsportslabs.guardian.constant.OidcPrompt;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class AuthorizeRequestDto {
  @QueryParam(value = "response_type")
  private OidcResponseType responseType;

  @QueryParam(value = "scope")
  private String scope;

  @QueryParam(value = "client_id")
  private String clientId;

  @QueryParam(value = "redirect_uri")
  private String redirectUri;

  @QueryParam(value = "state")
  private String state;

  @QueryParam(value = "nonce")
  private String nonce;

  @QueryParam(value = "code_challenge")
  private String codeChallenge;

  @QueryParam(value = "code_challenge_method")
  private OidcCodeChallengeMethod codeChallengeMethod;

  @QueryParam(value = "prompt")
  private OidcPrompt prompt;

  @QueryParam(value = "login_hint")
  private String loginHint;

  public void validate() {
    if (responseType == null) {
      throw INVALID_REQUEST.getCustomException("response_type is required");
    }

    if (StringUtils.isBlank(clientId)) {
      throw INVALID_REQUEST.getCustomException("client_id is required");
    }

    if (StringUtils.isBlank(scope)) {
      throw INVALID_REQUEST.getCustomException("scope is required");
    }

    // Validate scope format - should be space-delimited values
    String[] scopeValues = scope.split("\\s+");
    for (String scopeValue : scopeValues) {
      if (StringUtils.isBlank(scopeValue)) {
        throw INVALID_REQUEST.getCustomException("scope contains empty or malformed values");
      }
    }

    if (StringUtils.isBlank(redirectUri)) {
      throw INVALID_REQUEST.getCustomException("redirect_uri is required");
    }

    if ((StringUtils.isBlank(codeChallenge) && codeChallengeMethod != null)
        || (StringUtils.isNotBlank(codeChallenge) && codeChallengeMethod == null)) {
      throw INVALID_REQUEST.getCustomException(
          "code_challenge and code_challenge_method must be provided together");
    }
  }
}
