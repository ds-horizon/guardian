package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.V1BlockContactRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1UnblockContactRequestDto;
import com.dreamsportslabs.guardian.service.ContactBlockService;
import com.google.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/contact")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ContactBlockFlow {
  private final ContactBlockService contactBlockService;

  @POST
  @Path("/block")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> blockContact(
      @Context HttpHeaders headers, V1BlockContactRequestDto requestDto) {
    requestDto.validate();
    String tenantId = headers.getHeaderString(TENANT_ID);

    return contactBlockService
        .blockContactApis(requestDto, tenantId)
        .map(Response.ResponseBuilder::build)
        .toCompletionStage();
  }

  @POST
  @Path("/unblock")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> unblockContact(
      @Context HttpHeaders headers, V1UnblockContactRequestDto requestDto) {
    requestDto.validate();
    String tenantId = headers.getHeaderString(TENANT_ID);

    return contactBlockService
        .unblockContactApis(requestDto, tenantId)
        .map(Response.ResponseBuilder::build)
        .toCompletionStage();
  }

  @GET
  @Path("/{contactId}/blocked-apis")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getBlockedApis(
      @Context HttpHeaders headers, @PathParam("contactId") String contactId) {

    String tenantId = headers.getHeaderString(TENANT_ID);

    if (contactId == null || contactId.trim().isEmpty()) {
      return CompletableFuture.completedFuture(
          Response.status(Response.Status.BAD_REQUEST)
              .entity("contactId must not be null or empty")
              .build());
    }

    return contactBlockService
        .getBlockedApis(tenantId, contactId)
        .map(Response.ResponseBuilder::build)
        .toCompletionStage();
  }
}
