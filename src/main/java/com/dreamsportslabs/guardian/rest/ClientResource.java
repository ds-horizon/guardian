package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CLIENT_NOT_FOUND;

import com.dreamsportslabs.guardian.dto.request.CreateClientRequestDto;
import com.dreamsportslabs.guardian.dto.request.UpdateClientRequestDto;
import com.dreamsportslabs.guardian.service.ClientService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
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
@Path("/v1/admin/client")
public class ClientResource {
  private final ClientService clientService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createClient(
      @Context HttpHeaders headers, CreateClientRequestDto requestDto) {
    String tenantId = headers.getHeaderString(TENANT_ID);

    return clientService
        .createClient(requestDto, tenantId)
        .map(client -> Response.status(Response.Status.CREATED).entity(client).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getClients(
      @Context HttpHeaders headers,
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("limit") @DefaultValue("20") int limit) {
    String tenantId = headers.getHeaderString(TENANT_ID);

    return clientService
        .getClients(tenantId, page, limit)
        .map(clients -> Response.ok(clients).build())
        .toCompletionStage();
  }

  @GET
  @Path("/{clientId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getClient(
      @Context HttpHeaders headers, @PathParam("clientId") String clientId) {
    String tenantId = headers.getHeaderString(TENANT_ID);

    return clientService
        .getClient(clientId, tenantId)
        .map(client -> Response.ok(client).build())
        .switchIfEmpty(Single.error(CLIENT_NOT_FOUND.getException()))
        .toCompletionStage();
  }

  @PATCH
  @Path("/{clientId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateClient(
      @Context HttpHeaders headers,
      @PathParam("clientId") String clientId,
      UpdateClientRequestDto requestDto) {
    String tenantId = headers.getHeaderString(TENANT_ID);

    return clientService
        .updateClient(clientId, requestDto, tenantId)
        .map(client -> Response.ok(client).build())
        .toCompletionStage();
  }

  @DELETE
  @Path("/{clientId}")
  public CompletionStage<Response> deleteClient(
      @Context HttpHeaders headers, @PathParam("clientId") String clientId) {
    String tenantId = headers.getHeaderString(TENANT_ID);

    return clientService
        .deleteClient(clientId, tenantId)
        .map(deleted -> Response.noContent().build())
        .toCompletionStage();
  }

  @POST
  @Path("/{clientId}/regenerate-secret")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> regenerateClientSecret(
      @Context HttpHeaders headers, @PathParam("clientId") String clientId) {
    String tenantId = headers.getHeaderString(TENANT_ID);

    return clientService
        .regenerateClientSecret(clientId, tenantId)
        .map(
            newSecret -> Response.ok().entity(java.util.Map.of("client_secret", newSecret)).build())
        .toCompletionStage();
  }
}
