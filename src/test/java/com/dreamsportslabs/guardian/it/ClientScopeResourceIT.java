package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.ADMIN_EMAIL;
import static com.dreamsportslabs.guardian.Constants.AUTHORIZATION_CODE;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NAME;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NOT_FOUND_MSG;
import static com.dreamsportslabs.guardian.Constants.CLIENT_URI;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.CONTACTS;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_CALLBACK;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_COM;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_LOGO;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_POLICY;
import static com.dreamsportslabs.guardian.Constants.GRANT_TYPES;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.LOGO_URI;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_VALID_SCOPES;
import static com.dreamsportslabs.guardian.Constants.POLICY_URI;
import static com.dreamsportslabs.guardian.Constants.REDIRECT_URIS;
import static com.dreamsportslabs.guardian.Constants.REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_TYPES;
import static com.dreamsportslabs.guardian.Constants.SCOPES;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ALREADY_EXISTS_MSG;
import static com.dreamsportslabs.guardian.Constants.SCOPE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.SKIP_CONSENT;
import static com.dreamsportslabs.guardian.Constants.SOME_SCOPES_NOT_EXIST;
import static com.dreamsportslabs.guardian.Constants.TEST_CLIENT_PREFIX;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getClientScopes;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;

import com.dreamsportslabs.guardian.Setup;
import com.dreamsportslabs.guardian.utils.DbUtils;
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

  public static String tenant1 = "tenant1";
  public static String tenant2 = "tenant2";

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
    DbUtils.cleanupScopes(tenant1);
    DbUtils.cleanupScopes(tenant2);
    DbUtils.addScope(tenant1, TEST_SCOPE_1);
    DbUtils.addScope(tenant1, TEST_SCOPE_2);
    DbUtils.addScope(tenant1, TEST_SCOPE_3);
    DbUtils.addScope(tenant2, TEST_SCOPE_4);
    DbUtils.addScope(tenant2, TEST_SCOPE_5);
    DbUtils.addScope(tenant2, TEST_SCOPE_6);
  }

  @BeforeEach
  void setUp() {
    // Clean up any existing test data
    DbUtils.cleanupClientScopes(tenant1);
    DbUtils.cleanupClients(tenant1);
    DbUtils.cleanupClientScopes(tenant2);
    DbUtils.cleanupClients(tenant2);

    // Create test client and scope
    testClientId = createTestClient();
  }

  @Test
  @DisplayName("Should add scope to client successfully")
  public void testAddScopeToClientSuccess() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(SCOPES, List.of(TEST_SCOPE_1, TEST_SCOPE_2));

    // Act
    Response response = createClientScope(tenant1, testClientId, requestBody);

    // Validate
    response.then().statusCode(SC_NO_CONTENT);
    assertThat(DbUtils.clientScopeExists(tenant1, testClientId, TEST_SCOPE_1), equalTo(true));
    assertThat(DbUtils.clientScopeExists(tenant1, testClientId, TEST_SCOPE_2), equalTo(true));
  }

  @Test
  @DisplayName("Should return error when adding non-existent scope to client")
  public void testAddScopeToClientScopeNotFound() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(SCOPES, List.of(TEST_SCOPE_4));

    // Act
    Response response = createClientScope(tenant1, testClientId, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, containsString(NO_VALID_SCOPES));
  }

  @Test
  @DisplayName("Should return error when adding non-existent scope to client")
  public void testAddScopeToClientSomeScopeNotFound() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(SCOPES, List.of(TEST_SCOPE_4, TEST_SCOPE_2));

    // Act
    Response response = createClientScope(tenant1, testClientId, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, containsString(SOME_SCOPES_NOT_EXIST));
  }

  @Test
  @DisplayName("Should return error when adding scope to non-existent client")
  public void testAddScopeToClientClientNotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(SCOPES, List.of(TEST_SCOPE_1));

    // Act
    Response response = createClientScope(tenant1, nonExistentClientId, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, containsString(CLIENT_NOT_FOUND_MSG));
  }

  @Test
  @DisplayName("Should return error when adding duplicate scope to client")
  public void testAddScopeToClientDuplicateScope() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(SCOPES, List.of(TEST_SCOPE_1));

    // Add scope first time
    Response firstResponse = createClientScope(tenant1, testClientId, requestBody);
    firstResponse.then().statusCode(SC_NO_CONTENT);

    // Act
    Response response = createClientScope(tenant1, testClientId, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(SCOPE_ALREADY_EXISTS))
        .body(MESSAGE, equalTo(SCOPE_ALREADY_EXISTS_MSG));
  }

  @Test
  @DisplayName("Should list client scopes successfully")
  public void testGetClientScopesSuccess() {
    // Arrange
    addScopeToClient(tenant1, testClientId, TEST_SCOPE_1, TEST_SCOPE_2);

    // Act
    Response response = getClientScopes(tenant1, testClientId);

    // Validate
    response.then().statusCode(SC_OK).body(SCOPES, isA(List.class)).body(SCOPES, hasSize(2));

    List<String> scopes = response.jsonPath().getList(SCOPES);
    assertThat(scopes.contains(TEST_SCOPE_1), equalTo(true));
    assertThat(scopes.contains(TEST_SCOPE_2), equalTo(true));
  }

  @Test
  @DisplayName("Should return empty list when client has no scopes")
  public void testGetClientScopesEmptyList() {
    // Arrange - no scopes added

    // Act
    Response response = getClientScopes(tenant1, testClientId);

    // Validate
    response.then().statusCode(SC_OK).body(SCOPES, isA(List.class)).body(SCOPES, hasSize(0));
  }

  @Test
  @DisplayName("Should return error when listing scopes for non-existent client")
  public void testGetClientScopesClientNotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();

    // Act
    Response response = getClientScopes(tenant1, nonExistentClientId);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, containsString(CLIENT_NOT_FOUND_MSG));
  }

  @Test
  @DisplayName("Should remove scope from client successfully")
  public void testRemoveScopeFromClientSuccess() {
    // Arrange
    addScopeToClient(tenant1, testClientId, TEST_SCOPE_2);

    // Act
    Response response = deleteClientScope(tenant1, testClientId, TEST_SCOPE_2);

    // Validate
    response.then().statusCode(SC_NO_CONTENT);
    assertThat(DbUtils.clientScopeExists(tenant1, testClientId, TEST_SCOPE_2), equalTo(false));
  }

  @Test
  @DisplayName("Should return error when removing non-existent scope from client")
  public void testRemoveScopeFromClientScopeNotAssigned() {
    // Arrange - no scope assigned

    // Act
    Response response = deleteClientScope(tenant1, testClientId, TEST_SCOPE_2);

    // Validate
    response.then().statusCode(SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should return error when removing scope from non-existent client")
  public void testRemoveScopeFromClientClientNotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();

    // Act
    Response response = deleteClientScope(tenant1, nonExistentClientId, TEST_SCOPE_2);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, containsString(CLIENT_NOT_FOUND_MSG));
  }

  @Test
  @DisplayName("Should validate scopes is required in request body")
  public void testAddScopeToClientMissingFields() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    // Missing scopes

    // Act
    Response response = createClientScope(tenant1, testClientId, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(SCOPE_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when tenant ID is missing")
  public void testAddScopeToClientMissingTenantId() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(SCOPES, List.of(TEST_SCOPE_1));

    // Act
    Response response = createClientScope(null, testClientId, requestBody);

    // Validate
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should handle multi-tenant isolation for client-scope operations")
  public void testMultiTenantClientScopeIsolation() {
    // Arrange
    String clientIdT2 = createTestClient(tenant2);
    addScopeToClient(tenant2, clientIdT2, TEST_SCOPE_4);

    // Act & Validate
    // Try to access tenant2's client-scope from tenant1
    Response response1 = getClientScopes(tenant1, clientIdT2);
    response1
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, containsString(CLIENT_NOT_FOUND_MSG));

    // Try to remove tenant2's client-scope from tenant1
    Response response2 = deleteClientScope(tenant1, clientIdT2, TEST_SCOPE_4);
    response2
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, containsString(CLIENT_NOT_FOUND_MSG));

    // Verify tenant2 can access its own client-scope
    Response response3 = getClientScopes(tenant2, clientIdT2);
    response3.then().statusCode(SC_OK).body(SCOPES, isA(List.class)).body(SCOPES, hasSize(1));

    List<String> scopes = response3.jsonPath().getList(SCOPES);
    assertThat(scopes.contains(TEST_SCOPE_4), equalTo(true));
  }

  private static String createTestClient() {
    return createTestClient(tenant1);
  }

  private static String createTestClient(String tenantId) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(CLIENT_NAME, TEST_CLIENT_PREFIX + RandomStringUtils.randomAlphanumeric(8));
    requestBody.put(CLIENT_URI, EXAMPLE_COM);
    requestBody.put(CONTACTS, Arrays.asList(ADMIN_EMAIL));
    requestBody.put(GRANT_TYPES, Arrays.asList(AUTHORIZATION_CODE, REFRESH_TOKEN));
    requestBody.put(RESPONSE_TYPES, Arrays.asList(CODE));
    requestBody.put(REDIRECT_URIS, Arrays.asList(EXAMPLE_CALLBACK));
    requestBody.put(LOGO_URI, EXAMPLE_LOGO);
    requestBody.put(POLICY_URI, EXAMPLE_POLICY);
    requestBody.put(SKIP_CONSENT, false);

    Response response = createClient(tenantId, requestBody);
    response.then().statusCode(SC_CREATED);
    return response.jsonPath().getString(CLIENT_ID);
  }

  private void addScopeToClient(String tenantId, String clientId, String... scopes) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(SCOPES, List.of(scopes));

    Response response = createClientScope(tenantId, clientId, requestBody);
    response.then().statusCode(SC_NO_CONTENT);
  }
}
