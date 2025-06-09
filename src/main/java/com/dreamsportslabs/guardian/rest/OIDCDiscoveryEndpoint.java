package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.service.OIDCDiscoveryService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/.well-known/openid-configuration")
public class OIDCDiscoveryEndpoint {

  @Inject private OIDCDiscoveryService oidcDiscoveryService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Single<Response> getOpenIdConfiguration(@HeaderParam(TENANT_ID) String tenantId) {
        return oidcDiscoveryService
                .getOIDCDiscovery(tenantId)
                .map(config -> Response.ok(config).build());
    }
}
