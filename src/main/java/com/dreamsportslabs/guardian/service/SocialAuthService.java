package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.NO_PICTURE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_FAMILY_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_FULL_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_GIVEN_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_MIDDLE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_PICTURE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PROVIDERS_FACEBOOK;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PROVIDERS_GOOGLE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_TOKENS_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_TOKENS_ID_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_PROVIDER_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_PROVIDER_USER_ID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FLOW_BLOCKED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_NOT_EXISTS;

import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.dreamsportslabs.guardian.constant.Flow;
import com.dreamsportslabs.guardian.dto.Provider;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.V1AuthFbRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1AuthGoogleRequestDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.service.impl.idproviders.FacebookIdProvider;
import com.dreamsportslabs.guardian.service.impl.idproviders.GoogleIdProvider;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class SocialAuthService {
  private final UserService userService;
  private final AuthorizationService authorizationService;
  private final Registry registry;
  private final UserFlowBlockService userFlowBlockService;

  private static final String FACEBOOK_FIELDS_EMAIL = "email";
  private static final String FACEBOOK_FIELDS_USER_ID = "id";
  private static final String FACEBOOK_FIELDS_FULL_NAME = "name";
  private static final String FACEBOOK_FIELDS_FIRST_NAME = "first_name";
  private static final String FACEBOOK_FIELDS_MIDDLE_NAME = "middle_name";
  private static final String FACEBOOK_FIELDS_LAST_NAME = "last_name";
  private static final String FACEBOOK_FIELDS_PICTURE = "picture";
  private static final String FACEBOOK_FIELDS_PICTURE_DATA = "data";
  private static final String FACEBOOK_FIELDS_PICTURE_DATA_URL = "url";

  public Single<Object> authFb(
      V1AuthFbRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    return registry
        .get(tenantId, FacebookIdProvider.class)
        .getUserIdentity(dto.getAccessToken())
        .flatMap(
            fbUserData -> {
              String email = fbUserData.getString(FACEBOOK_FIELDS_EMAIL);
              if (email != null) {
                return userFlowBlockService
                    .isFlowBlocked(tenantId, List.of(email), BlockFlow.SOCIAL_AUTH)
                    .map(
                        blockedResult -> {
                          if (blockedResult.blocked()) {
                            throw FLOW_BLOCKED.getCustomException(blockedResult.reason());
                          }
                          return fbUserData;
                        });
              }
              return Single.just(fbUserData);
            })
        .flatMap(
            fbUserData ->
                userService
                    .getUser(
                        Map.of(
                            USER_FILTERS_EMAIL,
                            fbUserData.getString(FACEBOOK_FIELDS_EMAIL),
                            USER_FILTERS_PROVIDER_NAME,
                            OIDC_PROVIDERS_FACEBOOK,
                            USER_FILTERS_PROVIDER_USER_ID,
                            fbUserData.getString(FACEBOOK_FIELDS_USER_ID)),
                        headers,
                        tenantId)
                    .map(res -> Pair.of(fbUserData, res)))
        .flatMap(
            userDetails -> {
              JsonObject fbUserData = userDetails.getLeft();
              JsonObject userRes = userDetails.getRight();

              boolean userExists = userRes.getString(USERID) != null;
              if (dto.getFlow() == Flow.SIGNIN && !userExists) {
                return Single.error(USER_NOT_EXISTS.getException());
              } else if (dto.getFlow() == Flow.SIGNUP && userExists) {
                return Single.error(USER_EXISTS.getException());
              }

              if (!userExists) {
                return userService.createUser(
                    getUserDtoFromFbUserData(fbUserData, dto.getAccessToken()), headers, tenantId);
              } else {
                return userService
                    .addProvider(
                        userRes.getString(USERID),
                        headers,
                        getFbProviderData(fbUserData, dto.getAccessToken()),
                        tenantId)
                    .andThen(Single.just(userRes));
              }
            })
        .flatMap(
            user ->
                authorizationService.generate(
                    user, dto.getResponseType(), dto.getMetaInfo(), tenantId));
  }

  private UserDto getUserDtoFromFbUserData(JsonObject fbUserData, String accessToken) {
    return UserDto.builder()
        .name(fbUserData.getString(FACEBOOK_FIELDS_FULL_NAME))
        .firstName(fbUserData.getString(FACEBOOK_FIELDS_FIRST_NAME))
        .middleName(fbUserData.getString(FACEBOOK_FIELDS_MIDDLE_NAME))
        .lastName(fbUserData.getString(FACEBOOK_FIELDS_LAST_NAME))
        .email(fbUserData.getString(FACEBOOK_FIELDS_EMAIL))
        .picture(
            fbUserData
                .getJsonObject(FACEBOOK_FIELDS_PICTURE, NO_PICTURE)
                .getJsonObject(FACEBOOK_FIELDS_PICTURE_DATA)
                .getString(FACEBOOK_FIELDS_PICTURE_DATA_URL))
        .provider(getFbProviderData(fbUserData, accessToken))
        .build();
  }

  private Provider getFbProviderData(JsonObject fbUserData, String accessToken) {
    return Provider.builder()
        .name(OIDC_PROVIDERS_FACEBOOK)
        .providerUserId(fbUserData.getString(FACEBOOK_FIELDS_USER_ID))
        .data(fbUserData.getMap())
        .credentials(Map.of(OIDC_TOKENS_ACCESS_TOKEN, accessToken))
        .build();
  }

  public Single<Object> authGoogle(
      V1AuthGoogleRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    return registry
        .get(tenantId, GoogleIdProvider.class)
        .getUserIdentity(dto.getIdToken())
        .flatMap(
            googleUserData -> {
              String email = googleUserData.getString(OIDC_CLAIMS_EMAIL);
              if (email != null) {
                return userFlowBlockService
                    .isFlowBlocked(tenantId, List.of(email), BlockFlow.SOCIAL_AUTH)
                    .map(
                        result -> {
                          if (result.blocked()) {
                            throw FLOW_BLOCKED.getCustomException(result.reason());
                          }
                          return googleUserData;
                        });
              }
              return Single.just(googleUserData);
            })
        .flatMap(
            googleUserData ->
                userService
                    .getUser(
                        Map.of(
                            USER_FILTERS_EMAIL,
                            googleUserData.getString(OIDC_CLAIMS_EMAIL),
                            USER_FILTERS_PROVIDER_NAME,
                            OIDC_PROVIDERS_GOOGLE,
                            USER_FILTERS_PROVIDER_USER_ID,
                            googleUserData.getString(OIDC_CLAIMS_SUB)),
                        headers,
                        tenantId)
                    .map(res -> Pair.of(googleUserData, res)))
        .flatMap(
            userDetails -> {
              JsonObject googleUserData = userDetails.getLeft();
              JsonObject userRes = userDetails.getRight();

              boolean userExists = userRes.getString(USERID) != null;
              if (dto.getFlow() == Flow.SIGNIN && !userExists) {
                return Single.error(USER_NOT_EXISTS.getException());
              } else if (dto.getFlow() == Flow.SIGNUP && userExists) {
                return Single.error(USER_EXISTS.getException());
              }

              if (!userExists) {
                return userService.createUser(
                    getUserDtoFromGoogleUserData(googleUserData, dto.getIdToken()),
                    headers,
                    tenantId);
              } else {
                return userService
                    .addProvider(
                        userRes.getString(USERID),
                        headers,
                        getGoogleProviderData(googleUserData, dto.getIdToken()),
                        tenantId)
                    .andThen(Single.just(userRes));
              }
            })
        .flatMap(
            user ->
                authorizationService.generate(
                    user, dto.getResponseType().getResponseType(), dto.getMetaInfo(), tenantId));
  }

  private UserDto getUserDtoFromGoogleUserData(JsonObject googleUserData, String idToken) {
    return UserDto.builder()
        .name(googleUserData.getString(OIDC_CLAIMS_FULL_NAME))
        .firstName(googleUserData.getString(OIDC_CLAIMS_GIVEN_NAME))
        .middleName(googleUserData.getString(OIDC_CLAIMS_MIDDLE_NAME))
        .lastName(googleUserData.getString(OIDC_CLAIMS_FAMILY_NAME))
        .email(googleUserData.getString(OIDC_CLAIMS_EMAIL))
        .picture(googleUserData.getString(OIDC_CLAIMS_PICTURE))
        .provider(getGoogleProviderData(googleUserData, idToken))
        .build();
  }

  private Provider getGoogleProviderData(JsonObject googleUserData, String idToken) {
    return Provider.builder()
        .name(OIDC_PROVIDERS_GOOGLE)
        .providerUserId(googleUserData.getString(OIDC_CLAIMS_SUB))
        .data(googleUserData.getMap())
        .credentials(Map.of(OIDC_TOKENS_ID_TOKEN, idToken))
        .build();
  }
}
