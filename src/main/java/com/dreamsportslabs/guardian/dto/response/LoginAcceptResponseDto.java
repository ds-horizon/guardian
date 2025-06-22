package com.dreamsportslabs.guardian.dto.response;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginAcceptResponseDto {
  private String redirectUri;
  private String consentChallenge;

  public Response toResponse() {
    UriBuilder uriBuilder = UriBuilder.fromUri(redirectUri);

    if (consentChallenge != null) {
      uriBuilder.queryParam("consent_challenge", consentChallenge);
    }

    return Response.status(Response.Status.FOUND).location(uriBuilder.build()).build();
  }
}
