package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.V1GuestLoginRequestDto;
import com.dreamsportslabs.guardian.service.GuestLoginService;
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
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Path("/v1/guest/login")
public class GuestLogin {

  private final GuestLoginService guestService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> guestLogin(
      @Context HttpHeaders headers, V1GuestLoginRequestDto requestDto) {
    requestDto.validate();
    return guestService
        .login(requestDto, headers.getHeaderString(TENANT_ID))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }
}
