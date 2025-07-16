package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SCOPE_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.dto.request.scope.CreateScopeRequestDto;
import com.dreamsportslabs.guardian.dto.request.scope.GetScopeRequestDto;
import com.dreamsportslabs.guardian.dto.request.scope.UpdateScopeRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ScopeService {
  private final ScopeDao scopeDao;

  public Single<List<ScopeModel>> getScopes(
      String tenantId, GetScopeRequestDto getScopeRequestDto) {

    if (!getScopeRequestDto.hasSpecificNames()) {
      return scopeDao.getScopesWithPagination(
          tenantId,
          (getScopeRequestDto.getPage() - 1) * getScopeRequestDto.getPageSize(),
          getScopeRequestDto.getPageSize());
    } else {
      return scopeDao.getScopes(tenantId, getScopeRequestDto.getNames());
    }
  }

  public Single<ScopeModel> createScope(String tenantId, CreateScopeRequestDto requestDto) {
    ScopeModel scopeModel =
        ScopeModel.builder()
            .tenantId(tenantId)
            .name(requestDto.getName())
            .displayName(requestDto.getDisplayName())
            .description(requestDto.getDescription())
            .claims(requestDto.getClaims())
            .iconUrl(requestDto.getIconUrl())
            .isOidc(requestDto.getIsOidc())
            .build();

    return scopeDao.saveScope(scopeModel);
  }

  public Single<Boolean> deleteScope(String tenantId, String name) {
    return scopeDao.deleteScope(tenantId, name);
  }

  public Single<ScopeModel> updateScope(
      String tenantId, String name, UpdateScopeRequestDto requestDto) {
    return scopeDao
        .getScopes(tenantId, List.of(name))
        .filter(existingScopes -> !existingScopes.isEmpty())
        .switchIfEmpty(Single.error(SCOPE_NOT_FOUND.getException()))
        .flatMap(
            scopeModels ->
                scopeDao
                    .updateScope(tenantId, name, requestDto)
                    .map(
                        updated -> {
                          if (!updated) {
                            throw INTERNAL_SERVER_ERROR.getCustomException(
                                "internal error while updating scope");
                          }
                          return buildUpdatedScopeModel(
                              scopeModels.get(0), requestDto, tenantId, name);
                        }));
  }

  public Single<List<String>> getOidcScopes(String tenantId) {
    return scopeDao
        .oidcScopes(tenantId)
        .map(scopeModels -> scopeModels.stream().map(ScopeModel::getName).toList());
  }

  private ScopeModel buildUpdatedScopeModel(
      ScopeModel existing, UpdateScopeRequestDto requestDto, String tenantId, String name) {
    return ScopeModel.builder()
        .tenantId(tenantId)
        .name(name)
        .displayName(getValueOrDefault(requestDto.getDisplayName(), existing.getDisplayName()))
        .description(getValueOrDefault(requestDto.getDescription(), existing.getDescription()))
        .claims(getValueOrDefault(requestDto.getClaims(), existing.getClaims()))
        .iconUrl(getValueOrDefault(requestDto.getIconUrl(), existing.getIconUrl()))
        .isOidc(getValueOrDefault(requestDto.getIsOidc(), existing.getIsOidc()))
        .build();
  }

  private <T> T getValueOrDefault(T newValue, T defaultValue) {
    return newValue != null ? newValue : defaultValue;
  }
}
