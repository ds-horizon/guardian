package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.RevocationRequestDto;
import com.dreamsportslabs.guardian.service.RevocationService;
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

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/revocations")
public class Revocations {
  private final RevocationService revocationService;

  @GET
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getRevocations(
      @Context HttpHeaders headers, @BeanParam RevocationRequestDto requestDto) {
    String tenantId = headers.getHeaderString(TENANT_ID);

    return revocationService
        .getRevocations(requestDto, tenantId)
        .map(dto -> Response.ok(dto).build())
        .toCompletionStage();
  }
}
