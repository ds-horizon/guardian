package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.constant.Constants.SHUTDOWN_STATUS;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApplicationUtil {

  public static void setShutdownStatus(Vertx vertx) {
    AtomicBoolean shutdown =
        VertxUtil.getOrCreateSharedData(vertx, SHUTDOWN_STATUS, () -> new AtomicBoolean(true));
    shutdown.set(true);
  }

  public static AtomicBoolean getShutdownStatus(Vertx vertx) {
    return VertxUtil.getOrCreateSharedData(vertx, SHUTDOWN_STATUS, () -> new AtomicBoolean(false));
  }

  public Single<Response> getHealthCheckResponse(Vertx vertx) {
    boolean shutdownStatus = getShutdownStatus(vertx).get();
    return shutdownStatus
        ? Single.just(Response.serverError().build())
        : Single.just(Response.ok().build());
  }
}
