package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.dreamsportslabs.guardian.dao.ContactFlowBlockDao;
import com.dreamsportslabs.guardian.dao.model.ContactFlowBlockModel;
import com.dreamsportslabs.guardian.dto.request.V1BlockContactFlowRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1UnblockContactFlowRequestDto;
import com.dreamsportslabs.guardian.dto.response.V1BlockContactFlowResponseDto;
import com.dreamsportslabs.guardian.dto.response.V1ContactBlockedFlowsResponseDto;
import com.dreamsportslabs.guardian.dto.response.V1UnblockContactFlowResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ContactFlowBlockService {
  private final ContactFlowBlockDao contactFlowBlockDao;

  public Single<ResponseBuilder> blockContactFlows(
      V1BlockContactFlowRequestDto dto, String tenantId) {
    log.info("Blocking flows for contact: {} in tenant: {}", dto.getContact(), tenantId);

    List<Completable> saves =
        dto.getBlockFlows().stream()
            .map(
                flowName ->
                    ContactFlowBlockModel.builder()
                        .tenantId(tenantId)
                        .contact(dto.getContact())
                        .flowName(flowName)
                        .reason(dto.getReason())
                        .operator(dto.getOperator())
                        .unblockedAt(dto.getUnblockedAt())
                        .isActive(true)
                        .build())
            .map(contactFlowBlockDao::blockFlows)
            .collect(Collectors.toList());

    return Completable.merge(saves)
        .andThen(
            Single.fromCallable(
                () ->
                    Response.ok(
                        V1BlockContactFlowResponseDto.builder()
                            .contact(dto.getContact())
                            .blockedFlows(dto.getBlockFlows())
                            .message("Flows blocked successfully")
                            .build())))
        .doOnSuccess(response -> log.info("Response built successfully"))
        .doOnError(error -> log.error("Error in response building: {}", error.getMessage(), error));
  }

  public Single<ResponseBuilder> unblockContactFlows(
      V1UnblockContactFlowRequestDto dto, String tenantId) {
    log.info("Unblocking flows for contact: {} of tenant: {}", dto.getContact(), tenantId);

    List<Completable> operations =
        dto.getUnblockFlows().stream()
            .map(flowName -> contactFlowBlockDao.unblockFlows(tenantId, dto.getContact(), flowName))
            .collect(Collectors.toList());

    return Completable.merge(operations)
        .andThen(
            Single.just(
                Response.ok(
                    V1UnblockContactFlowResponseDto.builder()
                        .contact(dto.getContact())
                        .unblockedFlows(dto.getUnblockFlows())
                        .message("Flows unblocked successfully")
                        .build())));
  }

  public Single<ResponseBuilder> getBlockedFlows(String tenantId, String contact) {
    return contactFlowBlockDao
        .getActiveFlowBlocksByContact(tenantId, contact)
        .map(
            blocks -> {
              List<String> blockedFlows =
                  blocks.stream()
                      .filter(this::isBlockActive)
                      .map(ContactFlowBlockModel::getFlowName)
                      .collect(Collectors.toList());

              return Response.ok(
                  V1ContactBlockedFlowsResponseDto.builder()
                      .contact(contact)
                      .blockedFlows(blockedFlows)
                      .totalCount(blockedFlows.size())
                      .build());
            });
  }

  public Single<Boolean> isApiBlocked(String tenantId, String contact, String apiPath) {
    // Check if any flow that contains this API path is blocked
    List<Single<Boolean>> flowChecks =
        BlockFlow.getAllFlowNames().stream()
            .map(
                flowName -> {
                  BlockFlow flow = BlockFlow.fromString(flowName);
                  if (flow.getApiPaths().contains(apiPath)) {
                    return contactFlowBlockDao.isFlowBlocked(tenantId, contact, flowName);
                  }
                  return Single.just(false);
                })
            .collect(Collectors.toList());

    return Single.merge(flowChecks).reduce(false, (blocked, isBlocked) -> blocked || isBlocked);
  }

  private boolean isBlockActive(ContactFlowBlockModel block) {
    long currentTimestamp = System.currentTimeMillis() / 1000;
    return currentTimestamp < block.getUnblockedAt();
  }
}
