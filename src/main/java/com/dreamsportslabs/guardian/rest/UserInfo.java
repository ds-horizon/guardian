package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.APPLICATION_JWT;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.utils.Utils.getAccessTokenFromAuthHeader;

import com.dreamsportslabs.guardian.service.UserInfoService;
import com.google.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/userinfo")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserInfo {
  private final UserInfoService userInfoService;

  @GET
  @Produces({MediaType.APPLICATION_JSON, APPLICATION_JWT})
  public CompletionStage<Response> getUserInfo(
      @Context HttpHeaders headers, @HeaderParam(TENANT_ID) String tenantId) {
    String accessToken =
        getAccessTokenFromAuthHeader(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
    return userInfoService
        .getUserInfo(accessToken, headers.getRequestHeaders(), tenantId)
        .map(claims -> Response.ok(claims).build())
        .toCompletionStage();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON, APPLICATION_JWT})
  public CompletionStage<Response> postUserInfo(
      @Context HttpHeaders headers, @HeaderParam(TENANT_ID) String tenantId) {
    String accessToken =
        getAccessTokenFromAuthHeader(headers.getHeaderString(HttpHeaders.AUTHORIZATION));
    return userInfoService
        .getUserInfo(accessToken, headers.getRequestHeaders(), tenantId)
        .map(claims -> Response.ok(claims).build())
        .toCompletionStage();
  }
}
