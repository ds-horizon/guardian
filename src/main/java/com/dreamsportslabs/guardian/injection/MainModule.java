package com.dreamsportslabs.guardian.injection;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.client.impl.MysqlClientImpl;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.SharedDataUtils;
import com.google.inject.AbstractModule;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.rxjava3.redis.client.Redis;

public class MainModule extends AbstractModule {
  private final Vertx vertx;

  public MainModule(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  protected void configure() {
    bind(Vertx.class).toInstance(this.vertx);
    bind(io.vertx.rxjava3.core.Vertx.class)
        .toInstance(io.vertx.rxjava3.core.Vertx.newInstance(vertx));
    bind(MysqlClient.class).toProvider(() -> SharedDataUtils.get(vertx, MysqlClientImpl.class));
    bind(Redis.class).toProvider(() -> SharedDataUtils.get(vertx, Redis.class));
    bind(WebClient.class).toProvider(() -> SharedDataUtils.get(vertx, WebClient.class));
    bind(Registry.class).toProvider(() -> SharedDataUtils.get(vertx, Registry.class));
    bind(TenantCache.class).toProvider(() -> SharedDataUtils.get(vertx, TenantCache.class));
  }
}
