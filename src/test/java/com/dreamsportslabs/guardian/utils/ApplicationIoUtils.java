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
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ApplicationIoUtils {

  private static Response execute(
      Map<String, Object> body,
      Map<String, String> headers,
      Map<String, String> queryParams,
      Function<RequestSpecification, Response> fn) {
    RequestSpecification spec = given();
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

  public static Response getJwks(String tenantId) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenantId);

    return execute(null, headers, new HashMap<>(), spec -> spec.get("/v1/certs"));
  }

  public static Response generateRsaKey(Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, "application/json");

    return execute(body, headers, new HashMap<>(), spec -> spec.post("/v1/keys/generate"));
  }
}
