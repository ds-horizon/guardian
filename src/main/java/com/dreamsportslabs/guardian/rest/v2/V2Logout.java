package com.dreamsportslabs.guardian.rest.v2;

import static com.dreamsportslabs.guardian.constant.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.v2.V2LogoutRequestDto;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.google.inject.Inject;
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

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v2/logout")
@Slf4j
public class V2Logout {

  private final AuthorizationService authorizationService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> v2Logout(
      @HeaderParam(TENANT_ID) String tenantId,
      @CookieParam(REFRESH_TOKEN_COOKIE_NAME) String cookieRefreshToken,
      V2LogoutRequestDto requestDto) {
    requestDto.setRefreshTokenFromCookie(cookieRefreshToken);

    return authorizationService
        .logout(requestDto, tenantId)
        .toSingleDefault(
            Response.noContent()
                .cookie(authorizationService.getAccessTokenCookie(null, tenantId))
                .cookie(authorizationService.getRefreshTokenCookie(null, tenantId))
                .cookie(authorizationService.getSsoTokenCookie(null, tenantId))
                .build())
        .toCompletionStage();
  }
}
