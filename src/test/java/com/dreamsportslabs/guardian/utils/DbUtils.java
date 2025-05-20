package com.dreamsportslabs.guardian.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Slf4j
public class DbUtils {
  private static HikariDataSource mysqlConnectionPool;
  private static JedisPool redisConnectionPool;
  private static final String INSERT_REFRESH_TOKEN =
      "INSERT INTO refresh_tokens (tenant_id, user_id, refresh_token, refresh_token_exp, source, device_name, location, ip) VALUES (?, ?, ?, ?, ?, ?, ?, INET6_ATON(?))";

  public static void initializeRedisConnectionPool(String host, int port) {
    if (redisConnectionPool != null) {
      return;
    }

    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(10);
    poolConfig.setMaxIdle(5);
    poolConfig.setMinIdle(2);

    redisConnectionPool = new JedisPool(poolConfig, host, port);
  }

  public static void initializeMysqlConnectionPool(
      String host, String port, String username, String password, String database) {
    if (mysqlConnectionPool != null) {
      return;
    }

    HikariConfig conf = new HikariConfig();
    String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;
    conf.setJdbcUrl(jdbcUrl);
    conf.setUsername(username);
    conf.setPassword(password);
    conf.setMaximumPoolSize(10);
    conf.setConnectionTimeout(1000);

    mysqlConnectionPool = new HikariDataSource(conf);
  }

  public static void executeSqlFile(String filePath) throws IOException, SQLException {
    String rawSql = Files.readString(Paths.get(filePath));

    String[] statements = rawSql.split("(?m);\\s*");

    try (Connection conn = mysqlConnectionPool.getConnection();
        Statement stmt = conn.createStatement()) {

      for (String s : statements) {
        String trimmed = s.trim();
        if (!trimmed.isEmpty()) {
          stmt.execute(trimmed);
        }
      }
    }
  }

  public static String insertRefreshToken(
      String tenantId,
      String userId,
      long exp,
      String source,
      String deviceName,
      String location,
      String ip) {
    String refreshToken = RandomStringUtils.randomAlphanumeric(32);

    try (PreparedStatement stmt =
        mysqlConnectionPool.getConnection().prepareStatement(INSERT_REFRESH_TOKEN)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, userId);
      stmt.setString(3, refreshToken);
      stmt.setLong(4, Instant.now().getEpochSecond() + exp);
      stmt.setString(5, source);
      stmt.setString(6, deviceName);
      stmt.setString(7, location);
      stmt.setString(8, ip);

      stmt.executeUpdate();
    } catch (Exception e) {
      log.error("Error while inserting refresh token", e);
      return null;
    }

    return refreshToken;
  }
}
