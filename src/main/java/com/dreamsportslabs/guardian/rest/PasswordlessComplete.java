package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.V1PasswordlessCompleteRequestDto;
import com.dreamsportslabs.guardian.service.Passwordless;
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

@Path("/v1/passwordless/complete")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class PasswordlessComplete {
  private final Passwordless passwordless;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> complete(
      @HeaderParam(TENANT_ID) String tenantId, V1PasswordlessCompleteRequestDto dto) {
    dto.validate();
    return passwordless
        .complete(dto, tenantId)
        .map(res -> Response.ok(res).build())
        .toCompletionStage();
  }
}
