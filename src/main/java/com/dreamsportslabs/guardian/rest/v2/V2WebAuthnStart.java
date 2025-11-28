package com.dreamsportslabs.guardian.rest.v2;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.v2.V2WebAuthnStartRequestDto;
import com.dreamsportslabs.guardian.service.WebAuthnService;
import com.google.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
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
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v2/webauthn/start")
public class V2WebAuthnStart {
  private final WebAuthnService webauthnService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> start(
      @Context HttpHeaders headers,
      @HeaderParam(TENANT_ID) String tenantId,
      @Valid V2WebAuthnStartRequestDto requestDto) {
    return webauthnService
        .start(requestDto, headers, tenantId)
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }
}
