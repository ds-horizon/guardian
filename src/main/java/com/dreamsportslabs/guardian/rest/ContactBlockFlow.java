package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.V1BlockContactFlowRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1UnblockContactFlowRequestDto;
import com.dreamsportslabs.guardian.service.ContactFlowBlockService;
import com.google.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/v1/contact")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ContactBlockFlow {
  private final ContactFlowBlockService contactFlowBlockService;

  @POST
  @Path("/block")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> blockContact(
      @Context HttpHeaders headers, V1BlockContactFlowRequestDto requestDto) {
    requestDto.validate();
    String tenantId = headers.getHeaderString(TENANT_ID);

    return contactFlowBlockService
        .blockContactFlows(requestDto, tenantId)
        .map(Response.ResponseBuilder::build)
        .toCompletionStage();
  }

  @POST
  @Path("/unblock")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> unblockContact(
      @Context HttpHeaders headers, V1UnblockContactFlowRequestDto requestDto) {
    requestDto.validate();
    String tenantId = headers.getHeaderString(TENANT_ID);

    return contactFlowBlockService
        .unblockContactFlows(requestDto, tenantId)
        .map(Response.ResponseBuilder::build)
        .toCompletionStage();
  }

  @GET
  @Path("/{contactId}/blocked-flows")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getBlockedFlows(
      @Context HttpHeaders headers, @PathParam("contactId") String contactId) {

    String tenantId = headers.getHeaderString(TENANT_ID);

    return contactFlowBlockService
        .getBlockedFlows(tenantId, contactId)
        .map(Response.ResponseBuilder::build)
        .toCompletionStage();
  }
}
