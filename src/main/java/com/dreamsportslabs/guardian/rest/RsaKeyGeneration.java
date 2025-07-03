package com.dreamsportslabs.guardian.rest;

import com.dreamsportslabs.guardian.dto.request.GenerateRsaKeyRequestDto;
import com.dreamsportslabs.guardian.dto.response.RsaKeyResponseDto;
import com.dreamsportslabs.guardian.service.RsaKeyPairGeneratorService;
import com.google.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/v1/keys")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class RsaKeyGeneration {

  final RsaKeyPairGeneratorService rsaKeyService;

  @POST
  @Path("/generate")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> generateRsaKey(GenerateRsaKeyRequestDto request) {

    return CompletableFuture.supplyAsync(
        () -> {
          RsaKeyResponseDto response = rsaKeyService.generateKey(request);
          return Response.ok(response).build();
        });
  }
}
