package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.json.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Slf4j
public class DbUtils {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static HikariDataSource mysqlConnectionPool;
  private static JedisPool redisConnectionPool;
  private static final String INSERT_REFRESH_TOKEN =
      "INSERT INTO refresh_tokens (tenant_id, user_id, refresh_token, refresh_token_exp, source, device_name, location, ip) VALUES (?, ?, ?, ?, ?, ?, ?, INET6_ATON(?))";

  private static final String INSERT_REFRESH_TOKEN_ALL_VALUES =
      "INSERT INTO refresh_tokens (tenant_id, client_id, user_id, refresh_token, refresh_token_exp, scope, device_name, ip, source, location, auth_method) VALUES (?, ?, ?, ?, ?, ?, ?, INET6_ATON(?), ?, ?, ?)";

  private static final String INSERT_USER_CONSENT =
      "INSERT INTO consent (tenant_id, client_id, user_id, scope, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";

  private static final String GET_SCOPE_BY_NAME =
      "SELECT name, display_name, description, claims, tenant_id, icon_url, is_oidc FROM scope WHERE tenant_id = ? AND name = ?";

  private static final String INSERT_OIDC_REFRESH_TOKEN =
      "INSERT INTO refresh_tokens (tenant_id, client_id, user_id, refresh_token, refresh_token_exp, scope, is_active, device_name, ip) VALUES (?, ?, ?, ?, ?, ?, ?, ?, INET6_ATON(?))";

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

  public static JsonObject getScope(String tenantId, String name) {
    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(GET_SCOPE_BY_NAME)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, name);
      var resultSet = stmt.executeQuery();
      if (resultSet.next()) {
        JsonObject scope = new JsonObject();
        scope.put("name", resultSet.getString("name"));
        scope.put("display_name", resultSet.getString("display_name"));
        scope.put("description", resultSet.getString("description"));
        scope.put(
            "claims",
            resultSet.getString("claims").equals("[]") ? null : resultSet.getString("claims"));
        scope.put("tenantId", resultSet.getString("tenant_id"));
        scope.put("icon_url", resultSet.getString("icon_url"));
        scope.put("is_oidc", resultSet.getBoolean("is_oidc"));
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
    } catch (Exception e) {
      log.error("Error while inserting refresh token", e);
      return null;
    }

    return refreshToken;
  }

  public static String insertOidcRefreshToken(
      String tenantId,
      String clientId,
      String userId,
      long exp,
      String scope,
      String deviceName,
      String ip,
      String source,
      String location,
      String authMethod) {
    String refreshToken = RandomStringUtils.randomAlphanumeric(32);

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(INSERT_REFRESH_TOKEN_ALL_VALUES)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, clientId);
      stmt.setString(3, userId);
      stmt.setString(4, refreshToken);
      stmt.setLong(5, Instant.now().getEpochSecond() + exp);
      stmt.setString(6, scope);
      stmt.setString(7, deviceName);
      stmt.setString(8, ip);
      stmt.setString(9, source);
      stmt.setString(10, location);
      stmt.setString(11, authMethod);

      stmt.executeUpdate();
    } catch (Exception e) {
      log.error("Error while inserting OIDC refresh token", e);
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

  public static boolean isSsoTokenRevoked(String refreshToken, String tenantId) {
    String checkSsoToken =
        "SELECT is_active FROM sso_token WHERE tenant_id = ? AND refresh_token = ?";
    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(checkSsoToken)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, refreshToken);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return !rs.getBoolean("is_active");
      }
    } catch (Exception e) {
      log.error("Error checking SSO token status", e);
    }
    return false;
  }

  public static long getStateTtl(String state, String tenantId) {
    String key = "STATE" + "_" + tenantId + "_" + state;

    try (Jedis jedis = redisConnectionPool.getResource()) {
      return jedis.ttl(key);
    } catch (Exception e) {
      log.error("Error getting TTL for Redis key: {}", key, e);
      throw new RuntimeException("Error getting TTL for Redis key: " + key, e);
    }
  }

  public static void cleanUpScopes(String tenantId) {
    String deleteScopes = "DELETE FROM scope WHERE tenant_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt1 = conn.prepareStatement(deleteScopes)) {

      stmt1.setString(1, tenantId);
      stmt1.executeUpdate();
    } catch (Exception e) {
      log.error("Error while cleaning up clients", e);
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

  public static JsonObject getClient(String tenantId, String clientId) {
    String query = "SELECT * FROM client WHERE tenant_id = ? AND client_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, clientId);

      var rs = stmt.executeQuery();
      rs.next();
      JsonObject response = new JsonObject();
      response.put("client_name", rs.getString("client_name"));
      response.put("client_uri", rs.getString("client_uri"));
      response.put("client_id", rs.getString("client_id"));
      response.put("client_secret", rs.getString("client_secret"));
      response.put("redirect_uris", rs.getString("redirect_uris"));
      response.put("contacts", rs.getString("contacts"));
      response.put("grant_types", rs.getString("grant_types"));
      response.put("response_types", rs.getString("response_types"));
      response.put("logo_uri", rs.getString("logo_uri"));
      response.put("policy_uri", rs.getString("policy_uri"));
      response.put("client_type", rs.getString("client_type"));
      response.put("is_default", rs.getBoolean("is_default"));
      stmt.close();
      return response;
    } catch (Exception e) {
      log.error("Error while checking client existence", e);
    }
    return null;
  }

  // Scope management utilities
  public static void cleanupScopes(String tenantId) {
    String deleteScopes = "DELETE FROM scope WHERE tenant_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt1 = conn.prepareStatement(deleteScopes)) {

      stmt1.setString(1, tenantId);
      stmt1.executeUpdate();
    } catch (Exception e) {
      log.error("Error while cleaning up scopes", e);
    }
  }

  // Scope management utilities
  public static void addScope(String tenantId, String scope) {
    String addScopeQuery = "INSERT INTO scope (tenant_id, name, claims) VALUES (?, ?, ?)";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt1 = conn.prepareStatement(addScopeQuery)) {

      stmt1.setString(1, tenantId);
      stmt1.setString(2, scope);
      stmt1.setString(3, "[]");
      stmt1.executeUpdate();
    } catch (Exception e) {
      log.error("Error while cleaning up scopes", e);
    }
  }

  public static String insertSsoToken(
      String tenantId,
      String userId,
      String clientId,
      long expiryOffset,
      List<String> authMethods) {
    String ssoToken = RandomStringUtils.randomAlphanumeric(15);
    String refreshToken = RandomStringUtils.randomAlphanumeric(32);
    long expiry = getCurrentTimeInSeconds() + expiryOffset;

    String insertSsoToken =
        "INSERT INTO sso_token (tenant_id, client_id_issues_to, user_id, refresh_token, sso_token, expiry, auth_methods, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(insertSsoToken)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, clientId);
      stmt.setString(3, userId);
      stmt.setString(4, refreshToken);
      stmt.setString(5, ssoToken);
      stmt.setLong(6, expiry);
      ArrayNode authMethodsArray = objectMapper.createArrayNode();
      authMethods.forEach(authMethodsArray::add);
      stmt.setString(7, objectMapper.writeValueAsString(authMethodsArray));
      stmt.executeUpdate();
    } catch (Exception e) {
      log.error("Error inserting SSO token", e);
    }
    return ssoToken;
  }

  public static String insertSsoTokenWithRefreshToken(
      String tenantId, String clientId, String userId, String refreshToken, long expiryOffset) {
    String ssoToken = RandomStringUtils.randomAlphanumeric(15);
    long expiry = getCurrentTimeInSeconds() + expiryOffset;

    String insertSsoToken =
        "INSERT INTO sso_token (tenant_id, client_id_issues_to, user_id, refresh_token, sso_token, expiry, auth_methods, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(insertSsoToken)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, clientId);
      stmt.setString(3, userId);
      stmt.setString(4, refreshToken);
      stmt.setString(5, ssoToken);
      stmt.setLong(6, expiry);
      stmt.setString(7, "[\"PASSWORD\"]");
      stmt.executeUpdate();
    } catch (Exception e) {
      log.error("Error inserting SSO token with refresh token", e);
    }
    return ssoToken;
  }

  public static String insertExpiredSsoToken(String tenantId, String userId, String clientId) {
    return insertSsoToken(tenantId, userId, clientId, -1800L, Arrays.asList("ONE_TIME_PASSWORD"));
  }

  public static String addFirstPartyClient(String tenantId) {
    String clientId = RandomStringUtils.randomAlphanumeric(10);
    String addClient =
        "INSERT INTO client (tenant_id, client_id, client_name, client_secret, client_uri, contacts, grant_types, logo_uri, policy_uri, redirect_uris, response_types, client_type, is_default, mfa_policy, allowed_mfa_methods) VALUES (?,?,?,'s3cr3tKey123','https://clientapp.example.com',JSON_ARRAY('admin@example.com','support@example.com'),JSON_ARRAY('authorization_code','refresh_token','client_credentials'),'https://clientapp.example.com/logo.png','https://clientapp.example.com/policy',JSON_ARRAY('https://clientapp.example.com/callback','https://clientapp.example.com/redirect'),JSON_ARRAY('code'),'first_party',TRUE,'mandatory',JSON_ARRAY('password','pin','sms-otp','email-otp'));";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt1 = conn.prepareStatement(addClient)) {

      stmt1.setString(1, tenantId);
      stmt1.setString(2, clientId);
      stmt1.setString(3, clientId);
      stmt1.executeUpdate();
    } catch (Exception e) {
      log.error("Error while cleaning up scopes", e);
    }
    return clientId;
  }

  public static String addThirdPartyClient(String tenantId) {
    String clientId = RandomStringUtils.randomAlphanumeric(10);
    String addClient =
        "INSERT INTO client (tenant_id, client_id, client_name, client_secret, client_uri, contacts, grant_types, logo_uri, policy_uri, redirect_uris, response_types, client_type, is_default) VALUES (?,?,?,'s3cr3tKey123','https://clientapp.example.com',JSON_ARRAY('admin@example.com','support@example.com'),JSON_ARRAY('authorization_code','refresh_token','client_credentials'),'https://clientapp.example.com/logo.png','https://clientapp.example.com/policy',JSON_ARRAY('https://clientapp.example.com/callback','https://clientapp.example.com/redirect'),JSON_ARRAY('code'),'third_party',FALSE);";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt1 = conn.prepareStatement(addClient)) {

      stmt1.setString(1, tenantId);
      stmt1.setString(2, clientId);
      stmt1.setString(3, clientId);
      stmt1.executeUpdate();
    } catch (Exception e) {
      log.error("Error while cleaning up scopes", e);
    }
    return clientId;
  }

  // Client-Scope relationship utilities
  public static void cleanupClientScopes(String tenantId) {
    String deleteQuery = "DELETE FROM client_scope WHERE tenant_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
      stmt.setString(1, tenantId);
      stmt.executeUpdate();
    } catch (Exception e) {
      log.error("Error while cleaning up client scopes", e);
    }
  }

  public static void addDefaultClientScopes(String tenantId, String clientId, String scope) {
    String addClientScope =
        "insert into client_scope (tenant_id, client_id, scope, is_default) values (?, ?, ?, TRUE);";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt1 = conn.prepareStatement(addClientScope)) {

      stmt1.setString(1, tenantId);
      stmt1.setString(2, clientId);
      stmt1.setString(3, scope);
      stmt1.executeUpdate();
    } catch (Exception e) {
      log.error("Error while adding client scopes", e);
    }
  }

  public static void updateClientAllowedMfaMethods(
      String tenantId, String clientId, List<String> allowedMfaMethods) {
    String updateQuery =
        "UPDATE client SET allowed_mfa_methods = ? WHERE tenant_id = ? AND client_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
      if (allowedMfaMethods == null || allowedMfaMethods.isEmpty()) {
        stmt.setString(1, null);
      } else {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (String method : allowedMfaMethods) {
          arrayNode.add(method);
        }
        stmt.setString(1, arrayNode.toString());
      }
      stmt.setString(2, tenantId);
      stmt.setString(3, clientId);
      stmt.executeUpdate();
    } catch (Exception e) {
      log.error("Error while updating client allowed MFA methods", e);
    }
  }

  public static void updateClientMfaPolicy(String tenantId, String clientId, String mfaPolicy) {
    String updateQuery = "UPDATE client SET mfa_policy = ? WHERE tenant_id = ? AND client_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
      stmt.setString(1, mfaPolicy);
      stmt.setString(2, tenantId);
      stmt.setString(3, clientId);
      stmt.executeUpdate();
    } catch (Exception e) {
      log.error("Error while updating client MFA policy", e);
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
    } catch (Exception e) {
      log.error("Error while checking client scope existence", e);
    }
    return false;
  }

  public static boolean authorizeSessionExists(String tenantId, String loginChallenge) {
    int maxRetries = 3;
    int retryCount = 0;

    while (retryCount < maxRetries) {
      try (Jedis jedis = redisConnectionPool.getResource()) {
        String key = "AUTH_SESSION_" + tenantId + "_" + loginChallenge;
        boolean keyExists = jedis.exists(key);

        if (!keyExists) {
          return false;
        }

        retryCount++;
        if (retryCount < maxRetries) {
          log.info("Key still exists, retrying (attempt {}/{}): {}", retryCount, maxRetries, key);
        }
      } catch (Exception e) {
        log.error("Error while checking authorize session exists", e);
        return false;
      }
    }

    // After all retries, key still exists
    return true;
  }

  public static JsonObject getAuthorizeSession(String tenantId, String challenge) {
    try (Jedis jedis = redisConnectionPool.getResource()) {
      String cacheKey = "AUTH_SESSION_" + tenantId + "_" + challenge;
      String sessionData = jedis.get(cacheKey);
      if (sessionData != null) {
        return new JsonObject(sessionData);
      } else {
        return null;
      }
    } catch (Exception e) {
      log.error("Error while fetching authorize session", e);
      return null;
    }
  }

  public static JsonObject getOidcCode(String tenantId, String code) {
    try (Jedis jedis = redisConnectionPool.getResource()) {
      String cacheKey = "AUTH_CODE_" + tenantId + "_" + code;
      String codeData = jedis.get(cacheKey);
      if (codeData != null) {
        return new JsonObject(codeData);
      } else {
        return null;
      }
    } catch (Exception e) {
      log.error("Error while fetching OIDC code", e);
      return null;
    }
  }

  public static void cleanupRedis() {
    try (Jedis jedis = redisConnectionPool.getResource()) {
      jedis.flushAll();
    } catch (Exception e) {
      log.error("Error while cleaning up Redis", e);
    }
  }

  public static void insertUserConsent(
      String tenantId, String clientId, String userId, List<String> scopes) {
    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(INSERT_USER_CONSENT)) {

      for (String scope : scopes) {
        stmt.setString(1, tenantId);
        stmt.setString(2, clientId);
        stmt.setString(3, userId);
        stmt.setString(4, scope);
        stmt.executeUpdate();
      }
    } catch (Exception e) {
      log.error("Error while inserting user consent", e);
    }
  }

  public static void expireAuthorizeSession(String tenantId, String challenge) {
    try (Jedis jedis = redisConnectionPool.getResource()) {
      String key = "AUTH_SESSION_" + tenantId + "_" + challenge;
      // Set a very short TTL (1 second) to expire the session quickly
      jedis.expire(key, 0);
    } catch (Exception e) {
      log.error("Error while expiring authorize session", e);
    }
  }

  public static String insertOidcRefreshToken(
      String tenantId,
      String clientId,
      String userId,
      long exp,
      List<String> scopes,
      Boolean isActive,
      String deviceName,
      String ip) {
    String refreshToken = RandomStringUtils.randomAlphanumeric(32);

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(INSERT_OIDC_REFRESH_TOKEN)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, clientId);
      stmt.setString(3, userId);
      stmt.setString(4, refreshToken);
      stmt.setLong(5, Instant.now().getEpochSecond() + exp);
      ArrayNode scopesArray = objectMapper.createArrayNode();
      scopes.forEach(scopesArray::add);
      stmt.setString(6, objectMapper.writeValueAsString(scopesArray));
      stmt.setBoolean(7, isActive);
      stmt.setString(8, deviceName);
      stmt.setString(9, ip);

      stmt.executeUpdate();
    } catch (Exception e) {
      log.error("Error while inserting refresh token", e);
      return null;
    }

    return refreshToken;
  }

  public static void cleanupOidcRefreshTokens(String tenantId) {
    String deleteQuery = "DELETE FROM refresh_tokens WHERE tenant_id = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
      stmt.setString(1, tenantId);
      stmt.executeUpdate();
    } catch (Exception e) {
      log.error("Error while cleaning up OIDC refresh tokens", e);
    }
  }

  public static boolean isOidcRefreshTokenActive(
      String tenantId, String clientId, String refreshToken) {
    String deleteQuery =
        "SELECT is_active FROM refresh_tokens WHERE tenant_id = ? and client_id = ? and refresh_token = ?";

    try (Connection conn = mysqlConnectionPool.getConnection();
        PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
      stmt.setString(1, tenantId);
      stmt.setString(2, clientId);
      stmt.setString(3, refreshToken);
      var rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getBoolean(1);
      }
    } catch (Exception e) {
      log.error("Error while checking OIDC refresh token status", e);
    }
    return false;
  }
}
