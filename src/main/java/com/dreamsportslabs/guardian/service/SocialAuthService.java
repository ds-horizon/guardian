package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.FACEBOOK;
import static com.dreamsportslabs.guardian.constant.Constants.GOOGLE;
import static com.dreamsportslabs.guardian.constant.Constants.NO_PICTURE;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_NOT_EXISTS;

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

  public Single<Object> authFb(
      V1AuthFbRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    return registry
        .get(tenantId, FacebookIdProvider.class)
        .getUserIdentity(dto.getAccessToken())
        .flatMap(
            fbUserData ->
                userService
                    .getUser(
                        Map.of(
                            "email",
                            fbUserData.getString("email"),
                            "providerName",
                            "facebook",
                            "providerId",
                            fbUserData.getString("id")),
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
                    .map(res -> userRes);
              }
            })
        .flatMap(
            user ->
                authorizationService.generate(
                    user, dto.getResponseType(), dto.getMetaInfo(), tenantId));
  }

  private UserDto getUserDtoFromFbUserData(JsonObject fbUserData, String accessToken) {
    return UserDto.builder()
        .name(fbUserData.getString("name"))
        .firstName(fbUserData.getString("first_name"))
        .middleName(fbUserData.getString("middle_name"))
        .lastName(fbUserData.getString("last_name"))
        .email(fbUserData.getString("email"))
        .picture(
            fbUserData.getJsonObject("picture", NO_PICTURE).getJsonObject("data").getString("url"))
        .provider(getFbProviderData(fbUserData, accessToken))
        .build();
  }

  private Provider getFbProviderData(JsonObject fbUserData, String accessToken) {
    return Provider.builder()
        .name(FACEBOOK)
        .providerUserId(fbUserData.getString("id"))
        .data(fbUserData.getMap())
        .credentials(Map.of("access_token", accessToken))
        .build();
  }

  public Single<Object> authGoogle(
      V1AuthGoogleRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    return registry
        .get(tenantId, GoogleIdProvider.class)
        .getUserIdentity(dto.getIdToken())
        .flatMap(
            googleUserData ->
                userService
                    .getUser(
                        Map.of(
                            "email",
                            googleUserData.getString("email"),
                            "providerName",
                            "google",
                            "providerId",
                            googleUserData.getString("sub")),
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
                    .map(res -> userRes);
              }
            })
        .flatMap(
            user ->
                authorizationService.generate(
                    user, dto.getResponseType().getResponseType(), dto.getMetaInfo(), tenantId));
  }

  private UserDto getUserDtoFromGoogleUserData(JsonObject googleUserData, String idToken) {
    return UserDto.builder()
        .name(googleUserData.getString("name"))
        .firstName(googleUserData.getString("given_name"))
        .middleName(googleUserData.getString("middle_name"))
        .lastName(googleUserData.getString("family_name"))
        .email(googleUserData.getString("email"))
        .picture(googleUserData.getString("picture"))
        .provider(getGoogleProviderData(googleUserData, idToken))
        .build();
  }

  private Provider getGoogleProviderData(JsonObject googleUserData, String idToken) {
    return Provider.builder()
        .name(GOOGLE)
        .providerUserId(googleUserData.getString("id"))
        .data(googleUserData.getMap())
        .credentials(Map.of("id_token", idToken))
        .build();
  }
}
