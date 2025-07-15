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
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_SCOPE_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_ADDRESS_SCOPE_INVALID_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_EMAIL_SCOPE_INVALID_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_OPENID_SCOPE_INVALID_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PHONE_SCOPE_INVALID_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SCOPE_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TEST_ADDRESS_SCOPE_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_ADDRESS_SCOPE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_SCOPE_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_SCOPE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_EXTRA_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_ICON_URL;
import static com.dreamsportslabs.guardian.Constants.TEST_NAME_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_OPENID_SCOPE_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_OPENID_SCOPE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_PARTIAL_UPDATE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_PHONE_SCOPE_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_PHONE_SCOPE_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_PICTURE_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_UPDATED_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_UPDATED_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_UPDATED_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_UPDATED_ICON_URL;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateScope;
import static com.dreamsportslabs.guardian.utils.ScopeUtils.getValidScopeRequestBody;
import static com.dreamsportslabs.guardian.utils.ScopeUtils.validateInDb;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class UpdateScopeIT {
  private Map<String, Object> getValidUpdateRequestBody(
      String displayName, String description, List<String> claims, String iconUrl, Boolean isOidc) {
    Map<String, Object> body = new HashMap<>();
    if (displayName != null) body.put(BODY_PARAM_DISPLAY_NAME, displayName);
    if (description != null) body.put(BODY_PARAM_DESCRIPTION, description);
    if (claims != null) body.put(BODY_PARAM_CLAIMS, claims);
    if (iconUrl != null) body.put(BODY_PARAM_ICON_URL, iconUrl);
    if (isOidc != null) body.put(BODY_PARAM_IS_OIDC, isOidc);
    return body;
  }

  @Test
  @DisplayName("Should update scope successfully with all fields")
  public void testUpdateScopeSuccess() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            TEST_UPDATED_DISPLAY_NAME,
            TEST_UPDATED_DESCRIPTION,
            List.of(TEST_UPDATED_CLAIM, TEST_NAME_CLAIM),
            TEST_UPDATED_ICON_URL,
            true);

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(scopeName))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_DESCRIPTION, equalTo(TEST_UPDATED_DESCRIPTION))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_UPDATED_CLAIM))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_NAME_CLAIM))
        .body(BODY_PARAM_ICON_URL, equalTo(TEST_UPDATED_ICON_URL))
        .body(BODY_PARAM_IS_OIDC, equalTo(true));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(updateBody);
    expectedDbBody.put(BODY_PARAM_SCOPE, scopeName);
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should update scope with partial fields only")
  public void testPartialUpdateScope() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(BODY_PARAM_DISPLAY_NAME, TEST_PARTIAL_UPDATE_DISPLAY_NAME);

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(scopeName))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_PARTIAL_UPDATE_DISPLAY_NAME))
        .body(BODY_PARAM_DESCRIPTION, equalTo(TEST_DESCRIPTION)) // Should remain unchanged
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_EMAIL_CLAIM)) // Should remain unchanged
        .body(BODY_PARAM_ICON_URL, equalTo(TEST_ICON_URL)) // Should remain unchanged
        .body(BODY_PARAM_IS_OIDC, equalTo(false)); // Should remain unchanged

    // Validate in database - only displayName should be updated
    Map<String, Object> expectedDbBody = new HashMap<>(createBody);
    expectedDbBody.put(BODY_PARAM_DISPLAY_NAME, TEST_PARTIAL_UPDATE_DISPLAY_NAME);
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when no updates provided")
  public void testUpdateScopeWithNoChanges() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> emptyUpdateBody = new HashMap<>();

    // Act
    Response response = updateScope(TENANT_1, scopeName, emptyUpdateBody);

    // Validate - should return existing scope unchanged
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE))
        .body(MESSAGE, equalTo(ERROR_MSG_NO_FIELDS_TO_UPDATE));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when scope does not exist")
  public void testUpdateNonExistentScope() {
    // Arrange
    String nonExistentScope = "non-existent-scope";
    Map<String, Object> updateBody =
        getValidUpdateRequestBody(TEST_UPDATED_DISPLAY_NAME, null, null, null, null);

    // Act
    Response response = updateScope(TENANT_1, nonExistentScope, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_SCOPE_NOT_FOUND))
        .body(MESSAGE, containsString(ERROR_MSG_SCOPE_NOT_FOUND));
  }

  @Test
  @DisplayName("Should return error when updating scope in different tenant")
  public void testUpdateScopeInDifferentTenant() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(TEST_UPDATED_DISPLAY_NAME, null, null, null, null);

    // Act - try to update scope from TENANT_1 using TENANT_2
    Response response = updateScope(TENANT_2, scopeName, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_SCOPE_NOT_FOUND));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should update openid scope with valid sub claim")
  public void testUpdateOpenidScopeWithValidClaim() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_OPENID,
            TEST_OPENID_SCOPE_DISPLAY_NAME,
            TEST_OPENID_SCOPE_DESCRIPTION,
            List.of(CLAIM_SUB),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            TEST_UPDATED_DISPLAY_NAME,
            TEST_UPDATED_DESCRIPTION,
            List.of(CLAIM_SUB), // Valid for openid scope
            TEST_UPDATED_ICON_URL,
            true);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_OPENID, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(SCOPE_OPENID))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_CLAIMS, hasItem(CLAIM_SUB));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(updateBody);
    expectedDbBody.put(BODY_PARAM_SCOPE, SCOPE_OPENID);
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_OPENID);
  }

  @Test
  @DisplayName("Should return error when updating openid scope with invalid claims")
  public void testUpdateOpenidScopeWithInvalidClaims() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_OPENID,
            TEST_OPENID_SCOPE_DISPLAY_NAME,
            TEST_OPENID_SCOPE_DESCRIPTION,
            List.of(CLAIM_SUB),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            null,
            null,
            List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM, CLAIM_SUB), // Invalid for openid scope
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_OPENID, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_OPENID_SCOPE_INVALID_CLAIMS));

    // Cleanup
    deleteScope(TENANT_1, SCOPE_OPENID);
  }

  @Test
  @DisplayName("Should update phone scope with valid claims")
  public void testUpdatePhoneScopeWithValidClaims() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_PHONE,
            TEST_PHONE_SCOPE_DISPLAY_NAME,
            TEST_PHONE_SCOPE_DESCRIPTION,
            List.of(CLAIM_PHONE_NUMBER),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            TEST_UPDATED_DISPLAY_NAME,
            TEST_UPDATED_DESCRIPTION,
            List.of(CLAIM_PHONE_NUMBER, CLAIM_PHONE_NUMBER_VERIFIED), // Valid for phone scope
            TEST_UPDATED_ICON_URL,
            false);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_PHONE, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(SCOPE_PHONE))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_DESCRIPTION, equalTo(TEST_UPDATED_DESCRIPTION))
        .body(BODY_PARAM_CLAIMS, hasItem(CLAIM_PHONE_NUMBER))
        .body(BODY_PARAM_CLAIMS, hasItem(CLAIM_PHONE_NUMBER_VERIFIED))
        .body(BODY_PARAM_ICON_URL, equalTo(TEST_UPDATED_ICON_URL))
        .body(BODY_PARAM_IS_OIDC, equalTo(false));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(updateBody);
    expectedDbBody.put(BODY_PARAM_SCOPE, SCOPE_PHONE);
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_PHONE);
  }

  @Test
  @DisplayName("Should return error when updating phone scope with invalid claims")
  public void testUpdatePhoneScopeWithInvalidClaims() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_PHONE,
            TEST_PHONE_SCOPE_DISPLAY_NAME,
            TEST_PHONE_SCOPE_DESCRIPTION,
            List.of(CLAIM_PHONE_NUMBER, CLAIM_PHONE_NUMBER_VERIFIED),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            null,
            null,
            List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM), // Invalid for phone scope
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_PHONE, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_PHONE_SCOPE_INVALID_CLAIMS));

    // Cleanup
    deleteScope(TENANT_1, SCOPE_PHONE);
  }

  @Test
  @DisplayName("Should return error when updating phone scope with too many claims")
  public void testUpdatePhoneScopeWithTooManyClaims() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_PHONE,
            TEST_PHONE_SCOPE_DISPLAY_NAME,
            TEST_PHONE_SCOPE_DESCRIPTION,
            List.of(CLAIM_PHONE_NUMBER, CLAIM_PHONE_NUMBER_VERIFIED),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            null,
            null,
            List.of(
                CLAIM_PHONE_NUMBER,
                CLAIM_PHONE_NUMBER_VERIFIED,
                TEST_EXTRA_CLAIM), // Too many claims
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_PHONE, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_PHONE_SCOPE_INVALID_CLAIMS));

    // Cleanup
    deleteScope(TENANT_1, SCOPE_PHONE);
  }

  @Test
  @DisplayName("Should update email scope with valid claims")
  public void testUpdateEmailScopeWithValidClaims() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_EMAIL,
            TEST_EMAIL_SCOPE_DISPLAY_NAME,
            TEST_EMAIL_SCOPE_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, CLAIM_EMAIL_VERIFIED),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            TEST_UPDATED_DISPLAY_NAME,
            TEST_UPDATED_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, CLAIM_EMAIL_VERIFIED), // Valid for email scope
            TEST_UPDATED_ICON_URL,
            false);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_EMAIL, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(SCOPE_EMAIL))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_DESCRIPTION, equalTo(TEST_UPDATED_DESCRIPTION))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_EMAIL_CLAIM))
        .body(BODY_PARAM_CLAIMS, hasItem(CLAIM_EMAIL_VERIFIED))
        .body(BODY_PARAM_ICON_URL, equalTo(TEST_UPDATED_ICON_URL))
        .body(BODY_PARAM_IS_OIDC, equalTo(false));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(updateBody);
    expectedDbBody.put(BODY_PARAM_SCOPE, SCOPE_EMAIL);
    validateInDb(TENANT_1, expectedDbBody);
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_EMAIL);
  }

  @Test
  @DisplayName("Should return error when updating email scope with invalid claims")
  public void testUpdateEmailScopeWithInvalidClaims() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_EMAIL,
            TEST_EMAIL_SCOPE_DISPLAY_NAME,
            TEST_EMAIL_SCOPE_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, CLAIM_EMAIL_VERIFIED),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            null,
            null,
            List.of(CLAIM_PHONE_NUMBER, TEST_NAME_CLAIM), // Invalid for email scope
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_EMAIL, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_EMAIL_SCOPE_INVALID_CLAIMS));

    // Cleanup
    deleteScope(TENANT_1, SCOPE_EMAIL);
  }

  @Test
  @DisplayName("Should return error when updating email scope with too many claims")
  public void testUpdateEmailScopeWithTooManyClaims() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_EMAIL,
            TEST_EMAIL_SCOPE_DISPLAY_NAME,
            TEST_EMAIL_SCOPE_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, CLAIM_EMAIL_VERIFIED),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            null,
            null,
            List.of(TEST_EMAIL_CLAIM, CLAIM_EMAIL_VERIFIED, TEST_EXTRA_CLAIM), // Too many claims
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_EMAIL, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_EMAIL_SCOPE_INVALID_CLAIMS));

    // Cleanup
    deleteScope(TENANT_1, SCOPE_EMAIL);
  }

  @Test
  @DisplayName("Should update address scope with valid claim")
  public void testUpdateAddressScopeWithValidClaim() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_ADDRESS,
            TEST_ADDRESS_SCOPE_DISPLAY_NAME,
            TEST_ADDRESS_SCOPE_DESCRIPTION,
            List.of(CLAIM_ADDRESS),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            TEST_UPDATED_DISPLAY_NAME,
            TEST_UPDATED_DESCRIPTION,
            List.of(CLAIM_ADDRESS), // Valid for address scope
            TEST_UPDATED_ICON_URL,
            false);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_ADDRESS, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(SCOPE_ADDRESS))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_DESCRIPTION, equalTo(TEST_UPDATED_DESCRIPTION))
        .body(BODY_PARAM_CLAIMS, hasItem(CLAIM_ADDRESS))
        .body(BODY_PARAM_ICON_URL, equalTo(TEST_UPDATED_ICON_URL))
        .body(BODY_PARAM_IS_OIDC, equalTo(false));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(updateBody);
    expectedDbBody.put(BODY_PARAM_SCOPE, SCOPE_ADDRESS);
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_ADDRESS);
  }

  @Test
  @DisplayName("Should return error when updating address scope with invalid claims")
  public void testUpdateAddressScopeWithInvalidClaims() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_ADDRESS,
            TEST_ADDRESS_SCOPE_DISPLAY_NAME,
            TEST_ADDRESS_SCOPE_DESCRIPTION,
            List.of(CLAIM_ADDRESS),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            null,
            null,
            List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM), // Invalid for address scope
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_ADDRESS, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_ADDRESS_SCOPE_INVALID_CLAIMS));

    // Cleanup
    deleteScope(TENANT_1, SCOPE_ADDRESS);
  }

  @Test
  @DisplayName("Should return error when updating address scope with too many claims")
  public void testUpdateAddressScopeWithTooManyClaims() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_ADDRESS,
            TEST_ADDRESS_SCOPE_DISPLAY_NAME,
            TEST_ADDRESS_SCOPE_DESCRIPTION,
            List.of(CLAIM_ADDRESS),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            null,
            null,
            List.of(CLAIM_ADDRESS, TEST_EXTRA_CLAIM), // Too many claims
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_ADDRESS, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString(ERROR_MSG_ADDRESS_SCOPE_INVALID_CLAIMS));

    // Cleanup
    deleteScope(TENANT_1, SCOPE_ADDRESS);
  }

  @ParameterizedTest
  @DisplayName("Should update scope with isOidc values")
  @ValueSource(booleans = {false, true})
  public void testUpdateScopeWithIsOidc(Boolean isOidc) {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            !isOidc); // Create with opposite value

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody = getValidUpdateRequestBody(null, null, null, null, isOidc);

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate
    response.then().statusCode(SC_OK).body(BODY_PARAM_IS_OIDC, equalTo(isOidc));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(createBody);
    expectedDbBody.put(BODY_PARAM_IS_OIDC, isOidc);
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @ParameterizedTest
  @DisplayName("Should update scope with valid iconUrl values")
  @EmptySource
  @ValueSource(strings = {"https://cdn.example.com/icons/updated-icon.svg"})
  public void testUpdateScopeWithValidIconUrl(String iconUrl) {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody = getValidUpdateRequestBody(null, null, null, iconUrl, null);

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate

    response.then().statusCode(SC_OK).body(BODY_PARAM_ICON_URL, equalTo(iconUrl));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(createBody);
    expectedDbBody.put(BODY_PARAM_ICON_URL, iconUrl);
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should update scope with multiple claims")
  public void testUpdateScopeWithMultipleClaims() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    List<String> updatedClaims =
        List.of(TEST_EMAIL_CLAIM, TEST_NAME_CLAIM, TEST_PICTURE_CLAIM, TEST_UPDATED_CLAIM);
    Map<String, Object> updateBody =
        getValidUpdateRequestBody(null, null, updatedClaims, null, null);

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_CLAIMS + ".size()", equalTo(4))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_EMAIL_CLAIM))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_NAME_CLAIM))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_PICTURE_CLAIM))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_UPDATED_CLAIM));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(createBody);
    expectedDbBody.put(BODY_PARAM_CLAIMS, updatedClaims);
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  // ========== PHONE SCOPE EDGE CASES ==========

  @Test
  @DisplayName("Should update phone scope with single phone_number claim")
  public void testUpdatePhoneScopeWithSinglePhoneNumberClaim() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_PHONE,
            TEST_PHONE_SCOPE_DISPLAY_NAME,
            TEST_PHONE_SCOPE_DESCRIPTION,
            List.of(CLAIM_PHONE_NUMBER, CLAIM_PHONE_NUMBER_VERIFIED),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            TEST_UPDATED_DISPLAY_NAME,
            null,
            List.of(CLAIM_PHONE_NUMBER), // Single valid claim
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_PHONE, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(SCOPE_PHONE))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_CLAIMS + ".size()", equalTo(1))
        .body(BODY_PARAM_CLAIMS, hasItem(CLAIM_PHONE_NUMBER));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(createBody);
    expectedDbBody.put(BODY_PARAM_DISPLAY_NAME, TEST_UPDATED_DISPLAY_NAME);
    expectedDbBody.put(BODY_PARAM_CLAIMS, List.of(CLAIM_PHONE_NUMBER));
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_PHONE);
  }

  @Test
  @DisplayName("Should update phone scope with single phone_number_verified claim")
  public void testUpdatePhoneScopeWithSinglePhoneVerifiedClaim() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_PHONE,
            TEST_PHONE_SCOPE_DISPLAY_NAME,
            TEST_PHONE_SCOPE_DESCRIPTION,
            List.of(CLAIM_PHONE_NUMBER),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            TEST_UPDATED_DISPLAY_NAME,
            null,
            List.of(CLAIM_PHONE_NUMBER_VERIFIED), // Single valid claim
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_PHONE, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(SCOPE_PHONE))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_CLAIMS + ".size()", equalTo(1))
        .body(BODY_PARAM_CLAIMS, hasItem(CLAIM_PHONE_NUMBER_VERIFIED));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(createBody);
    expectedDbBody.put(BODY_PARAM_DISPLAY_NAME, TEST_UPDATED_DISPLAY_NAME);
    expectedDbBody.put(BODY_PARAM_CLAIMS, List.of(CLAIM_PHONE_NUMBER_VERIFIED));
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_PHONE);
  }

  // ========== EMAIL SCOPE EDGE CASES ==========

  @Test
  @DisplayName("Should update email scope with single email claim")
  public void testUpdateEmailScopeWithSingleEmailClaim() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_EMAIL,
            TEST_EMAIL_SCOPE_DISPLAY_NAME,
            TEST_EMAIL_SCOPE_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM, CLAIM_EMAIL_VERIFIED),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            TEST_UPDATED_DISPLAY_NAME,
            null,
            List.of(TEST_EMAIL_CLAIM), // Single valid claim
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_EMAIL, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(SCOPE_EMAIL))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_CLAIMS + ".size()", equalTo(1))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_EMAIL_CLAIM));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(createBody);
    expectedDbBody.put(BODY_PARAM_DISPLAY_NAME, TEST_UPDATED_DISPLAY_NAME);
    expectedDbBody.put(BODY_PARAM_CLAIMS, List.of(TEST_EMAIL_CLAIM));
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_EMAIL);
  }

  @Test
  @DisplayName("Should update email scope with single email_verified claim")
  public void testUpdateEmailScopeWithSingleEmailVerifiedClaim() {
    // Arrange
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            SCOPE_EMAIL,
            TEST_EMAIL_SCOPE_DISPLAY_NAME,
            TEST_EMAIL_SCOPE_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            true);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody =
        getValidUpdateRequestBody(
            TEST_UPDATED_DISPLAY_NAME,
            null,
            List.of(CLAIM_EMAIL_VERIFIED), // Single valid claim
            null,
            null);

    // Act
    Response response = updateScope(TENANT_1, SCOPE_EMAIL, updateBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(SCOPE_EMAIL))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_CLAIMS + ".size()", equalTo(1))
        .body(BODY_PARAM_CLAIMS, hasItem(CLAIM_EMAIL_VERIFIED));

    // Validate in database
    Map<String, Object> expectedDbBody = new HashMap<>(createBody);
    expectedDbBody.put(BODY_PARAM_DISPLAY_NAME, TEST_UPDATED_DISPLAY_NAME);
    expectedDbBody.put(BODY_PARAM_CLAIMS, List.of(CLAIM_EMAIL_VERIFIED));
    validateInDb(TENANT_1, expectedDbBody);

    // Cleanup
    deleteScope(TENANT_1, SCOPE_EMAIL);
  }

  // ========== INPUT VALIDATION EDGE CASES ==========

  @Test
  @DisplayName("Should update scope with null displayName")
  public void testUpdateScopeWithNullDisplayName() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(BODY_PARAM_DISPLAY_NAME, null);
    updateBody.put(BODY_PARAM_DESCRIPTION, TEST_UPDATED_DESCRIPTION);

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate - null displayName should be handled gracefully
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(scopeName))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_DISPLAY_NAME)) // Should remain unchanged
        .body(BODY_PARAM_DESCRIPTION, equalTo(TEST_UPDATED_DESCRIPTION));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when updating scope with empty string displayName")
  public void testUpdateScopeWithEmptyDisplayName() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(BODY_PARAM_DISPLAY_NAME, "");
    updateBody.put(BODY_PARAM_DESCRIPTION, TEST_UPDATED_DESCRIPTION);

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate - empty string should be rejected
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString("Display name cannot be empty"));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should return error when updating scope with blank displayName")
  public void testUpdateScopeWithBlankDisplayName() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(BODY_PARAM_DISPLAY_NAME, "   "); // Spaces only
    updateBody.put(BODY_PARAM_DESCRIPTION, TEST_UPDATED_DESCRIPTION);

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate - blank string should be rejected
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(MESSAGE, containsString("Display name cannot be empty"));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should update scope with null description")
  public void testUpdateScopeWithNullDescription() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(BODY_PARAM_DISPLAY_NAME, TEST_UPDATED_DISPLAY_NAME);
    updateBody.put(BODY_PARAM_DESCRIPTION, null);

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate - null description should be handled gracefully
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(scopeName))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_DESCRIPTION, equalTo(TEST_DESCRIPTION)); // Should remain unchanged

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should update scope with empty string description")
  public void testUpdateScopeWithEmptyDescription() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(BODY_PARAM_DISPLAY_NAME, TEST_UPDATED_DISPLAY_NAME);
    updateBody.put(BODY_PARAM_DESCRIPTION, "");

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate - empty string should be accepted
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(scopeName))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_DESCRIPTION, equalTo(""));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should update scope with empty claims list")
  public void testUpdateScopeWithEmptyClaims() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(BODY_PARAM_DISPLAY_NAME, TEST_UPDATED_DISPLAY_NAME);
    updateBody.put(BODY_PARAM_CLAIMS, List.of()); // Empty claims list

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate - empty claims list should be accepted for custom scopes
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(scopeName))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_CLAIMS + ".size()", equalTo(0));

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }

  @Test
  @DisplayName("Should update scope with null claims")
  public void testUpdateScopeWithNullClaims() {
    // Arrange
    String scopeName = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> createBody =
        getValidScopeRequestBody(
            scopeName,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(TEST_EMAIL_CLAIM),
            TEST_ICON_URL,
            false);

    createScope(TENANT_1, createBody);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(BODY_PARAM_DISPLAY_NAME, TEST_UPDATED_DISPLAY_NAME);
    updateBody.put(BODY_PARAM_CLAIMS, null);

    // Act
    Response response = updateScope(TENANT_1, scopeName, updateBody);

    // Validate - null claims should leave existing claims unchanged
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_SCOPE, equalTo(scopeName))
        .body(BODY_PARAM_DISPLAY_NAME, equalTo(TEST_UPDATED_DISPLAY_NAME))
        .body(BODY_PARAM_CLAIMS, hasItem(TEST_EMAIL_CLAIM)); // Should remain unchanged

    // Cleanup
    deleteScope(TENANT_1, scopeName);
  }
}
