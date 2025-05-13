package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.V1LoginRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1RegisterRequestDto;
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

  public Single<Object> login(
      V1LoginRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
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

  public Single<Object> register(
      V1RegisterRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
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
