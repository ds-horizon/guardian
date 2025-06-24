package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.SCOPE_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.dto.request.scope.CreateScopeRequestDto;
import com.dreamsportslabs.guardian.dto.request.scope.GetScopeRequestDto;
import com.dreamsportslabs.guardian.dto.response.ScopeListResponseDto;
import com.dreamsportslabs.guardian.dto.response.ScopeResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ScopeService {
  private final ScopeDao scopeDao;

  public Single<ScopeListResponseDto> getScopes(
      String tenantId, GetScopeRequestDto getScopeRequestDto) {

    Single<List<ScopeModel>> scopesSingle;

    if (StringUtils.isEmpty(getScopeRequestDto.getScope())) {
      scopesSingle =
          scopeDao.getAllScopes(
              tenantId, getScopeRequestDto.getPage() - 1, getScopeRequestDto.getPageSize());
    } else {
      scopesSingle = scopeDao.getScopes(tenantId, getScopeRequestDto.getScope());
    }

    return scopesSingle
        .map(scopesList -> scopesList.stream().map(this::toResponseDto).toList())
        .map(ScopeListResponseDto::new);
  }

  public Single<ScopeResponseDto> createScope(String tenantId, CreateScopeRequestDto requestDto) {
    return scopeDao
        .getScopes(tenantId, requestDto.getScope())
        .flatMap(
            scopeModels -> {
              if (!scopeModels.isEmpty()) {
                return Single.error(
                    SCOPE_ALREADY_EXISTS.getCustomException("Scope already exists for tenant"));
              }

              ScopeModel scopeModel =
                  ScopeModel.builder()
                      .tenantId(tenantId)
                      .scope(requestDto.getScope())
                      .displayName(requestDto.getDisplayName())
                      .description(requestDto.getDescription())
                      .claims(requestDto.getClaims())
                      .iconUrl(requestDto.getIconUrl())
                      .isOidc(requestDto.getIsOidc())
                      .build();

              return scopeDao.saveScopes(scopeModel);
            })
        .map(this::toResponseDto);
  }

  public Single<Boolean> deleteScope(String tenantId, String name) {
    return scopeDao.deleteScope(tenantId, name);
  }

  private ScopeResponseDto toResponseDto(ScopeModel model) {
    return ScopeResponseDto.builder()
        .scope(model.getScope())
        .displayName(model.getDisplayName())
        .description(model.getDescription())
        .claims(model.getClaims())
        .iconUrl(model.getIconUrl())
        .isOidc(model.getIsOidc())
        .build();
  }
}
