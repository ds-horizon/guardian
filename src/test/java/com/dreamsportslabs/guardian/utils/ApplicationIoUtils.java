package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_PASSWORD;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERNAME;
import static com.dreamsportslabs.guardian.Constants.HEADER_TENANT_ID;
import static io.restassured.RestAssured.given;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
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
      spec.header("Content-type", "application/json").and().body(body);
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
}
