package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.AUTHORIZATION;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.V1AdminLogoutRequestDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.dreamsportslabs.guardian.utils.AdminUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
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
@Path("/v1/admin/logout")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AdminLogout {
  private final AuthorizationService authorizationService;
  private final Registry registry;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> adminLogout(
      @HeaderParam(TENANT_ID) String tenantId,
      @HeaderParam(AUTHORIZATION) String authorizationHeader,
      V1AdminLogoutRequestDto requestDto) {

    requestDto.validate();

    AdminUtils.validateAdminCredentials(authorizationHeader, tenantId, registry);

    return authorizationService
        .adminLogout(requestDto.getUserId(), tenantId)
        .andThen(
            Single.just(
                Response.noContent()
                    .cookie(authorizationService.getAccessTokenCookie(null, tenantId))
                    .cookie(authorizationService.getRefreshTokenCookie(null, tenantId))
                    .build()))
        .toCompletionStage();
  }
}
