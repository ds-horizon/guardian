package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.V1CodeTokenExchangeRequestDto;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.google.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/v1/code-token-exchange")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CodeTokenExchange {
  private final AuthorizationService authorizationService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> codeTokenExchange(
      @HeaderParam(TENANT_ID) String tenantId, V1CodeTokenExchangeRequestDto dto) {
    dto.validate();

    return authorizationService
        .codeTokenExchange(dto, tenantId)
        .map(res -> Response.ok(res).build())
        .toCompletionStage();
  }
}
