package com.dreamsportslabs.guardian.exception;

import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_ERROR;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_ERROR_DESCRIPTION;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_STATE;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.Getter;

@Getter
public enum OidcErrorEnum {
  INVALID_REQUEST(
      "invalid_request",
      "The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than once, or is otherwise malformed",
      302),
  UNAUTHORIZED_CLIENT(
      "unauthorized_client",
      "The client is not authorized to request an authorization code using this method",
      302),
  ACCESS_DENIED(
      "access_denied", "The resource owner or authorization server denied the request", 302),
  UNSUPPORTED_RESPONSE_TYPE(
      "unsupported_response_type",
      "The authorization server does not support obtaining an authorization code using this method",
      302),
  INVALID_SCOPE("invalid_scope", "The requested scope is invalid, unknown, or malformed", 302),
  SERVER_ERROR(
      "server_error",
      "The authorization server encountered an unexpected condition that prevented it from fulfilling the request",
      302),
  TEMPORARILY_UNAVAILABLE(
      "temporarily_unavailable",
      "The authorization server is currently unable to handle the request due to a temporary overloading or maintenance of the server",
      302);

  private final String error;
  private final String errorDescription;
  private final int httpStatus;

  OidcErrorEnum(String error, String errorDescription, int httpStatus) {
    this.error = error;
    this.errorDescription = errorDescription;
    this.httpStatus = httpStatus;
  }

  public WebApplicationException getRedirectCustomException(
      String customMessage, String redirectUri, String state) {
    String errorDescription = customMessage != null ? customMessage : this.getErrorDescription();

    Response response =
        (state != null)
            ? Response.status(Response.Status.FOUND)
                .location(
                    UriBuilder.fromUri(redirectUri)
                        .queryParam(OIDC_PARAM_STATE, state)
                        .queryParam(OIDC_PARAM_ERROR, this.getError())
                        .queryParam(OIDC_PARAM_ERROR_DESCRIPTION, errorDescription)
                        .build())
                .build()
            : Response.status(Response.Status.FOUND)
                .location(
                    UriBuilder.fromUri(redirectUri)
                        .queryParam(OIDC_PARAM_ERROR, this.getError())
                        .queryParam(OIDC_PARAM_ERROR_DESCRIPTION, errorDescription)
                        .build())
                .build();

    return new WebApplicationException(this.getError(), response);
  }
}
