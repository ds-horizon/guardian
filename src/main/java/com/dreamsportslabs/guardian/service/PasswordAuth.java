package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.FLOW_BLOCKED;

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
  private final ContactFlowBlockService contactFlowBlockService;
  private final ContactExtractionService contactExtractionService;

  public Single<Object> signIn(
      V1SignInRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    // Extract contact information from username
    List<String> contacts = contactExtractionService.extractContactsFromSignIn(dto);

    // Check if password flow is blocked for any contact
    if (!contacts.isEmpty()) {
      String contact = contacts.get(0);
      return contactFlowBlockService
          .isApiBlocked(tenantId, contact, "/v1/signin")
          .flatMap(
              isBlocked -> {
                if (isBlocked) {
                  log.warn(
                      "Password signin flow is blocked for contact: {} in tenant: {}",
                      contact,
                      tenantId);
                  return Single.error(
                      FLOW_BLOCKED.getCustomException(
                          "Password signin flow is blocked for this contact"));
                }
                return performSignIn(dto, headers, tenantId);
              });
    }
    return performSignIn(dto, headers, tenantId);
  }

  public Single<Object> signUp(
      V1SignUpRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    // Extract contact information from username
    List<String> contacts = contactExtractionService.extractContactsFromSignUp(dto);

    // Check if password flow is blocked for any contact
    if (!contacts.isEmpty()) {
      String contact = contacts.get(0); // Take the first contact (username)
      return contactFlowBlockService
          .isApiBlocked(tenantId, contact, "/v1/signup")
          .flatMap(
              isBlocked -> {
                if (isBlocked) {
                  log.warn(
                      "Password signup flow is blocked for contact: {} in tenant: {}",
                      contact,
                      tenantId);
                  return Single.error(
                      FLOW_BLOCKED.getCustomException(
                          "Password signup flow is blocked for this contact"));
                }
                return performSignUp(dto, headers, tenantId);
              });
    }

    // If no valid contact found, proceed with registration
    return performSignUp(dto, headers, tenantId);
  }

  private Single<Object> performSignIn(
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

  private Single<Object> performSignUp(
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
