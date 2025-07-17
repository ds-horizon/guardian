package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_QUERY_PARAM;

import com.dreamsportslabs.guardian.dto.request.V1BlockUserFlowRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1UnblockUserFlowRequestDto;
import com.dreamsportslabs.guardian.dto.response.V1BlockUserFlowResponseDto;
import com.dreamsportslabs.guardian.dto.response.V1UnblockUserFlowResponseDto;
import com.dreamsportslabs.guardian.dto.response.V1UserBlockedFlowsResponseDto;
import com.dreamsportslabs.guardian.service.UserFlowBlockService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Path("/v1/user/flow")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserBlockFlow {
  private final UserFlowBlockService userFlowBlockService;

  @POST
  @Path("/block")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> blockUser(
      @HeaderParam(TENANT_ID) String tenantId, V1BlockUserFlowRequestDto requestDto) {

    requestDto.validate();

    return userFlowBlockService
        .blockUserFlows(requestDto, tenantId)
        .andThen(
            Single.fromCallable(
                () ->
                    V1BlockUserFlowResponseDto.builder()
                        .userIdentifier(requestDto.getUserIdentifier())
                        .blockedFlows(requestDto.getBlockFlows())
                        .build()))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @POST
  @Path("/unblock")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> unblockUser(
      @HeaderParam(TENANT_ID) String tenantId, V1UnblockUserFlowRequestDto requestDto) {

    requestDto.validate();

    return userFlowBlockService
        .unblockUserFlows(requestDto, tenantId)
        .andThen(
            Single.fromCallable(
                () ->
                    V1UnblockUserFlowResponseDto.builder()
                        .userIdentifier(requestDto.getUserIdentifier())
                        .unblockedFlows(requestDto.getUnblockFlows())
                        .build()))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @GET
  @Path("/blocked")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getBlockedFlows(
      @HeaderParam(TENANT_ID) String tenantId,
      @QueryParam("userIdentifier") String userIdentifier) {

    if (StringUtils.isBlank(userIdentifier)) {
      throw INVALID_QUERY_PARAM.getCustomException("userIdentifier is required");
    }

    return userFlowBlockService
        .getActiveFlowsBlockedForUser(tenantId, userIdentifier)
        .map(
            activeBlockFlows -> {
              V1UserBlockedFlowsResponseDto responseDto =
                  V1UserBlockedFlowsResponseDto.builder()
                      .userIdentifier(userIdentifier)
                      .blockedFlows(activeBlockFlows)
                      .totalCount(activeBlockFlows.size())
                      .build();

              return Response.ok(responseDto).build();
            })
        .toCompletionStage();
  }
}
