package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.SSO_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.SERVER_ERROR;

import com.dreamsportslabs.guardian.dto.request.LoginAcceptRequestDto;
import com.dreamsportslabs.guardian.dto.response.AuthCodeResponseDto;
import com.dreamsportslabs.guardian.dto.response.LoginAcceptResponseDto;
import com.dreamsportslabs.guardian.service.LoginAcceptService;
import com.google.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/login-accept")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class LoginAccept {
  private final LoginAcceptService loginAcceptService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> loginAccept(
      @Valid LoginAcceptRequestDto requestDto,
      @HeaderParam(TENANT_ID) String tenantId,
      @CookieParam(REFRESH_TOKEN_COOKIE_NAME) String cookieRefreshToken,
      @CookieParam(SSO_TOKEN_COOKIE_NAME) String cookieSsoToken) {
    requestDto.setRefreshTokenFromCookie(cookieRefreshToken);
    requestDto.setSsoTokenFromCookie(cookieSsoToken);

    return loginAcceptService
        .loginAccept(requestDto, tenantId)
        .map(
            res -> {
              Response response;
              if (res instanceof LoginAcceptResponseDto loginAcceptResponseDto) {
                response = loginAcceptResponseDto.toResponse();
              } else if (res instanceof AuthCodeResponseDto authCodeResponseDto) {
                response = authCodeResponseDto.toResponse();
              } else {
                throw SERVER_ERROR.getException();
              }
              return response;
            })
        .toCompletionStage();
  }
}
