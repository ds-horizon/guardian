package com.dreamsportslabs.guardian.dto.response;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginAcceptResponseDto {
  private String redirectUri;
  private String state;
  private String code;
  private String clientId;
  private String scopes;
  private String consentChallenge;

  public LoginAcceptResponseDto(String redirectUri, String state, String code) {
    this.redirectUri = redirectUri;
    this.state = state;
    this.code = code;
  }

  public LoginAcceptResponseDto(String consentPageUri, String clientId, String scopes, String consentChallenge) {
    this.redirectUri = consentPageUri;
    this.clientId = clientId;
    this.scopes = scopes;
    this.consentChallenge = consentChallenge;
  }

  public Response toResponse() {
    UriBuilder uriBuilder = UriBuilder.fromUri(redirectUri);

    if (code != null) {
      uriBuilder.queryParam("code", code);
    }

    if (state != null) {
      uriBuilder.queryParam("state", state);
    }

    if (clientId != null) {
      uriBuilder.queryParam("client_id", clientId);
    }

    if (scopes != null) {
      uriBuilder.queryParam("scopes", scopes);
    }

    if (consentChallenge != null) {
      uriBuilder.queryParam("consent_challenge", consentChallenge);
    }

    return Response.status(Response.Status.FOUND).location(uriBuilder.build()).build();
  }
} 