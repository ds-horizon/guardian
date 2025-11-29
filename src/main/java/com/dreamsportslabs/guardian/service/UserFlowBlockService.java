package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FLOW_BLOCKED;

import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.dao.UserFlowBlockDao;
import com.dreamsportslabs.guardian.dao.model.PasswordlessModel;
import com.dreamsportslabs.guardian.dao.model.UserFlowBlockModel;
import com.dreamsportslabs.guardian.dto.request.*;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserFlowBlockService {
  private final UserFlowBlockDao userFlowBlockDao;

  public Completable blockUserFlows(V1BlockUserFlowRequestDto dto, String tenantId) {
    log.info(
        "Blocking flows for userIdentifier: {} in tenant: {}", dto.getUserIdentifier(), tenantId);

    List<UserFlowBlockModel> models =
        dto.getBlockFlows().stream()
            .map(
                flowName ->
                    UserFlowBlockModel.builder()
                        .tenantId(tenantId)
                        .userIdentifier(dto.getUserIdentifier())
                        .flowName(flowName)
                        .reason(dto.getReason())
                        .unblockedAt(dto.getUnblockedAt())
                        .isActive(true)
                        .build())
            .toList();

    return userFlowBlockDao.blockFlows(models);
  }

  public Completable unblockUserFlows(V1UnblockUserFlowRequestDto dto, String tenantId) {
    log.info(
        "Unblocking flows for userIdentifier: {} of tenant: {}", dto.getUserIdentifier(), tenantId);

    return userFlowBlockDao.unblockFlows(tenantId, dto.getUserIdentifier(), dto.getUnblockFlows());
  }

  public Single<List<String>> getActiveFlowsBlockedForUser(String tenantId, String userIdentifier) {
    return userFlowBlockDao.getActiveFlowBlocksByUser(tenantId, userIdentifier);
  }

  public Single<Optional<UserFlowBlockModel>> checkFlowBlockedWithReasonBatch(
      String tenantId, List<String> userIdentifiers, BlockFlow flowName) {

    if (userIdentifiers.isEmpty()) {
      return Single.just(Optional.empty());
    }

    return userFlowBlockDao
        .checkFlowBlockedWithReasonBatch(tenantId, userIdentifiers, flowName)
        .map(list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0)));
  }

  public Completable isUserBlocked(PasswordlessModel model, String tenantId) {
    if (model.getContacts() == null || model.getContacts().isEmpty()) {
      return Completable.complete();
    }

    if (model.getUser() != null && model.getUser().get(USERID) != null) {
      return isFlowBlocked(
          tenantId, List.of(model.getUser().get(USERID).toString()), BlockFlow.PASSWORDLESS);
    }
    List<String> contacts =
        model.getContacts().stream()
            .map(Contact::getIdentifier)
            .filter(StringUtils::isNotBlank)
            .toList();

    return isFlowBlocked(tenantId, contacts, BlockFlow.PASSWORDLESS);
  }

  public Completable isFlowBlocked(String tenantId, List<String> userIdentifiers, BlockFlow flow) {
    return checkFlowBlockedWithReasonBatch(tenantId, userIdentifiers, flow)
        .flatMapCompletable(
            optionalModel -> {
              if (optionalModel.isPresent()) {
                UserFlowBlockModel model = optionalModel.get();
                log.info(
                    "{} flow is blocked for userIdentifiers: {} in tenant: {} with reason: {}, unblockedAt: {}",
                    flow,
                    userIdentifiers,
                    tenantId,
                    model.getReason(),
                    model.getUnblockedAt());

                Map<String, Object> meta = Map.of("retryAfter", model.getUnblockedAt());
                return Completable.error(FLOW_BLOCKED.getCustomException(model.getReason(), meta));
              }
              return Completable.complete();
            });
  }
}
