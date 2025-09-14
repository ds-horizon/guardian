package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.utils.ApplicationUtil.getShutdownStatus;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import jakarta.ws.rs.core.Response;

public class HealthCheckService {

  public Single<Response> getHealthCheckResponse() {
    boolean shutdownStatus = getShutdownStatus(Vertx.currentContext().owner().getDelegate()).get();
    return shutdownStatus
        ? Single.just(Response.serverError().build())
        : Single.just(Response.ok().build());
  }
}
