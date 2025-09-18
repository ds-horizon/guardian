package com.dreamsportslabs.guardian.rest.v2;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.v2.V2IdpConnectRequestDto;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.dreamsportslabs.guardian.service.IdpConnectService;
import com.google.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
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
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v2/idp/connect")
public class V2IdpConnect {
  private final IdpConnectService idpConnectService;
  private final AuthorizationService authorizationService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> connect(
      @Context HttpHeaders headers,
      @HeaderParam(TENANT_ID) String tenantId,
      @Valid V2IdpConnectRequestDto requestDto) {
    return idpConnectService
        .v2Connect(requestDto, headers.getRequestHeaders(), tenantId)
        .map(
            response -> {
              if (StringUtils.isNotBlank(response.getCode())) {
                return Response.ok(response).build();
              } else {
                return Response.ok(response)
                    .cookie(authorizationService.getIDPConnectCookies(response, tenantId))
                    .build();
              }
            })
        .toCompletionStage();
  }
}
