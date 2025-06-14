package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.V1SignUpRequestDto;
import com.dreamsportslabs.guardian.dto.response.TokenResponseDto;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.dreamsportslabs.guardian.service.PasswordAuth;
import com.google.inject.Inject;
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
@Path("/v1/signup")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class SignUp {
  private final PasswordAuth passwordAuth;
  private final AuthorizationService authorizationService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> signUp(
      @Context HttpHeaders headers, V1SignUpRequestDto requestDto) {
    requestDto.validate();
    String tenantId = headers.getHeaderString(TENANT_ID);

    return passwordAuth
        .signUp(requestDto, headers.getRequestHeaders(), tenantId)
        .map(
            resp -> {
              if (resp instanceof TokenResponseDto tokenResponseDto) {
                return Response.ok(tokenResponseDto)
                    .cookie(authorizationService.getCookies(tokenResponseDto, tenantId))
                    .build();
              }
              return Response.ok(resp).build();
            })
        .toCompletionStage();
  }
}
