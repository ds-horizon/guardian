package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.dto.request.scope.CreateScopeRequestDto;
import com.dreamsportslabs.guardian.dto.request.scope.GetScopeRequestDto;
import com.dreamsportslabs.guardian.dto.request.scope.UpdateScopeRequestDto;
import com.dreamsportslabs.guardian.dto.response.ScopeListResponseDto;
import com.dreamsportslabs.guardian.dto.response.ScopeResponseDto;
import com.dreamsportslabs.guardian.service.ScopeService;
import com.google.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/scopes")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class Scope {
  private final ScopeService scopeService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> listScopes(
      @HeaderParam(TENANT_ID) String tenantId, @BeanParam GetScopeRequestDto requestDto) {

    requestDto.validate();

    return scopeService
        .getScopes(tenantId, requestDto)
        .map(scopeModels -> scopeModels.stream().map(this::toResponseDto).toList())
        .map(ScopeListResponseDto::new)
        .map(dto -> Response.ok(dto).build())
        .toCompletionStage();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createScope(
      @HeaderParam(TENANT_ID) String tenantId, CreateScopeRequestDto requestDto) {
    requestDto.validate();
    return scopeService
        .createScope(tenantId, requestDto)
        .map(this::toResponseDto)
        .map(dto -> Response.status(Response.Status.CREATED).entity(dto).build())
        .toCompletionStage();
  }

  @DELETE
  @Path("/{name}")
  public CompletionStage<Response> deleteScope(
      @HeaderParam(TENANT_ID) String tenantId, @PathParam("name") String name) {

    return scopeService
        .deleteScope(tenantId, name)
        .map(deleted -> deleted ? Response.noContent() : Response.status(Response.Status.NOT_FOUND))
        .map(Response.ResponseBuilder::build)
        .toCompletionStage();
  }

  @PATCH
  @Path("/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateScope(
      @HeaderParam(TENANT_ID) String tenantId,
      @PathParam("name") String name,
      UpdateScopeRequestDto requestDto) {

    requestDto.validate(name);

    return scopeService
        .updateScope(tenantId, name, requestDto)
        .map(this::toResponseDto)
        .map(responseDto -> Response.ok(responseDto).build())
        .toCompletionStage();
  }

  private ScopeResponseDto toResponseDto(ScopeModel model) {
    return new ScopeResponseDto(
        model.getName(),
        model.getDisplayName(),
        model.getDescription(),
        model.getIconUrl(),
        model.getIsOidc(),
        model.getClaims());
  }
}
