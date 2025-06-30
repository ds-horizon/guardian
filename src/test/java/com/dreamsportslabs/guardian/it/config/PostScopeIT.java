package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_ICON_URL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_OIDC;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_SCOPE_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_OIDC_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SCOPE_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SCOPE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TEST_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_ICON_URL;
import static com.dreamsportslabs.guardian.Constants.TEST_NAME_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_PHONE_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_PICTURE_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_SCOPE_NAME;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteScope;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;

import io.restassured.response.Response;
import java.util.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PostScopeIT {
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
    Response response = createScope(TENANT_1, requestBody);

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
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when scope already exists")
  public void testScopeAlreadyExists() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBody(
            scopeName, "Duplicate Scope", TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));

    // Act
    createScope(TENANT_1, body);
    Response response = createScope(TENANT_1, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_SCOPE_ALREADY_EXISTS))
        .body(MESSAGE, equalTo(ERROR_MSG_SCOPE_ALREADY_EXISTS));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when scope is missing")
  public void testScopeMissing() {
    // Act
    Response response = createScope(TENANT_1, getScopeRequestBodyWithoutField(BODY_PARAM_SCOPE));

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
    Map<String, Object> body =
        getValidScopeRequestBody("", TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));

    // Act
    Response response = createScope(TENANT_1, body);

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
    Map<String, Object> body = getScopeRequestBodyWithoutField(BODY_PARAM_DISPLAY_NAME);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);

    // Cleanup
    deleteScope(TENANT_1, body.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should return success when displayName is blank")
  public void testDisplayNameBlank() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBody(scope, "", TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM));

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should return success when description is missing")
  public void testDescriptionMissing() {
    // Arrange
    Map<String, Object> body = getScopeRequestBodyWithoutField(BODY_PARAM_DESCRIPTION);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);

    // Cleanup
    deleteScope(TENANT_1, body.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should return success when description is blank")
  public void testDescriptionBlank() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBody(scope, TEST_SCOPE_NAME, "", List.of(TEST_EMAIL_CLAIM));

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should return success when claims is missing")
  public void testClaimsMissing() {
    // Arrange
    Map<String, Object> body = getScopeRequestBodyWithoutField(BODY_PARAM_CLAIMS);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);

    // Cleanup
    deleteScope(TENANT_1, body.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should create scope with multiple claims")
  public void testCreateScopeWithMultipleClaims() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    List<String> claims =
        List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM, TEST_PICTURE_CLAIM, TEST_PHONE_CLAIM);
    Map<String, Object> body =
        getValidScopeRequestBody(scope, "Multiple Claims Scope", TEST_DESCRIPTION, claims);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_CLAIMS + ".size()", equalTo(4));

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should return success when iconurl is missing")
  public void testIconUrlMissing() {
    // Arrange
    Map<String, Object> body = getScopeRequestBodyWithoutField(BODY_PARAM_ICON_URL);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);

    // Cleanup
    deleteScope(TENANT_1, body.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should return success when iconurl is blank")
  public void testIconUrlBlank() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), "", true);
    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_ICON_URL, equalTo(""));

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should return success when iconurl is null")
  public void testIconUrlNull() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);

    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), null, true);
    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should return error when isOidc is missing")
  public void testIsOidcMissing() {
    // Act
    Response response = createScope(TENANT_1, getScopeRequestBodyWithoutField(BODY_PARAM_IS_OIDC));

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
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope,
            TEST_SCOPE_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_IS_OIDC, equalTo(false));

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should create scope with isOidc true")
  public void testIsOidcTrue() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope,
            TEST_SCOPE_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_IS_OIDC, equalTo(true));

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should return error when isOidc is explicitly null")
  public void testIsOidcExplicitlyNull() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope,
            TEST_SCOPE_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            null);

    // Act
    Response response = createScope(TENANT_1, body);

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
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_SCOPE, scope);
    body.put(BODY_PARAM_DISPLAY_NAME, TEST_SCOPE_NAME);
    body.put(BODY_PARAM_DESCRIPTION, TEST_DESCRIPTION);
    body.put(BODY_PARAM_CLAIMS, List.of(TEST_EMAIL_CLAIM));
    body.put(BODY_PARAM_ICON_URL, TEST_ICON_URL);
    body.put(BODY_PARAM_IS_OIDC, "true");

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);

    // Cleanup

    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should create scope with valid iconurl")
  public void testValidIconUrl() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    String iconUrl = "https://cdn.example.com/icons/custom-icon.svg";
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), iconUrl, true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_ICON_URL, equalTo(iconUrl));

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should handle very long iconurl")
  public void testLongIconUrl() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    String longIconUrl =
        "https://very-long-domain-name-for-testing-purposes.example.com/very/long"
            + "/path/to/icon/file/with/many/subdirectories/icon.png?param1=value1&param2=value2";
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), longIconUrl, true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_ICON_URL, equalTo(longIconUrl));

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should handle iconurl with special characters")
  public void testIconUrlWithSpecialCharacters() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    String specialUrl =
        "https://example.com/icons/icon-with-special_chars@2x.png?v=1.0&cache=false";
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), specialUrl, false);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response
        .then()
        .statusCode(SC_CREATED)
        .body(BODY_PARAM_ICON_URL, equalTo(specialUrl))
        .body(BODY_PARAM_IS_OIDC, equalTo(false));

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  private Map<String, Object> getValidScopeRequestBody(
      String scope, String displayName, String description, List<String> claims) {
    return getValidScopeRequestBodyWithIconAndOidc(
        scope, displayName, description, claims, TEST_ICON_URL, true);
  }

  private Map<String, Object> getValidScopeRequestBodyWithIconAndOidc(
      String scope,
      String displayName,
      String description,
      List<String> claims,
      String iconurl,
      Boolean isOidc) {

    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_SCOPE, scope);
    body.put(BODY_PARAM_DISPLAY_NAME, displayName);
    body.put(BODY_PARAM_DESCRIPTION, description);
    body.put(BODY_PARAM_CLAIMS, claims);
    body.put(BODY_PARAM_ICON_URL, iconurl);
    body.put(BODY_PARAM_IS_OIDC, isOidc);
    return body;
  }

  private Map<String, Object> getScopeRequestBodyWithoutField(String exclude) {
    Map<String, Object> body = new HashMap<>();
    if (!BODY_PARAM_SCOPE.equals(exclude))
      body.put(BODY_PARAM_SCOPE, RandomStringUtils.randomAlphabetic(10));
    if (!BODY_PARAM_DISPLAY_NAME.equals(exclude))
      body.put(BODY_PARAM_DISPLAY_NAME, TEST_SCOPE_NAME);
    if (!BODY_PARAM_DESCRIPTION.equals(exclude)) body.put(BODY_PARAM_DESCRIPTION, TEST_DESCRIPTION);
    if (!BODY_PARAM_CLAIMS.equals(exclude)) body.put(BODY_PARAM_CLAIMS, List.of(TEST_EMAIL_CLAIM));
    if (!BODY_PARAM_ICON_URL.equals(exclude)) body.put(BODY_PARAM_ICON_URL, TEST_ICON_URL);
    if (!BODY_PARAM_IS_OIDC.equals(exclude)) body.put(BODY_PARAM_IS_OIDC, true);
    return body;
  }
}
