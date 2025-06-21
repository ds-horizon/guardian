package com.dreamsportslabs.guardian.exception;

import lombok.Getter;

public enum OidcErrorEnum {
  INVALID_REQUEST(
      "invalid_request",
      "The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than once, or is otherwise malformed"),
  UNAUTHORIZED_CLIENT(
      "unauthorized_client",
      "The client is not authorized to request an authorization code using this method"),
  ACCESS_DENIED("access_denied", "The resource owner or authorization server denied the request"),
  UNSUPPORTED_RESPONSE_TYPE(
      "unsupported_response_type",
      "The authorization server does not support obtaining an authorization code using this method"),
  INVALID_SCOPE("invalid_scope", "The requested scope is invalid, unknown, or malformed"),
  SERVER_ERROR(
      "server_error",
      "The authorization server encountered an unexpected condition that prevented it from fulfilling the request"),
  TEMPORARILY_UNAVAILABLE(
      "temporarily_unavailable",
      "The authorization server is currently unable to handle the request due to a temporary overloading or maintenance of the server");

  @Getter private final String code;
  @Getter private final String message;

  OidcErrorEnum(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public RuntimeException getException(String state, String redirectUri) {
    return new OidcErrorRuntimeException(this.code, this.message, null, state, redirectUri);
  }

  public RuntimeException getException(String state, String redirectUri, Throwable cause) {
    return new OidcErrorRuntimeException(this.code, this.message, null, state, redirectUri, cause);
  }

  public RuntimeException getCustomException(
      String customMessage, String state, String redirectUri) {
    String message = customMessage == null ? this.message : customMessage;
    return new OidcErrorRuntimeException(this.code, message, null, state, redirectUri);
  }

  public RuntimeException getCustomException(
      String customMessage, String errorUri, String state, String redirectUri) {
    String message = customMessage == null ? this.message : customMessage;
    return new OidcErrorRuntimeException(this.code, message, errorUri, state, redirectUri);
  }

  @Getter
  public static class OidcErrorRuntimeException extends RuntimeException {
    private final String error;
    private final String errorDescription;
    private final String errorUri;
    private final String state;
    private final String redirectUri;

    public OidcErrorRuntimeException(
        String error, String errorDescription, String errorUri, String state, String redirectUri) {
      super(error + ": " + errorDescription);
      this.error = error;
      this.errorDescription = errorDescription;
      this.errorUri = errorUri;
      this.state = state;
      this.redirectUri = redirectUri;
    }

    public OidcErrorRuntimeException(
        String error,
        String errorDescription,
        String errorUri,
        String state,
        String redirectUri,
        Throwable cause) {
      super(error + ": " + errorDescription, cause);
      this.error = error;
      this.errorDescription = errorDescription;
      this.errorUri = errorUri;
      this.state = state;
      this.redirectUri = redirectUri;
    }
  }
}
