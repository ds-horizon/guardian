package com.dreamsportslabs.guardian.dto.response;

import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_CONSENT_CHALLENGE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_STATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginAcceptResponseDto {

  @JsonProperty("consent_page_uri")
  private String consentPageUri;

  @JsonProperty("consent_challenge")
  private String consentChallenge;

  private String state;

  public Response toResponse() {
    UriBuilder uriBuilder = UriBuilder.fromUri(consentPageUri);

    if (consentChallenge != null) {
      uriBuilder.queryParam(OIDC_PARAM_CONSENT_CHALLENGE, consentChallenge);
    }

    if (state != null) {
      uriBuilder.queryParam(OIDC_PARAM_STATE, state);
    }

    return Response.status(Response.Status.FOUND).location(uriBuilder.build()).build();
  }
}
