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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        .flatMap(this::validateAndExtractUserData)
        .flatMap(userData -> buildUserInfoResponse(userData, headers, tenantId));
  }

  private Single<UserData> validateAndExtractUserData(Map<String, Object> claims) {
    String userId = (String) claims.get("sub");
    if (userId == null) {
      return Single.error(UNAUTHORIZED.getCustomException("Invalid token: missing sub claim"));
    }

    String scope = (String) claims.get("scope");
    List<String> scopes = scope != null ? List.of(scope.split(" ")) : Collections.emptyList();

    return Single.just(new UserData(userId, scopes));
  }

  private Single<JsonObject> buildUserInfoResponse(
      UserData userData, MultivaluedMap<String, String> headers, String tenantId) {
    return userService
        .getUser(Map.of(USERID, userData.userId()), headers, tenantId)
        .flatMap(
            userInfo -> {
              if (userData.scopes().isEmpty()) {
                return Single.just(buildBasicUserInfo(userData.userId(), tenantId));
              }

              return buildScopedUserInfo(userData.userId(), userInfo, userData.scopes(), tenantId);
            });
  }

  private Single<JsonObject> buildScopedUserInfo(
      String userId, JsonObject userInfo, List<String> scopes, String tenantId) {
    return scopeDao
        .getScopes(tenantId, scopes)
        .map(this::extractClaimsFromScopes)
        .map(claims -> buildFilteredUserInfo(userId, userInfo, claims, tenantId));
  }

  private List<String> extractClaimsFromScopes(List<ScopeModel> scopeModels) {
    return scopeModels.stream()
        .filter(scopeModel -> scopeModel.getClaims() != null)
        .flatMap(scopeModel -> scopeModel.getClaims().stream())
        .distinct()
        .collect(Collectors.toList());
  }

  private JsonObject buildBasicUserInfo(String userId, String tenantId) {
    JsonObject userInfo = new JsonObject();
    userInfo.put("sub", userId);
    userInfo.put("iss", getIssuer(tenantId));
    return userInfo;
  }

  private JsonObject buildFilteredUserInfo(
      String userId, JsonObject userInfo, List<String> claims, String tenantId) {
    JsonObject filteredInfo = new JsonObject();

    filteredInfo.put("sub", userId);
    filteredInfo.put("iss", getIssuer(tenantId));

    claims.forEach(
        claim -> {
          Object value = userInfo.getValue(claim);
          if (value != null) {
            filteredInfo.put(claim, value);
          }
        });

    return filteredInfo;
  }

  private String getIssuer(String tenantId) {
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    return tenantConfig.getTokenConfig().getIssuer();
  }

  private record UserData(String userId, List<String> scopes) {}
}
