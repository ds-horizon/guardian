package com.dreamsportslabs.guardian.dto.response;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorizeErrorResponseDto {
  private String error;
  private String errorDescription;
  private String errorUri;
  private String state;
  private String redirectUri;

  public Response toResponse() {
    UriBuilder uriBuilder = UriBuilder.fromUri(redirectUri);

    if (error != null) {
      uriBuilder.queryParam("error", error);
    }

    if (errorDescription != null) {
      uriBuilder.queryParam("error_description", errorDescription);
    }

    if (errorUri != null) {
      uriBuilder.queryParam("error_uri", errorUri);
    }

    if (state != null) {
      uriBuilder.queryParam("state", state);
    }

    return Response.status(Response.Status.FOUND).location(uriBuilder.build()).build();
  }

  public static AuthorizeErrorResponseDto invalidRequest(
      String redirectUri, String state, String description) {
    return new AuthorizeErrorResponseDto("invalid_request", description, null, state, redirectUri);
  }

  public static AuthorizeErrorResponseDto unauthorizedClient(
      String redirectUri, String state, String description) {
    return new AuthorizeErrorResponseDto(
        "unauthorized_client", description, null, state, redirectUri);
  }

  public static AuthorizeErrorResponseDto accessDenied(
      String redirectUri, String state, String description) {
    return new AuthorizeErrorResponseDto("access_denied", description, null, state, redirectUri);
  }

  public static AuthorizeErrorResponseDto unsupportedResponseType(
      String redirectUri, String state, String description) {
    return new AuthorizeErrorResponseDto(
        "unsupported_response_type", description, null, state, redirectUri);
  }

  public static AuthorizeErrorResponseDto invalidScope(
      String redirectUri, String state, String description) {
    return new AuthorizeErrorResponseDto("invalid_scope", description, null, state, redirectUri);
  }

  public static AuthorizeErrorResponseDto serverError(
      String redirectUri, String state, String description) {
    return new AuthorizeErrorResponseDto("server_error", description, null, state, redirectUri);
  }

  public static AuthorizeErrorResponseDto temporarilyUnavailable(
      String redirectUri, String state, String description) {
    return new AuthorizeErrorResponseDto(
        "temporarily_unavailable", description, null, state, redirectUri);
  }
}
