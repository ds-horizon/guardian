package com.dreamsportslabs.guardian.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.json.JsonObject;
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

  private static final String GET_SCOPE_BY_NAME =
      "SELECT name, display_name, description, claims, tenant_id, icon_url, is_oidc FROM scope WHERE tenant_id = ? AND name = ?";

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
    conf.setMaximumPoolSize(20);
    conf.setConnectionTimeout(1000);

    mysqlConnectionPool = new HikariDataSource(conf);
  }

  public static JsonObject getScope(String tenantId, String name) {
    try (PreparedStatement stmt =
        mysqlConnectionPool.getConnection().prepareStatement(GET_SCOPE_BY_NAME)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, name);
      var resultSet = stmt.executeQuery();
      if (resultSet.next()) {
        JsonObject scope = new JsonObject();
        scope.put("name", resultSet.getString("name"));
        scope.put("displayName", resultSet.getString("display_name"));
        scope.put("description", resultSet.getString("description"));
        scope.put("claims", resultSet.getString("claims"));
        scope.put("tenantId", resultSet.getString("tenant_id"));
        scope.put("iconUrl", resultSet.getString("icon_url"));
        scope.put("isOidc", resultSet.getBoolean("is_oidc"));
        return scope;
      } else {
        return null;
      }
    } catch (Exception e) {
      log.error("Error while fetching scopes", e);
      return null;
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

  public static void createContactOtpSendState(
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
      Map<String, Object> contact,
      long expiry) {
    String key = "STATE" + "_otp_only_" + tenantId + "_" + state;
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
            .put("headers", headers)
            .put("contact", contact)
            .put("expiry", expiry);

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

  public static JsonObject getContactState(String state, String tenantId) {
    String key = "STATE" + "_otp_only_" + tenantId + "_" + state;

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
}
