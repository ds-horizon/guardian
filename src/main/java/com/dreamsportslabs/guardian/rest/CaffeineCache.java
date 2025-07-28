package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.cache.TenantCache;
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
@Path("/cache")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CaffeineCache {
  private final TenantCache tenantCache;

  @POST
  @Path("/clear")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createClient(@HeaderParam(TENANT_ID) String tenantId) {

    tenantCache.invalidateCache(tenantId);

    return Single.just(
            Response.status(Response.Status.NO_CONTENT).entity("Cache invalidated").build())
        .toCompletionStage();
  }
}
