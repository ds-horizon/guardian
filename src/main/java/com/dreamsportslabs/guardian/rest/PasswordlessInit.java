package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.V1PasswordlessInitRequestDto;
import com.dreamsportslabs.guardian.dto.response.V1PasswordlessInitResponseDto;
import com.dreamsportslabs.guardian.service.Passwordless;
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
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/passwordless/init")
public class PasswordlessInit {
  private final Passwordless passwordless;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> init(
      @Context HttpHeaders headers, V1PasswordlessInitRequestDto requestDto) {
    requestDto.validate();
    return passwordless
        .init(requestDto, headers.getRequestHeaders(), headers.getHeaderString(TENANT_ID))
        .map(
            model ->
                Response.ok(
                        V1PasswordlessInitResponseDto.builder()
                            .tries(model.getTries())
                            .retriesLeft(model.getMaxTries() - model.getTries())
                            .resends(model.getResends())
                            .resendsLeft(model.getMaxResends() - model.getResends())
                            .resendAfter(model.getResendAfter())
                            .isNewUser(model.getUser().get("isNewUser") != null)
                            .state(model.getState())
                            .build())
                    .build())
        .toCompletionStage();
  }
}
