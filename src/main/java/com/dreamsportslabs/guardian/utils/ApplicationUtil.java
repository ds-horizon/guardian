package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.constant.Constants.SHUTDOWN_STATUS;

import io.vertx.core.Vertx;
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
}
