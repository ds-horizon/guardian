package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.AuthorizeRequestDto;
import com.dreamsportslabs.guardian.service.OidcService;
import com.google.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
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
@Path("/authorize")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class Authorize {
  private final OidcService oidcService;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> authorize(
      @BeanParam AuthorizeRequestDto requestDto, @Context HttpHeaders headers) {
    requestDto.validate();

    return oidcService
        .authorize(requestDto, headers.getRequestHeaders(), headers.getHeaderString(TENANT_ID))
        .map(authorizeResponseDto -> authorizeResponseDto.toResponse())
        .toCompletionStage();
  }
}
