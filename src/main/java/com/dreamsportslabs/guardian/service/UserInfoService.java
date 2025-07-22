package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserInfoService {
  private final Registry registry;
  private final TokenVerifier tokenVerifier;
  private final UserService userService;
  private final ScopeDao scopeDao;

  public Single<JsonObject> getUserInfo(
      String accessToken, MultivaluedMap<String, String> headers, String tenantId) {

    return Single.fromCallable(() -> tokenVerifier.verifyAccessToken(accessToken, tenantId))
        .flatMap(
            claims -> {
              String userId = (String) claims.get("sub");
              if (userId == null) {
                return Single.error(
                    UNAUTHORIZED.getCustomException("Invalid token: missing sub claim"));
              }

              String scope = (String) claims.get("scope");
              return fetchUserInfoWithScopedClaims(userId, scope, headers, tenantId);
            });
  }

  private Single<JsonObject> fetchUserInfoWithScopedClaims(
      String userId, String scope, MultivaluedMap<String, String> headers, String tenantId) {

    List<String> scopes =
        scope.isEmpty() ? Collections.emptyList() : Arrays.asList(scope.split(" "));

    return scopeDao
        .getScopes(tenantId, scopes)
        .map(this::extractClaimNamesFromScopeModels)
        .flatMap(
            claims -> {
              Map<String, String> userFilters = new HashMap<>();
              userFilters.put(USERID, userId);

              if (!claims.isEmpty()) {
                userFilters.put("claims", String.join(",", claims));
              }

              return userService
                  .getOidcUser(userFilters, headers, tenantId)
                  .map(
                      userInfo -> {
                        userInfo.put("iss", getIssuer(tenantId));
                        return userInfo;
                      });
            });
  }

  private List<String> extractClaimNamesFromScopeModels(List<ScopeModel> scopeModels) {
    return scopeModels.stream()
        .filter(scopeModel -> scopeModel.getClaims() != null)
        .flatMap(scopeModel -> scopeModel.getClaims().stream())
        .distinct()
        .collect(Collectors.toList());
  }

  private String getIssuer(String tenantId) {
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    return tenantConfig.getTokenConfig().getIssuer();
  }
}
