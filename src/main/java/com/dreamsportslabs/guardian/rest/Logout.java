package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.V1LogoutRequestDto;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
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
@Path("/v1/logout")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class Logout {
  private final AuthorizationService authorizationService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> logout(
      @Context HttpHeaders headers, V1LogoutRequestDto requestDto) {
    if (requestDto == null) {
      requestDto = new V1LogoutRequestDto();
    }
    String tenantId = headers.getHeaderString(TENANT_ID);

    requestDto.validate();
    return authorizationService
        .logout(requestDto, tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
