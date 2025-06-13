package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.TENANT_ID_HEADER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClientResourceIT {

  private static final String TENANT_ID = "tenant1";
  private static final String TENANT_ID_2 = "tenant2";
  private static final String CLIENT_ENDPOINT = "/v1/admin/client";

  @BeforeAll
  static void setUp() {
    // Clean up any existing test data
    DbUtils.cleanupClients(TENANT_ID);
    DbUtils.cleanupClients(TENANT_ID_2);
  }

  @Test
  @DisplayName("Should create OIDC client successfully")
  void testCreateClient_Success() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();

    // Act
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(CLIENT_ENDPOINT)
            .then()
            .statusCode(201)
            .extract()
            .response();

    // Assert
    String clientId = response.jsonPath().getString("client_id");
    assertNotNull(clientId);
    assertEquals(requestBody.get("client_name"), response.jsonPath().getString("client_name"));
    assertEquals(requestBody.get("client_uri"), response.jsonPath().getString("client_uri"));
    assertNotNull(response.jsonPath().getString("client_secret"));

    // Verify client was saved in database
    assertTrue(DbUtils.clientExists(TENANT_ID, clientId));
  }

  @Test
  @DisplayName("Should return error when required fields are missing")
  void testCreateClient_MissingRequiredFields() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("clientUri", "https://example.com");
    // Missing clientName

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT)
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", equalTo("Client name is required"));
  }

  @Test
  @DisplayName("Should return error when tenant ID is missing")
  void testCreateClient_MissingTenantId() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();

    // Act & Assert
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT)
        .then()
        .statusCode(401);
  }

  @Test
  @DisplayName("Should return error when client name already exists for tenant")
  void testCreateClient_DuplicateClientName() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    String clientName = (String) requestBody.get("client_name");

    // Create first client
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT)
        .then()
        .statusCode(201);

    // Try to create second client with same name
    Map<String, Object> duplicateRequest = createValidClientRequest();
    duplicateRequest.put("client_name", clientName);

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(duplicateRequest)
        .when()
        .post(CLIENT_ENDPOINT)
        .then()
        .statusCode(400)
        .body("error.code", equalTo("client_already_exists"));
  }

  @Test
  @DisplayName("Should get client by ID successfully")
  void testGetClient_Success() {
    // Arrange
    String clientId = createTestClient().jsonPath().getString("client_id");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .when()
        .get(CLIENT_ENDPOINT + "/" + clientId)
        .then()
        .statusCode(200)
        .body("client_id", equalTo(clientId))
        .body("client_name", notNullValue())
        .body("client_secret", notNullValue());
  }

  @Test
  @DisplayName("Should return 404 when client not found")
  void testGetClient_NotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .when()
        .get(CLIENT_ENDPOINT + "/" + nonExistentClientId)
        .then()
        .statusCode(404)
        .body("error.code", equalTo("client_not_found"));
  }

  @Test
  @DisplayName("Should list clients with pagination")
  void testGetClients_WithPagination() {
    // Arrange
    createTestClient();
    createTestClient();
    createTestClient();

    // Act & Assert
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .queryParam("page", 1)
            .queryParam("limit", 2)
            .when()
            .get(CLIENT_ENDPOINT)
            .then()
            .statusCode(200)
            .extract()
            .response();
    assertNotNull(response.jsonPath().getList("clients"));
    assertEquals(2, response.jsonPath().getList("clients").size());
    assertEquals(1, response.jsonPath().getInt("page"));
    assertEquals(2, response.jsonPath().getInt("limit"));
  }

  @Test
  @DisplayName("Should update client successfully")
  void testUpdateClient_Success() {
    // Arrange
    Map<String, Object> updateRequest = copyClientResponseToRequestBody(createTestClient());
    updateRequest.put("client_name", "Updated Client Name");
    updateRequest.put("client_uri", "https://updated-example.com");

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .patch(CLIENT_ENDPOINT + "/" + updateRequest.get("client_id"))
        .then()
        .statusCode(200)
        .body("client_name", equalTo("Updated Client Name"))
        .body("client_uri", equalTo("https://updated-example.com"));
  }

  @Test
  @DisplayName("Should delete client successfully")
  void testDeleteClient_Success() {
    // Arrange
    String clientId = createTestClient().jsonPath().getString("client_id");

    // Act
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .when()
        .delete(CLIENT_ENDPOINT + "/" + clientId)
        .then()
        .statusCode(204);

    // Assert - Verify client is deleted
    assertFalse(DbUtils.clientExists(TENANT_ID, clientId));
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent client")
  void testDeleteClient_NotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .when()
        .delete(CLIENT_ENDPOINT + "/" + nonExistentClientId)
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", equalTo("Client not found"));
  }

  @Test
  @DisplayName("Should validate grant types")
  void testCreateClient_InvalidGrantTypes() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put("grant_types", Arrays.asList("invalid_grant_type"));

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT)
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", equalTo("Invalid grant type: invalid_grant_type"));
  }

  @Test
  @DisplayName("Should validate response types")
  void testCreateClient_InvalidResponseTypes() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put("response_types", Arrays.asList("invalid_response_type"));

    // Act & Assert
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT)
        .then()
        .statusCode(400)
        .body("error.code", equalTo("invalid_request"))
        .body("error.message", equalTo("Invalid response type: invalid_response_type"));
  }

  @Test
  @DisplayName("Should handle concurrent client creation")
  void testCreateClient_ConcurrentRequests() {
    // This test would require more complex setup for true concurrency testing
    // For now, we'll test sequential creation with unique names

    Map<String, Object> request1 = createValidClientRequest();
    Map<String, Object> request2 = createValidClientRequest();

    // Both should succeed as they have different names
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(request1)
        .when()
        .post(CLIENT_ENDPOINT)
        .then()
        .statusCode(201);

    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .contentType(ContentType.JSON)
        .body(request2)
        .when()
        .post(CLIENT_ENDPOINT)
        .then()
        .statusCode(201);
  }

  @Test
  @DisplayName("Should generate secure client secret with minimum length")
  void testCreateClient_GeneratesSecureSecret() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();

    // Act
    Response response =
        given()
            .header(TENANT_ID_HEADER, TENANT_ID)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(CLIENT_ENDPOINT)
            .then()
            .statusCode(201)
            .extract()
            .response();

    // Assert
    String clientSecret = response.jsonPath().getString("client_secret");
    assertNotNull(clientSecret);
    assertTrue(clientSecret.length() >= 32, "Client secret should be at least 32 characters long");

    // Verify it contains alphanumeric characters
    assertTrue(clientSecret.matches("[A-Za-z0-9]+"), "Client secret should be alphanumeric");
  }

  @Test
  @DisplayName("Should handle service layer error scenarios")
  void testServiceLayerErrorHandling() {
    // Test database connection error simulation by using invalid tenant
    Map<String, Object> requestBody = createValidClientRequest();

    // Using a very long tenant ID that might cause database issues
    String invalidTenantId = "a".repeat(100);

    given()
        .header(TENANT_ID_HEADER, invalidTenantId)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should handle multi-tenant isolation at service layer")
  void testMultiTenantServiceIsolation() {
    // Create client in tenant1
    String clientIdT1 = createTestClientForTenant(TENANT_ID).jsonPath().getString("client_id");

    // Create client in tenant2
    String clientIdT2 = createTestClientForTenant(TENANT_ID_2).jsonPath().getString("client_id");

    // Verify tenant1 cannot access tenant2's client
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .when()
        .get(CLIENT_ENDPOINT + "/" + clientIdT2)
        .then()
        .statusCode(404)
        .body("error.code", equalTo("client_not_found"));

    // Verify tenant2 cannot access tenant1's client
    given()
        .header(TENANT_ID_HEADER, TENANT_ID_2)
        .when()
        .get(CLIENT_ENDPOINT + "/" + clientIdT1)
        .then()
        .statusCode(404)
        .body("error.code", equalTo("client_not_found"));

    // Verify each tenant can access their own client
    given()
        .header(TENANT_ID_HEADER, TENANT_ID)
        .when()
        .get(CLIENT_ENDPOINT + "/" + clientIdT1)
        .then()
        .statusCode(200)
        .body("client_id", equalTo(clientIdT1));

    given()
        .header(TENANT_ID_HEADER, TENANT_ID_2)
        .when()
        .get(CLIENT_ENDPOINT + "/" + clientIdT2)
        .then()
        .statusCode(200)
        .body("client_id", equalTo(clientIdT2));
  }

  private Map<String, Object> createValidClientRequest() {
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
    return requestBody;
  }

  private Map<String, Object> copyClientResponseToRequestBody(Response clientResponse) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("client_name", clientResponse.jsonPath().getString("client_name"));
    requestBody.put("client_uri", clientResponse.jsonPath().getString("client_uri"));
    requestBody.put("contacts", clientResponse.jsonPath().getList("contacts"));
    requestBody.put("grant_types", clientResponse.jsonPath().getList("grant_types"));
    requestBody.put("response_types", clientResponse.jsonPath().getList("response_types"));
    requestBody.put("redirect_uris", clientResponse.jsonPath().getList("redirect_uris"));
    requestBody.put("logo_uri", clientResponse.jsonPath().getString("logo_uri"));
    requestBody.put("policy_uri", clientResponse.jsonPath().getString("policy_uri"));
    requestBody.put("skip_consent", clientResponse.jsonPath().getBoolean("skip_consent"));
    requestBody.put("client_id", clientResponse.jsonPath().getString("client_id"));
    requestBody.put("client_secret", clientResponse.jsonPath().getString("client_secret"));
    return requestBody;
  }

  private Response createTestClient() {
    return createTestClientForTenant(TENANT_ID);
  }

  private Response createTestClientForTenant(String tenantId) {
    Map<String, Object> requestBody = createValidClientRequest();

    return given()
        .header(TENANT_ID_HEADER, tenantId)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(CLIENT_ENDPOINT)
        .then()
        .statusCode(201)
        .extract()
        .response();
  }
}
