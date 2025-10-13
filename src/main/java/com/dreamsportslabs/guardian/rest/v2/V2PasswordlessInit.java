package com.dreamsportslabs.guardian.rest.v2;

import static com.dreamsportslabs.guardian.constant.Constants.IS_NEW_USER;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.v2.V2PasswordlessInitRequestDto;
import com.dreamsportslabs.guardian.dto.response.v2.V2PasswordlessInitResponseDto;
import com.dreamsportslabs.guardian.service.Passwordless;
import com.google.inject.Inject;
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
@Path("/v2/passwordless/init")
public class V2PasswordlessInit {
  private final Passwordless passwordless;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> init(
      @Context HttpHeaders headers, @Valid V2PasswordlessInitRequestDto requestDto) {

    return passwordless
        .init(requestDto, headers.getRequestHeaders(), headers.getHeaderString(TENANT_ID))
        .map(
            model ->
                Response.ok(
                        V2PasswordlessInitResponseDto.builder()
                            .tries(model.getTries())
                            .retriesLeft(model.getMaxTries() - model.getTries())
                            .resends(model.getResends())
                            .resendsLeft(model.getMaxResends() - model.getResends())
                            .resendAfter(model.getResendAfter())
                            .isNewUser((Boolean) model.getUser().get(IS_NEW_USER))
                            .state(model.getState())
                            .build())
                    .build())
        .toCompletionStage();
  }
}
