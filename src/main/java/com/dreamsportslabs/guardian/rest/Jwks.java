package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.KEYS;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.service.JwksService;
import com.google.inject.Inject;
import io.vertx.core.json.JsonObject;
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
@Path("/certs")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class Jwks {

  private final JwksService jwksService;

  @GET
  public CompletionStage<Response> getJwks(@HeaderParam(TENANT_ID) String tenantId) {
    return jwksService
        .getJwks(tenantId)
        .map(keys -> Response.ok(new JsonObject().put(KEYS, keys)).build())
        .toCompletionStage();
  }
}
