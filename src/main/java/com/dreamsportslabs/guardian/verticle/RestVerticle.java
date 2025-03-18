package com.dreamsportslabs.guardian.verticle;

import com.dream11.rest.AbstractRestVerticle;
import com.dream11.rest.ClassInjector;
import com.dream11.rest.provider.JsonProvider;
import com.dream11.rest.provider.impl.JacksonProvider;
import com.dreamsportslabs.guardian.injection.GuiceInjector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestVerticle extends AbstractRestVerticle {
  private static final String PACKAGE_NAME = "com.dreamsportslabs.guardian";

  public RestVerticle(HttpServerOptions options) {
    super(PACKAGE_NAME, options);
  }

  @Override
  protected ClassInjector getInjector() {
    return GuiceInjector.getGuiceInjector();
  }

  protected JsonProvider getJsonProvider() {
    return new JacksonProvider(
        DatabindCodec.mapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
  }
}
