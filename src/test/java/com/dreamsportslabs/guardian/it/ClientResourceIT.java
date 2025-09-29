package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.ADMIN_EMAIL;
import static com.dreamsportslabs.guardian.Constants.AUTHORIZATION_CODE;
import static com.dreamsportslabs.guardian.Constants.BLANK_STRING;
import static com.dreamsportslabs.guardian.Constants.CALLBACK_1;
import static com.dreamsportslabs.guardian.Constants.CALLBACK_2;
import static com.dreamsportslabs.guardian.Constants.CALLBACK_3;
import static com.dreamsportslabs.guardian.Constants.CLIENTS;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.CLIENT_CREDENTIALS;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NAME;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NAME_BLANK;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NAME_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NOT_FOUND_MSG;
import static com.dreamsportslabs.guardian.Constants.CLIENT_SECRET;
import static com.dreamsportslabs.guardian.Constants.CLIENT_TYPE;
import static com.dreamsportslabs.guardian.Constants.CLIENT_URI;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.CONTACTS;
import static com.dreamsportslabs.guardian.Constants.DEV_EMAIL;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_CALLBACK;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_COM;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_LOGO;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_POLICY;
import static com.dreamsportslabs.guardian.Constants.GRANT_TYPES;
import static com.dreamsportslabs.guardian.Constants.GRANT_TYPES_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.HACKED_NAME;
import static com.dreamsportslabs.guardian.Constants.INVALID_GRANT_TYPE;
import static com.dreamsportslabs.guardian.Constants.INVALID_GRANT_TYPES_MSG;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.INVALID_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.INVALID_RESPONSE_TYPES_MSG;
import static com.dreamsportslabs.guardian.Constants.INVALID_TENANT;
import static com.dreamsportslabs.guardian.Constants.IS_DEFAULT;
import static com.dreamsportslabs.guardian.Constants.LOGO_URI;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.MINIMAL_CLIENT_PREFIX;
import static com.dreamsportslabs.guardian.Constants.MIN_CLIENT_ID_LENGTH;
import static com.dreamsportslabs.guardian.Constants.MIN_SECRET_LENGTH;
import static com.dreamsportslabs.guardian.Constants.NEW_URI_EXAMPLE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.PAGE;
import static com.dreamsportslabs.guardian.Constants.PAGE_SIZE;
import static com.dreamsportslabs.guardian.Constants.PAGE_SIZE_ERROR;
import static com.dreamsportslabs.guardian.Constants.PAGE_VALUE_ERROR;
import static com.dreamsportslabs.guardian.Constants.POLICY_URI;
import static com.dreamsportslabs.guardian.Constants.REDIRECT_URIS;
import static com.dreamsportslabs.guardian.Constants.REDIRECT_URIS_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_TYPES;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_TYPES_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.SUPPORT_EMAIL;
import static com.dreamsportslabs.guardian.Constants.TEST_CLIENT_PREFIX;
import static com.dreamsportslabs.guardian.Constants.UPDATED_CLIENT_NAME;
import static com.dreamsportslabs.guardian.Constants.UPDATED_EXAMPLE_COM;
import static com.dreamsportslabs.guardian.Constants.UPDATED_NAME_ONLY;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.listClients;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.regenerateClientSecret;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateClient;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClientResourceIT {

  public static String tenant1 = "tenant1";
  public static String tenant2 = "tenant2";

  @BeforeEach
  void setUp() {
    // Clean up any existing test data
    DbUtils.cleanupClients(tenant1);
    DbUtils.cleanupClients(tenant2);
  }

  @Test
  @DisplayName("Should create OIDC client successfully")
  public void testCreateClientSuccess() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body(CLIENT_ID, isA(String.class))
        .body(CLIENT_NAME, equalTo(requestBody.get(CLIENT_NAME)))
        .body(CLIENT_URI, equalTo(requestBody.get(CLIENT_URI)))
        .body(CLIENT_SECRET, isA(String.class))
        .body(REDIRECT_URIS, hasSize(1))
        .body("redirect_uris[0]", equalTo(EXAMPLE_CALLBACK))
        .body(CONTACTS, hasSize(1))
        .body("contacts[0]", equalTo(ADMIN_EMAIL))
        .body(GRANT_TYPES, hasSize(2))
        .body("grant_types[0]", equalTo("AUTHORIZATION_CODE"))
        .body("grant_types[1]", equalTo("REFRESH_TOKEN"))
        .body(RESPONSE_TYPES, hasSize(1))
        .body("response_types[0]", equalTo("CODE"))
        .body(LOGO_URI, equalTo(EXAMPLE_LOGO))
        .body(POLICY_URI, equalTo(EXAMPLE_POLICY))
        .body(CLIENT_TYPE, equalTo("third_party"))
        .body(IS_DEFAULT, equalTo(false));
    assertThat(
        response.jsonPath().getString(CLIENT_SECRET).length(),
        greaterThanOrEqualTo(MIN_SECRET_LENGTH));
    assertThat(
        response.jsonPath().getString(CLIENT_ID).length(),
        greaterThanOrEqualTo(MIN_CLIENT_ID_LENGTH));
    assertThat(response.jsonPath().getString(CLIENT_ID), matchesPattern("[A-Za-z0-9-]+"));

    // Verify client exists in database
    String clientId = response.jsonPath().getString(CLIENT_ID);
    assertThat(DbUtils.clientExists(tenant1, clientId), equalTo(true));
    JsonObject resultSet = DbUtils.getClient(tenant1, clientId);
    assertThat(resultSet, notNullValue());
    assertThat(resultSet.getString(CLIENT_NAME), equalTo(requestBody.get(CLIENT_NAME)));
    assertThat(
        resultSet.getString(CLIENT_SECRET), equalTo(response.jsonPath().getString(CLIENT_SECRET)));
    assertThat(resultSet.getString(CLIENT_URI), equalTo(requestBody.get(CLIENT_URI)));
    assertThat(
        resultSet.getString(CLIENT_SECRET).length(), greaterThanOrEqualTo(MIN_SECRET_LENGTH));
    assertThat(resultSet.getString(REDIRECT_URIS), equalTo("[\"" + EXAMPLE_CALLBACK + "\"]"));
    assertThat(resultSet.getString(CONTACTS), equalTo("[\"" + ADMIN_EMAIL + "\"]"));
    assertThat(
        resultSet.getString(GRANT_TYPES), equalTo("[\"AUTHORIZATION_CODE\", \"REFRESH_TOKEN\"]"));
    assertThat(resultSet.getString(RESPONSE_TYPES), equalTo("[\"CODE\"]"));
    assertThat(resultSet.getString(LOGO_URI), equalTo(EXAMPLE_LOGO));
    assertThat(resultSet.getString(POLICY_URI), equalTo(EXAMPLE_POLICY));
    assertThat(resultSet.getString(CLIENT_TYPE), equalTo("third_party"));
    assertThat(resultSet.getBoolean(IS_DEFAULT), equalTo(false));
  }

  @Test
  @DisplayName("Should return error when required fields are missing")
  public void testCreateClientMissingRequiredFields() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("clientUri", EXAMPLE_COM);
    // Missing clientName

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(CLIENT_NAME_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when tenant ID is missing")
  public void testCreateClientMissingTenantId() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();

    // Act
    Response response = createClient(INVALID_TENANT, requestBody);

    // Validate
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should return error when client name already exists for tenant")
  public void testCreateClientDuplicateClientName() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    String clientName = (String) requestBody.get(CLIENT_NAME);

    // Create first client
    Response firstResponse = createClient(tenant1, requestBody);
    firstResponse.then().statusCode(SC_CREATED);

    // Try to create second client with same name
    Map<String, Object> duplicateRequest = createValidClientRequest();
    duplicateRequest.put(CLIENT_NAME, clientName);

    // Act
    Response response = createClient(tenant1, duplicateRequest);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(CLIENT_ALREADY_EXISTS));
  }

  @Test
  @DisplayName("Should get client by ID successfully")
  public void testGetClientSuccess() {
    // Arrange
    String clientId = createTestClient().jsonPath().getString(CLIENT_ID);

    // Act
    Response response = getClient(tenant1, clientId);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(CLIENT_ID, equalTo(clientId))
        .body(CLIENT_NAME, notNullValue())
        .body(CLIENT_SECRET, notNullValue());
  }

  @Test
  @DisplayName("Should return 404 when client not found")
  public void testGetClientNotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();

    // Act
    Response response = getClient(tenant1, nonExistentClientId);

    // Validate
    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR).body(CODE, equalTo(CLIENT_NOT_FOUND));
  }

  @Test
  @DisplayName("Should list clients with pagination")
  public void testGetClientsWithPagination() {
    // Arrange
    createTestClient();
    createTestClient();
    createTestClient();

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(PAGE, "1");
    queryParams.put(PAGE_SIZE, "2");

    // Act
    Response response = listClients(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(CLIENTS, hasSize(2))
        .body(PAGE, equalTo(1))
        .body(PAGE_SIZE, equalTo(2));

    // Validate first page content
    String client1Name = response.jsonPath().getString(CLIENTS + "[0]." + CLIENT_NAME);
    String client2Name = response.jsonPath().getString(CLIENTS + "[1]." + CLIENT_NAME);

    // Arrange - check next page
    queryParams.put(PAGE, "2");
    queryParams.put(PAGE_SIZE, "2");

    // Act
    Response nextPageResponse = listClients(tenant1, queryParams);

    // Validate
    nextPageResponse
        .then()
        .statusCode(SC_OK)
        .body(CLIENTS, hasSize(1))
        .body(PAGE, equalTo(2))
        .body(PAGE_SIZE, equalTo(1));
    String client3Name = nextPageResponse.jsonPath().getString(CLIENTS + "[0]." + CLIENT_NAME);
    assertThat(client3Name, not(equalTo(client1Name)));
    assertThat(client3Name, not(equalTo(client2Name)));
  }

  @Test
  @DisplayName("Should update client successfully")
  public void testUpdateClientSuccess() {
    // Arrange
    Response createResponse = createTestClient();
    String clientId = createResponse.jsonPath().getString(CLIENT_ID);

    Map<String, Object> updateRequest = copyClientResponseToRequestBody(createResponse);
    updateRequest.put(CLIENT_NAME, UPDATED_CLIENT_NAME);
    updateRequest.put(CLIENT_URI, UPDATED_EXAMPLE_COM);

    // Act
    Response response = updateClient(tenant1, clientId, updateRequest);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(CLIENT_NAME, equalTo(UPDATED_CLIENT_NAME))
        .body(CLIENT_URI, equalTo(UPDATED_EXAMPLE_COM));
  }

  @Test
  @DisplayName("Should delete client successfully")
  public void testDeleteClientSuccess() {
    // Arrange
    String clientId = createTestClient().jsonPath().getString(CLIENT_ID);

    // Act
    Response response = deleteClient(tenant1, clientId);

    // Validate
    response.then().statusCode(SC_NO_CONTENT);
    assertThat(DbUtils.clientExists(tenant1, clientId), equalTo(false));
  }

  @Test
  @DisplayName("Should return 400 when deleting non-existent client")
  public void testDeleteClientNotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();

    // Act
    Response response = deleteClient(tenant1, nonExistentClientId);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(CLIENT_NOT_FOUND_MSG));
  }

  @Test
  @DisplayName("Should validate invalid grant types as error")
  public void testCreateClientInvalidGrantTypes() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put(GRANT_TYPES, Arrays.asList(INVALID_GRANT_TYPE));

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(INVALID_GRANT_TYPES_MSG));
  }

  @Test
  @DisplayName("Should validate invalid response types as error")
  public void testCreateClientInvalidResponseTypes() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put(RESPONSE_TYPES, Arrays.asList(INVALID_RESPONSE_TYPE));

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(INVALID_RESPONSE_TYPES_MSG));
  }

  @Test
  @DisplayName("Should generate secure client secret with minimum length")
  public void testCreateClientGeneratesSecureSecret() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body(CLIENT_SECRET, isA(String.class))
        .body(CLIENT_SECRET, matchesPattern("[A-Za-z0-9]{32,}"));

    String clientSecret = response.jsonPath().getString(CLIENT_SECRET);
    assertThat(clientSecret.length(), greaterThanOrEqualTo(MIN_SECRET_LENGTH));
  }

  @Test
  @DisplayName("Should handle multi-tenant isolation at service layer")
  public void testMultiTenantServiceIsolation() {
    // Arrange
    String clientIdT1 = createTestClientForTenant(tenant1).jsonPath().getString(CLIENT_ID);
    String clientIdT2 = createTestClientForTenant(tenant2).jsonPath().getString(CLIENT_ID);

    // Act & Validate
    // Verify tenant1 cannot access tenant2's client
    Response response1 = getClient(tenant1, clientIdT2);
    response1.then().statusCode(SC_NOT_FOUND).rootPath(ERROR).body(CODE, equalTo(CLIENT_NOT_FOUND));

    // Verify tenant2 cannot access tenant1's client
    Response response2 = getClient(tenant2, clientIdT1);
    response2.then().statusCode(SC_NOT_FOUND).rootPath(ERROR).body(CODE, equalTo(CLIENT_NOT_FOUND));

    // Verify each tenant can access their own client
    Response response3 = getClient(tenant1, clientIdT1);
    response3.then().statusCode(SC_OK).body(CLIENT_ID, equalTo(clientIdT1));

    Response response4 = getClient(tenant2, clientIdT2);
    response4.then().statusCode(SC_OK).body(CLIENT_ID, equalTo(clientIdT2));
  }

  @Test
  @DisplayName("Should return error when grant_types is empty list")
  public void testCreateClientEmptyGrantTypes() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put(GRANT_TYPES, Arrays.asList());

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(GRANT_TYPES_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when redirect_uris is empty list")
  public void testCreateClientEmptyRedirectUris() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put(REDIRECT_URIS, Arrays.asList());

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(REDIRECT_URIS_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when response_types is empty list")
  public void testCreateClientEmptyResponseTypes() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put(RESPONSE_TYPES, Arrays.asList());

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(RESPONSE_TYPES_REQUIRED));
  }

  @Test
  @DisplayName("Should handle partial update with only client_name")
  public void testUpdateClientPartialClientName() {
    // Arrange
    Response createResponse = createTestClient();
    String clientId = createResponse.jsonPath().getString(CLIENT_ID);
    String originalUri = createResponse.jsonPath().getString(CLIENT_URI);

    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put(CLIENT_NAME, UPDATED_NAME_ONLY);

    // Act
    Response response = updateClient(tenant1, clientId, updateRequest);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(CLIENT_NAME, equalTo(UPDATED_NAME_ONLY))
        .body(CLIENT_URI, equalTo(originalUri)); // Should preserve original

    JsonObject resultSet = DbUtils.getClient(tenant1, clientId);
    assertThat(resultSet, notNullValue());
    assertThat(resultSet.getString(CLIENT_NAME), equalTo(UPDATED_NAME_ONLY));
  }

  @Test
  @DisplayName("Should handle partial update with only client_uri")
  public void testUpdateClientPartialClientUri() {
    // Arrange
    Response createResponse = createTestClient();
    String clientId = createResponse.jsonPath().getString(CLIENT_ID);
    String originalName = createResponse.jsonPath().getString(CLIENT_NAME);

    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put(CLIENT_URI, NEW_URI_EXAMPLE);

    // Act
    Response response = updateClient(tenant1, clientId, updateRequest);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(CLIENT_NAME, equalTo(originalName)) // Should preserve original
        .body(CLIENT_URI, equalTo(NEW_URI_EXAMPLE));

    JsonObject resultSet = DbUtils.getClient(tenant1, clientId);
    assertThat(resultSet, notNullValue());
    assertThat(resultSet.getString(CLIENT_URI), equalTo(NEW_URI_EXAMPLE));
  }

  @Test
  @DisplayName("Should return error when updating non-existent client")
  public void testUpdateClientNotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();
    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put(CLIENT_NAME, UPDATED_NAME_ONLY);

    // Act
    Response response = updateClient(tenant1, nonExistentClientId, updateRequest);

    // Validate
    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo(CLIENT_NOT_FOUND))
        .body(MESSAGE, equalTo(CLIENT_NOT_FOUND_MSG));
  }

  @Test
  @DisplayName("Should return error when updating with blank client_name")
  public void testUpdateClientBlankClientName() {
    // Arrange
    Response createResponse = createTestClient();
    String clientId = createResponse.jsonPath().getString(CLIENT_ID);

    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put(CLIENT_NAME, BLANK_STRING); // Blank string

    // Act
    Response response = updateClient(tenant1, clientId, updateRequest);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(CLIENT_NAME_BLANK));
  }

  @Test
  @DisplayName("Should preserve client_secret during update")
  public void testUpdateClientPreservesSecret() {
    // Arrange
    Response createResponse = createTestClient();
    String clientId = createResponse.jsonPath().getString(CLIENT_ID);
    String originalSecret = createResponse.jsonPath().getString(CLIENT_SECRET);

    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put(CLIENT_NAME, UPDATED_NAME_ONLY);

    // Act
    Response response = updateClient(tenant1, clientId, updateRequest);

    // Validate
    response.then().statusCode(SC_OK).body(CLIENT_SECRET, equalTo(originalSecret));
  }

  @Test
  @DisplayName("Should handle update with no fields provided")
  public void testUpdateClientNoFieldsProvided() {
    // Arrange
    Response createResponse = createTestClient();
    String clientId = createResponse.jsonPath().getString(CLIENT_ID);
    String originalName = createResponse.jsonPath().getString(CLIENT_NAME);
    String originalUri = createResponse.jsonPath().getString(CLIENT_URI);
    String originalSecret = createResponse.jsonPath().getString(CLIENT_SECRET);

    Map<String, Object> updateRequest = new HashMap<>(); // Empty request body

    // Act
    Response response = updateClient(tenant1, clientId, updateRequest);

    // Validate - Should return the client unchanged or appropriate error
    // Assuming the API should return an error for empty update requests
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE))
        .body(MESSAGE, equalTo(ERROR_MSG_NO_FIELDS_TO_UPDATE));

    // Verify client data remains unchanged in database
    JsonObject resultSet = DbUtils.getClient(tenant1, clientId);
    assertThat(resultSet, notNullValue());
    assertThat(resultSet.getString(CLIENT_NAME), equalTo(originalName));
    assertThat(resultSet.getString(CLIENT_URI), equalTo(originalUri));
    assertThat(resultSet.getString(CLIENT_SECRET), equalTo(originalSecret));
  }

  @Test
  @DisplayName("Should list clients with default pagination")
  public void testGetClientsDefaultPagination() {
    // Arrange
    createTestClient();
    createTestClient();

    // Act
    Response response = listClients(tenant1, new HashMap<>());

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(CLIENTS, hasSize(2))
        .body(PAGE, equalTo(1))
        .body(PAGE_SIZE, equalTo(2));
  }

  @Test
  @DisplayName("Should return empty list when no clients exist")
  public void testGetClientsEmptyList() {
    // Arrange - no clients created

    // Act
    Response response = listClients(tenant1, new HashMap<>());

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(CLIENTS, hasSize(0))
        .body(PAGE, equalTo(1))
        .body(PAGE_SIZE, equalTo(0));
  }

  @Test
  @DisplayName("Should return error for invalid pagination parameters")
  public void testGetClientsInvalidPagination() {
    // Arrange
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(PAGE, "0"); // Invalid page
    queryParams.put(PAGE_SIZE, "10");

    // Act
    Response response = listClients(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(PAGE_VALUE_ERROR));
  }

  @Test
  @DisplayName("Should return error for pageSize exceeding limit")
  public void testGetClientsPageSizeExceedsLimit() {
    // Arrange
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(PAGE, "1");
    queryParams.put(PAGE_SIZE, "101"); // Exceeds limit of 100

    // Act
    Response response = listClients(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(PAGE_SIZE_ERROR));
  }

  @Test
  @DisplayName("Should regenerate client secret successfully")
  public void testRegenerateClientSecretSuccess() {
    // Arrange
    Response createResponse = createTestClient();
    String clientId = createResponse.jsonPath().getString(CLIENT_ID);
    String originalSecret = createResponse.jsonPath().getString(CLIENT_SECRET);

    // Act
    Response response = regenerateClientSecret(tenant1, clientId);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(CLIENT_SECRET, isA(String.class))
        .body(CLIENT_SECRET, matchesPattern("[A-Za-z0-9]{32}"));

    String newSecret = response.jsonPath().getString(CLIENT_SECRET);
    assertThat(newSecret, not(equalTo(originalSecret)));
    assertThat(newSecret.length(), equalTo(MIN_SECRET_LENGTH));
    JsonObject resultSet = DbUtils.getClient(tenant1, clientId);
    assertThat(resultSet, notNullValue());
    assertThat(resultSet.getString(CLIENT_SECRET), equalTo(newSecret));
  }

  @Test
  @DisplayName("Should return error when regenerating secret for non-existent client")
  public void testRegenerateClientSecretNotFound() {
    // Arrange
    String nonExistentClientId = UUID.randomUUID().toString();

    // Act
    Response response = regenerateClientSecret(tenant1, nonExistentClientId);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(CLIENT_NOT_FOUND_MSG));
  }

  @Test
  @DisplayName("Should handle multi-tenant isolation for regenerate secret")
  public void testRegenerateClientSecretMultiTenantIsolation() {
    // Arrange
    String clientIdT1 = createTestClientForTenant(tenant1).jsonPath().getString(CLIENT_ID);

    // Act
    Response response = regenerateClientSecret(tenant2, clientIdT1);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(CLIENT_NOT_FOUND_MSG));
  }

  @Test
  @DisplayName("Should handle multi-tenant isolation for update")
  public void testUpdateClientMultiTenantIsolation() {
    // Arrange
    String clientIdT1 = createTestClientForTenant(tenant1).jsonPath().getString(CLIENT_ID);
    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put(CLIENT_NAME, HACKED_NAME);

    // Act
    Response response = updateClient(tenant2, clientIdT1, updateRequest);

    // Validate
    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo(CLIENT_NOT_FOUND))
        .body(MESSAGE, equalTo(CLIENT_NOT_FOUND_MSG));
  }

  @Test
  @DisplayName("Should handle multi-tenant isolation for delete")
  public void testDeleteClientMultiTenantIsolation() {
    // Arrange
    String clientIdT1 = createTestClientForTenant(tenant1).jsonPath().getString(CLIENT_ID);

    // Act
    Response response = deleteClient(tenant2, clientIdT1);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_REQUEST))
        .body(MESSAGE, equalTo(CLIENT_NOT_FOUND_MSG));
  }

  @Test
  @DisplayName("Should create client with all optional fields")
  public void testCreateClientWithAllOptionalFields() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put(LOGO_URI, EXAMPLE_LOGO);
    requestBody.put(POLICY_URI, EXAMPLE_POLICY);
    requestBody.put(CLIENT_TYPE, "first_party");
    requestBody.put(IS_DEFAULT, true);

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body(LOGO_URI, equalTo(EXAMPLE_LOGO))
        .body(POLICY_URI, equalTo(EXAMPLE_POLICY))
        .body(CLIENT_TYPE, equalTo("first_party"))
        .body(IS_DEFAULT, equalTo(true));
  }

  @Test
  @DisplayName("Should create client with minimal required fields only")
  public void testCreateClientMinimalFields() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(CLIENT_NAME, MINIMAL_CLIENT_PREFIX + RandomStringUtils.randomAlphanumeric(8));
    requestBody.put(GRANT_TYPES, Arrays.asList(AUTHORIZATION_CODE));
    requestBody.put(RESPONSE_TYPES, Arrays.asList(CODE));
    requestBody.put(REDIRECT_URIS, Arrays.asList(EXAMPLE_CALLBACK));

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body(CLIENT_ID, isA(String.class))
        .body(CLIENT_SECRET, isA(String.class))
        .body(CLIENT_NAME, equalTo(requestBody.get(CLIENT_NAME)))
        .body(CLIENT_TYPE, equalTo("third_party")) // Default value
        .body(IS_DEFAULT, equalTo(false)); // Default value
  }

  @Test
  @DisplayName("Should validate all supported grant types")
  public void testCreateClientAllGrantTypes() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put(
        GRANT_TYPES, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED).body(GRANT_TYPES, hasSize(3));
  }

  @Test
  @DisplayName("Should handle multiple redirect URIs")
  public void testCreateClientMultipleRedirectUris() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put(REDIRECT_URIS, Arrays.asList(CALLBACK_1, CALLBACK_2, CALLBACK_3));

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED).body(REDIRECT_URIS, hasSize(3));
  }

  @Test
  @DisplayName("Should handle multiple contacts")
  public void testCreateClientMultipleContacts() {
    // Arrange
    Map<String, Object> requestBody = createValidClientRequest();
    requestBody.put(CONTACTS, Arrays.asList(ADMIN_EMAIL, SUPPORT_EMAIL, DEV_EMAIL));

    // Act
    Response response = createClient(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED).body(CONTACTS, hasSize(3));
  }

  private Map<String, Object> createValidClientRequest() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(CLIENT_NAME, TEST_CLIENT_PREFIX + RandomStringUtils.randomAlphanumeric(8));
    requestBody.put(CLIENT_URI, EXAMPLE_COM);
    requestBody.put(CONTACTS, Arrays.asList(ADMIN_EMAIL));
    requestBody.put(GRANT_TYPES, Arrays.asList(AUTHORIZATION_CODE, REFRESH_TOKEN));
    requestBody.put(RESPONSE_TYPES, Arrays.asList(CODE));
    requestBody.put(REDIRECT_URIS, Arrays.asList(EXAMPLE_CALLBACK));
    requestBody.put(LOGO_URI, EXAMPLE_LOGO);
    requestBody.put(POLICY_URI, EXAMPLE_POLICY);
    requestBody.put(CLIENT_TYPE, "third_party");
    requestBody.put(IS_DEFAULT, false);
    return requestBody;
  }

  private Map<String, Object> copyClientResponseToRequestBody(Response clientResponse) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(CLIENT_NAME, clientResponse.jsonPath().getString(CLIENT_NAME));
    requestBody.put(CLIENT_URI, clientResponse.jsonPath().getString(CLIENT_URI));
    requestBody.put(CONTACTS, clientResponse.jsonPath().getList(CONTACTS));
    requestBody.put(GRANT_TYPES, clientResponse.jsonPath().getList(GRANT_TYPES));
    requestBody.put(RESPONSE_TYPES, clientResponse.jsonPath().getList(RESPONSE_TYPES));
    requestBody.put(REDIRECT_URIS, clientResponse.jsonPath().getList(REDIRECT_URIS));
    requestBody.put(LOGO_URI, clientResponse.jsonPath().getString(LOGO_URI));
    requestBody.put(POLICY_URI, clientResponse.jsonPath().getString(POLICY_URI));
    requestBody.put(CLIENT_TYPE, clientResponse.jsonPath().getString(CLIENT_TYPE));
    requestBody.put(IS_DEFAULT, clientResponse.jsonPath().getBoolean(IS_DEFAULT));
    requestBody.put(CLIENT_ID, clientResponse.jsonPath().getString(CLIENT_ID));
    requestBody.put(CLIENT_SECRET, clientResponse.jsonPath().getString(CLIENT_SECRET));
    return requestBody;
  }

  private Response createTestClient() {
    return createTestClientForTenant(tenant1);
  }

  private Response createTestClientForTenant(String tenantId) {
    Map<String, Object> requestBody = createValidClientRequest();
    Response response = createClient(tenantId, requestBody);
    response.then().statusCode(SC_CREATED);
    return response;
  }
}
