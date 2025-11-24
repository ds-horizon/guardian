package com.dreamsportslabs.guardian.rest.v2;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.v2.V2MfaSignInRequestDto;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.dreamsportslabs.guardian.service.MfaService;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.vertx.core.json.JsonObject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
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
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v2/mfa")
public class V2Mfa {

  private final MfaService mfaService;
  private final AuthorizationService authorizationService;

  @POST
  @Path("/signin")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> v2MfaSignIn(
      @Context HttpHeaders httpHeaders, @Valid V2MfaSignInRequestDto requestDto) {
    String tenantId = httpHeaders.getHeaderString(TENANT_ID);
    return mfaService
        .mfaSignIn(requestDto, httpHeaders.getRequestHeaders(), tenantId)
        .map(
            tokenResponseDto ->
                Response.ok(Utils.convertKeysToSnakeCase(JsonObject.mapFrom(tokenResponseDto)))
                    .cookie(authorizationService.getCookies(tokenResponseDto, tenantId))
                    .build())
        .toCompletionStage();
  }

  @POST
  @Path("/enroll")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> v2MfaEnroll(
      @Context HttpHeaders httpHeaders, @Valid V2MfaSignInRequestDto requestDto) {
    String tenantId = httpHeaders.getHeaderString(TENANT_ID);
    return mfaService
        .mfaEnroll(requestDto, httpHeaders.getRequestHeaders(), tenantId)
        .map(
            tokenResponseDto ->
                Response.ok(Utils.convertKeysToSnakeCase(JsonObject.mapFrom(tokenResponseDto)))
                    .cookie(authorizationService.getCookies(tokenResponseDto, tenantId))
                    .build())
        .toCompletionStage();
  }
}
