package com.dreamsportslabs.guardian.rest;

import com.dreamsportslabs.guardian.utils.ApplicationUtil;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Hidden;
import io.vertx.core.Vertx;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/healthcheck")
public class HealthCheck {

  final ApplicationUtil applicationUtil;

  @GET
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  @Hidden
  public CompletionStage<Response> healthcheck() {
    return applicationUtil
        .getHealthCheckResponse(Vertx.currentContext().owner())
        .toCompletionStage();
  }
}
