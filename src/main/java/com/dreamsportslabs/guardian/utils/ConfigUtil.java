package com.dreamsportslabs.guardian.utils;

import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.Vertx;

public final class ConfigUtil {
  private static final Long DEFAULT_SCAN_PERIOD_MS = 0L;

  private ConfigUtil() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static ConfigRetriever getConfigRetriever(Vertx vertx, String filePath) {
    ConfigStoreOptions fileStoreOptions = getFileStoreOptions(filePath).setOptional(true);

    return ConfigRetriever.create(
        vertx,
        new ConfigRetrieverOptions()
            .addStore(fileStoreOptions) // application.conf file
            .setScanPeriod(DEFAULT_SCAN_PERIOD_MS));
  }

  private static ConfigStoreOptions getFileStoreOptions(String filePath) {
    return new ConfigStoreOptions()
        .setType("file")
        .setFormat("hocon")
        .setConfig(new JsonObject().put("path", filePath));
  }
}
