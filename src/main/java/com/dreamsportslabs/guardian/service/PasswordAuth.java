package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.V1SignInRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1SignUpRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class PasswordAuth {
  private final UserService userService;
  private final AuthorizationService authorizationService;

  public Single<Object> signIn(
      V1SignInRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    return userService
        .authenticate(
            UserDto.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .additionalInfo(dto.getAdditionalInfo())
                .build(),
            headers,
            tenantId)
        .flatMap(
            user ->
                authorizationService.generate(
                    user, dto.getResponseType(), dto.getMetaInfo(), tenantId));
  }

  public Single<Object> signUp(
      V1SignUpRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    return userService
        .createUser(
            UserDto.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .additionalInfo(dto.getAdditionalInfo())
                .build(),
            headers,
            tenantId)
        .flatMap(
            user ->
                authorizationService.generate(
                    user, dto.getResponseType(), dto.getMetaInfo(), tenantId));
  }
}
