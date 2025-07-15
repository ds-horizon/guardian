package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_ADDITIONAL_INFO;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONTACTS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_FLOW;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_META_INFO;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_PASSWORD;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERNAME;
import static com.dreamsportslabs.guardian.Constants.HEADER_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.QUERY_PARAM_NAME;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

public class ApplicationIoUtils {

  private static Response execute(
      Map<String, Object> body,
      Map<String, String> headers,
      Map<String, String> queryParams,
      Function<RequestSpecification, Response> fn) {
    RequestSpecification spec = given().redirects().follow(false);
    if (body != null) {
      spec.header(CONTENT_TYPE, "application/json").and().body(body);
    }

    if (headers != null) {
      spec.headers(headers);
    }

    if (queryParams != null) {
      spec.queryParams(queryParams);
    }

    return fn.apply(spec);
  }

  public static Response signIn(
      String tenantId, String username, String password, String responseType) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_USERNAME, username);
    body.put(BODY_PARAM_PASSWORD, password);
    body.put(BODY_PARAM_RESPONSE_TYPE, responseType);

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/signin"));
  }

  public static Response signUp(
      String tenantId, String username, String password, String responseType) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_USERNAME, username);
    body.put(BODY_PARAM_PASSWORD, password);
    body.put(BODY_PARAM_RESPONSE_TYPE, responseType);

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/signup"));
  }

  public static Response refreshToken(String tenantId, String refreshToken) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_REFRESH_TOKEN, refreshToken);

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/refreshToken"));
  }

  public static Response passwordlessInit(
      String tenantId,
      String flow,
      String responseType,
      List<Map<String, Object>> contacts,
      Map<String, Object> metaInfo,
      Map<String, Object> additionalInfo) {
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_FLOW, flow);
    body.put(BODY_PARAM_RESPONSE_TYPE, responseType);
    body.put(BODY_PARAM_CONTACTS, contacts);
    body.put(BODY_PARAM_META_INFO, metaInfo);
    body.put(BODY_PARAM_ADDITIONAL_INFO, additionalInfo);

    return passwordlessInit(tenantId, body);
  }

  public static Response passwordlessInit(String tenantId, String state) {
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_STATE, state);

    return passwordlessInit(tenantId, body);
  }

  public static Response passwordlessInit(String tenantId, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);
    headers.put(CONTENT_TYPE, "application/json");

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/passwordless/init"));
  }

  // Scope Config API methods
  public static Response createScope(String tenantId, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put("tenant-id", tenantId);

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/scopes"));
  }

  public static Response listScopes(String tenantId, Map<String, String> queryParams) {
    return listScopes(tenantId, queryParams, null);
  }

  public static Response listScopesByNames(String tenantId, List<String> scopeNames) {
    return listScopes(tenantId, null, scopeNames);
  }

  public static Response listScopes(
      String tenantId, Map<String, String> queryParams, List<String> scopeNames) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    RequestSpecification spec = given().headers(headers);

    // Add regular query parameters
    if (queryParams != null) {
      queryParams.forEach(spec::queryParam);
    }

    // Add multiple scope names if provided (each as separate query param with same key)
    if (scopeNames != null && !scopeNames.isEmpty()) {
      for (String scopeName : scopeNames) {
        spec.queryParam(QUERY_PARAM_NAME, scopeName);
      }
    }

    return spec.get("/scopes");
  }

  public static Response deleteScope(String tenantId, String scopeName) {
    Map<String, String> headers = new HashMap<>();
    headers.put("tenant-id", tenantId);
    headers.put(CONTENT_TYPE, "application/json");

    if (StringUtils.isBlank(scopeName)) {
      return execute(null, headers, new HashMap<>(), spec -> spec.delete("/scopes/"));
    }

    return execute(null, headers, new HashMap<>(), spec -> spec.delete("/scopes/" + scopeName));
  }

  public static Response updateScope(String tenantId, String scopeName, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put("tenant-id", tenantId);

    return execute(body, headers, new HashMap<>(), spec -> spec.patch("/scopes/" + scopeName));
  }

  public static Response sendOtp(
      String tenantId, Map<String, Object> body, Map<String, String> headers) {
    headers.put(HEADER_TENANT_ID, tenantId);
    headers.put(CONTENT_TYPE, "application/json");
    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/otp/send"));
  }

  public static Response sendOtp(String tenantId, Map<String, Object> body) {
    return sendOtp(tenantId, body, new HashMap<>());
  }

  public static Response verifyOtp(String tenantId, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);
    headers.put(CONTENT_TYPE, "application/json");
    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/otp/verify"));
  }

  public static Response authFb(
      String tenantId, String accessToken, String flow, String responseType) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    Map<String, Object> body = new HashMap<>();
    body.put("accessToken", accessToken);
    body.put(BODY_PARAM_FLOW, flow);
    body.put(BODY_PARAM_RESPONSE_TYPE, responseType);

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/auth/fb"));
  }

  public static Response authGoogle(
      String tenantId, String idToken, String flow, String responseType) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    Map<String, Object> body = new HashMap<>();
    body.put("idToken", idToken);
    body.put(BODY_PARAM_FLOW, flow);
    body.put(BODY_PARAM_RESPONSE_TYPE, responseType);

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/auth/google"));
  }

  public static Response getJwks(String tenantId) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    return execute(null, headers, new HashMap<>(), spec -> spec.get("/v1/certs"));
  }

  // Client API methods
  public static Response createClient(String tenantId, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/admin/client"));
  }

  public static Response getClient(String tenantId, String clientId) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    return execute(
        null, headers, new HashMap<>(), spec -> spec.get("/v1/admin/client/" + clientId));
  }

  public static Response listClients(String tenantId, Map<String, String> queryParams) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    return execute(null, headers, queryParams, spec -> spec.get("/v1/admin/client"));
  }

  public static Response updateClient(String tenantId, String clientId, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    return execute(
        body, headers, new HashMap<>(), spec -> spec.patch("/v1/admin/client/" + clientId));
  }

  public static Response deleteClient(String tenantId, String clientId) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    return execute(
        null, headers, new HashMap<>(), spec -> spec.delete("/v1/admin/client/" + clientId));
  }

  public static Response regenerateClientSecret(String tenantId, String clientId) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    return execute(
        null,
        headers,
        new HashMap<>(),
        spec -> spec.post("/v1/admin/client/" + clientId + "/regenerate-secret"));
  }

  // Client Scope API methods
  public static Response createClientScope(
      String tenantId, String clientId, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    return execute(
        body,
        headers,
        new HashMap<>(),
        spec -> spec.post("/v1/admin/client/" + clientId + "/scope"));
  }

  public static Response getClientScopes(String tenantId, String clientId) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    return execute(
        null,
        headers,
        new HashMap<>(),
        spec -> spec.get("/v1/admin/client/" + clientId + "/scope"));
  }

  public static Response deleteClientScope(String tenantId, String clientId, String scope) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("scope", scope);

    return execute(
        null, headers, queryParams, spec -> spec.delete("/v1/admin/client/" + clientId + "/scope"));
  }

  public static Response getOidcDiscovery(
      Map<String, String> headers, Map<String, String> queryParams) {
    return execute(
        null, headers, queryParams, spec -> spec.get("/.well-known/openid-configuration"));
  }

  public static Response generateRsaKey(Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, "application/json");

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/keys/generate"));
  }

  public static Response blockUserFlows(String tenantId, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);
    headers.put(CONTENT_TYPE, "application/json");

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/user/flow/block"));
  }

  public static Response unblockUserFlows(String tenantId, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);
    headers.put(CONTENT_TYPE, "application/json");

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/user/flow/unblock"));
  }

  public static Response getBlockedFlows(String tenantId, String userIdentifier) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("userIdentifier", userIdentifier);

    return execute(null, headers, queryParams, spec -> spec.get("/v1/user/flow/blocked"));
  }

  public static Response adminLogout(String tenantId, String authHeader, String userId) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);
    if (authHeader != null) {
      headers.put("Authorization", authHeader);
    }
    headers.put(CONTENT_TYPE, "application/json");

    Map<String, Object> body = new HashMap<>();
    if (userId != null) {
      body.put("userId", userId);
    }

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/admin/logout"));
  }

  public static Response authorize(String tenantId, Map<String, String> queryParams) {
    Map<String, String> headers = new HashMap<>();
    if (tenantId != null) {
      headers.put(HEADER_TENANT_ID, tenantId);
    }

    return execute(null, headers, queryParams, spec -> spec.get("/authorize"));
  }
}
