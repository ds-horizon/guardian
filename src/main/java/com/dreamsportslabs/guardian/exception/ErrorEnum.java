package com.dreamsportslabs.guardian.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import lombok.Getter;

public enum ErrorEnum {
  INVALID_REQUEST("invalid_request", "Invalid request params", 400),
  UNAUTHORIZED("unauthorized", "Unauthorized", 401),
  INTERNAL_SERVER_ERROR("internal_server_error", "Something went wrong", 500),
  USER_SERVICE_ERROR("user_service_error", "User service error", 500),
  SMS_SERVICE_ERROR("sms_service_error", "SMS service error", 500),
  EMAIL_SERVICE_ERROR("email_service_error", "Email service error", 500),

  INVALID_CODE("invalid_code", "Invalid code", 400),

  INVALID_STATE("invalid_state", "Invalid state", 400),
  RESENDS_EXHAUSTED("resends_exhausted", "Resends exhausted", 400),
  RESEND_NOT_ALLOWED("resends_not_allowed", "Resend triggered too quick, Try again later", 400),
  INCORRECT_OTP("incorrect_otp", "Incorrect otp", 400),
  RETRIES_EXHAUSTED("retries_exhausted", "Retries exhausted", 400),

  USER_EXISTS("user_exists", "User already exists", 400),
  USER_NOT_EXISTS("user_not_exists", "User does not exist", 400);

  private final String code;
  private final String message;
  private final int httpStatusCode;

  @Getter private final WebApplicationException exception;

  ErrorEnum(String code, String message, int httpStatusCode) {
    this.code = code;
    this.message = message;
    this.httpStatusCode = httpStatusCode;
    Response response =
        Response.status(httpStatusCode)
            .header("Content-Type", "application/json")
            .entity(new ErrorEntity(code, message))
            .build();
    this.exception = new WebApplicationException(response);
  }

  public WebApplicationException getException(Throwable t) {
    return new WebApplicationException(t, this.exception.getResponse());
  }

  public WebApplicationException getCustomException(String message) {
    message = message == null ? this.message : message;

    Response response =
        Response.status(this.httpStatusCode)
            .header("Content-Type", "application/json")
            .entity(new ErrorEntity(this.code, message))
            .build();
    return new WebApplicationException(response);
  }

  public WebApplicationException getCustomException(Map<String, Object> data) {
    Response response =
        Response.status(this.httpStatusCode)
            .header("Content-Type", "application/json")
            .entity(new ErrorEntity(this.code, this.message, data))
            .build();
    return new WebApplicationException(response);
  }

  public WebApplicationException getCustomException(String message, Map<String, Object> data) {
    message = message == null ? this.message : message;

    Response response =
        Response.status(this.httpStatusCode)
            .header("Content-Type", "application/json")
            .entity(new ErrorEntity(this.code, message, data))
            .build();
    return new WebApplicationException(response);
  }

  @Getter
  static class ErrorEntity {
    final Error error;

    ErrorEntity(String code, String message) {
      this.error = new Error(code, message);
    }

    ErrorEntity(String code, String message, Map<String, Object> data) {
      this.error = new Error(code, message, data);
    }

    @Getter
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    static class Error {
      final String code;
      final String message;
      final Map<String, Object> metadata;

      Error(String code, String message) {
        this(code, message, null);
      }

      Error(String code, String message, Map<String, Object> metadata) {
        this.code = code;
        this.message = message;
        this.metadata = metadata;
      }
    }
  }
}
