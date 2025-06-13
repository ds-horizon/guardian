package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.TENANT_ID_HEADER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.dreamsportslabs.guardian.Setup;
import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Setup.class)
public class ClientScopeResourceIT {

  private static final String TENANT_ID = "tenant1";
  private static final String TENANT_ID_2 = "tenant2";
  private static final String CLIENT_ENDPOINT = "/v1/admin/client";
  private static final String SCOPE_ENDPOINT = "/v1/admin/scope";

  private static final String TEST_SCOPE_1 = "scope1";
  private static final String TEST_SCOPE_2 = "scope2";
  private static final String TEST_SCOPE_3 = "scope3";
  private static final String TEST_SCOPE_4 = "scope4";
  private static final String TEST_SCOPE_5 = "scope5";
  private static final String TEST_SCOPE_6 = "scope6";

  private String testClientId;

  @BeforeAll
  static void setUpTestScopes() {
    // Create test scopes
    DbUtils.cleanupScopes(TENANT_ID);
    DbUtils.cleanupScopes(TENANT_ID_2);
    DbUtils.addScope(TENANT_ID, TEST_SCOPE_1);
    DbUtils.addScope(TENANT_ID, TEST_SCOPE_2);
    DbUtils.addScope(TENANT_ID, TEST_SCOPE_3);
    DbUtils.addScope(TENANT_ID_2, TEST_SCOPE_4);
    DbUtils.addScope(TENANT_ID_2, TEST_SCOPE_5);
    DbUtils.addScope(TENANT_ID_2, TEST_SCOPE_6);
  }

  @BeforeEach
  void setUp() {
    // Clean up any existing test data
    DbUtils.cleanupClientScopes(TENANT_ID);
    DbUtils.cleanupClients(TENANT_ID);
    DbUtils.cleanupClientScopes(TENANT_ID_2);
    DbUtils.cleanupClients(TENANT_ID_2);

    // Create test client and scope
    testClientId = createTestClient();
  }

  @Test
  @DisplayName("Should add scope to client successfully")
  void testAddScopeToClient_Success() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("scopes", List.of(TEST_SCOPE_1, TEST_SCOPE_2));

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT + "/" + testClientId + "/scope")
        .then()
        .statusCode(204);

    // Verify relationship was created in database
    assertTrue(DbUtils.clientScopeExists(TENANT_ID, testClientId, TEST_SCOPE_1));
    assertTrue(DbUtils.clientScopeExists(TENANT_ID, testClientId, TEST_SCOPE_2));
  }

  @Test
  @DisplayName("Should return error when adding non-existent scope to client")
  void testAddScopeToClient_ScopeNotFound() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("scopes", List.of(TEST_SCOPE_4));

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT + "/" + testClientId + "/scope")
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", containsString("Scopes set are not valid"));
  }

  @Test
  @DisplayName("Should return error when adding scope to non-existent client")
  void testAddScopeToClient_ClientNotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("scopes", List.of(TEST_SCOPE_1));

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT + "/" + nonExistentClientId + "/scope")
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", containsString("Client not found"));
  }

  @Test
  @DisplayName("Should return error when adding duplicate scope to client")
  void testAddScopeToClient_DuplicateScope() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("scopes", List.of(TEST_SCOPE_1));

    // Add scope first time
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT + "/" + testClientId + "/scope")
        .then()
        .statusCode(204);

    // Try to add same scope again
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT + "/" + testClientId + "/scope")
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", equalTo(TEST_SCOPE_1 + " already exists for client"));
  }

  @Test
  @DisplayName("Should list client scopes successfully")
  void testGetClientScopes_Success() {
    // Arrange
    addScopeToClient(TENANT_ID, testClientId, TEST_SCOPE_1, TEST_SCOPE_2);

    // Act & Assert
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .when()
            .get(CLIENT_ENDPOINT + "/" + testClientId + "/scope")
            .then()
            .statusCode(200)
            .extract()
            .response();

    assertNotNull(response.jsonPath().getList("scopes"));
    assertEquals(2, response.jsonPath().getList("scopes").size());
    assertTrue(response.jsonPath().getList("scopes").contains(TEST_SCOPE_1));
    assertTrue(response.jsonPath().getList("scopes").contains(TEST_SCOPE_2));
  }

  @Test
  @DisplayName("Should return empty list when client has no scopes")
  void testGetClientScopes_EmptyList() {
    // Act & Assert
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .when()
            .get(CLIENT_ENDPOINT + "/" + testClientId + "/scope")
            .then()
            .statusCode(200)
            .extract()
            .response();
    assertNotNull(response.jsonPath().getList("scopes"));
    assertTrue(response.jsonPath().getList("scopes").isEmpty());
  }

  @Test
  @DisplayName("Should return error when listing scopes for non-existent client")
  void testGetClientScopes_ClientNotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .when()
        .get(CLIENT_ENDPOINT + "/" + nonExistentClientId + "/scope")
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", containsString("Client not found"));
  }

  @Test
  @DisplayName("Should remove scope from client successfully")
  void testRemoveScopeFromClient_Success() {
    // Arrange
    addScopeToClient(TENANT_ID, testClientId, TEST_SCOPE_2);

    // Act
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParam("scope", TEST_SCOPE_2)
        .when()
        .delete(CLIENT_ENDPOINT + "/" + testClientId + "/scope")
        .then()
        .statusCode(204);

    // Assert - Verify relationship was removed
    assertFalse(DbUtils.clientScopeExists(TENANT_ID, testClientId, TEST_SCOPE_2));
  }

  @Test
  @DisplayName("Should return error when removing non-existent scope from client")
  void testRemoveScopeFromClient_ScopeNotAssigned() {
    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParam("scope", TEST_SCOPE_2)
        .when()
        .delete(CLIENT_ENDPOINT + "/" + testClientId + "/scope")
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", equalTo("Client scope does not exist for client: " + testClientId));
  }

  @Test
  @DisplayName("Should return error when removing scope from non-existent client")
  void testRemoveScopeFromClient_ClientNotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParam("scope", TEST_SCOPE_2)
        .when()
        .delete(CLIENT_ENDPOINT + "/" + nonExistentClientId + "/scope")
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", containsString("Client not found"));
  }

  @Test
  @DisplayName("Should validate required fields when adding scope")
  void testAddScopeToClient_MissingFields() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    // Missing scopes

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT + "/" + testClientId + "/scope")
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", equalTo("Scope is required"));
  }

  @Test
  @DisplayName("Should return error when tenant ID is missing")
  void testAddScopeToClient_MissingTenantId() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("scopes", List.of(TEST_SCOPE_1));

    // Act & Assert
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT + "/" + testClientId + "/scope")
        .then()
        .statusCode(401);
  }

  @Test
  @DisplayName("Should handle multi-tenant isolation for client-scope operations")
  void testMultiTenantClientScopeIsolation() {
    // Create client and scope in tenant2
    String clientIdT2 = createTestClient(TENANT_ID_2);

    // Add scope to client in tenant2
    addScopeToClient(TENANT_ID_2, clientIdT2, TEST_SCOPE_4);

    // Try to access tenant2's client-scope from tenant1
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .when()
        .get(CLIENT_ENDPOINT + "/" + clientIdT2 + "/scope")
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", containsString("Client not found"));

    // Try to remove tenant2's client-scope from tenant1
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .queryParam("scope", TEST_SCOPE_4)
        .when()
        .delete(CLIENT_ENDPOINT + "/" + clientIdT2 + "/scope")
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", containsString("Client not found"));

    // Verify tenant2 can access its own client-scope
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID_2)
            .when()
            .get(CLIENT_ENDPOINT + "/" + clientIdT2 + "/scope")
            .then()
            .statusCode(200)
            .extract()
            .response();

    assertNotNull(response.jsonPath().getList("scopes"));
    assertEquals(1, response.jsonPath().getList("scopes").size());
    assertTrue(response.jsonPath().getList("scopes").contains(TEST_SCOPE_4));
  }

  private static String createTestClient() {
    return createTestClient(TENANT_ID);
  }

  private static String createTestClient(String tenantId) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("client_name", "Test Client " + RandomStringUtils.randomAlphanumeric(8));
    requestBody.put("client_uri", "https://example.com");
    requestBody.put("contacts", Arrays.asList("admin@example.com"));
    requestBody.put("grant_types", Arrays.asList("authorization_code", "refresh_token"));
    requestBody.put("response_types", Arrays.asList("code"));
    requestBody.put("redirect_uris", Arrays.asList("https://example.com/callback"));
    requestBody.put("logo_uri", "https://example.com/logo.png");
    requestBody.put("policy_uri", "https://example.com/policy");
    requestBody.put("skip_consent", false);

    Response response =
        given()
            .header(TENANT_ID_HEADER, tenantId)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(CLIENT_ENDPOINT)
            .then()
            .statusCode(201)
            .extract()
            .response();

    return response.jsonPath().getString("client_id");
  }

  private void addScopeToClient(String tenantId, String clientId, String... scopes) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("scopes", List.of(scopes));

    given()
        .header(TENANT_ID_HEADER, tenantId)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT + "/" + clientId + "/scope")
        .then()
        .statusCode(204);
  }
}
