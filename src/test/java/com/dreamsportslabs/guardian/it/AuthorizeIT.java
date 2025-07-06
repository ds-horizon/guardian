package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.TENANT_ID_HEADER;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_ERROR;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_ERROR_DESCRIPTION;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_LOGIN_HINT;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_PROMPT;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_STATE;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dreamsportslabs.guardian.Setup;
import com.dreamsportslabs.guardian.constant.OidcCodeChallengeMethod;
import com.dreamsportslabs.guardian.constant.OidcPrompt;
import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.response.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Setup.class)
public class AuthorizeIT {

  private static final String TENANT_ID = "tenant1";
  private static final String TENANT_ID_2 = "tenant2";
  private static final String AUTHORIZE_ENDPOINT = "/authorize";

  private static final String TEST_CLIENT_ID = "test-client-id";
  private static final String TEST_REDIRECT_URI = "https://example.com/callback";
  private static final String TEST_SCOPE = "openid profile email";
  private static final String TEST_RESPONSE_TYPE = "code";

  @BeforeAll
  static void setUp() {
    // Clean up any existing test data
    DbUtils.cleanupClients(TENANT_ID);
    DbUtils.cleanupClients(TENANT_ID_2);

    // Note: Test clients should be created via seed data or manually in test database
    // For now, assuming test clients exist in the database
  }

  // ========== SUCCESS SCENARIOS ==========

  @Test
  @DisplayName("Should authorize successfully with valid minimal parameters")
  void testAuthorizeSuccess() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();

    // Act
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .queryParams(queryParams)
            .when()
            .get(AUTHORIZE_ENDPOINT)
            .then()
            .statusCode(302)
            .extract()
            .response();

    // Assert
    String location = response.getHeader("Location");
    assertNotNull(location);
    assertTrue(location.contains(OIDC_PARAM_LOGIN_CHALLENGE));

    // Verify login challenge is present
    String loginChallenge = extractQueryParam(location, OIDC_PARAM_LOGIN_CHALLENGE);
    assertNotNull(loginChallenge);
  }

  @Test
  @DisplayName("Should authorize successfully with all optional parameters")
  void testAuthorizeSuccessWithAllParameters() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("state", "test-state-123");
    queryParams.put("nonce", "test-nonce-456");
    queryParams.put("prompt", OidcPrompt.LOGIN.getValue());
    queryParams.put("login_hint", "user@example.com");
    queryParams.put("code_challenge", "test-code-challenge");
    queryParams.put("code_challenge_method", OidcCodeChallengeMethod.S256.getValue());

    // Act
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .queryParams(queryParams)
            .when()
            .get(AUTHORIZE_ENDPOINT)
            .then()
            .statusCode(302)
            .extract()
            .response();

    // Assert
    String location = response.getHeader("Location");
    assertNotNull(location);
    assertTrue(location.contains(OIDC_PARAM_LOGIN_CHALLENGE));
    assertTrue(location.contains(OIDC_PARAM_STATE + "=test-state-123"));
    assertTrue(location.contains(OIDC_PARAM_PROMPT + "=" + OidcPrompt.LOGIN.getValue()));
    assertTrue(location.contains(OIDC_PARAM_LOGIN_HINT + "=user@example.com"));
  }

  @Test
  @DisplayName("Should authorize successfully with different prompt values")
  void testAuthorizeSuccessWithDifferentPrompts() {
    String[] validPrompts = {
      OidcPrompt.LOGIN.getValue(),
      OidcPrompt.CONSENT.getValue(),
      OidcPrompt.NONE.getValue(),
      OidcPrompt.SELECT_ACCOUNT.getValue()
    };

    for (String prompt : validPrompts) {
      // Arrange
      Map<String, String> queryParams = createValidAuthorizeRequest();
      queryParams.put("prompt", prompt);

      // Act
      Response response =
          given()
              .header(TENANT_ID_HEADER, TENANT_ID)
              .queryParams(queryParams)
              .when()
              .get(AUTHORIZE_ENDPOINT)
              .then()
              .statusCode(302)
              .extract()
              .response();

      // Assert
      String location = response.getHeader("Location");
      assertNotNull(location);
      assertTrue(location.contains(OIDC_PARAM_PROMPT + "=" + prompt));
    }
  }

  @Test
  @DisplayName("Should authorize successfully with different code challenge methods")
  void testAuthorizeSuccessWithDifferentCodeChallengeMethods() {
    String[] validMethods = {
      OidcCodeChallengeMethod.PLAIN.getValue(), OidcCodeChallengeMethod.S256.getValue()
    };

    for (String method : validMethods) {
      // Arrange
      Map<String, String> queryParams = createValidAuthorizeRequest();
      queryParams.put("code_challenge", "test-challenge-" + method);
      queryParams.put("code_challenge_method", method);

      // Act
      given()
          .header(TENANT_ID_HEADER, TENANT_ID)
          .queryParams(queryParams)
          .when()
          .get(AUTHORIZE_ENDPOINT)
          .then()
          .statusCode(302);
    }
  }

  @Test
  @DisplayName("Should authorize successfully with multiple scopes including openid")
  void testAuthorizeSuccessWithMultipleScopes() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("scope", "openid profile email address phone");

    // Act
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(302);
  }

  // ========== VALIDATION ERROR SCENARIOS ==========

  @Test
  @DisplayName("Should return error when client_id is missing")
  void testAuthorizeMissingClientId() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.remove("client_id");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should return error when scope is missing")
  void testAuthorizeMissingScope() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.remove("scope");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should return error when redirect_uri is missing")
  void testAuthorizeMissingRedirectUri() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.remove("redirect_uri");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should return error when response_type is missing")
  void testAuthorizeMissingResponseType() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.remove("response_type");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should return error when scope doesn't contain openid")
  void testAuthorizeScopeMissingOpenid() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("scope", "profile email"); // Missing openid

    // Act & Assert
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .queryParams(queryParams)
            .when()
            .get(AUTHORIZE_ENDPOINT)
            .then()
            .statusCode(302)
            .extract()
            .response();

    // Should redirect to error
    String location = response.getHeader("Location");
    assertTrue(location.contains(OIDC_PARAM_ERROR + "=invalid_scope"));
    assertTrue(location.contains(OIDC_PARAM_ERROR_DESCRIPTION));
  }

  @Test
  @DisplayName("Should return error when prompt value is invalid")
  void testAuthorizeInvalidPrompt() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("prompt", "invalid_prompt");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should return error when code_challenge_method is invalid")
  void testAuthorizeInvalidCodeChallengeMethod() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("code_challenge", "test-challenge");
    queryParams.put("code_challenge_method", "INVALID");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should return error when code_challenge is provided without code_challenge_method")
  void testAuthorizeCodeChallengeWithoutMethod() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("code_challenge", "test-challenge");
    // Missing code_challenge_method

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should return error when code_challenge_method is provided without code_challenge")
  void testAuthorizeCodeChallengeMethodWithoutChallenge() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("code_challenge_method", OidcCodeChallengeMethod.S256.getValue());
    // Missing code_challenge

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(400);
  }

  // ========== CLIENT VALIDATION ERROR SCENARIOS ==========

  @Test
  @DisplayName("Should return error when client_id doesn't exist")
  void testAuthorizeClientNotFound() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("client_id", "non-existent-client");

    // Act & Assert
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .queryParams(queryParams)
            .when()
            .get(AUTHORIZE_ENDPOINT)
            .then()
            .statusCode(302)
            .extract()
            .response();

    // Should redirect to error
    String location = response.getHeader("Location");
    assertTrue(location.contains(OIDC_PARAM_ERROR + "=invalid_request"));
    assertTrue(location.contains("Invalid client_id"));
  }

  @Test
  @DisplayName("Should return error when redirect_uri is not registered for client")
  void testAuthorizeInvalidRedirectUri() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("redirect_uri", "https://malicious.com/callback");

    // Act & Assert
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .queryParams(queryParams)
            .when()
            .get(AUTHORIZE_ENDPOINT)
            .then()
            .statusCode(302)
            .extract()
            .response();

    // Should redirect to error
    String location = response.getHeader("Location");
    assertTrue(location.contains(OIDC_PARAM_ERROR + "=invalid_request"));
    assertTrue(location.contains("Invalid redirect_uri"));
  }

  // @Test
  // @DisplayName("Should return error when response_type is not supported by client")
  // void testAuthorizeUnsupportedResponseType() {
  //   // Note: This test requires creating test clients with specific response types
  //   // Implementation depends on available DbUtils methods or test data setup
  // }

  // ========== TENANT ISOLATION SCENARIOS ==========

  @Test
  @DisplayName("Should return error when tenant-id header is missing")
  void testAuthorizeMissingTenantId() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();

    // Act & Assert
    given().queryParams(queryParams).when().get(AUTHORIZE_ENDPOINT).then().statusCode(401);
  }

  @Test
  @DisplayName("Should return error when client doesn't exist in specified tenant")
  void testAuthorizeTenantIsolation() {
    // Arrange - Use client from tenant1 but request from tenant2
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("client_id", TEST_CLIENT_ID); // This client exists in tenant1, not tenant2

    // Act & Assert
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID_2)
            .queryParams(queryParams)
            .when()
            .get(AUTHORIZE_ENDPOINT)
            .then()
            .statusCode(302)
            .extract()
            .response();

    // Should redirect to error
    String location = response.getHeader("Location");
    assertTrue(location.contains(OIDC_PARAM_ERROR + "=invalid_request"));
    assertTrue(location.contains("Invalid client_id"));
  }

  @Test
  @DisplayName("Should successfully authorize with client from correct tenant")
  void testAuthorizeTenantIsolationSuccess() {
    // Arrange - Use client from tenant2
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("client_id", TEST_CLIENT_ID + "-2");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID_2)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(302);
  }

  // ========== SCOPE FILTERING SCENARIOS ==========

  // @Test
  // @DisplayName("Should filter unsupported scopes and allow request with supported scopes")
  // void testAuthorizeScopeFiltering() {
  //   // Note: This test requires creating test clients with specific scopes
  //   // Implementation depends on available DbUtils methods or test data setup
  // }

  // ========== LOGIN CHALLENGE GENERATION SCENARIOS ==========

  @Test
  @DisplayName("Should generate unique login challenges for concurrent requests")
  void testAuthorizeUniqueLoginChallenges() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();

    // Act - Make multiple concurrent requests
    Response response1 =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .queryParams(queryParams)
            .when()
            .get(AUTHORIZE_ENDPOINT)
            .then()
            .statusCode(302)
            .extract()
            .response();

    Response response2 =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .queryParams(queryParams)
            .when()
            .get(AUTHORIZE_ENDPOINT)
            .then()
            .statusCode(302)
            .extract()
            .response();

    // Assert - Should have different login challenges
    String location1 = response1.getHeader("Location");
    String location2 = response2.getHeader("Location");

    String challenge1 = extractQueryParam(location1, OIDC_PARAM_LOGIN_CHALLENGE);
    String challenge2 = extractQueryParam(location2, OIDC_PARAM_LOGIN_CHALLENGE);

    assertNotNull(challenge1);
    assertNotNull(challenge2);
    assertTrue(!challenge1.equals(challenge2));
  }

  // ========== PKCE SCENARIOS ==========

  @Test
  @DisplayName("Should handle PKCE with Plain method correctly")
  void testAuthorizePKCEPlain() {
    // Arrange
    String codeVerifier = RandomStringUtils.randomAlphanumeric(43);
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("code_challenge", codeVerifier);
    queryParams.put("code_challenge_method", OidcCodeChallengeMethod.PLAIN.getValue());

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(302);
  }

  @Test
  @DisplayName("Should handle PKCE with S256 method correctly")
  void testAuthorizePKCES256() {
    // Arrange
    String codeVerifier = RandomStringUtils.randomAlphanumeric(43);
    String codeChallenge = DigestUtils.sha256Hex(codeVerifier);
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("code_challenge", codeChallenge);
    queryParams.put("code_challenge_method", OidcCodeChallengeMethod.S256.getValue());

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(302);
  }

  // ========== EDGE CASE SCENARIOS ==========

  @Test
  @DisplayName("Should handle extremely long valid parameters")
  void testAuthorizeWithLongParameters() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("state", RandomStringUtils.randomAlphanumeric(2000));
    queryParams.put("nonce", RandomStringUtils.randomAlphanumeric(1000));
    queryParams.put(
        "login_hint",
        "very.long.email.address.with.multiple.subdomains@example.very.long.domain.com");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(302);
  }

  @Test
  @DisplayName("Should handle special characters in parameters")
  void testAuthorizeWithSpecialCharacters() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest();
    queryParams.put("state", "test-state-with-special-chars-@#$%^&*()");
    queryParams.put("login_hint", "user+tag@example-domain.co.uk");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParams(queryParams)
        .when()
        .get(AUTHORIZE_ENDPOINT)
        .then()
        .statusCode(302);
  }

  // ========== HELPER METHODS ==========

  private Map<String, String> createValidAuthorizeRequest() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("client_id", TEST_CLIENT_ID);
    queryParams.put("redirect_uri", TEST_REDIRECT_URI);
    queryParams.put("scope", TEST_SCOPE);
    queryParams.put("response_type", TEST_RESPONSE_TYPE);
    return queryParams;
  }

  // Note: Helper methods for creating test clients would need to be implemented
  // based on available DbUtils methods or test data setup approach used in this project

  private String extractQueryParam(String url, String paramName) {
    try {
      URI uri = new URI(url);
      String query = uri.getQuery();
      if (query == null) return null;

      String[] pairs = query.split("&");
      for (String pair : pairs) {
        String[] keyValue = pair.split("=");
        if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
          return keyValue[1];
        }
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
