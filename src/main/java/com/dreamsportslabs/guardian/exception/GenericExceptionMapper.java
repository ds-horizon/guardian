package com.dreamsportslabs.guardian.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
  @Override
  public Response toResponse(Throwable exception) {
    log.error("GenericException", exception);
    WebApplicationException e = ErrorEnum.INTERNAL_SERVER_ERROR.getException();
    return e.getResponse();
  }
}
