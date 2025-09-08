package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_AMR;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_CLIENT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_EXP;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_IAT;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_ISS;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_JTI;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SCOPE;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_TENANT_ID_CLAIM;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN_TYPE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CLIENT_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_SCOPE;
import static com.dreamsportslabs.guardian.utils.Utils.decryptUsingAESCBCAlgo;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;

import com.dreamsportslabs.guardian.config.tenant.GuestConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.ClientScopeDao;
import com.dreamsportslabs.guardian.dao.model.ClientScopeModel;
import com.dreamsportslabs.guardian.dto.request.V1GuestLoginRequestDto;
import com.dreamsportslabs.guardian.dto.response.GuestLoginResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class GuestLoginService {

  private final Registry registry;
  private final TokenIssuer tokenIssuer;
  private final ClientScopeDao clientScopeDao;

  public Single<GuestLoginResponseDto> login(V1GuestLoginRequestDto requestDto, String tenantId) {
    TenantConfig config = registry.get(tenantId, TenantConfig.class);
    GuestConfig guestConfig = config.getGuestConfig();

    Boolean isEncrypted = guestConfig.getIsEncrypted();
    String sharedSecretKey = guestConfig.getSecretKey();
    List<String> guestAllowedScopes = guestConfig.getAllowedScopes();
    List<String> scopes = requestDto.getScopes();
    String guestIdentifier = requestDto.getGuestIdentifier();
    if (isEncrypted) {
      guestIdentifier = decryptUsingAESCBCAlgo(guestIdentifier, sharedSecretKey);
    }
    scopes.forEach(
        scope -> {
          if (!guestAllowedScopes.contains(scope)) {
            throw INVALID_SCOPE.getCustomException("Invalid scope '" + scope + "'");
          }
        });

    String scope = String.join(" ", scopes);
    Long iat = getCurrentTimeInSeconds();

    Map<String, Object> claims = new HashMap<>();
    claims.put(JWT_CLAIMS_SUB, guestIdentifier);
    claims.put(JWT_CLAIMS_JTI, RandomStringUtils.randomAlphanumeric(32));
    claims.put(JWT_CLAIMS_IAT, iat);
    claims.put(JWT_CLAIMS_EXP, iat + config.getTokenConfig().getAccessTokenExpiry());
    claims.put(JWT_CLAIMS_ISS, config.getTokenConfig().getIssuer());
    claims.put(JWT_TENANT_ID_CLAIM, tenantId);
    claims.put(JWT_CLAIMS_SCOPE, scope);
    claims.put(JWT_CLAIMS_AMR, Collections.emptyList());
    claims.put(JWT_CLAIMS_CLIENT_ID, requestDto.getClientId());

    return validateClientScopes(requestDto.getClientId(), tenantId, scopes)
        .andThen(tokenIssuer.generateAccessToken(claims, tenantId))
        .map(
            accessToken ->
                GuestLoginResponseDto.builder()
                    .accessToken(accessToken)
                    .tokenType(TOKEN_TYPE)
                    .expiresIn(config.getTokenConfig().getAccessTokenExpiry())
                    .build());
  }

  public Completable validateClientScopes(
      String clientId, String tenantId, List<String> requestedScopes) {
    return clientScopeDao
        .getClientScopes(clientId, tenantId)
        .map(
            clientScopes -> {
              if (clientScopes.isEmpty()) {
                throw CLIENT_NOT_FOUND.getException();
              }
              return clientScopes.stream()
                  .map(ClientScopeModel::getScope)
                  .collect(Collectors.toSet());
            })
        .flatMapCompletable(
            allowedClientScopes -> {
              requestedScopes.forEach(
                  scope -> {
                    if (!allowedClientScopes.contains(scope)) {
                      throw INVALID_SCOPE.getCustomException("Invalid scope '" + scope + "'");
                    }
                  });
              return Completable.complete();
            });
  }
}
