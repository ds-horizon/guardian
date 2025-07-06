package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.CreateScopeRequestDto;
import com.dreamsportslabs.guardian.service.ScopeService;
import com.google.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/config/scope")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ScopeConfig {
  private final ScopeService scopeService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> listScopes(
      @HeaderParam(TENANT_ID) String tenantId,
      @QueryParam("name") String name,
      @DefaultValue("1") @QueryParam("page") int page,
      @DefaultValue("10") @QueryParam("pageSize") int pageSize) {
    return scopeService
        .getScopes(tenantId, name, page, pageSize)
        .map(dto -> Response.ok(dto).build())
        .toCompletionStage();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createScope(
      @HeaderParam(TENANT_ID) String tenantId, CreateScopeRequestDto request) {
    request.validate();
    return scopeService
        .createScope(tenantId, request)
        .map(dto -> Response.status(Response.Status.CREATED).entity(dto).build())
        .toCompletionStage();
  }

  @DELETE
  @Path("/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> deleteScope(
      @HeaderParam(TENANT_ID) String tenantId, @PathParam("name") String name) {

    return scopeService
        .deleteScope(tenantId, name)
        .map(deleted -> deleted ? Response.noContent() : Response.status(Response.Status.NOT_FOUND))
        .map(Response.ResponseBuilder::build)
        .toCompletionStage();
  }
}
