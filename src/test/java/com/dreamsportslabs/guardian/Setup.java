package com.dreamsportslabs.guardian;

import com.dreamsportslabs.guardian.injection.GuiceInjector;
import com.dreamsportslabs.guardian.injection.MainModule;
import com.dreamsportslabs.guardian.utils.DbUtils;
import com.dreamsportslabs.guardian.utils.SharedDataUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
import io.restassured.RestAssured;
import io.vertx.rxjava3.core.Vertx;
import java.lang.reflect.Field;
import java.util.List;
import liquibase.command.CommandScope;
import liquibase.command.core.ExecuteSqlCommandStep;
import liquibase.command.core.UpdateCommandStep;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.testcontainers.containers.MySQLContainer;

@Slf4j
public class Setup
    implements ExtensionContext.Store.CloseableResource,
        BeforeAllCallback,
        TestInstancePostProcessor {
  private final Vertx vertx;
  private MySQLContainer<?> mysqlContainer;
  private RedisContainer redisContainer;

  private WireMockServer wireMockServer;

  private final String mysqlUsername;
  private final String mysqlPassword;
  private final String mysqlDatabase;
  private final int redisPort;
  private final int port;

  public Setup() throws Exception {
    this.mysqlUsername = System.getenv().getOrDefault("GUARDIAN_MYSQL_USER", "root");
    this.mysqlPassword = System.getenv().getOrDefault("GUARDIAN_MYSQL_PASSWORD", "root");
    this.mysqlDatabase = System.getenv().getOrDefault("GUARDIAN_MYSQL_DATABASE", "guardian");

    this.redisPort = Integer.parseInt(System.getenv().getOrDefault("GUARDIAN_REDIS_PORT", "6379"));

    this.port = Integer.parseInt(System.getenv().getOrDefault("GUARDIAN_PORT", "8080"));

    initializeMysqlContainer();
    initializeRedisContainer();
    initializeRestAssuredConfig();
    initializeWiremockServer();

    DbUtils.initializeMysqlConnectionPool(
        "localhost", "3306", mysqlUsername, mysqlPassword, mysqlDatabase);
    DbUtils.initializeRedisConnectionPool("localhost", redisPort);

    // Run migrations to create tables in db
    migrations();
    // Add tenant related seed data for testing
    seedData();

    this.vertx = Vertx.vertx();
    startApplication();
  }

  private void initializeWiremockServer() {
    this.wireMockServer = new WireMockServer(9090);
    this.wireMockServer.start();
  }

  private void initializeRestAssuredConfig() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  private void initializeMysqlContainer() {
    this.mysqlContainer =
        new MySQLContainer<>("mysql:8")
            .withDatabaseName(this.mysqlDatabase)
            .withUsername(this.mysqlUsername)
            .withPassword(this.mysqlPassword);
    this.mysqlContainer.setPortBindings(List.of("3306:3306"));
    this.mysqlContainer.start();
  }

  private void initializeRedisContainer() {
    this.redisContainer = new RedisContainer("redis:6");
    this.redisContainer.setPortBindings(List.of(this.redisPort + ":6379"));
    this.redisContainer.start();
  }

  private void migrations() throws Exception {
    new CommandScope(UpdateCommandStep.COMMAND_NAME)
        .addArgumentValue("changelogFile", "changelog.xml")
        .addArgumentValue("url", "jdbc:mysql://localhost:3306/" + this.mysqlDatabase)
        .addArgumentValue("username", this.mysqlUsername)
        .addArgumentValue("password", this.mysqlPassword)
        .execute();
  }

  private void seedData() throws Exception {
    new CommandScope(ExecuteSqlCommandStep.COMMAND_NAME)
        .addArgumentValue("url", "jdbc:mysql://localhost:3306/" + this.mysqlDatabase)
        .addArgumentValue("username", this.mysqlUsername)
        .addArgumentValue("password", this.mysqlPassword)
        .addArgumentValue("sqlFile", "src/test/resources/test.sql")
        .execute();
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
    this.wireMockServer.stop();
    this.vertx.close().blockingAwait();
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context)
      throws Exception {
    for (Field field : testInstance.getClass().getDeclaredFields()) {
      if (field.getType() == WireMockServer.class) {
        field.setAccessible(true);
        field.set(testInstance, this.wireMockServer);
      }
    }
  }
}
