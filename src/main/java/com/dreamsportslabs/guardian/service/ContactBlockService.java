package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.dao.ContactBlockDao;
import com.dreamsportslabs.guardian.dao.model.ContactApiBlockModel;
import com.dreamsportslabs.guardian.dto.request.V1BlockContactRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1UnblockContactRequestDto;
import com.dreamsportslabs.guardian.dto.response.V1BlockContactResponseDto;
import com.dreamsportslabs.guardian.dto.response.V1ContactBlockedApisResponseDto;
import com.dreamsportslabs.guardian.dto.response.V1UnblockContactResponseDto;
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
public class ContactBlockService {
  private final ContactBlockDao contactBlockDao;

  public Single<ResponseBuilder> blockContactApis(V1BlockContactRequestDto dto, String tenantId) {
    log.info("Blocking APIs for contact: {} in tenant: {}", dto.getContact(), tenantId);

    List<Completable> saves =
        dto.getBlockApis().stream()
            .map(
                apiPath ->
                    ContactApiBlockModel.builder()
                        .tenantId(tenantId)
                        .contact(dto.getContact())
                        .apiPath(apiPath)
                        .reason(dto.getReason())
                        .operator(dto.getOperator())
                        .unblockedAt(dto.getUnblockedAt())
                        .isActive(true)
                        .build())
            .map(contactBlockDao::blockApis)
            .collect(Collectors.toList());

    return Completable.merge(saves)
        .andThen(
            Single.fromCallable(
                () ->
                    Response.ok(
                        V1BlockContactResponseDto.builder()
                            .contact(dto.getContact())
                            .blockedApis(dto.getBlockApis())
                            .message("APIs blocked successfully")
                            .build())))
        .doOnSuccess(response -> log.info("Response built successfully"))
        .doOnError(error -> log.error("Error in response building: {}", error.getMessage(), error));
  }

  public Single<ResponseBuilder> unblockContactApis(
      V1UnblockContactRequestDto dto, String tenantId) {
    log.info("Unblocking APIs for contact: {} of tenant: {}", dto.getContact(), tenantId);

    List<Completable> operations =
        dto.getUnblockApis().stream()
            .map(apiPath -> contactBlockDao.unblockApis(tenantId, dto.getContact(), apiPath))
            .collect(Collectors.toList());

    return Completable.merge(operations)
        .andThen(
            Single.just(
                Response.ok(
                    V1UnblockContactResponseDto.builder()
                        .contact(dto.getContact())
                        .unblockedApis(dto.getUnblockApis())
                        .message("APIs unblocked successfully")
                        .build())));
  }

  public Single<ResponseBuilder> getBlockedApis(String tenantId, String contact) {
    return contactBlockDao
        .getActiveBlocksByContact(tenantId, contact)
        .map(
            blocks -> {
              List<String> blockedApis =
                  blocks.stream()
                      .filter(this::isBlockActive)
                      .map(ContactApiBlockModel::getApiPath)
                      .collect(Collectors.toList());

              return Response.ok(
                  V1ContactBlockedApisResponseDto.builder()
                      .contact(contact)
                      .blockedApis(blockedApis)
                      .totalCount(blockedApis.size())
                      .build());
            });
  }

  public Single<Boolean> isApiBlocked(String tenantId, String contact, String apiPath) {
    return contactBlockDao.isApiBlocked(tenantId, contact, apiPath);
  }

  private boolean isBlockActive(ContactApiBlockModel block) {
    if (block.getUnblockedAt() == null) {
      return true; // Permanent block
    }
    long currentTimestamp = System.currentTimeMillis() / 1000;
    return currentTimestamp < block.getUnblockedAt();
  }
}
