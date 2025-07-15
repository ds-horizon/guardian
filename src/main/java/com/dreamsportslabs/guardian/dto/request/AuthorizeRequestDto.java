package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_LOGIN_HINT;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_NONCE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_STATE;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.OidcCodeChallengeMethod;
import com.dreamsportslabs.guardian.constant.OidcPrompt;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.ws.rs.QueryParam;
import java.net.URI;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class AuthorizeRequestDto {
  private static final String BASE64URL_PATTERN = "^[A-Za-z0-9_-]+$";
  private static final String SAFE_PARAMETER_PATTERN = "^[A-Za-z0-9@._-]+$";

  @QueryParam(value = "response_type")
  private String responseType;

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
  private String codeChallengeMethod;

  @QueryParam(value = "prompt")
  private String prompt;

  @QueryParam(value = "login_hint")
  private String loginHint;

  @JsonIgnore private OidcResponseType oidcResponseType;

  @JsonIgnore private OidcCodeChallengeMethod oidcCodeChallengeMethod;

  @JsonIgnore private OidcPrompt oidcPrompt;

  public void validate() {
    if (StringUtils.isBlank(responseType)) {
      throw INVALID_REQUEST.getJsonCustomException("response_type is required");
    }

    setOidcResponseType();

    if (StringUtils.isNotBlank(codeChallengeMethod)) {
      setOidcCodeChallengeMethod();
    }

    if (StringUtils.isNotBlank(prompt)) {
      setOidcPrompt();
    }

    if (StringUtils.isBlank(clientId)) {
      throw INVALID_REQUEST.getJsonCustomException("client_id is required");
    }

    if (StringUtils.isBlank(scope)) {
      throw INVALID_REQUEST.getJsonCustomException("scope is required");
    }

    String[] scopeValues = scope.split("\\s+");
    for (String scopeValue : scopeValues) {
      if (StringUtils.isBlank(scopeValue)) {
        throw INVALID_REQUEST.getJsonCustomException("scope contains empty or malformed values");
      }
    }

    if (StringUtils.isBlank(redirectUri)) {
      throw INVALID_REQUEST.getJsonCustomException("redirect_uri is required");
    }

    try {
      URI uri = URI.create(redirectUri);
      if (!uri.isAbsolute() || uri.getFragment() != null) {
        throw INVALID_REQUEST.getJsonCustomException(
            "redirect_uri must be absolute without fragment");
      }
    } catch (IllegalArgumentException e) {
      throw INVALID_REQUEST.getJsonCustomException("redirect_uri is malformed");
    }

    if ((StringUtils.isBlank(codeChallenge) && codeChallengeMethod != null)
        || (StringUtils.isNotBlank(codeChallenge) && codeChallengeMethod == null)) {
      throw INVALID_REQUEST.getJsonCustomException(
          "code_challenge and code_challenge_method must be provided together");
    }

    if (StringUtils.isNotBlank(codeChallenge)) {
      if (!codeChallenge.matches(BASE64URL_PATTERN)) {
        throw INVALID_REQUEST.getJsonCustomException(
            "code_challenge must contain only base64url characters (A-Z, a-z, 0-9, -, _)");
      }

      if (codeChallenge.length() < 43 || codeChallenge.length() > 128) {
        throw INVALID_REQUEST.getJsonCustomException(
            "code_challenge must be between 43 and 128 characters");
      }
    }

    validateOptionalParameter(OIDC_PARAM_STATE, state);
    validateOptionalParameter(OIDC_PARAM_NONCE, nonce);
    validateOptionalParameter(OIDC_PARAM_LOGIN_HINT, loginHint);
  }

  private void validateOptionalParameter(String paramName, String value) {
    if (StringUtils.isNotBlank(value)) {
      if (!value.matches(SAFE_PARAMETER_PATTERN)) {
        throw INVALID_REQUEST.getJsonCustomException(
            paramName
                + " contains invalid characters. Only alphanumeric, @, ., _, and - are allowed");
      }
    }
  }

  private void setOidcResponseType() {
    try {
      this.oidcResponseType = OidcResponseType.valueOf(responseType.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw INVALID_REQUEST.getJsonCustomException(
          "Unsupported response_type: '" + responseType + "'");
    }
  }

  private void setOidcCodeChallengeMethod() {
    try {
      this.oidcCodeChallengeMethod =
          OidcCodeChallengeMethod.valueOf(codeChallengeMethod.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw INVALID_REQUEST.getJsonCustomException(
          "Unsupported code_challenge_method: '" + codeChallengeMethod + "'");
    }
  }

  private void setOidcPrompt() {
    try {
      this.oidcPrompt = OidcPrompt.valueOf(prompt.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw INVALID_REQUEST.getJsonCustomException("Unsupported prompt: '" + prompt + "'");
    }
  }
}
