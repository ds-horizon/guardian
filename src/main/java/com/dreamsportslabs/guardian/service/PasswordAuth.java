package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.cache.DefaultClientScopesCache;
import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.V1SignInRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1SignUpRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class PasswordAuth {
  private final UserService userService;
  private final AuthorizationService authorizationService;
  private final UserFlowBlockService userFlowBlockService;
  private final DefaultClientScopesCache defaultClientScopesCache;

  public Single<Object> signIn(
      V1SignInRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {

    return userFlowBlockService
        .isFlowBlocked(tenantId, List.of(dto.getUsername()), BlockFlow.PASSWORD)
        .andThen(
            userService.authenticate(
                UserDto.builder()
                    .username(dto.getUsername())
                    .password(dto.getPassword())
                    .additionalInfo(dto.getAdditionalInfo())
                    .build(),
                headers,
                tenantId))
        .flatMap(
            user ->
                defaultClientScopesCache
                    .getDefaultClientScopes(tenantId)
                    .flatMap(
                        pair -> {
                          String clientId = pair.getLeft();
                          String scopes = String.join(" ", pair.getRight());
                          return authorizationService.generate(
                              user,
                              dto.getResponseType(),
                              scopes,
                              List.of(AuthMethod.PASSWORD),
                              dto.getMetaInfo(),
                              clientId,
                              tenantId);
                        }));
  }

  public Single<Object> signUp(
      V1SignUpRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    return userFlowBlockService
        .isFlowBlocked(tenantId, List.of(dto.getUsername()), BlockFlow.PASSWORD)
        .andThen(
            userService.createUser(
                UserDto.builder()
                    .username(dto.getUsername())
                    .password(dto.getPassword())
                    .additionalInfo(dto.getAdditionalInfo())
                    .build(),
                headers,
                tenantId))
        .flatMap(
            user ->
                defaultClientScopesCache
                    .getDefaultClientScopes(tenantId)
                    .flatMap(
                        pair -> {
                          String clientId = pair.getLeft();
                          String scopes = String.join(" ", pair.getRight());
                          return authorizationService.generate(
                              user,
                              dto.getResponseType(),
                              scopes,
                              List.of(AuthMethod.PASSWORD),
                              dto.getMetaInfo(),
                              clientId,
                              tenantId);
                        }));
  }
}
