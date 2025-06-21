package com.dreamsportslabs.guardian.exception;

import com.dreamsportslabs.guardian.dto.response.AuthorizeErrorResponseDto;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class OidcErrorExceptionMapper
    implements ExceptionMapper<OidcErrorEnum.OidcErrorRuntimeException> {

  @Override
  public Response toResponse(OidcErrorEnum.OidcErrorRuntimeException exception) {
    log.error("OIDC Error Exception: {}", exception.getMessage(), exception);

    AuthorizeErrorResponseDto errorResponse =
        new AuthorizeErrorResponseDto(
            exception.getError(),
            exception.getErrorDescription(),
            exception.getErrorUri(),
            exception.getState(),
            exception.getRedirectUri());

    return errorResponse.toResponse();
  }
}
