package com.dreamsportslabs.guardian;

import com.dreamsportslabs.guardian.injection.GuiceInjector;
import com.dreamsportslabs.guardian.injection.MainModule;
import com.dreamsportslabs.guardian.utils.SharedDataUtils;
import com.redis.testcontainers.RedisContainer;
import io.vertx.rxjava3.core.Vertx;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MySQLContainer;

@Slf4j
public class Setup implements ExtensionContext.Store.CloseableResource, BeforeAllCallback {
  private final Vertx vertx;
  private MySQLContainer<?> mysqlContainer;
  private RedisContainer redisContainer;

  public Setup() {
    initializeMysqlContainer();
    initializeRedisContainer();

    this.vertx = Vertx.vertx();
    startApplication();
  }

  private void initializeMysqlContainer() {
    String username = System.getenv("GUARDIAN_MYSQL_USER");
    String password = System.getenv("GUARDIAN_MYSQL_PASSWORD");
    String database = System.getenv("GUARDIAN_MYSQL_DATABASE");

    this.mysqlContainer =
        new MySQLContainer<>("mysql:8")
            .withDatabaseName(database)
            .withUsername(username)
            .withPassword(password);
    this.mysqlContainer.setPortBindings(List.of("3306:3306"));
    this.mysqlContainer.start();
  }

  private void initializeRedisContainer() {
    String port = System.getenv("GUARDIAN_REDIS_PORT");
    this.redisContainer = new RedisContainer("redis:6");
    this.redisContainer.setPortBindings(List.of(port + ":6379"));
    this.redisContainer.start();
  }

  private void startApplication() {
    GuiceInjector.initialize(List.of(new MainModule(this.vertx.getDelegate())));
    SharedDataUtils.put(this.vertx.getDelegate(), GuiceInjector.class);

    this.vertx.rxDeployVerticle("com.dreamsportslabs.guardian.verticle.MainVerticle").blockingGet();
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put("Setup", this);
  }

  @Override
  public void close() {
    this.mysqlContainer.stop();
    this.redisContainer.stop();
    this.vertx.close().blockingAwait();
  }
}
