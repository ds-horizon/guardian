package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.AUTHORIZATION;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.dto.request.RevokeTokenRequestDto;
import com.dreamsportslabs.guardian.service.OidcTokenService;
import com.google.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
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
@Path("/token/revoke")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class RevokeToken {

  private final OidcTokenService oidcTokenService;

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> revokeToken(
      @HeaderParam(value = TENANT_ID) String tenantId,
      @HeaderParam(value = AUTHORIZATION) String authorizationHeader,
      @BeanParam RevokeTokenRequestDto revokeTokenRequestDto) {

    if (revokeTokenRequestDto == null) {
      throw INVALID_REQUEST.getJsonCustomException("request body is required");
    }
    revokeTokenRequestDto.validate();
    revokeTokenRequestDto.validateAuth(authorizationHeader);

    return oidcTokenService
        .revokeOidcToken(revokeTokenRequestDto, tenantId, authorizationHeader)
        .toSingleDefault(Response.ok().build())
        .toCompletionStage();
  }
}
