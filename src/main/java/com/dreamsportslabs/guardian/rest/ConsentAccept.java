package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.ConsentAcceptRequestDto;
import com.dreamsportslabs.guardian.service.ConsentAcceptService;
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
@Path("/consent-accept")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ConsentAccept {
  private final ConsentAcceptService consentAcceptService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> consentAccept(
      ConsentAcceptRequestDto requestDto, @Context HttpHeaders headers) {
    requestDto.validate();

    return consentAcceptService
        .consentAccept(requestDto, headers.getHeaderString(TENANT_ID))
        .toCompletionStage();
  }
}
