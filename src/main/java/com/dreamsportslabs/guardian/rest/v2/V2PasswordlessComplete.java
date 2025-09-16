package com.dreamsportslabs.guardian.rest.v2;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.v1.V1PasswordlessCompleteRequestDto;
import com.dreamsportslabs.guardian.dto.response.TokenResponseDto;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.dreamsportslabs.guardian.service.Passwordless;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.vertx.core.json.JsonObject;
import jakarta.validation.Valid;
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

@Path("/v1/passwordless/complete")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class V2PasswordlessComplete {
  private final Passwordless passwordless;
  private final AuthorizationService authorizationService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> complete(
      @HeaderParam(TENANT_ID) String tenantId, @Valid V1PasswordlessCompleteRequestDto dto) {
    return passwordless
        .complete(dto, tenantId)
        .map(
            resp -> {
              if (resp instanceof TokenResponseDto tokenResponseDto) {
                return Response.ok(
                        Utils.convertKeysToSnakeCase(JsonObject.mapFrom(tokenResponseDto)))
                    .cookie(authorizationService.getCookies(tokenResponseDto, tenantId))
                    .build();
              }
              return Response.ok(Utils.convertKeysToSnakeCase(JsonObject.mapFrom(resp))).build();
            })
        .toCompletionStage();
  }
}
