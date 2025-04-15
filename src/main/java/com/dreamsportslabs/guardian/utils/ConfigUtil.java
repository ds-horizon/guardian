package com.dreamsportslabs.guardian.utils;

import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.Vertx;

public final class ConfigUtil {
  private ConfigUtil() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static ConfigRetriever getConfigRetriever(Vertx vertx) {
    ConfigStoreOptions defaultConfig =
        new ConfigStoreOptions()
            .setType("file")
            .setFormat("hocon")
            .setConfig(new JsonObject().put("path", "guardian-default.conf"));
    ConfigStoreOptions envConfig =
        new ConfigStoreOptions()
            .setType("file")
            .setFormat("hocon")
            .setConfig(new JsonObject().put("path", "guardian.conf"));

    return ConfigRetriever.create(
        vertx, new ConfigRetrieverOptions().addStore(defaultConfig).addStore(envConfig));
  }
}
