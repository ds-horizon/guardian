package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.V1SendOtpRequestDto;
import com.dreamsportslabs.guardian.dto.request.VerifyOtpRequestDto;
import com.dreamsportslabs.guardian.dto.response.OtpSendResponseDto;
import com.dreamsportslabs.guardian.service.ContactVerifyService;
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
@Path("/v1/otp")
public class ContactVerify {
  private final ContactVerifyService contactVerifyService;

  @POST
  @Path("/send")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> send(
      @Context HttpHeaders headers, V1SendOtpRequestDto requestDto) {

    requestDto.validate();

    return contactVerifyService
        .initOtp(requestDto, headers.getRequestHeaders(), headers.getHeaderString(TENANT_ID))
        .map(
            model ->
                Response.ok(
                        OtpSendResponseDto.builder()
                            .tries(model.getTries())
                            .retriesLeft(model.getMaxTries() - model.getTries())
                            .resends(model.getResends())
                            .resendsLeft(model.getMaxResends() - model.getResends())
                            .resendAfter(model.getResendAfter())
                            .state(model.getState())
                            .build())
                    .build())
        .toCompletionStage();
  }

  @POST
  @Path("/verify")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> verify(
      @Context HttpHeaders headers, VerifyOtpRequestDto requestDto) {
    requestDto.validate();
    return contactVerifyService
        .verifyOtp(requestDto.getState(), requestDto.getOtp(), headers.getHeaderString(TENANT_ID))
        .map(success -> Response.noContent().build())
        .toCompletionStage();
  }
}
