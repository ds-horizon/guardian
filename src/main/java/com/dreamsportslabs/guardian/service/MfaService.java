package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.MFA_FACTOR_ALREADY_ENROLLED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.MFA_FACTOR_NOT_SUPPORTED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;

import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.constant.AuthMethodCategory;
import com.dreamsportslabs.guardian.constant.MfaFactor;
import com.dreamsportslabs.guardian.dao.model.RefreshTokenModel;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.v2.V2MfaSignInRequestDto;
import com.dreamsportslabs.guardian.dto.response.MfaFactorDto;
import com.dreamsportslabs.guardian.dto.response.TokenResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MfaService {

  private final AuthorizationService authorizationService;
  private final ClientService clientService;
  private final UserService userService;

  public Single<TokenResponseDto> mfaEnroll(
      V2MfaSignInRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {
    return clientService
        .validateFirstPartyClientAndClientScopes(
            tenantId, requestDto.getClientId(), requestDto.getScopes())
        .andThen(
            authorizationService.validateRefreshToken(
                tenantId, requestDto.getClientId(), requestDto.getRefreshToken()))
        .flatMap(
            refreshTokenModel -> {
              List<AuthMethod> authMethods = refreshTokenModel.getAuthMethod();
              if (authMethods == null || authMethods.isEmpty()) {
                return Single.error(
                    INVALID_REQUEST.getCustomException(
                        "Refresh token must have at least one authentication method"));
              }
              // Check if the factor being enrolled is already in the refresh token's auth methods
              AuthMethod factorAuthMethod = requestDto.getFactor().getAuthMethod();
              if (authMethods.contains(factorAuthMethod)) {
                return Single.error(
                    MFA_FACTOR_ALREADY_ENROLLED.getCustomException(
                        "The factor is already enrolled in the refresh token"));
              }
              if (authMethods.size() == 1) {
                return validateSingleFactorEnrollment(refreshTokenModel.getUserId(), tenantId)
                    .andThen(Single.just(refreshTokenModel));
              }
              return Single.just(refreshTokenModel);
            })
        .flatMap(
            refreshTokenModel ->
                enrollFactor(requestDto, headers, refreshTokenModel, tenantId)
                    .flatMap(
                        user ->
                            updateRefreshToken(
                                user,
                                requestDto.getRefreshToken(),
                                getMergedScopes(
                                    refreshTokenModel.getScope(), requestDto.getScopes()),
                                getMergedAuthMethods(
                                    refreshTokenModel.getAuthMethod(),
                                    requestDto.getFactor().getAuthMethod()),
                                requestDto.getClientId(),
                                tenantId)));
  }

  public Single<TokenResponseDto> mfaSignIn(
      V2MfaSignInRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {
    return clientService
        .validateFirstPartyClientAndClientScopes(
            tenantId, requestDto.getClientId(), requestDto.getScopes())
        .andThen(
            validateRefreshToken(
                tenantId,
                requestDto.getClientId(),
                requestDto.getRefreshToken(),
                requestDto.getFactor()))
        .flatMap(
            refreshTokenModel ->
                authenticateAndGetUserDetails(
                        requestDto, headers, refreshTokenModel.getUserId(), tenantId)
                    .flatMap(
                        user ->
                            updateRefreshToken(
                                user,
                                requestDto.getRefreshToken(),
                                getMergedScopes(
                                    refreshTokenModel.getScope(), requestDto.getScopes()),
                                getMergedAuthMethods(
                                    refreshTokenModel.getAuthMethod(),
                                    requestDto.getFactor().getAuthMethod()),
                                requestDto.getClientId(),
                                tenantId)));
  }

  private Single<RefreshTokenModel> validateRefreshToken(
      String tenantId, String clientId, String refreshToken, MfaFactor factor) {
    return authorizationService
        .validateRefreshToken(tenantId, clientId, refreshToken)
        .map(
            refreshTokenModel -> {
              validateAuthMethodCategory(refreshTokenModel.getAuthMethod(), factor);
              return refreshTokenModel;
            });
  }

  private void validateAuthMethodCategory(List<AuthMethod> existingAuthMethods, MfaFactor factor) {
    Set<AuthMethodCategory> existingCategories =
        existingAuthMethods.stream().map(AuthMethod::getCategory).collect(Collectors.toSet());
    AuthMethodCategory currentCategory = factor.getAuthMethod().getCategory();
    if (existingCategories.contains(currentCategory)) {
      throw MFA_FACTOR_NOT_SUPPORTED.getCustomException(
          "The refresh token is already authenticated using same category of authentication factor");
    }
  }

  private Single<TokenResponseDto> updateRefreshToken(
      JsonObject user,
      String refreshToken,
      List<String> scopes,
      List<AuthMethod> authMethods,
      String clientId,
      String tenantId) {
    return authorizationService.generateMfaSignInTokens(
        user, refreshToken, scopes, authMethods, clientId, tenantId);
  }

  private Single<JsonObject> authenticateAndGetUserDetails(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String userId,
      String tenantId) {
    return switch (requestDto.getFactor()) {
      case PASSWORD, PIN -> validateFactorEnrolledAndAuthenticate(
          requestDto, headers, userId, tenantId);
      default -> Single.error(MFA_FACTOR_NOT_SUPPORTED.getException());
    };
  }

  private Single<JsonObject> validateFactorEnrolledAndAuthenticate(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String userId,
      String tenantId) {
    MfaFactor factor = requestDto.getFactor();
    String factorSetField = factor == MfaFactor.PASSWORD ? "passwordSet" : "pinSet";
    String factorName = factor == MfaFactor.PASSWORD ? "password" : "PIN";

    return userService
        .getUser(Map.of(USERID, userId), headers, tenantId)
        .flatMap(
            user -> {
              Boolean factorSet = user.getBoolean(factorSetField, false);
              if (!Boolean.TRUE.equals(factorSet)) {
                return Single.error(
                    MFA_FACTOR_NOT_SUPPORTED.getCustomException(
                        String.format(
                            "%s factor is not enrolled for the user. Please enroll it first.",
                            factorName)));
              }
              return authenticateUser(requestDto, headers, userId, tenantId);
            });
  }

  private Single<JsonObject> authenticateUser(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String userId,
      String tenantId) {
    UserDto userDto = buildUserDto(requestDto);

    return userService
        .authenticate(userDto, headers, tenantId)
        .flatMap(
            user -> {
              if (!user.getString(USERID).equals(userId)) {
                return Single.error(
                    UNAUTHORIZED.getCustomException(
                        "User identifier does not match refresh token"));
              }
              return Single.just(user);
            });
  }

  private List<String> getMergedScopes(List<String> existingScopes, List<String> newScopes) {
    LinkedHashSet<String> mergedScopes = new LinkedHashSet<>(existingScopes);
    mergedScopes.addAll(newScopes);
    return new ArrayList<>(mergedScopes);
  }

  private List<AuthMethod> getMergedAuthMethods(
      List<AuthMethod> authMethods, AuthMethod newAuthMethod) {
    List<AuthMethod> mergedAuthMethods = new ArrayList<>(authMethods);
    mergedAuthMethods.add(newAuthMethod);
    return mergedAuthMethods;
  }

  private UserDto buildUserDto(V2MfaSignInRequestDto requestDto) {
    UserDto.UserDtoBuilder userDtoBuilder = UserDto.builder();

    if (StringUtils.isNotBlank(requestDto.getUsername())) {
      userDtoBuilder.username(requestDto.getUsername());
    } else if (StringUtils.isNotBlank(requestDto.getEmail())) {
      userDtoBuilder.email(requestDto.getEmail());
    } else {
      userDtoBuilder.phoneNumber(requestDto.getPhoneNumber());
    }

    if (StringUtils.isNotBlank(requestDto.getPassword())) {
      userDtoBuilder.password(requestDto.getPassword());
    } else {
      userDtoBuilder.pin(requestDto.getPin());
    }

    return userDtoBuilder.build();
  }

  private Completable validateSingleFactorEnrollment(String userId, String tenantId) {
    // Dummy method - user will add the actual implementation
    // This should return Completable on success or Completable error on failure
    return Completable.complete();
  }

  private Single<JsonObject> enrollFactor(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      RefreshTokenModel refreshTokenModel,
      String tenantId) {
    String userId = refreshTokenModel.getUserId();
    MfaFactor factor = requestDto.getFactor();

    return switch (factor) {
      case PASSWORD -> validateAndEnrollPassword(requestDto, headers, userId, tenantId);
      case PIN -> validateAndEnrollPin(requestDto, headers, userId, tenantId);
      default -> Single.error(MFA_FACTOR_NOT_SUPPORTED.getException());
    };
  }

  private Single<JsonObject> validateAndEnrollPassword(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String userId,
      String tenantId) {
    return validateAndEnrollFactor(
        requestDto,
        headers,
        userId,
        tenantId,
        "passwordSet",
        "Password factor cannot be enrolled as it is already set for the user",
        userDtoBuilder -> userDtoBuilder.password(requestDto.getPassword()));
  }

  private Single<JsonObject> validateAndEnrollPin(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String userId,
      String tenantId) {
    return validateAndEnrollFactor(
        requestDto,
        headers,
        userId,
        tenantId,
        "pinSet",
        "PIN factor cannot be enrolled as it is already set for the user",
        userDtoBuilder -> userDtoBuilder.pin(requestDto.getPin()));
  }

  private Single<JsonObject> validateAndEnrollFactor(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String userId,
      String tenantId,
      String factorSetField,
      String errorMessage,
      Consumer<UserDto.UserDtoBuilder> userDtoBuilderConsumer) {
    return userService
        .getUser(Map.of(USERID, userId), headers, tenantId)
        .flatMap(
            user -> {
              Boolean factorSet = user.getBoolean(factorSetField, false);
              if (Boolean.TRUE.equals(factorSet)) {
                return Single.error(MFA_FACTOR_ALREADY_ENROLLED.getCustomException(errorMessage));
              }
              UserDto.UserDtoBuilder userDtoBuilder = UserDto.builder();
              userDtoBuilderConsumer.accept(userDtoBuilder);
              UserDto userDto = userDtoBuilder.build();
              return userService
                  .updateUser(userId, userDto, headers, tenantId)
                  .andThen(Single.just(user));
            });
  }

  public static Set<AuthMethodCategory> getUsedCategories(List<AuthMethod> authMethods) {
    return authMethods.stream().map(AuthMethod::getCategory).collect(Collectors.toSet());
  }

  public static List<MfaFactor> getAvailableMfaFactors(Set<AuthMethodCategory> usedCategories) {
    List<MfaFactor> availableFactors = new ArrayList<>();

    for (MfaFactor factor : MfaFactor.values()) {
      AuthMethodCategory factorCategory = factor.getAuthMethod().getCategory();
      if (!usedCategories.contains(factorCategory)) {
        availableFactors.add(factor);
      }
    }
    return availableFactors;
  }

  public static boolean isFactorEnabled(MfaFactor factor, JsonObject user) {
    if (user == null) {
      return false;
    }

    return switch (factor) {
      case PASSWORD -> {
        Boolean isPasswordSet = user.getBoolean("passwordSet");
        yield isPasswordSet != null && isPasswordSet;
      }
      case PIN -> {
        Boolean isPinSet = user.getBoolean("pinSet");
        yield isPinSet != null && isPinSet;
      }
      case SMS_OTP -> {
        String phoneNumber = user.getString("phoneNumber");
        yield phoneNumber != null && !phoneNumber.isBlank();
      }
      case EMAIL_OTP -> {
        String email = user.getString("email");
        yield email != null && !email.isBlank();
      }
      default -> false;
    };
  }

  public static List<MfaFactorDto> buildMfaFactors(
      List<AuthMethod> currentAuthMethods, JsonObject user, List<String> clientMfaEnabled) {
    if (currentAuthMethods == null || currentAuthMethods.isEmpty()) {
      throw INTERNAL_SERVER_ERROR.getCustomException(
          "AuthMethods cannot be null or empty when building MFA factors");
    }

    Set<AuthMethodCategory> usedCategories = getUsedCategories(currentAuthMethods);
    List<MfaFactor> availableFactors = getAvailableMfaFactors(usedCategories);

    List<MfaFactor> clientEnabledFactors = new ArrayList<>();
    if (clientMfaEnabled != null && !clientMfaEnabled.isEmpty()) {
      for (MfaFactor factor : availableFactors) {
        if (clientMfaEnabled.contains(factor.getValue())) {
          clientEnabledFactors.add(factor);
        }
      }
    } else {
      return new ArrayList<>();
    }

    List<MfaFactorDto> mfaFactors = new ArrayList<>();
    for (MfaFactor factor : clientEnabledFactors) {
      mfaFactors.add(
          MfaFactorDto.builder()
              .factor(factor.getValue())
              .isEnabled(isFactorEnabled(factor, user))
              .build());
    }
    return mfaFactors;
  }
}
