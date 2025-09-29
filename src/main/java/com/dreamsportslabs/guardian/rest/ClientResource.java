package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.dto.request.CreateClientRequestDto;
import com.dreamsportslabs.guardian.dto.request.GetClientsRequestDto;
import com.dreamsportslabs.guardian.dto.request.UpdateClientRequestDto;
import com.dreamsportslabs.guardian.dto.response.ClientListResponseDto;
import com.dreamsportslabs.guardian.dto.response.ClientResponseDto;
import com.dreamsportslabs.guardian.service.ClientService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
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
      @HeaderParam(TENANT_ID) String tenantId, CreateClientRequestDto requestDto) {
    requestDto.validate();
    return clientService
        .createClient(requestDto, tenantId)
        .map(this::mapToResponseDto)
        .map(client -> Response.status(Response.Status.CREATED).entity(client).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getClients(
      @HeaderParam(TENANT_ID) String tenantId, @BeanParam GetClientsRequestDto requestDto) {
    requestDto.validate();
    return clientService
        .getClients(tenantId, requestDto.getPage(), requestDto.getPageSize())
        .map(
            clientModels ->
                ClientListResponseDto.builder()
                    .clients(clientModels.stream().map(this::mapToResponseDto).toList())
                    .pageSize(clientModels.size())
                    .page(requestDto.getPage())
                    .build())
        .map(clients -> Response.ok(clients).build())
        .toCompletionStage();
  }

  @GET
  @Path("/{clientId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getClient(
      @HeaderParam(TENANT_ID) String tenantId, @PathParam("clientId") String clientId) {

    return clientService
        .getClient(clientId, tenantId)
        .map(this::mapToResponseDto)
        .map(client -> Response.ok(client).build())
        .toCompletionStage();
  }

  @PATCH
  @Path("/{clientId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateClient(
      @HeaderParam(TENANT_ID) String tenantId,
      @PathParam("clientId") String clientId,
      UpdateClientRequestDto requestDto) {
    requestDto.validate();
    return clientService
        .updateClient(clientId, requestDto, tenantId)
        .map(this::mapToResponseDto)
        .map(client -> Response.ok(client).build())
        .toCompletionStage();
  }

  @DELETE
  @Path("/{clientId}")
  public CompletionStage<Response> deleteClient(
      @HeaderParam(TENANT_ID) String tenantId, @PathParam("clientId") String clientId) {
    return clientService
        .deleteClient(clientId, tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }

  @POST
  @Path("/{clientId}/regenerate-secret")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> regenerateClientSecret(
      @HeaderParam(TENANT_ID) String tenantId, @PathParam("clientId") String clientId) {
    return clientService
        .regenerateClientSecret(clientId, tenantId)
        .map(newSecret -> Response.ok().entity(Map.of("client_secret", newSecret)).build())
        .toCompletionStage();
  }

  private ClientResponseDto mapToResponseDto(ClientModel model) {
    return ClientResponseDto.builder()
        .clientId(model.getClientId())
        .clientName(model.getClientName())
        .clientSecret(model.getClientSecret())
        .clientUri(model.getClientUri())
        .contacts(model.getContacts())
        .grantTypes(model.getGrantTypes())
        .logoUri(model.getLogoUri())
        .policyUri(model.getPolicyUri())
        .redirectUris(model.getRedirectUris())
        .responseTypes(model.getResponseTypes())
        .clientType(model.getClientType())
        .isDefault(model.getIsDefault())
        .build();
  }
}
