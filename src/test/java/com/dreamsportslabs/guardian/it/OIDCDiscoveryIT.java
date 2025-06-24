package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.HEADER_TENANT_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.execute;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class OIDCDiscoveryIT {

  public static String tenant1 = "tenant1";
  public static String invalidTenant = "invalid-tenant";

  private Response sendGetRequestToOIDCDiscovery(
      Map<String, String> headers, Map<String, String> queryParams) {
    return execute(
        null, headers, queryParams, spec -> spec.get("/.well-known/openid-configuration"));
  }

  private Response sendPostRequestToOIDCDiscovery(
      Map<String, String> headers, Map<String, Object> body) {
    return execute(
        body, headers, new HashMap<>(), spec -> spec.post("/.well-known/openid-configuration"));
  }

  private Response sendPatchRequestToOIDCDiscovery(
      Map<String, String> headers, Map<String, String> queryParams, Map<String, Object> body) {
    return execute(
        body, headers, queryParams, spec -> spec.patch("/.well-known/openid-configuration"));
  }

  private Response sendPutRequestToOIDCDiscovery(
      Map<String, String> headers, Map<String, String> queryParams, Map<String, Object> body) {
    return execute(
        body, headers, queryParams, spec -> spec.put("/.well-known/openid-configuration"));
  }

  @Test
  @DisplayName("Should fetch OIDC configuration with valid tenant")
  public void testFetchOIDCConfigurationWithValidTenant() {

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenant1);
    Map<String, String> queryParams = new HashMap<>();

    Response response = sendGetRequestToOIDCDiscovery(headers, queryParams);

    response.then().statusCode(HttpStatus.SC_OK);

    String issuer = response.jsonPath().getString("issuer");
    String authEndpoint = response.jsonPath().getString("authorization_endpoint");
    String tokenEndpoint = response.jsonPath().getString("token_endpoint");
    String userinfoEndpoint = response.jsonPath().getString("userinfo_endpoint");
    String revocationEndpoint = response.jsonPath().getString("revocation_endpoint");
    String jwksUri = response.jsonPath().getString("jwks_uri");

    assertThat("issuer should not be empty", issuer, not(emptyString()));

    assertThat("authorization_endpoint should not be empty", authEndpoint, not(emptyString()));

    assertThat("token_endpoint should not be empty", tokenEndpoint, not(emptyString()));

    assertThat("userinfo_endpoint should not be empty", userinfoEndpoint, not(emptyString()));

    assertThat("revocation_endpoint should not be empty", revocationEndpoint, not(emptyString()));

    assertThat("jwks_uri should not be empty", jwksUri, not(emptyString()));

    List<String> responseTypesSupported = response.jsonPath().getList("response_types_supported");
    List<String> grantTypesSupported = response.jsonPath().getList("grant_types_supported");

    assertThat(
        "response_types_supported should not be null", responseTypesSupported, notNullValue());
    assertThat(
        "response_types_supported should not be empty",
        responseTypesSupported.isEmpty(),
        is(false));

    assertThat("grant_types_supported should not be null", grantTypesSupported, notNullValue());
    assertThat(
        "grant_types_supported should not be empty", grantTypesSupported.isEmpty(), is(false));

    List<String> subjectTypesSupported = response.jsonPath().getList("subject_types_supported");
    assertThat(
        "subject_types_supported array size is not 1", subjectTypesSupported.size(), equalTo(1));
    assertThat(
        "subject_types_supported array does not contain public",
        subjectTypesSupported.get(0),
        equalTo("public"));

    List<String> idTokenSigningAlgSupported =
        response.jsonPath().getList("id_token_signing_alg_values_supported");
    assertThat(
        "id_token_signing_alg_values_supported array size is not 1",
        idTokenSigningAlgSupported.size(),
        equalTo(1));
    assertThat(
        "id_token_signing_alg_values_supported array does not contain RS256",
        idTokenSigningAlgSupported.get(0),
        equalTo("RS256"));

    List<String> tokenEndpointAuthMethods =
        response.jsonPath().getList("token_endpoint_auth_methods_supported");
    assertThat(
        "token_endpoint_auth_methods_supported array size is not 2",
        tokenEndpointAuthMethods.size(),
        equalTo(2));
    assertThat(
        "token_endpoint_auth_methods_supported array does not contain client_secret_basic",
        tokenEndpointAuthMethods.contains("client_secret_basic"),
        is(true));
    assertThat(
        "token_endpoint_auth_methods_supported array does not contain client_secret_post",
        tokenEndpointAuthMethods.contains("client_secret_post"),
        is(true));

    List<String> userinfoSigningAlgSupported =
        response.jsonPath().getList("userinfo_signing_alg_values_supported");
    assertThat(
        "userinfo_signing_alg_values_supported array size is not 1",
        userinfoSigningAlgSupported.size(),
        equalTo(1));
    assertThat(
        "userinfo_signing_alg_values_supported array does not contain RS256",
        userinfoSigningAlgSupported.get(0),
        equalTo("RS256"));

    List<String> scopesSupported = response.jsonPath().getList("scopes_supported");
    List<String> claimsSupported = response.jsonPath().getList("claims_supported");

    assertThat("scopes_supported should not be null", scopesSupported, notNullValue());
    assertThat("claims_supported should not be null", claimsSupported, notNullValue());
    assertThat("scopes_supported should not be empty", scopesSupported.isEmpty(), is(false));
    assertThat("claims_supported should not be empty", claimsSupported.isEmpty(), is(false));

    assertThat(
        "scopes_supported should contain openid scope",
        scopesSupported.contains("openid"),
        is(true));
  }

  @Test
  @DisplayName("Should return error for missing tenant-id header")
  public void testFetchOIDCConfigurationWithMissingTenantIdHeader() {

    Map<String, String> headers = new HashMap<>();

    Map<String, String> queryParams = new HashMap<>();

    Response response = sendGetRequestToOIDCDiscovery(headers, queryParams);

    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return error for invalid tenant-id")
  public void testFetchOIDCConfigurationWithInvalidTenantId() {

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, invalidTenant);
    Map<String, String> queryParams = new HashMap<>();

    Response response = sendGetRequestToOIDCDiscovery(headers, queryParams);

    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("No config found"));
  }

  @Test
  @DisplayName("Should return 405 Method Not Allowed for POST request")
  public void testFetchOIDCConfigurationWithPostMethod() {

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenant1);
    Map<String, Object> body = new HashMap<>();

    Response response = sendPostRequestToOIDCDiscovery(headers, body);

    response.then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
  }

  @Test
  @DisplayName("Should return 405 Method Not Allowed for PATCH request")
  public void testFetchOIDCConfigurationWithPatchMethod() {

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenant1);
    Map<String, String> queryParams = new HashMap<>();
    Map<String, Object> body = new HashMap<>();

    Response response = sendPatchRequestToOIDCDiscovery(headers, queryParams, body);

    response.then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
  }

  @Test
  @DisplayName("Should return 405 Method Not Allowed for PUT request")
  public void testFetchOIDCConfigurationWithPutMethod() {

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, tenant1);
    Map<String, String> queryParams = new HashMap<>();
    Map<String, Object> body = new HashMap<>();

    Response response = sendPutRequestToOIDCDiscovery(headers, queryParams, body);

    response.then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
  }
}
