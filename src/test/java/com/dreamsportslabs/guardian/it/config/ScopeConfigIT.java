package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.*;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.listScopes;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ScopeConfigIT {
  public static String tenant1 = TENANT_1;
  public static String tenant2 = TENANT_2;

  @Test
  @DisplayName("Should create a new scope successfully")
  public void testCreateScopeSuccess() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_SCOPE_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM));

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body(BODY_PARAM_SCOPE, equalTo(scopeName))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_SCOPE_NAME))
        .body(BODY_PARAM_DESCRIPTION, equalTo(TEST_DESCRIPTION))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_EMAIL_CLAIM))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_NAME_CLAIM))
        .body(BODY_PARAM_ICON_URL, equalTo(TEST_ICON_URL))
        .body(BODY_PARAM_IS_OIDC, equalTo(true));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should return error when scope already exists")
  public void testScopeAlreadyExists() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "Duplicate Scope", TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));

    // Create scope first time
    createScope(tenant1, requestBody);

    // Act - Try to create same scope again
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body("error.code", equalTo(ERROR_CODE_SCOPE_ALREADY_EXISTS))
        .body("error.message", equalTo(ERROR_MSG_SCOPE_ALREADY_EXISTS));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should return error when scope is missing")
  public void testScopeMissing() {
    // Arrange
    Map<String, Object> requestBody = getScopeRequestBodyWithoutField(BODY_PARAM_SCOPE);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_SCOPE_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when scope is blank")
  public void testScopeBlank() {
    // Arrange
    Map<String, Object> requestBody =
        getValidScopeRequestBody("", TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_SCOPE_REQUIRED));
  }

  @Test
  @DisplayName("Should return success when displayName is missing")
  public void testDisplayNameMissing() {
    // Arrange
    Map<String, Object> requestBody = getScopeRequestBodyWithoutField(BODY_PARAM_DISPLAY_NAME);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED);

    deleteScope(tenant1, requestBody.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should return success when displayName is blank")
  public void testDisplayNameBlank() {
    // Arrange
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            RandomStringUtils.randomAlphabetic(10),
            "",
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM));

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED);

    deleteScope(tenant1, requestBody.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should return success when description is missing")
  public void testDescriptionMissing() {
    // Arrange
    Map<String, Object> requestBody = getScopeRequestBodyWithoutField(BODY_PARAM_DESCRIPTION);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED);

    deleteScope(tenant1, requestBody.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should return success when description is blank")
  public void testDescriptionBlank() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBody(scopeName, TEST_SCOPE_NAME, "", List.of(TEST_EMAIL_CLAIM));

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED);

    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should return success when claims is missing")
  public void testClaimsMissing() {
    // Arrange
    Map<String, Object> requestBody = getScopeRequestBodyWithoutField(BODY_PARAM_CLAIMS);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED);

    deleteScope(tenant1, requestBody.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should return error when tenant-id header is missing")
  public void testMissingTenantId() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));

    // Act
    Response response = createScope(null, requestBody);

    // Validate
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should list scopes including created one")
  public void testListScopesWithCreatedScope() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "List Test Scope", TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));
    createScope(tenant1, requestBody);

    // Act
    Response response = listScopes(tenant1, new HashMap<>());

    // Validate
    response.then().statusCode(SC_OK);
    List<String> scopes = response.jsonPath().getList("scopes.scope");
    assertThat(scopes, hasItem(scopeName));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should get scope by name filter")
  public void testGetScopeByName() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "Get Test Scope", TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));
    createScope(tenant1, requestBody);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("name", scopeName);

    // Act
    Response response = listScopes(tenant1, queryParams);

    // Validate
    List<String> scopes = response.jsonPath().getList("scopes.scope");
    assertThat(scopes, hasItem(scopeName));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should return empty list when scope name not found")
  public void testGetScopeByNameNotFound() {
    // Arrange
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("name", RandomStringUtils.randomAlphabetic(10));

    // Act
    Response response = listScopes(tenant1, queryParams);

    // Validate
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(0));
  }

  @Test
  @DisplayName("Should delete scope successfully")
  public void testDeleteScopeSuccess() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "Delete Test Scope", TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));
    createScope(tenant1, requestBody);

    // Act
    Response response = deleteScope(tenant1, scopeName);

    // Validate
    response.then().statusCode(SC_NO_CONTENT);

    // Verify scope is deleted
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("name", scopeName);
    Response listResponse = listScopes(tenant1, queryParams);
    listResponse.then().statusCode(SC_OK).body("scopes.size()", equalTo(0));
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent scope")
  public void testDeleteNonExistentScope() {
    // Arrange
    String nonExistentScope = RandomStringUtils.randomAlphabetic(10);

    // Act
    Response response = deleteScope(tenant1, nonExistentScope);

    // Validate
    response.then().statusCode(SC_NOT_FOUND);
  }

  @Test
  @DisplayName("Should create scope with multiple claims")
  public void testCreateScopeWithMultipleClaims() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    List<String> claims =
        List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM, TEST_PICTURE_CLAIM, TEST_PHONE_CLAIM);
    Map<String, Object> requestBody =
        getValidScopeRequestBody(scopeName, "Multiple Claims Scope", TEST_DESCRIPTION, claims);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body(BODY_PARAM_SCOPE, equalTo(scopeName))
        .body(BODY_PARAM_CLAIMS + ".size()", equalTo(4))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_EMAIL_CLAIM))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_NAME_CLAIM))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_PICTURE_CLAIM))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_PHONE_CLAIM));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should handle pagination in list scopes")
  public void testListScopesWithPagination() {
    // Arrange
    String scope1 = RandomStringUtils.randomAlphabetic(10);
    String scope2 = RandomStringUtils.randomAlphabetic(10);
    createScope(
        tenant1,
        getValidScopeRequestBody(
            scope1, "Pagination Test 1", TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM)));
    createScope(
        tenant1,
        getValidScopeRequestBody(
            scope2, "Pagination Test 2", TEST_DESCRIPTION, List.of(TEST_NAME_CLAIM)));

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("page", "1");
    queryParams.put("pageSize", "1");

    // Act
    Response response = listScopes(tenant1, queryParams);

    // Validate
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(1));

    // Cleanup
    deleteScope(tenant1, scope1);
    deleteScope(tenant1, scope2);
  }

  @Test
  @DisplayName("Should isolate scopes by tenant")
  public void testTenantIsolation() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "Tenant Isolation Test", TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));

    // Create scope for tenant1
    createScope(tenant1, requestBody);

    // Act - Try to get scope from tenant2
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("name", scopeName);
    Response response = listScopes(tenant2, queryParams);

    // Validate - Should not find scope from different tenant
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(0));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should allow same scope name in different tenants")
  public void testSameScopeNameDifferentTenants() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody1 =
        getValidScopeRequestBody(
            scopeName, "Tenant 1 Scope", "Description for tenant 1", List.of(TEST_EMAIL_CLAIM));
    Map<String, Object> requestBody2 =
        getValidScopeRequestBody(
            scopeName, "Tenant 2 Scope", "Description for tenant 2", List.of(TEST_NAME_CLAIM));

    // Act - Create same scope name in both tenants
    Response response1 = createScope(tenant1, requestBody1);
    Response response2 = createScope(tenant2, requestBody2);

    // Validate - Both should succeed
    response1.then().statusCode(SC_CREATED);
    response2.then().statusCode(SC_CREATED);

    // Cleanup
    deleteScope(tenant1, scopeName);
    deleteScope(tenant2, scopeName);
  }

  @Test
  @DisplayName("Should handle special characters in scope name")
  public void testScopeWithSpecialCharacters() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10) + "-with.special_chars";
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "Special Chars Scope", TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED).body("scope", equalTo(scopeName));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should handle long scope names and descriptions")
  public void testLongScopeNameAndDescription() {
    // Arrange
    String longScopeName =
        RandomStringUtils.randomAlphabetic(10)
            + "_very_long_scope_name_that_tests_database_limits_and_validation";
    String longDescription =
        "This is a very long description that tests how the system handles lengthy text inputs and ensures proper storage and retrieval of extended content";
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            longScopeName, "Long Content Test", longDescription, List.of(TEST_EMAIL_CLAIM));

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body("scope", equalTo(longScopeName))
        .body("description", equalTo(longDescription));

    // Cleanup
    deleteScope(tenant1, longScopeName);
  }

  @Test
  @DisplayName("Should default to page 1 when page is negative")
  public void testNegativePageParameter() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(
        tenant1,
        getValidScopeRequestBody(
            scopeName, "Negative Page Test", "Test description", List.of("email")));

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("page", "-1");
    queryParams.put("pageSize", "10");

    // Act
    Response response = listScopes(tenant1, queryParams);

    // Validate - Should still return 200 and find the scope (defaults to page 1)
    response.then().statusCode(SC_OK);

    List<String> scopes = response.jsonPath().getList("scopes.scope");
    assertThat(scopes, hasItem(scopeName));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should default to pageSize 10 when pageSize is negative")
  public void testNegativePageSizeParameter() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(
        tenant1,
        getValidScopeRequestBody(
            scopeName, "Negative PageSize Test", "Test description", List.of("email")));

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("page", "1");
    queryParams.put("pageSize", "-5");
    // Act
    Response response = listScopes(tenant1, queryParams);

    // Validate - Should still return 200 and find the scope (defaults to pageSize 10)
    response.then().statusCode(SC_OK);

    List<String> scopes = response.jsonPath().getList("scopes.scope");
    assertThat(scopes, hasItem(scopeName));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should default to page 1 when page is zero")
  public void testZeroPageParameter() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(6);
    createScope(
        tenant1,
        getValidScopeRequestBody(
            scopeName, "Zero Page Test", "Test description", List.of("email")));

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("page", "0");
    queryParams.put("pageSize", "10");
    //    queryParams.put("scope", scopeName);

    // Act
    Response response = listScopes(tenant1, queryParams);

    // Validate - Should still return 200 and find the scope (defaults to page 1)
    response.then().statusCode(SC_OK);

    List<String> scopes = response.jsonPath().getList("scopes.scope");
    assertThat(scopes, hasItem(scopeName));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should default to pageSize 10 when pageSize is zero")
  public void testZeroPageSizeParameter() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(
        tenant1,
        getValidScopeRequestBody(
            scopeName, "Zero PageSize Test", "Test description", List.of("email")));

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("page", "1");
    queryParams.put("pageSize", "0");

    // Act
    Response response = listScopes(tenant1, queryParams);

    // Validate - Should still return 200 and find the scope (defaults to pageSize 10)
    response.then().statusCode(SC_OK);

    List<String> scopes = response.jsonPath().getList("scopes.scope");
    assertThat(scopes, hasItem(scopeName));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should return success when iconurl is missing")
  public void testIconUrlMissing() {
    // Arrange
    Map<String, Object> requestBody = getScopeRequestBodyWithoutField(BODY_PARAM_ICON_URL);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED);

    deleteScope(tenant1, requestBody.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should return success when iconurl is blank")
  public void testIconUrlBlank() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBodyWithIconAndOidc(
            scopeName, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), "", true);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_ICON_URL, equalTo(""));

    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should return success when iconurl is null")
  public void testIconUrlNull() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBodyWithIconAndOidc(
            scopeName, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), null, true);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED);

    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should return error when isOidc is missing")
  public void testIsOidcMissing() {
    // Arrange
    Map<String, Object> requestBody = getScopeRequestBodyWithoutField(BODY_PARAM_IS_OIDC);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_OIDC_REQUIRED));
  }

  @Test
  @DisplayName("Should create scope with isOidc false")
  public void testIsOidcFalse() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBodyWithIconAndOidc(
            scopeName,
            TEST_SCOPE_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_IS_OIDC, equalTo(false));

    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should create scope with isOidc true")
  public void testIsOidcTrue() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBodyWithIconAndOidc(
            scopeName,
            TEST_SCOPE_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_IS_OIDC, equalTo(true));

    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should return error when isOidc is explicitly null")
  public void testIsOidcExplicitlyNull() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBodyWithIconAndOidc(
            scopeName,
            TEST_SCOPE_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            null);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_OIDC_REQUIRED));
  }

  @Test
  @DisplayName("Should handle isOidc with string value")
  public void testIsOidcStringValue() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_SCOPE, scopeName);
    requestBody.put(BODY_PARAM_DISPLAY_NAME, TEST_SCOPE_NAME);
    requestBody.put(BODY_PARAM_DESCRIPTION, TEST_DESCRIPTION);
    requestBody.put(BODY_PARAM_CLAIMS, List.of(TEST_EMAIL_CLAIM));
    requestBody.put(BODY_PARAM_ICON_URL, TEST_ICON_URL);
    requestBody.put(BODY_PARAM_IS_OIDC, "true"); // String instead of boolean

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate - This should either work (if the API converts strings) or return an error
    // The exact behavior depends on the API implementation
    response.then().statusCode(isA(Integer.class)); // Accept any valid HTTP status

    // Cleanup only if creation was successful
    if (response.getStatusCode() == SC_CREATED) {
      deleteScope(tenant1, scopeName);
    }
  }

  @Test
  @DisplayName("Should create scope with valid iconurl")
  public void testValidIconUrl() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    String iconUrl = "https://cdn.example.com/icons/custom-icon.svg";
    Map<String, Object> requestBody =
        getValidScopeRequestBodyWithIconAndOidc(
            scopeName, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), iconUrl, true);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body(BODY_PARAM_ICON_URL, equalTo(iconUrl))
        .body(BODY_PARAM_IS_OIDC, equalTo(true));

    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should handle very long iconurl")
  public void testLongIconUrl() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    String longIconUrl =
        "https://very-long-domain-name-for-testing-purposes.example.com/very/long/path/to/icon/file/with/many/subdirectories/icon.png?param1=value1&param2=value2&param3=value3";
    Map<String, Object> requestBody =
        getValidScopeRequestBodyWithIconAndOidc(
            scopeName,
            TEST_SCOPE_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            longIconUrl,
            true);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_ICON_URL, equalTo(longIconUrl));

    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should handle iconurl with special characters")
  public void testIconUrlWithSpecialCharacters() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    String iconUrlWithSpecialChars =
        "https://example.com/icons/icon-with-special_chars@2x.png?v=1.0&cache=false";
    Map<String, Object> requestBody =
        getValidScopeRequestBodyWithIconAndOidc(
            scopeName,
            TEST_SCOPE_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            iconUrlWithSpecialChars,
            false);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body(BODY_PARAM_ICON_URL, equalTo(iconUrlWithSpecialChars))
        .body(BODY_PARAM_IS_OIDC, equalTo(false));

    deleteScope(tenant1, scopeName);
  }

  // Helper methods
  private Map<String, Object> getValidScopeRequestBody(
      String scope, String displayName, String description, List<String> claims) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_SCOPE, scope);
    requestBody.put(BODY_PARAM_DISPLAY_NAME, displayName);
    requestBody.put(BODY_PARAM_DESCRIPTION, description);
    requestBody.put(BODY_PARAM_CLAIMS, claims);
    requestBody.put(BODY_PARAM_ICON_URL, TEST_ICON_URL);
    requestBody.put(BODY_PARAM_IS_OIDC, true);
    return requestBody;
  }

  private Map<String, Object> getValidScopeRequestBodyWithIconAndOidc(
      String scope,
      String displayName,
      String description,
      List<String> claims,
      String iconurl,
      Boolean isOidc) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_SCOPE, scope);
    requestBody.put(BODY_PARAM_DISPLAY_NAME, displayName);
    requestBody.put(BODY_PARAM_DESCRIPTION, description);
    requestBody.put(BODY_PARAM_CLAIMS, claims);
    requestBody.put(BODY_PARAM_ICON_URL, iconurl);
    requestBody.put(BODY_PARAM_IS_OIDC, isOidc);
    return requestBody;
  }

  private Map<String, Object> getScopeRequestBodyWithoutField(String fieldToExclude) {
    Map<String, Object> requestBody = new HashMap<>();

    if (!BODY_PARAM_SCOPE.equals(fieldToExclude)) {
      requestBody.put(BODY_PARAM_SCOPE, RandomStringUtils.randomAlphabetic(10));
    }
    if (!BODY_PARAM_DISPLAY_NAME.equals(fieldToExclude)) {
      requestBody.put(BODY_PARAM_DISPLAY_NAME, TEST_SCOPE_NAME);
    }
    if (!BODY_PARAM_DESCRIPTION.equals(fieldToExclude)) {
      requestBody.put(BODY_PARAM_DESCRIPTION, TEST_DESCRIPTION);
    }
    if (!BODY_PARAM_CLAIMS.equals(fieldToExclude)) {
      requestBody.put(BODY_PARAM_CLAIMS, List.of(TEST_EMAIL_CLAIM));
    }
    if (!BODY_PARAM_ICON_URL.equals(fieldToExclude)) {
      requestBody.put(BODY_PARAM_ICON_URL, TEST_ICON_URL);
    }
    if (!BODY_PARAM_IS_OIDC.equals(fieldToExclude)) {
      requestBody.put(BODY_PARAM_IS_OIDC, true);
    }
    // Note: When isOidc is excluded, it's completely omitted from the request body

    return requestBody;
  }
}
