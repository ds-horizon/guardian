package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.ERROR;
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
import static org.hamcrest.MatcherAssert.assertThat;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ScopeConfigIT {
  public static String tenant1 = "tenant1";
  public static String tenant2 = "tenant2";

  @Test
  @DisplayName("Should create a new scope successfully")
  public void testCreateScopeSuccess() {
    // Arrange
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            "test_scope_success", "Test Scope", "Test description", List.of("email", "name"));

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body("scope", equalTo("test_scope_success"))
        .body("displayName", equalTo("Test Scope"))
        .body("description", equalTo("Test description"))
        .body("claims", hasItem("email"))
        .body("claims", hasItem("name"));

    // Cleanup
    deleteScope(tenant1, "test_scope_success");
  }

  @Test
  @DisplayName("Should return error when scope already exists")
  public void testScopeAlreadyExists() {
    // Arrange
    String scopeName = "duplicate_scope";
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "Duplicate Scope", "Test description", List.of("email"));

    // Create scope first time
    createScope(tenant1, requestBody);

    // Act - Try to create same scope again
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body("error.code", equalTo("scope_already_exists"))
        .body("error.message", equalTo("Scope already exists for tenant"));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should return error when scope is missing")
  public void testScopeMissing() {
    // Arrange
    Map<String, Object> requestBody = getScopeRequestBodyWithoutField("scope");

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("message", containsString("scope is required"));
  }

  @Test
  @DisplayName("Should return error when scope is blank")
  public void testScopeBlank() {
    // Arrange
    Map<String, Object> requestBody =
        getValidScopeRequestBody("", "Test Scope", "Test description", List.of("email"));

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("message", containsString("scope is required"));
  }

  @Test
  @DisplayName("Should return error when displayName is missing")
  public void testDisplayNameMissing() {
    // Arrange
    Map<String, Object> requestBody = getScopeRequestBodyWithoutField("displayName");

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("message", containsString("display_name is required"));
  }

  @Test
  @DisplayName("Should return error when displayName is blank")
  public void testDisplayNameBlank() {
    // Arrange
    Map<String, Object> requestBody =
        getValidScopeRequestBody("test_scope", "", "Test description", List.of("email"));

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("message", containsString("display_name is required"));
  }

  @Test
  @DisplayName("Should return error when description is missing")
  public void testDescriptionMissing() {
    // Arrange
    Map<String, Object> requestBody = getScopeRequestBodyWithoutField("description");

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("message", containsString("description is required"));
  }

  @Test
  @DisplayName("Should return error when description is blank")
  public void testDescriptionBlank() {
    // Arrange
    Map<String, Object> requestBody =
        getValidScopeRequestBody("test_scope", "Test Scope", "", List.of("email"));

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("message", containsString("description is required"));
  }

  @Test
  @DisplayName("Should return error when claims is missing")
  public void testClaimsMissing() {
    // Arrange
    Map<String, Object> requestBody = getScopeRequestBodyWithoutField("claims");

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("message", containsString("claims is required"));
  }

  @Test
  @DisplayName("Should return error when claims is empty")
  public void testClaimsEmpty() {
    // Arrange
    Map<String, Object> requestBody =
        getValidScopeRequestBody("test_scope", "Test Scope", "Test description", List.of());

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("message", containsString("claims is required"));
  }

  @Test
  @DisplayName("Should return error when tenant-id header is missing")
  public void testMissingTenantId() {
    // Arrange
    Map<String, Object> requestBody =
        getValidScopeRequestBody("test_scope", "Test Scope", "Test description", List.of("email"));

    // Act
    Response response = createScope(null, requestBody);

    // Validate
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should list scopes including created one")
  public void testListScopesWithCreatedScope() {
    // Arrange
    String scopeName = "test_list_scope";
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "List Test Scope", "Test description", List.of("email"));
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
    String scopeName = "test_get_scope";
    Map<String, Object> requestBody =
        getValidScopeRequestBody(scopeName, "Get Test Scope", "Test description", List.of("email"));
    createScope(tenant1, requestBody);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("name", scopeName);

    // Act
    Response response = listScopes(tenant1, queryParams);

    // Validate
    response.then().statusCode(SC_OK).body("scopes[0].scope", equalTo(scopeName));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should return empty list when scope name not found")
  public void testGetScopeByNameNotFound() {
    // Arrange
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("name", "non_existent_scope");

    // Act
    Response response = listScopes(tenant1, queryParams);

    // Validate
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(0));
  }

  @Test
  @DisplayName("Should delete scope successfully")
  public void testDeleteScopeSuccess() {
    // Arrange
    String scopeName = "test_delete_scope";
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "Delete Test Scope", "Test description", List.of("email"));
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
    String nonExistentScope = "non_existent_scope";

    // Act
    Response response = deleteScope(tenant1, nonExistentScope);

    // Validate
    response.then().statusCode(SC_NOT_FOUND);
  }

  @Test
  @DisplayName("Should create scope with multiple claims")
  public void testCreateScopeWithMultipleClaims() {
    // Arrange
    String scopeName = "test_multiple_claims";
    List<String> claims = List.of("email", "name", "picture", "phone");
    Map<String, Object> requestBody =
        getValidScopeRequestBody(scopeName, "Multiple Claims Scope", "Test description", claims);

    // Act
    Response response = createScope(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body("scope", equalTo(scopeName))
        .body("claims.size()", equalTo(4))
        .body("claims", hasItem("email"))
        .body("claims", hasItem("name"))
        .body("claims", hasItem("picture"))
        .body("claims", hasItem("phone"));

    // Cleanup
    deleteScope(tenant1, scopeName);
  }

  @Test
  @DisplayName("Should handle pagination in list scopes")
  public void testListScopesWithPagination() {
    // Arrange
    String scope1 = "test_pagination_1";
    String scope2 = "test_pagination_2";
    createScope(
        tenant1,
        getValidScopeRequestBody(
            scope1, "Pagination Test 1", "Test description", List.of("email")));
    createScope(
        tenant1,
        getValidScopeRequestBody(scope2, "Pagination Test 2", "Test description", List.of("name")));

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
    String scopeName = "tenant_isolation_scope";
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "Tenant Isolation Test", "Test description", List.of("email"));

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
    String scopeName = "shared_scope_name";
    Map<String, Object> requestBody1 =
        getValidScopeRequestBody(
            scopeName, "Tenant 1 Scope", "Description for tenant 1", List.of("email"));
    Map<String, Object> requestBody2 =
        getValidScopeRequestBody(
            scopeName, "Tenant 2 Scope", "Description for tenant 2", List.of("name"));

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
    String scopeName = "test_scope-with.special_chars";
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            scopeName, "Special Chars Scope", "Test description", List.of("email"));

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
    String longScopeName = "very_long_scope_name_that_tests_database_limits_and_validation";
    String longDescription =
        "This is a very long description that tests how the system handles lengthy text inputs and ensures proper storage and retrieval of extended content";
    Map<String, Object> requestBody =
        getValidScopeRequestBody(
            longScopeName, "Long Content Test", longDescription, List.of("email"));

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
    String scopeName = "test_negative_page";
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
    String scopeName = "test_negative_pagesize";
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
    String scopeName = "test_zero_page";
    createScope(
        tenant1,
        getValidScopeRequestBody(
            scopeName, "Zero Page Test", "Test description", List.of("email")));

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("page", "0");
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
  @DisplayName("Should default to pageSize 10 when pageSize is zero")
  public void testZeroPageSizeParameter() {
    // Arrange
    String scopeName = "test_zero_pagesize";
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

  // Helper methods
  private Map<String, Object> getValidScopeRequestBody(
      String scope, String displayName, String description, List<String> claims) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("scope", scope);
    requestBody.put("displayName", displayName);
    requestBody.put("description", description);
    requestBody.put("claims", claims);
    return requestBody;
  }

  private Map<String, Object> getScopeRequestBodyWithoutField(String fieldToExclude) {
    Map<String, Object> requestBody = new HashMap<>();

    if (!"scope".equals(fieldToExclude)) {
      requestBody.put("scope", "test_scope");
    }
    if (!"displayName".equals(fieldToExclude)) {
      requestBody.put("displayName", "Test Scope");
    }
    if (!"description".equals(fieldToExclude)) {
      requestBody.put("description", "Test description");
    }
    if (!"claims".equals(fieldToExclude)) {
      requestBody.put("claims", List.of("email"));
    }

    return requestBody;
  }
}
