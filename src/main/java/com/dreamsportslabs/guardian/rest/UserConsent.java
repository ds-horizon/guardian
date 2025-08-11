package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.UserConsentRequestDto;
import com.dreamsportslabs.guardian.service.UserConsentService;
import com.google.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/user-consent")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserConsent {

  private final UserConsentService userConsentService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getUserConsent(
      @BeanParam UserConsentRequestDto requestDto, @HeaderParam(TENANT_ID) String tenantId) {

    requestDto.validate();

    return userConsentService
        .getUserConsent(requestDto.getConsentChallenge(), tenantId)
        .map(responseDto -> Response.ok(responseDto).build())
        .toCompletionStage();
  }
}
