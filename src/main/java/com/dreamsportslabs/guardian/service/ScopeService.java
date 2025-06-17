package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.SCOPE_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.dto.request.CreateScopeRequestDto;
import com.dreamsportslabs.guardian.dto.response.ScopeListResponseDto;
import com.dreamsportslabs.guardian.dto.response.ScopeResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ScopeService {
  private final ScopeDao scopeDao;

  public Single<ScopeListResponseDto> getScopes(
      String tenantId, String scopeName, int page, int pageSize) {

    // Validate pagination parameters
    if (page <= 0) {
      page = 1; // Default to page 1 if invalid
    }
    if (pageSize <= 0) {
      pageSize = 10; // Default to 10 if invalid
    }

    Single<List<ScopeModel>> scopesSingle;
    if (StringUtils.isEmpty(scopeName)) {
      int offset = (page - 1) * pageSize;
      scopesSingle = scopeDao.getAllScopes(tenantId, offset, pageSize);
    } else {
      scopesSingle = scopeDao.getScopesByName(tenantId, scopeName);
    }

    return scopesSingle
        .map(scopesList -> scopesList.stream().map(this::toResponseDto).toList())
        .map(ScopeListResponseDto::new);
  }

  public Single<HashSet<String>> filterExistingScopes(String tenantId, List<String> scopes) {
    return scopeDao
        .getScopesByName(tenantId, scopes)
        .map(
            scopeModels -> {
              HashSet<String> existingScopes = new HashSet<>();
              for (ScopeModel model : scopeModels) {
                existingScopes.add(model.getScope());
              }
              return existingScopes;
            });
  }

  public Single<ScopeResponseDto> createScope(String tenantId, CreateScopeRequestDto requestDto) {
    return scopeDao
        .getScopesByName(tenantId, requestDto.getScope())
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
        .id(model.getId())
        .scope(model.getScope())
        .displayName(model.getDisplayName())
        .description(model.getDescription())
        .claims(model.getClaims())
        .build();
  }
}
