package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.AUTHORIZATION;
import static com.dreamsportslabs.guardian.constant.Constants.CACHE_CONTROL_HEADER;
import static com.dreamsportslabs.guardian.constant.Constants.CACHE_CONTROL_NO_STORE;
import static com.dreamsportslabs.guardian.constant.Constants.PRAGMA_HEADER;
import static com.dreamsportslabs.guardian.constant.Constants.PRAGMA_NO_CACHE;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.dto.request.TokenRequestDto;
import com.dreamsportslabs.guardian.service.OidcTokenService;
import com.google.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/token")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class Token {

  private final OidcTokenService oidcTokenService;

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> token(
      @BeanParam TokenRequestDto requestDto, @Context HttpHeaders headers) {
    String tenantId = headers.getHeaderString(TENANT_ID);
    String authorizationHeader = headers.getHeaderString(AUTHORIZATION);

    if (requestDto == null) {
      throw INVALID_REQUEST.getJsonCustomException("request body is required");
    } else {
      requestDto.setDataFromHeaders(headers.getRequestHeaders());
    }

    requestDto.validate();
    requestDto.validateAuth(authorizationHeader);

    return oidcTokenService
        .getOidcTokens(requestDto, tenantId, authorizationHeader, headers.getRequestHeaders())
        .map(
            dto ->
                Response.ok(dto)
                    .header(CACHE_CONTROL_HEADER, CACHE_CONTROL_NO_STORE)
                    .header(PRAGMA_HEADER, PRAGMA_NO_CACHE)
                    .build())
        .toCompletionStage();
  }
}
