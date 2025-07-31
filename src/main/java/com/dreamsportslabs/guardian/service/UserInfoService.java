package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_TOKEN;

import com.dreamsportslabs.guardian.dao.ScopeDao;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
            jwtClaims -> {
              String userId = (String) jwtClaims.get("sub");
              if (userId == null) {
                return Single.error(
                    INVALID_TOKEN.getBearerAuthHeaderException("Invalid token: missing sub claim"));
              }

              String scope = (String) jwtClaims.get("scope");
              return fetchUserInfoWithScopedClaims(userId, scope, headers, tenantId);
            });
  }

  private Single<JsonObject> fetchUserInfoWithScopedClaims(
      String userId, String scope, MultivaluedMap<String, String> headers, String tenantId) {

    if (StringUtils.isBlank(scope)) {
      return Single.error(INVALID_REQUEST.getCustomException("No scopes provided in token"));
    }
    List<String> scopes = List.of(scope.split(" "));

    return scopeDao
        .getScopes(tenantId, scopes)
        .map(this::extractClaimNamesFromScopeModels)
        .flatMap(
            claims ->
                userService
                    .getUser(Map.of(USERID, userId), headers, tenantId)
                    .map(Utils::convertKeysToSnakeCase)
                    .map(userData -> filterUserData(claims, userData).put(CLAIM_SUB, userId)));
  }

  private List<String> extractClaimNamesFromScopeModels(List<ScopeModel> scopeModels) {
    return scopeModels.stream()
        .filter(scopeModel -> scopeModel.getClaims() != null)
        .flatMap(scopeModel -> scopeModel.getClaims().stream())
        .distinct()
        .collect(Collectors.toList());
  }

  public JsonObject filterUserData(List<String> scopedClaims, JsonObject userData) {

    return scopedClaims.stream()
        .filter(userData::containsKey)
        .collect(
            JsonObject::new,
            (obj, claim) -> obj.put(claim, userData.getValue(claim)),
            JsonObject::mergeIn);
  }
}
