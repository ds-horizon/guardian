package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.AuthorizeRequestDto;
import com.dreamsportslabs.guardian.dto.response.AuthorizeResponseDto;
import com.dreamsportslabs.guardian.service.AuthorizeService;
import com.google.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
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
@Path("/authorize")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class Authorize {
  private final AuthorizeService authorizeService;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> authorize(
      @BeanParam AuthorizeRequestDto requestDto, @HeaderParam(TENANT_ID) String tenantId) {
    requestDto.validate();

    return authorizeService
        .authorize(requestDto, tenantId)
        .map(AuthorizeResponseDto::toResponse)
        .toCompletionStage();
  }
}
