package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_EXP;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_IAT;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_ISS;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_JTI;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SCOPE;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_TENANT_ID_CLAIM;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN_TYPE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_SCOPES;
import static com.dreamsportslabs.guardian.utils.Utils.decryptUsingAESCBCAlgo;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;

import com.dreamsportslabs.guardian.config.tenant.GuestConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dto.request.V1GuestLoginRequestDto;
import com.dreamsportslabs.guardian.dto.response.GuestLoginResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class GuestLoginService {

  private final Registry registry;
  private final TokenIssuer tokenIssuer;

  public Single<GuestLoginResponseDto> login(V1GuestLoginRequestDto requestDto, String tenantId) {
    GuestConfig guestConfig = registry.get(tenantId, TenantConfig.class).getGuestConfig();
    TenantConfig config = registry.get(tenantId, TenantConfig.class);
    Boolean isEncrypted = guestConfig.getIsEncrypted();
    String sharedSecretKey = guestConfig.getSharedSecretKey();
    List<String> allowedScopes = guestConfig.getAllowedScopes();
    List<String> scopes = requestDto.getScopes();
    String guestIdentifier = requestDto.getGuestIdentifier();
    if (isEncrypted) {
      guestIdentifier = decryptUsingAESCBCAlgo(guestIdentifier, sharedSecretKey);
    }
    scopes.forEach(
        scope -> {
          if (!allowedScopes.contains(scope)) {
            throw INVALID_SCOPES.getCustomException("Invalid scope '" + scope + "'");
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
    claims.put(JWT_CLAIMS_TYPE, "guest");

    return tokenIssuer
        .generateAccessToken(claims, tenantId)
        .map(
            accessToken ->
                GuestLoginResponseDto.builder()
                    .access_token(accessToken)
                    .token_type(TOKEN_TYPE)
                    .expires_in(config.getTokenConfig().getAccessTokenExpiry())
                    .build());
  }
}
