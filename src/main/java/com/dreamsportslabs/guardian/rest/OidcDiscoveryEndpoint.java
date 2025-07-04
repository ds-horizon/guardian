package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.service.OidcDiscoveryService;
import com.google.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/.well-known/openid-configuration")
public class OidcDiscoveryEndpoint {

  @Inject private OidcDiscoveryService oidcDiscoveryService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getOpenIdConfiguration(@HeaderParam(TENANT_ID) String tenantId) {
    return oidcDiscoveryService
        .getOidcDiscovery(tenantId)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }
}
