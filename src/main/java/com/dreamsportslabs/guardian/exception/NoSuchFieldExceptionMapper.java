package com.dreamsportslabs.guardian.exception;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class NoSuchFieldExceptionMapper implements ExceptionMapper<NoSuchFieldException> {
  @Override
  public Response toResponse(NoSuchFieldException exception) {
    log.error("NoSuchFieldException - {}", exception.getMessage(), exception);
    WebApplicationException e =
        INVALID_REQUEST.getCustomException(
            "The value provided for the field is invalid or does not exist: "
                + exception.getMessage());
    return e.getResponse();
  }
}
