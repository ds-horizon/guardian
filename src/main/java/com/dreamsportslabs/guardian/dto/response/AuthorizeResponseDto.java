package com.dreamsportslabs.guardian.dto.response;

import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_LOGIN_HINT;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_PROMPT;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_STATE;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class AuthorizeResponseDto {
  private String loginChallenge;
  private String state;
  private String loginPageUri;
  private String prompt;
  private String loginHint;

  public Response toResponse() {
    UriBuilder uriBuilder;
    uriBuilder = UriBuilder.fromUri(loginPageUri);

    if (loginChallenge != null) {
      uriBuilder.queryParam(OIDC_PARAM_LOGIN_CHALLENGE, loginChallenge);
    }

    if (state != null) {
      uriBuilder.queryParam(OIDC_PARAM_STATE, state);
    }

    if (prompt != null) {
      uriBuilder.queryParam(OIDC_PARAM_PROMPT, prompt);
    }

    if (loginHint != null) {
      uriBuilder.queryParam(OIDC_PARAM_LOGIN_HINT, loginHint);
    }

    return Response.status(Response.Status.FOUND).location(uriBuilder.build()).build();
  }
}
