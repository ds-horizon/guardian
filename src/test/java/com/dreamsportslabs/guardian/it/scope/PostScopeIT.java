package com.dreamsportslabs.guardian.it.scope;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_ICON_URL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_OIDC;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.CLAIM_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.CLAIM_EMAIL_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_SCOPE_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_ADDRESS_SCOPE_INVALID_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_EMAIL_SCOPE_INVALID_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_OPENID_SCOPE_INVALID_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PHONE_SCOPE_INVALID_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SCOPE_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SCOPE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TEST_ADDRESS_SCOPE_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_ADDRESS_SCOPE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_DUPLICATE_SCOPE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_SCOPE_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_SCOPE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_EXTRA_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_ICON_URL;
import static com.dreamsportslabs.guardian.Constants.TEST_MULTIPLE_CLAIMS_SCOPE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_NAME_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_OPENID_SCOPE_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_OPENID_SCOPE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_PHONE_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_PHONE_SCOPE_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_PHONE_SCOPE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_PICTURE_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_SCOPE_NAME;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteScope;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class PostScopeIT {
  @Test
  @DisplayName("Should create a new scope successfully")
  public void testCreateScopeSuccess() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> requestBody =
        getValidScopeRequestBodyWithIconAndOidc(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM),
            TEST_ICON_URL,
            true);

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

    validateInDb(TENANT_1, scopeName, requestBody);

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when scope already exists")
  public void testScopeAlreadyExists() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scopeName,
            TEST_DUPLICATE_SCOPE_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            true);

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
        getValidScopeRequestBodyWithIconAndOidc(
            "", TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), TEST_ICON_URL, true);

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
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

    // Cleanup
    deleteScope(TENANT_1, body.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should return success when displayName is blank")
  public void testDisplayNameBlank() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope, "", TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), TEST_ICON_URL, true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

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
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

    // Cleanup
    deleteScope(TENANT_1, body.get(BODY_PARAM_SCOPE).toString());
  }

  @Test
  @DisplayName("Should return success when description is blank")
  public void testDescriptionBlank() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope, TEST_SCOPE_NAME, "", List.of(TEST_EMAIL_CLAIM), TEST_ICON_URL, true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

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
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

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
        getValidScopeRequestBodyWithIconAndOidc(
            scope,
            TEST_MULTIPLE_CLAIMS_SCOPE_DISPLAY_NAME,
            TEST_DESCRIPTION,
            claims,
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_CLAIMS + ".size()", equalTo(4));
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

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
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

    // Cleanup
    deleteScope(TENANT_1, body.get(BODY_PARAM_SCOPE).toString());
  }

  @ParameterizedTest
  @DisplayName("Should return success when iconurl is blank")
  @NullAndEmptySource
  public void testIconUrlBlank(String iconUrl) {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), iconUrl, true);
    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_ICON_URL, equalTo(iconUrl));
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @ParameterizedTest
  @DisplayName("Should create scope with isOidc")
  @ValueSource(booleans = {false, true})
  public void testIsOidcFalse(Boolean isOidc) {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope,
            TEST_SCOPE_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            isOidc);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_IS_OIDC, equalTo(isOidc));
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @ParameterizedTest
  @DisplayName("Should create scope with valid iconurl")
  @ValueSource(
      strings = {
        "https://cdn.example.com/icons/custom-icon.svg",
        "https://example.com/icons/icon-with-special_chars@2x.png?v=1.0&cache=false"
      })
  public void testValidIconUrl(String iconUrl) {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            scope, TEST_SCOPE_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), iconUrl, true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED).body(BODY_PARAM_ICON_URL, equalTo(iconUrl));
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

    // Cleanup
    deleteScope(TENANT_1, scope);
  }

  @Test
  @DisplayName("Should create openid scope with valid sub claim")
  public void testCreateOpenidScopeWithValidClaim() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_OPENID,
            TEST_OPENID_SCOPE_DISPLAY_NAME,
            TEST_OPENID_SCOPE_DESCRIPTION,
            List.of(CLAIM_SUB),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_OPENID);
  }

  @Test
  @DisplayName("Should return error when openid scope has invalid claims")
  public void testCreateOpenidScopeWithInvalidClaims() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_OPENID,
            TEST_OPENID_SCOPE_DISPLAY_NAME,
            TEST_OPENID_SCOPE_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_OPENID_SCOPE_INVALID_CLAIMS));
  }

  @Test
  @DisplayName("Should create phone scope with valid claims")
  public void testCreatePhoneScopeWithValidClaims() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_PHONE,
            TEST_PHONE_SCOPE_DISPLAY_NAME,
            TEST_PHONE_SCOPE_DESCRIPTION,
            List.of(CLAIM_PHONE_NUMBER, CLAIM_PHONE_NUMBER_VERIFIED),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_PHONE);
  }

  @Test
  @DisplayName("Should return error when phone scope has invalid claims")
  public void testCreatePhoneScopeWithInvalidClaims() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_PHONE,
            TEST_PHONE_SCOPE_DISPLAY_NAME,
            TEST_PHONE_SCOPE_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_PHONE_SCOPE_INVALID_CLAIMS));
  }

  @Test
  @DisplayName("Should create email scope with valid claims")
  public void testCreateEmailScopeWithValidClaims() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_EMAIL,
            TEST_EMAIL_SCOPE_DISPLAY_NAME,
            TEST_EMAIL_SCOPE_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, CLAIM_EMAIL_VERIFIED),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_EMAIL);
  }

  @Test
  @DisplayName("Should return error when email scope has invalid claims")
  public void testCreateEmailScopeWithInvalidClaims() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_EMAIL,
            TEST_EMAIL_SCOPE_DISPLAY_NAME,
            TEST_EMAIL_SCOPE_DESCRIPTION,
            List.of(CLAIM_PHONE_NUMBER, TEST_NAME_CLAIM),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_EMAIL_SCOPE_INVALID_CLAIMS));
  }

  @Test
  @DisplayName("Should create address scope with valid claim")
  public void testCreateAddressScopeWithValidClaim() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_ADDRESS,
            TEST_ADDRESS_SCOPE_DISPLAY_NAME,
            TEST_ADDRESS_SCOPE_DESCRIPTION,
            List.of(CLAIM_ADDRESS),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response.then().statusCode(SC_CREATED);
    validateInDb(TENANT_1, body.get(BODY_PARAM_SCOPE).toString(), body);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_ADDRESS);
  }

  @Test
  @DisplayName("Should return error when address scope has invalid claims")
  public void testCreateAddressScopeWithInvalidClaims() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_ADDRESS,
            TEST_ADDRESS_SCOPE_DISPLAY_NAME,
            TEST_ADDRESS_SCOPE_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_ADDRESS_SCOPE_INVALID_CLAIMS));
  }

  @Test
  @DisplayName("Should return error when phone scope has too many claims")
  public void testCreatePhoneScopeWithTooManyClaims() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_PHONE,
            TEST_PHONE_SCOPE_DISPLAY_NAME,
            TEST_PHONE_SCOPE_DESCRIPTION,
            List.of(CLAIM_PHONE_NUMBER, CLAIM_PHONE_NUMBER_VERIFIED, TEST_EXTRA_CLAIM),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_PHONE_SCOPE_INVALID_CLAIMS));
  }

  @Test
  @DisplayName("Should return error when email scope has too many claims")
  public void testCreateEmailScopeWithTooManyClaims() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_EMAIL,
            TEST_EMAIL_SCOPE_DISPLAY_NAME,
            TEST_EMAIL_SCOPE_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, CLAIM_EMAIL_VERIFIED, TEST_EXTRA_CLAIM),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_EMAIL_SCOPE_INVALID_CLAIMS));
  }

  @Test
  @DisplayName("Should return error when openid scope has too many claims")
  public void testCreateOpenidScopeWithTooManyClaims() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_OPENID,
            TEST_OPENID_SCOPE_DISPLAY_NAME,
            TEST_OPENID_SCOPE_DESCRIPTION,
            List.of(CLAIM_SUB, TEST_EXTRA_CLAIM),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_OPENID_SCOPE_INVALID_CLAIMS));
  }

  @Test
  @DisplayName("Should return error when address scope has too many claims")
  public void testCreateAddressScopeWithTooManyClaims() {
    // Arrange
    Map<String, Object> body =
        getValidScopeRequestBodyWithIconAndOidc(
            SCOPE_ADDRESS,
            TEST_ADDRESS_SCOPE_DISPLAY_NAME,
            TEST_ADDRESS_SCOPE_DESCRIPTION,
            List.of(CLAIM_ADDRESS, TEST_EXTRA_CLAIM),
            TEST_ICON_URL,
            true);

    // Act
    Response response = createScope(TENANT_1, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_ADDRESS_SCOPE_INVALID_CLAIMS));
  }

  private Map<String, Object> getValidScopeRequestBodyWithIconAndOidc(
      String scope,
      String displayName,
      String description,
      List<String> claims,
      String iconUrl,
      Boolean isOidc) {

    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_SCOPE, scope);
    body.put(BODY_PARAM_DISPLAY_NAME, displayName);
    body.put(BODY_PARAM_DESCRIPTION, description);
    body.put(BODY_PARAM_CLAIMS, claims);
    body.put(BODY_PARAM_ICON_URL, iconUrl);
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

  @SneakyThrows
  private void validateInDb(String tenant, String scope, Map<String, Object> body) {
    JsonObject dbScope = DbUtils.getScope(tenant, scope);
    Assertions.assertNotNull(dbScope);

    assertThat(dbScope.getString("name"), equalTo(scope));

    assertThat(
        dbScope.getString(BODY_PARAM_DISPLAY_NAME), equalTo(body.get(BODY_PARAM_DISPLAY_NAME)));

    assertThat(
        dbScope.getString(BODY_PARAM_DESCRIPTION), equalTo(body.get(BODY_PARAM_DESCRIPTION)));

    assertThat(dbScope.getString(BODY_PARAM_ICON_URL), equalTo(body.get(BODY_PARAM_ICON_URL)));

    assertThat(dbScope.getString("tenantId"), equalTo(tenant));

    if (body.get(BODY_PARAM_IS_OIDC) != null && (Boolean) body.get(BODY_PARAM_IS_OIDC)) {
      assertThat(dbScope.getBoolean(BODY_PARAM_IS_OIDC), equalTo(true));
    } else {
      assertThat(dbScope.getBoolean(BODY_PARAM_IS_OIDC), equalTo(false));
    }

    if (StringUtils.isNotBlank(dbScope.getString(BODY_PARAM_CLAIMS))
        && body.containsKey(BODY_PARAM_CLAIMS)) {
      List<String> claims =
          (new ObjectMapper())
              .readValue(dbScope.getString(BODY_PARAM_CLAIMS), new TypeReference<>() {});
      for (String claim : (List<String>) body.get(BODY_PARAM_CLAIMS)) {
        assertThat(claims, hasItem(claim));
      }
    }
  }
}
