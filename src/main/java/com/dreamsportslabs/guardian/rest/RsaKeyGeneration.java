package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.GenerateRsaKeyRequestDto;
import com.dreamsportslabs.guardian.dto.response.RsaKeyResponseDto;
import com.dreamsportslabs.guardian.service.RsaKeyPairGeneratorService;
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
@Path("/v1/keys")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class RsaKeyGeneration {

  final RsaKeyPairGeneratorService rsaKeyService;

  @POST
  @Path("/generate")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> generateRsaKey(
      @HeaderParam(TENANT_ID) String tenantId, GenerateRsaKeyRequestDto request) {

    return Single.fromCallable(
            () -> {
              RsaKeyResponseDto response = rsaKeyService.generateKey(request);
              log.info(
                  "Successfully generated RSA key with kid: {} for tenant: {}",
                  response.getKid(),
                  tenantId);
              return Response.ok(response).build();
            })
        .toCompletionStage();
  }
}
