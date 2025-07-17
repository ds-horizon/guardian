package com.dreamsportslabs.guardian.dto.response;

import static com.dreamsportslabs.guardian.constant.Constants.APP_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.APP_CODE;
import static com.dreamsportslabs.guardian.constant.Constants.APP_ID_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.APP_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.APP_TOKEN_CODE_EXPIRY;
import static com.dreamsportslabs.guardian.constant.Constants.APP_TOKEN_TYPE;

import com.dreamsportslabs.guardian.dao.model.IdpCredentials;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdpConnectResponseDto {
  private String code;
  private String accessToken;
  private String refreshToken;
  private String idToken;
  private String tokenType;
  private Integer expiresIn;
  private Boolean isNewUser;
  private IdpCredentials idpCredentials;

  public static IdpConnectResponseDto buildIdpConnectResponse(
      Object authResponse, Boolean isNewUser, IdpCredentials idpTokens) {
    JsonObject responseJson = JsonObject.mapFrom(authResponse);

    return new IdpConnectResponseDto(
        responseJson.getString(APP_CODE),
        responseJson.getString(APP_ACCESS_TOKEN),
        responseJson.getString(APP_REFRESH_TOKEN),
        responseJson.getString(APP_ID_TOKEN),
        responseJson.getString(APP_TOKEN_TYPE),
        responseJson.getInteger(APP_TOKEN_CODE_EXPIRY),
        isNewUser,
        idpTokens);
  }
}
