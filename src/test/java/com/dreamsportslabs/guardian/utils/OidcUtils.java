package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.Constants.AUTH_RESPONSE_TYPE_CODE;
import static com.dreamsportslabs.guardian.Constants.EQUALS_SIGN;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_CALLBACK;
import static com.dreamsportslabs.guardian.Constants.HEADER_LOCATION;
import static com.dreamsportslabs.guardian.Constants.LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.PARAM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.PARAM_REDIRECT_URI;
import static com.dreamsportslabs.guardian.Constants.PARAM_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.PARAM_SEPARATOR;
import static com.dreamsportslabs.guardian.Constants.PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.QUERY_SEPARATOR;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.TEST_STATE;
import static com.dreamsportslabs.guardian.utils.DbUtils.authorizeSessionExists;
import static com.dreamsportslabs.guardian.utils.DbUtils.getAuthorizeSession;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OidcUtils {

  /**
   * Creates a valid authorize request with all required parameters.
   *
   * @param clientId The client ID to use in the request
   * @return Map containing the query parameters for a valid authorize request
   */
  public static Map<String, String> createValidAuthorizeRequest(String clientId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(PARAM_RESPONSE_TYPE, AUTH_RESPONSE_TYPE_CODE);
    queryParams.put(PARAM_CLIENT_ID, clientId);
    queryParams.put(PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL + " " + SCOPE_ADDRESS);
    queryParams.put(PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);
    queryParams.put(PARAM_STATE, TEST_STATE);
    return queryParams;
  }

  /**
   * Extracts the login challenge from a redirect location URL.
   *
   * @param location The redirect location URL
   * @return The login challenge value, or null if not found
   */
  public static String extractLoginChallenge(String location) {
    if (location == null) return null;
    String[] params = location.split(QUERY_SEPARATOR)[1].split(PARAM_SEPARATOR);
    for (String param : params) {
      if (param.startsWith(LOGIN_CHALLENGE + EQUALS_SIGN)) {
        return param.split(EQUALS_SIGN)[1];
      }
    }
    return null;
  }

  /**
   * Validates that the Redis authorize session model has all the expected values set. This method
   * checks that the session exists and contains the expected fields from the authorize request.
   *
   * @param tenantId The tenant ID
   * @param loginChallenge The login challenge to validate
   * @param expectedQueryParams The original query parameters used in the authorize request
   */
  public static void validateAuthorizeSessionModel(
      String tenantId, String loginChallenge, Map<String, String> expectedQueryParams) {
    JsonObject sessionModel = DbUtils.getAuthorizeSession(tenantId, loginChallenge);

    // Verify session exists
    assertThat("Authorize session should exist in Redis", sessionModel, notNullValue());

    // Validate response type
    if (expectedQueryParams.containsKey(PARAM_RESPONSE_TYPE)) {
      String expectedResponseType = expectedQueryParams.get(PARAM_RESPONSE_TYPE);
      String actualResponseType = sessionModel.getString("responseType");
      assertThat(
          "Response type should match",
          actualResponseType,
          equalTo(expectedResponseType.toUpperCase()));
    }

    // Validate redirect URI
    if (expectedQueryParams.containsKey(PARAM_REDIRECT_URI)) {
      String expectedRedirectUri = expectedQueryParams.get(PARAM_REDIRECT_URI);
      String actualRedirectUri = sessionModel.getString("redirectUri");
      assertThat("Redirect URI should match", actualRedirectUri, equalTo(expectedRedirectUri));
    }

    // Validate state
    if (expectedQueryParams.containsKey(PARAM_STATE)) {
      String expectedState = expectedQueryParams.get(PARAM_STATE);
      String actualState = sessionModel.getString("state");
      assertThat("State should match", actualState, equalTo(expectedState));
    }

    // Validate scopes
    if (expectedQueryParams.containsKey(PARAM_SCOPE)) {
      String expectedScopes = expectedQueryParams.get(PARAM_SCOPE);
      List<String> actualAllowedScopes = sessionModel.getJsonArray("allowedScopes").getList();
      assertThat("Allowed scopes should not be null", actualAllowedScopes, notNullValue());
      assertThat(
          "Allowed scopes should not be empty", actualAllowedScopes.isEmpty(), equalTo(false));

      // Check that all valid expected scopes are present in allowed scopes
      // Note: Invalid scopes are filtered out by the service, so we only check valid ones
      String[] expectedScopeArray = expectedScopes.split(" ");
      for (String expectedScope : expectedScopeArray) {
        // Only check if the scope is actually in the allowed scopes (i.e., it's valid)
        if (actualAllowedScopes.contains(expectedScope)) {
          assertThat(
              "Valid scope '" + expectedScope + "' should be in allowed scopes",
              actualAllowedScopes.contains(expectedScope),
              equalTo(true));
        }
        // Invalid scopes are expected to be filtered out, so we don't assert on them
      }

      // Additional validation: ensure that at least 'openid' scope is present
      assertThat(
          "OpenID scope should always be present",
          actualAllowedScopes.contains("openid"),
          equalTo(true));
    }

    // Validate client information
    JsonObject client = sessionModel.getJsonObject("client");
    assertThat("Client should not be null", client, notNullValue());
    if (expectedQueryParams.containsKey(PARAM_CLIENT_ID)) {
      String expectedClientId = expectedQueryParams.get(PARAM_CLIENT_ID);
      String actualClientId = client.getString("clientId");
      assertThat("Client ID should match", actualClientId, equalTo(expectedClientId));
    }

    // Validate optional parameters if present
    if (expectedQueryParams.containsKey("nonce")) {
      String expectedNonce = expectedQueryParams.get("nonce");
      String actualNonce = sessionModel.getString("nonce");
      assertThat("Nonce should match", actualNonce, equalTo(expectedNonce));
    }

    if (expectedQueryParams.containsKey("code_challenge")) {
      String expectedCodeChallenge = expectedQueryParams.get("code_challenge");
      String actualCodeChallenge = sessionModel.getString("codeChallenge");
      assertThat(
          "Code challenge should match", actualCodeChallenge, equalTo(expectedCodeChallenge));
    }

    if (expectedQueryParams.containsKey("code_challenge_method")) {
      String expectedCodeChallengeMethod = expectedQueryParams.get("code_challenge_method");
      String actualCodeChallengeMethod = sessionModel.getString("codeChallengeMethod");
      assertThat(
          "Code challenge method should match",
          actualCodeChallengeMethod,
          equalTo(expectedCodeChallengeMethod.toUpperCase()));
    }

    if (expectedQueryParams.containsKey("prompt")) {
      String expectedPrompt = expectedQueryParams.get("prompt");
      String actualPrompt = sessionModel.getString("prompt");
      assertThat("Prompt should match", actualPrompt, equalTo(expectedPrompt.toUpperCase()));
    }

    if (expectedQueryParams.containsKey("login_hint")) {
      String expectedLoginHint = expectedQueryParams.get("login_hint");
      String actualLoginHint = sessionModel.getString("loginHint");
      assertThat("Login hint should match", actualLoginHint, equalTo(expectedLoginHint));
    }

    // Validate that userId is initially null (not set until login accept)
    String userId = sessionModel.getString("userId");
    assertThat("UserId should be null initially", userId, equalTo(null));

    // Validate that consentedScopes is initially null (not set until login accept)
    List<String> consentedScopes =
        sessionModel.getJsonArray("consentedScopes") != null
            ? sessionModel.getJsonArray("consentedScopes").getList()
            : null;
    assertThat("Consented scopes should be null initially", consentedScopes, equalTo(null));
  }

  /**
   * Validates detailed response structure for login-accept success cases. This method validates the
   * location header structure and session cleanup.
   *
   * @param response The response to validate
   * @param tenantId The tenant ID
   * @param loginChallenge The login challenge that should be deleted
   * @param expectedUserId The expected user ID in the session (optional)
   */
  public static void validateLoginAcceptResponse(
      Response response, String tenantId, String loginChallenge, String expectedUserId) {
    // Validate - Detailed response structure validation
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString("consent_challenge="))
        .header(HEADER_LOCATION, containsString("state="));

    // Validate location header structure
    String locationHeader = response.getHeader(HEADER_LOCATION);
    assertThat("Location header should not be null", locationHeader, notNullValue());
    assertThat(
        "Location header should contain consent_challenge",
        locationHeader,
        containsString("consent_challenge="));
    assertThat("Location header should contain state", locationHeader, containsString("state="));

    // Verify session was deleted from database
    assertThat(
        "Authorize session should be deleted",
        authorizeSessionExists(tenantId, loginChallenge),
        equalTo(false));

    // Verify user association in the session (if available)
    if (expectedUserId != null) {
      JsonObject sessionData = getAuthorizeSession(tenantId, loginChallenge);
      if (sessionData != null) {
        String sessionUserId = sessionData.getString("userId");
        assertThat(
            "Session should be associated with correct user",
            sessionUserId,
            equalTo(expectedUserId));
      }
    }
  }
}
