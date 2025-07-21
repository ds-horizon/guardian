package com.dreamsportslabs.guardian.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.json.JsonObject;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
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
    conf.setMaximumPoolSize(10);
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

  public static void createUserFlowBlockWithImmediateExpiry(
      String tenantId, String userIdentifier, String flowName, String reason) {
    String sql =
        "INSERT INTO user_flow_block (tenant_id, user_identifier, flow_name, reason, unblocked_at, is_active) "
            + "VALUES (?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE "
            + "reason = VALUES(reason), "
            + "unblocked_at = VALUES(unblocked_at), "
            + "is_active = VALUES(is_active), "
            + "updated_at = CURRENT_TIMESTAMP";

    try (var connection = mysqlConnectionPool.getConnection();
        var statement = connection.prepareStatement(sql)) {

      long currentTimestamp = Instant.now().getEpochSecond();

      statement.setString(1, tenantId);
      statement.setString(2, userIdentifier);
      statement.setString(3, flowName);
      statement.setString(4, reason);
      statement.setLong(5, currentTimestamp);
      statement.setBoolean(6, true);

      statement.executeUpdate();
      log.info(
          "Created user flow block with immediate expiry for tenant: {}, user: {}, flow: {}",
          tenantId,
          userIdentifier,
          flowName);
    } catch (Exception e) {
      log.error("Error creating user flow block with immediate expiry: ", e);
      throw new RuntimeException("Error creating user flow block", e);
    }
  }

  public static List<String> getRevocationsFromRedis(String tenantId) {
    String revocationsKey = "revocations_" + tenantId;

    try (Jedis jedis = redisConnectionPool.getResource()) {

      List<String> revocations = jedis.zrange(revocationsKey, 0, -1);
      return new ArrayList<>(revocations);
    } catch (Exception e) {
      log.error("Error getting revocations from Redis: ", e);
      throw new RuntimeException("Error getting revocations from Redis", e);
    }
  }

  public static boolean isRefreshTokenRevoked(String refreshToken, String tenantId) {
    String rftId = org.apache.commons.codec.digest.DigestUtils.md5Hex(refreshToken).toUpperCase();
    List<String> revocations = getRevocationsFromRedis(tenantId);
    return revocations.contains(rftId);
  }
}
