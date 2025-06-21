package com.dreamsportslabs.guardian.dto.response;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorizeResponseDto {
  private String loginChallenge;
  private String state;
  private String loginPageUri;

  public Response toResponse() {
    UriBuilder uriBuilder = UriBuilder.fromUri(loginPageUri);

    if (loginChallenge != null) {
      uriBuilder.queryParam("login_challenge", loginChallenge);
    }

    if (state != null) {
      uriBuilder.queryParam("state", state);
    }

    return Response.status(Response.Status.FOUND).location(uriBuilder.build()).build();
  }
}
