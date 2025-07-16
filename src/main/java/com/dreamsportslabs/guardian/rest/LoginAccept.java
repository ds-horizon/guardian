package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.LoginAcceptRequestDto;
import com.dreamsportslabs.guardian.service.LoginAcceptService;
import com.google.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
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
      LoginAcceptRequestDto requestDto,
      @HeaderParam(TENANT_ID) String tenantId,
      @CookieParam(REFRESH_TOKEN_COOKIE_NAME) String cookieRefreshToken) {
    requestDto.setRefreshTokenFromCookie(cookieRefreshToken);
    requestDto.validate();
    return loginAcceptService
        .loginAccept(requestDto, tenantId)
        .map(ResponseBuilder::build)
        .toCompletionStage();
  }
}
