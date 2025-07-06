package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.CreateClientScopeRequestDto;
import com.dreamsportslabs.guardian.service.ClientScopeService;
import com.google.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/admin/client/{clientId}/scope")
public class ClientScopeResource {
  private final ClientScopeService clientScopeService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createClientScope(
      @Context HttpHeaders headers,
      @PathParam("clientId") String clientId,
      CreateClientScopeRequestDto requestDto) {
    String tenantId = headers.getHeaderString(TENANT_ID);

    return clientScopeService
        .createClientScope(clientId, requestDto, tenantId)
        .map(status -> Response.noContent().build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getClientScopes(
      @Context HttpHeaders headers, @PathParam("clientId") String clientId) {
    String tenantId = headers.getHeaderString(TENANT_ID);

    return clientScopeService
        .getClientScopes(clientId, tenantId)
        .map(scopes -> Response.ok(scopes).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteClientScope(
      @Context HttpHeaders headers,
      @PathParam("clientId") String clientId,
      @QueryParam("scope") String scope) {
    String tenantId = headers.getHeaderString(TENANT_ID);

    return clientScopeService
        .deleteClientScope(clientId, scope, tenantId)
        .map(deleted -> Response.noContent().build())
        .toCompletionStage();
  }
}
