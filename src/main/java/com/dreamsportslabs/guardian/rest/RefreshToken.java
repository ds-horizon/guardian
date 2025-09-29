package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.UNAUTHORIZED_ERROR_CODE;

import com.dreamsportslabs.guardian.dto.request.V1RefreshTokenRequestDto;
import com.dreamsportslabs.guardian.dto.request.v2.V2RefreshTokenRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum.ErrorEntity;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.google.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/refreshToken")
public class RefreshToken {
  private final AuthorizationService authorizationService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> refreshTokens(
      @Context HttpHeaders headers, V1RefreshTokenRequestDto requestDto) {
    String tenantId = headers.getHeaderString(TENANT_ID);
    requestDto.validate();
    V2RefreshTokenRequestDto refreshTokenRequestDto = new V2RefreshTokenRequestDto();
    refreshTokenRequestDto.setRefreshToken(requestDto.getRefreshToken());
    return authorizationService
        .refreshTokens(refreshTokenRequestDto, headers.getRequestHeaders(), tenantId)
        .map(
            resp ->
                Response.ok(resp)
                    .cookie(
                        authorizationService.getAccessTokenCookie(resp.getAccessToken(), tenantId))
                    .build())
        .onErrorReturn(
            err -> {
              if (err instanceof WebApplicationException webAppEx
                  && webAppEx.getResponse() != null) {
                if (webAppEx.getResponse().getEntity() instanceof ErrorEntity errorEntity
                    && errorEntity.getError().getCode().equals(UNAUTHORIZED_ERROR_CODE)) {
                  return Response.status(webAppEx.getResponse().getStatus())
                      .entity(errorEntity)
                      .cookie(authorizationService.getAccessTokenCookie(null, tenantId))
                      .cookie(authorizationService.getRefreshTokenCookie(null, tenantId))
                      .cookie(authorizationService.getSsoTokenCookie(null, tenantId))
                      .build();
                }
                return webAppEx.getResponse();
              }
              throw err;
            })
        .toCompletionStage();
  }
}
