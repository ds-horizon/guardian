package com.dreamsportslabs.guardian.service;

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

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ScopeService {
  private final ScopeDao scopeDao;

  public Single<ScopeListResponseDto> getScopes(
      String tenantId, GetScopeRequestDto getScopeRequestDto) {

    Single<List<ScopeModel>> scopesSingle;

    if (!getScopeRequestDto.hasSpecificNames()) {
      scopesSingle =
          scopeDao.getScopesWithPagination(
              tenantId, getScopeRequestDto.getPage() - 1, getScopeRequestDto.getPageSize());
    } else {
      scopesSingle = scopeDao.getScopes(tenantId, getScopeRequestDto.getNames());
    }

    return scopesSingle
        .map(scopesList -> scopesList.stream().map(this::toResponseDto).toList())
        .map(ScopeListResponseDto::new);
  }

  public Single<ScopeResponseDto> createScope(String tenantId, CreateScopeRequestDto requestDto) {
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

    return scopeDao.saveScope(scopeModel).map(this::toResponseDto);
  }

  public Single<Boolean> deleteScope(String tenantId, String name) {
    return scopeDao.deleteScope(tenantId, name);
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
