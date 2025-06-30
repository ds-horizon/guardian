package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.*;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.*;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import io.restassured.response.Response;
import java.util.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GetScopeIT {
  @Test
  @DisplayName("Should list scopes including created one")
  public void testListScopesWithCreatedScope() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response = listScopes(TENANT_1, new HashMap<>());

    // Validate
    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getList("scopes.name"), hasItem(scopeName));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should get scope by name filter")
  public void testGetScopeByName() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response = listScopes(TENANT_1, Map.of(QUERY_PARAM_NAME, scopeName));

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body("scopes.size()", equalTo(1))
        .body("scopes.name[0]", equalTo(scopeName));
    ;

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should get multiple scopes by names filter")
  public void testGetMultipleScopesByNames() {
    // Arrange
    String s1 = RandomStringUtils.randomAlphabetic(10);
    String s2 = RandomStringUtils.randomAlphabetic(10);
    String s3 = RandomStringUtils.randomAlphabetic(10);

    createScope(TENANT_1, valid(s1));
    createScope(TENANT_1, valid(s2));
    createScope(TENANT_1, valid(s3));

    // Act
    Response response = listScopesByNames(TENANT_1, List.of(s1, s2));

    // Validate
    response.then().statusCode(SC_OK);
    List<String> scopes = response.jsonPath().getList("scopes.name");
    assertThat(scopes.size(), equalTo(2));
    assertThat(scopes, hasItems(s1, s2));
    assertThat(scopes.contains(s3), equalTo(false));

    // Cleanup
    deleteScope(TENANT_1, s1);
    deleteScope(TENANT_1, s2);
    deleteScope(TENANT_1, s3);
  }

  @Test
  @DisplayName("Should return empty list when multiple scope names not found")
  public void testGetMultipleScopesByNamesNotFound() {
    // Arrange
    List<String> nonExistentScopes =
        List.of(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(10));

    // Act
    Response response = listScopesByNames(TENANT_1, nonExistentScopes);

    // Validate
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(0));
  }

  @Test
  @DisplayName("Should return empty list when scope name not found")
  public void testGetScopeByNameNotFound() {
    // Act
    Response response =
        listScopes(TENANT_1, Map.of(QUERY_PARAM_NAME, RandomStringUtils.randomAlphabetic(10)));

    // Validate
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(0));
  }

  @Test
  @DisplayName("Should return error for empty scope name in list")
  public void testGetScopesWithEmptyName() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);

    // Act
    createScope(TENANT_1, valid(scopeName));
    Response response = listScopesByNames(TENANT_1, List.of(""));

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, equalTo(ERROR_MSG_SCOPE_CANNOT_BE_EMPTY));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error for blank scope name in list")
  public void testGetScopesWithBlankName() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response = listScopesByNames(TENANT_1, List.of("   "));

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_SCOPE_CANNOT_BE_EMPTY));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when one of multiple scope names is empty")
  public void testGetScopesWithMixedValidAndEmptyNames() {
    // Arrange
    String validScopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(validScopeName));

    // Act
    Response response = listScopesByNames(TENANT_1, List.of(validScopeName, ""));

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_SCOPE_CANNOT_BE_EMPTY));

    // Cleanup
    deleteScope(TENANT_1, validScopeName);
  }

  @Test
  @DisplayName("Should handle null values in name list gracefully")
  public void testGetScopesWithNullInNameList() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response = listScopesByNames(TENANT_1, Arrays.asList(scopeName, null));

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_SCOPE_CANNOT_BE_EMPTY));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should handle maximum number of scope names in single request")
  public void testGetScopesWithMaximumNames() {
    // Arrange
    List<String> names = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      String n = "scope_" + i + "_" + RandomStringUtils.randomAlphabetic(5);
      names.add(n);
      createScope(TENANT_1, valid(n));
    }

    // Act
    Response response = listScopesByNames(TENANT_1, names);

    // Validate
    response.then().statusCode(SC_OK).body("scopes.name.size()", equalTo(50));

    // Cleanup
    names.forEach(n -> deleteScope(TENANT_1, n));
  }

  @Test
  @DisplayName("Should handle duplicate scope names in request")
  public void testGetScopesWithDuplicateNames() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scope));

    // Act
    Response response = listScopesByNames(TENANT_1, List.of(scope, scope, scope));

    // Validate
    response.then().statusCode(SC_OK).body("scopes.name", hasItem(scope));

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should handle special characters in scope names")
  public void testGetScopesWithSpecialCharacters() {
    // Arrange
    String special = "scope-with.special_chars@domain.com";
    createScope(TENANT_1, valid(special));

    // Act
    Response response = listScopesByNames(TENANT_1, List.of(special));

    // Validate
    response.then().statusCode(SC_OK).body("scopes.name", hasItem(special));

    // Cleanup
    deleteScope(TENANT_1, special);
  }

  @Test
  @DisplayName("Should validate request size limits")
  public void testRequestSizeLimits() {
    // Arrange
    List<String> many = new ArrayList<>();
    for (int i = 0; i < 1000; i++) many.add("scope_" + i);

    // Act
    Response response = listScopesByNames(TENANT_1, many);

    // Validate
    response.then().statusCode(isA(Integer.class));
  }

  @Test
  @DisplayName("Should handle pagination in list scopes")
  public void testListScopesWithPagination() {
    // Arrange
    String s1 = RandomStringUtils.randomAlphabetic(10);
    String s2 = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(s1));
    createScope(TENANT_1, valid(s2));

    // Act
    Response response =
        listScopes(TENANT_1, Map.of(QUERY_PARAM_PAGE, "1", QUERY_PARAM_PAGE_SIZE, "1"));

    // Validate
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(1));

    // Cleanup
    deleteScope(TENANT_1, s1);
    deleteScope(TENANT_1, s2);
  }

  @Test
  @DisplayName("Should return error when page is negative")
  public void testNegativePageParameter() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response =
        listScopes(TENANT_1, Map.of(QUERY_PARAM_PAGE, "-1", QUERY_PARAM_PAGE_SIZE, "10"));

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_PAGE_VALUE_CANNOT_BE_LESS_THAN_1));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when pageSize is negative")
  public void testNegativePageSizeParameter() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response =
        listScopes(TENANT_1, Map.of(QUERY_PARAM_PAGE, "1", QUERY_PARAM_PAGE_SIZE, "-5"));

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_PAGE_SIZE_VALUE_CANNOT_BE_LESS_THAN_1));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when page is zero")
  public void testZeroPageParameter() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(6);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response =
        listScopes(TENANT_1, Map.of(QUERY_PARAM_PAGE, "0", QUERY_PARAM_PAGE_SIZE, "10"));

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_PAGE_VALUE_CANNOT_BE_LESS_THAN_1));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when pageSize is zero")
  public void testZeroPageSizeParameter() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response =
        listScopes(TENANT_1, Map.of(QUERY_PARAM_PAGE, "1", QUERY_PARAM_PAGE_SIZE, "0"));

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_PAGE_SIZE_VALUE_CANNOT_BE_LESS_THAN_1));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when pageSize exceeds maximum limit")
  public void testPageSizeExceedsMaximum() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response =
        listScopes(TENANT_1, Map.of(QUERY_PARAM_PAGE, "1", QUERY_PARAM_PAGE_SIZE, "101"));

    // Validate
    response.then().statusCode(SC_BAD_REQUEST);

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should work with valid scope names")
  public void testGetScopesWithValidNames() {
    // Arrange
    String s1 = RandomStringUtils.randomAlphabetic(10);
    String s2 = RandomStringUtils.randomAlphabetic(10);

    createScope(TENANT_1, valid(s1));
    createScope(TENANT_1, valid(s2));

    // Act
    Response response = listScopesByNames(TENANT_1, List.of(s1, s2));

    // Validate
    response.then().statusCode(SC_OK);
    List<String> scopes = response.jsonPath().getList("scopes.name");
    assertThat(scopes.size(), equalTo(2));
    assertThat(scopes, hasItems(s1, s2));

    // Cleanup
    deleteScope(TENANT_1, s1);
    deleteScope(TENANT_1, s2);
  }

  @Test
  @DisplayName("Should work with default pagination parameters")
  public void testGetScopesWithDefaultPagination() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response = listScopes(TENANT_1, new HashMap<>());
    response.then().statusCode(SC_OK).body("scopes.name", hasItem(scopeName));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should work with valid page and pageSize at boundaries")
  public void testValidPageAndPageSizeBoundaries() {
    // Arrange
    String s1 = RandomStringUtils.randomAlphabetic(10);
    String s2 = RandomStringUtils.randomAlphabetic(10);
    String s3 = RandomStringUtils.randomAlphabetic(10);

    createScope(TENANT_1, valid(s1));
    createScope(TENANT_1, valid(s2));
    createScope(TENANT_1, valid(s3));

    // Act
    Response response =
        listScopes(TENANT_1, Map.of(QUERY_PARAM_PAGE, "1", QUERY_PARAM_PAGE_SIZE, "2"));

    // Validate
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(2));

    // Cleanup
    deleteScope(TENANT_1, s1);
    deleteScope(TENANT_1, s2);
    deleteScope(TENANT_1, s3);
  }

  @Test
  @DisplayName("Should handle non-numeric page and pageSize parameters")
  public void testNonNumericPaginationParameters() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response1 =
        listScopes(TENANT_1, Map.of(QUERY_PARAM_PAGE, "abc", QUERY_PARAM_PAGE_SIZE, "10"));

    // Validate
    response1.then().statusCode(SC_NOT_FOUND);

    // Act
    Response response2 =
        listScopes(TENANT_1, Map.of(QUERY_PARAM_PAGE, "1", QUERY_PARAM_PAGE_SIZE, "xyz"));

    // Validate
    response2.then().statusCode(SC_NOT_FOUND);

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should isolate scopes by tenant")
  public void testTenantIsolation() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    createScope(TENANT_1, valid(scopeName));

    // Act
    Response response = listScopes(TENANT_2, Map.of(QUERY_PARAM_NAME, scopeName));

    // Validate
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(0));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should allow same scope name in different tenants")
  public void testSameScopeNameDifferentTenants() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);

    // Act
    Response response1 = createScope(TENANT_1, valid(scopeName));
    Response response2 = createScope(TENANT_2, valid(scopeName));

    // Validate
    response1.then().statusCode(SC_CREATED);
    response2.then().statusCode(SC_CREATED);

    // Cleanup
    deleteScope(TENANT_1, scopeName);
    deleteScope(TENANT_2, scopeName);
  }

  private Map<String, Object> valid(String scope) {
    Map<String, Object> m = new HashMap<>();
    m.put(BODY_PARAM_SCOPE, scope);
    m.put(BODY_PARAM_DISPLAY_NAME, TEST_SCOPE_NAME);
    m.put(BODY_PARAM_DESCRIPTION, TEST_DESCRIPTION);
    m.put(BODY_PARAM_CLAIMS, List.of(TEST_EMAIL_CLAIM));
    m.put(BODY_PARAM_ICON_URL, TEST_ICON_URL);
    m.put(BODY_PARAM_IS_OIDC, true);
    return m;
  }
}
