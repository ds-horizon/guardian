package com.dreamsportslabs.guardian.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.json.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import redis.clients.jedis.Jedis;
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
    conf.setMaximumPoolSize(45);
    conf.setConnectionTimeout(1000);

    mysqlConnectionPool = new HikariDataSource(conf);
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

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(INSERT_REFRESH_TOKEN)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, userId);
      stmt.setString(3, refreshToken);
      stmt.setLong(4, Instant.now().getEpochSecond() + exp);
      stmt.setString(5, source);
      stmt.setString(6, deviceName);
      stmt.setString(7, location);
      stmt.setString(8, ip);

      stmt.executeUpdate();
      stmt.close();
    } catch (Exception e) {
      log.error("Error while inserting refresh token", e);
      return null;
    }

    return refreshToken;
  }

  public static void createState(
      String tenantId,
      String state,
      int ttl,
      String otp,
      boolean isOtpMocked,
      int tries,
      int resends,
      long resendAfter,
      int resendInterval,
      int maxTries,
      int maxResends,
      Map<String, Object> user,
      List<Map<String, Object>> contacts,
      String flow,
      String responseType,
      Map<String, Object> metaInfo,
      Map<String, Object> additionalInfo,
      long createdAtEpoch,
      long expiry) {
    String key = "STATE" + "_" + tenantId + "_" + state;
    Map<String, String> headers = Map.of("tenant-id", tenantId);
    JsonObject value =
        new JsonObject()
            .put("state", state)
            .put("otp", otp)
            .put("isOtpMocked", isOtpMocked)
            .put("tries", tries)
            .put("resends", resends)
            .put("resendAfter", resendAfter)
            .put("resendInterval", resendInterval)
            .put("maxTries", maxTries)
            .put("maxResends", maxResends)
            .put("user", user)
            .put("headers", headers)
            .put("contacts", contacts)
            .put("flow", flow)
            .put("responseType", responseType)
            .put("metaInfo", metaInfo)
            .put("createdAtEpoch", createdAtEpoch)
            .put("expiry", expiry)
            .put("additionalInfo", additionalInfo);

    try (Jedis jedis = redisConnectionPool.getResource()) {
      jedis.setex(key, ttl, value.toString());
    } catch (Exception e) {
      log.error("Error setting key in Redis: ", e);
    }
  }

  public static JsonObject getState(String state, String tenantId) {
    String key = "STATE" + "_" + tenantId + "_" + state;

    try (Jedis jedis = redisConnectionPool.getResource()) {
      String jsonValue = jedis.get(key);
      if (jsonValue == null) {
        return null;
      }

      return new JsonObject(jsonValue);
    } catch (Exception e) {
      throw new RuntimeException("Error while fetching or parsing Redis key: " + key, e);
    }
  }

  // Client management utilities
  public static void cleanupClients(String tenantId) {
    String deleteClientScopes = "DELETE FROM client_scope WHERE tenant_id = ?";
    String deleteClients = "DELETE FROM client WHERE tenant_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt1 = conn.prepareStatement(deleteClientScopes);
        PreparedStatement stmt2 = conn.prepareStatement(deleteClients)) {

      stmt1.setString(1, tenantId);
      stmt1.executeUpdate();
      stmt1.close();

      stmt2.setString(1, tenantId);
      stmt2.executeUpdate();
      stmt2.close();
    } catch (Exception e) {
      log.error("Error while cleaning up clients", e);
    }
  }

  public static boolean clientExists(String tenantId, String clientId) {
    String query = "SELECT COUNT(*) FROM client WHERE tenant_id = ? AND client_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, clientId);

      var rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
      stmt.close();
    } catch (Exception e) {
      log.error("Error while checking client existence", e);
    }
    return false;
  }

  // Scope management utilities
  public static void cleanupScopes(String tenantId) {
    String deleteScopes = "DELETE FROM scopes WHERE tenant_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt1 = conn.prepareStatement(deleteScopes)) {

      stmt1.setString(1, tenantId);
      stmt1.executeUpdate();
      stmt1.close();
    } catch (Exception e) {
      log.error("Error while cleaning up scopes", e);
    }
  }

  // Scope management utilities
  public static void addScope(String tenantId, String scope) {
    String addScopeQuery = "INSERT INTO scope (tenant_id, scope) VALUES (?, ?)";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt1 = conn.prepareStatement(addScopeQuery)) {

      stmt1.setString(1, tenantId);
      stmt1.setString(2, scope);
      stmt1.executeUpdate();
      stmt1.close();
    } catch (Exception e) {
      log.error("Error while cleaning up scopes", e);
    }
  }

  // Client-Scope relationship utilities
  public static void cleanupClientScopes(String tenantId) {
    String deleteQuery = "DELETE FROM client_scope WHERE tenant_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
      stmt.setString(1, tenantId);
      stmt.executeUpdate();
      stmt.close();
    } catch (Exception e) {
      log.error("Error while cleaning up client scopes", e);
    }
  }

  public static boolean clientScopeExists(String tenantId, String clientId, String scope) {
    String query =
        "SELECT COUNT(*) FROM client_scope WHERE tenant_id = ? AND client_id = ? AND scope = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, clientId);
      stmt.setString(3, scope);

      var rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
      stmt.close();
    } catch (Exception e) {
      log.error("Error while checking client scope existence", e);
    }
    return false;
  }
}
