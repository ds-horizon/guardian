package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_SMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONTACTS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_FLOW;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_NEW_USER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_META_INFO_V2;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_OTP;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE_V2;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPES;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_TEMPLATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERNAME;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIMS_AMR;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_EXP;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_IAT;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNINUP;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_CREDENTIAL_TYPE_PIN;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_EMAIL_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_EMAIL_2;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_PASSWORD_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_PHONE_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_PHONE_2;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_PIN_2;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_USERNAME_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_USERNAME_2;
import static com.dreamsportslabs.guardian.utils.DbUtils.addDefaultClientScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.addFirstPartyClient;
import static com.dreamsportslabs.guardian.utils.DbUtils.addScope;
import static com.dreamsportslabs.guardian.utils.DbUtils.addThirdPartyClient;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.fusionauth.jwt.JWTDecoder;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class V2MfaSignInIT {

  private static final String TENANT_ID = "tenant1";
  private static final String TEST_SCOPE = "profile";
  private static String firstPartyClientId;
  private static String thirdPartyClientId;
  private WireMockServer wireMockServer;
  private final JWTDecoder decoder = JWT.getDecoder();

  @BeforeAll
  static void setup() {
    addScope(TENANT_ID, TEST_SCOPE);
    firstPartyClientId = addFirstPartyClient(TENANT_ID);
    thirdPartyClientId = addThirdPartyClient(TENANT_ID);
    addDefaultClientScopes(TENANT_ID, firstPartyClientId, TEST_SCOPE);
  }

  @Test
  @DisplayName(
      "V2MfaSignIn - Return valid token when password factor is used after passwordless signin")
  public void returnValidTokenWhenPasswordFactorIsUsedAfterPasswordlessSignin() {
    // Arrange - First sign in with passwordless to get refresh token
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    // Init passwordless flow
    Map<String, Object> initBody = getPasswordlessInitRequestBody(phoneNumber, BODY_CHANNEL_SMS);
    Response initResponse = ApplicationIoUtils.v2PasswordlessInit(TENANT_ID, initBody);
    initResponse.then().statusCode(SC_OK);
    String state = initResponse.jsonPath().getString("state");

    // Complete passwordless flow
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BODY_PARAM_OTP, "999999");

    Response passwordlessResponse =
        ApplicationIoUtils.v2PasswordlessComplete(TENANT_ID, completeBody);
    passwordlessResponse.then().statusCode(SC_OK);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    // Arrange - Setup stub for MFA signin with password
    StubMapping mfaStub =
        stubAuthenticateUserSuccess(
            V2_SIGNIN_TEST_USERNAME_1,
            V2_SIGNIN_TEST_EMAIL_1,
            phoneNumber,
            V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    // Act - MFA signin with password factor
    Response response =
        ApplicationIoUtils.v2MfaSignIn(
            TENANT_ID,
            "password",
            V2_SIGNIN_TEST_USERNAME_1,
            phoneNumber,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            refreshToken,
            List.of(TEST_SCOPE),
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("id_token", notNullValue())
        .body("sso_token", notNullValue())
        .body("expires_in", notNullValue())
        .header("Set-Cookie", notNullValue());

    validateSetCookieTokens(response);

    String accessToken = response.jsonPath().getString("access_token");
    int expiresIn = response.jsonPath().getInt("expires_in");
    validateAccessTokenClaims(
        accessToken,
        TEST_SCOPE,
        TENANT_ID,
        firstPartyClientId,
        expiresIn,
        V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    wireMockServer.removeStub(mfaStub);
  }

  @Test
  @DisplayName("V2MfaSignIn - Return valid token when pin factor is used after passwordless signin")
  public void returnValidTokenWhenPinFactorIsUsedAfterPasswordlessSignin() {
    // Arrange - First sign in with passwordless to get refresh token
    String email = generateRandomEmail();
    StubMapping getUserStub = stubGetUserForPasswordless(null, email, false);

    // Init passwordless flow
    Map<String, Object> initBody = getPasswordlessInitRequestBody(email, BODY_CHANNEL_EMAIL);
    Response initResponse = ApplicationIoUtils.v2PasswordlessInit(TENANT_ID, initBody);
    initResponse.then().statusCode(SC_OK);
    String state = initResponse.jsonPath().getString("state");

    // Complete passwordless flow
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BODY_PARAM_OTP, "999999");

    Response passwordlessResponse =
        ApplicationIoUtils.v2PasswordlessComplete(TENANT_ID, completeBody);
    passwordlessResponse.then().statusCode(SC_OK);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    // Arrange - Setup stub for MFA signin with pin
    StubMapping mfaStub =
        stubAuthenticateUserSuccess(
            V2_SIGNIN_TEST_USERNAME_2,
            email,
            V2_SIGNIN_TEST_PHONE_2,
            V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    // Act - MFA signin with pin factor
    Response response =
        ApplicationIoUtils.v2MfaSignIn(
            TENANT_ID,
            "pin",
            V2_SIGNIN_TEST_USERNAME_2,
            null,
            email,
            null,
            V2_SIGNIN_TEST_PIN_2,
            refreshToken,
            List.of(TEST_SCOPE),
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("id_token", notNullValue())
        .body("sso_token", notNullValue())
        .body("expires_in", notNullValue())
        .header("Set-Cookie", notNullValue());

    validateSetCookieTokens(response);

    String accessToken = response.jsonPath().getString("access_token");
    int expiresIn = response.jsonPath().getInt("expires_in");
    validateAccessTokenClaims(
        accessToken,
        TEST_SCOPE,
        TENANT_ID,
        firstPartyClientId,
        expiresIn,
        V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    wireMockServer.removeStub(mfaStub);
  }

  @Test
  @DisplayName("V2MfaSignIn - Return valid token when using email identifier with password factor")
  public void returnValidTokenWhenUsingEmailIdentifierWithPasswordFactor() {
    // Arrange - First sign in with passwordless
    String email = generateRandomEmail();
    StubMapping getUserStub = stubGetUserForPasswordless(null, email, false);

    // Init passwordless flow
    Map<String, Object> initBody = getPasswordlessInitRequestBody(email, BODY_CHANNEL_EMAIL);
    Response initResponse = ApplicationIoUtils.v2PasswordlessInit(TENANT_ID, initBody);
    initResponse.then().statusCode(SC_OK);
    String state = initResponse.jsonPath().getString("state");

    // Complete passwordless flow
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BODY_PARAM_OTP, "999999");

    Response passwordlessResponse =
        ApplicationIoUtils.v2PasswordlessComplete(TENANT_ID, completeBody);
    passwordlessResponse.then().statusCode(SC_OK);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    // Arrange - Setup stub for MFA signin
    StubMapping mfaStub =
        stubAuthenticateUserSuccess(
            V2_SIGNIN_TEST_USERNAME_1,
            email,
            V2_SIGNIN_TEST_PHONE_1,
            V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    // Act
    Response response =
        ApplicationIoUtils.v2MfaSignIn(
            TENANT_ID,
            "password",
            null,
            null,
            email,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            refreshToken,
            List.of(TEST_SCOPE),
            firstPartyClientId);

    // Assert
    response.then().statusCode(SC_OK).body("access_token", notNullValue());

    wireMockServer.removeStub(mfaStub);
  }

  @Test
  @DisplayName(
      "V2MfaSignIn - Return valid token when using phone number identifier with pin factor")
  public void returnValidTokenWhenUsingPhoneNumberIdentifierWithPinFactor() {
    // Arrange - First sign in with passwordless
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    // Init passwordless flow
    Map<String, Object> initBody = getPasswordlessInitRequestBody(phoneNumber, BODY_CHANNEL_SMS);
    Response initResponse = ApplicationIoUtils.v2PasswordlessInit(TENANT_ID, initBody);
    initResponse.then().statusCode(SC_OK);
    String state = initResponse.jsonPath().getString("state");

    // Complete passwordless flow
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BODY_PARAM_OTP, "999999");

    Response passwordlessResponse =
        ApplicationIoUtils.v2PasswordlessComplete(TENANT_ID, completeBody);
    passwordlessResponse.then().statusCode(SC_OK);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    // Arrange - Setup stub for MFA signin
    StubMapping mfaStub =
        stubAuthenticateUserSuccess(
            V2_SIGNIN_TEST_USERNAME_2,
            V2_SIGNIN_TEST_EMAIL_2,
            phoneNumber,
            V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    // Act
    Response response =
        ApplicationIoUtils.v2MfaSignIn(
            TENANT_ID,
            "pin",
            null,
            phoneNumber,
            null,
            null,
            V2_SIGNIN_TEST_PIN_2,
            refreshToken,
            List.of(TEST_SCOPE),
            firstPartyClientId);

    // Assert
    response.then().statusCode(SC_OK).body("access_token", notNullValue());

    wireMockServer.removeStub(mfaStub);
  }

  @Test
  @DisplayName("V2MfaSignIn - Return error when refresh token is invalid")
  public void returnErrorWhenRefreshTokenIsInvalid() {
    // Act
    Response response =
        ApplicationIoUtils.v2MfaSignIn(
            TENANT_ID,
            "password",
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            "invalid_refresh_token",
            List.of(TEST_SCOPE),
            firstPartyClientId);

    // Assert
    response.then().statusCode(SC_UNAUTHORIZED).rootPath(ERROR).body("code", notNullValue());
  }

  @Test
  @DisplayName("V2MfaSignIn - Return error when refresh token is missing")
  public void returnErrorWhenRefreshTokenIsMissing() {
    // Act
    Response response =
        ApplicationIoUtils.v2MfaSignIn(
            TENANT_ID,
            "password",
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            null,
            List.of(TEST_SCOPE),
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2MfaSignIn - Return error when factor is missing")
  public void returnErrorWhenFactorIsMissing() {
    // Arrange - Get a valid refresh token from passwordless
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Map<String, Object> initBody = getPasswordlessInitRequestBody(phoneNumber, BODY_CHANNEL_SMS);
    Response initResponse = ApplicationIoUtils.v2PasswordlessInit(TENANT_ID, initBody);
    initResponse.then().statusCode(SC_OK);
    String state = initResponse.jsonPath().getString("state");

    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BODY_PARAM_OTP, "999999");

    Response passwordlessResponse =
        ApplicationIoUtils.v2PasswordlessComplete(TENANT_ID, completeBody);
    passwordlessResponse.then().statusCode(SC_OK);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    // Act - Call MFA signin without factor (passing null as factor)
    Response response =
        ApplicationIoUtils.v2MfaSignIn(
            TENANT_ID,
            null,
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            refreshToken,
            List.of(TEST_SCOPE),
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2MfaSignIn - Return error when client_id is missing")
  public void returnErrorWhenClientIdIsMissing() {
    // Arrange - Get a valid refresh token from passwordless
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Map<String, Object> initBody = getPasswordlessInitRequestBody(phoneNumber, BODY_CHANNEL_SMS);
    Response initResponse = ApplicationIoUtils.v2PasswordlessInit(TENANT_ID, initBody);
    initResponse.then().statusCode(SC_OK);
    String state = initResponse.jsonPath().getString("state");

    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BODY_PARAM_OTP, "999999");

    Response passwordlessResponse =
        ApplicationIoUtils.v2PasswordlessComplete(TENANT_ID, completeBody);
    passwordlessResponse.then().statusCode(SC_OK);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    // Act
    Response response =
        ApplicationIoUtils.v2MfaSignIn(
            TENANT_ID,
            "password",
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            refreshToken,
            List.of(TEST_SCOPE),
            null);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2MfaSignIn - Return error when using same factor category as initial signin")
  public void returnErrorWhenUsingSameFactorCategoryAsInitialSignin() {
    // Arrange - First sign in with passwordless (OTP - POSSESSION category)
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Map<String, Object> initBody = getPasswordlessInitRequestBody(phoneNumber, BODY_CHANNEL_SMS);
    Response initResponse = ApplicationIoUtils.v2PasswordlessInit(TENANT_ID, initBody);
    initResponse.then().statusCode(SC_OK);
    String state = initResponse.jsonPath().getString("state");

    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BODY_PARAM_OTP, "999999");

    Response passwordlessResponse =
        ApplicationIoUtils.v2PasswordlessComplete(TENANT_ID, completeBody);
    passwordlessResponse.then().statusCode(SC_OK);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    // Act - Try to use password factor (KNOWLEDGE category) - this should work
    // But if we try to use another KNOWLEDGE factor after password, it should fail
    // First enroll password
    StubMapping getUserStubForEnroll = stubGetUserSuccess(V2_SIGNIN_TEST_USERNAME_1, false, false);
    StubMapping updateUserStub = stubUpdateUserSuccess(V2_SIGNIN_TEST_USERNAME_1);

    Response enrollResponse =
        ApplicationIoUtils.v2MfaEnroll(
            TENANT_ID,
            "password",
            V2_SIGNIN_TEST_USERNAME_1,
            phoneNumber,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            refreshToken,
            List.of(TEST_SCOPE),
            firstPartyClientId);

    enrollResponse.then().statusCode(SC_OK);
    String newRefreshToken = enrollResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStubForEnroll);
    wireMockServer.removeStub(updateUserStub);

    // Now try to use PIN (same KNOWLEDGE category) - should fail
    StubMapping mfaStub =
        stubAuthenticateUserSuccess(
            V2_SIGNIN_TEST_USERNAME_1,
            V2_SIGNIN_TEST_EMAIL_1,
            phoneNumber,
            V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    Response response =
        ApplicationIoUtils.v2MfaSignIn(
            TENANT_ID,
            "pin",
            V2_SIGNIN_TEST_USERNAME_1,
            phoneNumber,
            null,
            null,
            V2_SIGNIN_TEST_PIN_2,
            newRefreshToken,
            List.of(TEST_SCOPE),
            firstPartyClientId);

    // Assert
    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body("code", notNullValue());

    wireMockServer.removeStub(mfaStub);
  }

  @Test
  @DisplayName("V2MfaSignIn - Return error when credentials are incorrect")
  public void returnErrorWhenCredentialsAreIncorrect() {
    // Arrange - First sign in with passwordless
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Map<String, Object> initBody = getPasswordlessInitRequestBody(phoneNumber, BODY_CHANNEL_SMS);
    Response initResponse = ApplicationIoUtils.v2PasswordlessInit(TENANT_ID, initBody);
    initResponse.then().statusCode(SC_OK);
    String state = initResponse.jsonPath().getString("state");

    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BODY_PARAM_OTP, "999999");

    Response passwordlessResponse =
        ApplicationIoUtils.v2PasswordlessComplete(TENANT_ID, completeBody);
    passwordlessResponse.then().statusCode(SC_OK);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    // Arrange - Setup stub for incorrect credentials
    StubMapping mfaStub = stubAuthenticateUserIncorrectCredentials();

    // Act - MFA signin with wrong password
    Response response =
        ApplicationIoUtils.v2MfaSignIn(
            TENANT_ID,
            "password",
            V2_SIGNIN_TEST_USERNAME_1,
            phoneNumber,
            null,
            "wrongpassword",
            null,
            refreshToken,
            List.of(TEST_SCOPE),
            firstPartyClientId);

    // Assert
    response.then().statusCode(SC_UNAUTHORIZED).rootPath(ERROR).body("code", notNullValue());

    wireMockServer.removeStub(mfaStub);
  }

  // ========== Helper Methods ==========

  /** Helper to build passwordless init request body */
  private Map<String, Object> getPasswordlessInitRequestBody(String identifier, String channel) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_SCOPES, List.of(TEST_SCOPE));
    requestBody.put(BODY_PARAM_FLOW, PASSWORDLESS_FLOW_SIGNINUP);
    requestBody.put(BODY_PARAM_RESPONSE_TYPE_V2, BODY_PARAM_RESPONSE_TYPE_TOKEN);
    requestBody.put(BODY_PARAM_META_INFO_V2, getMetaInfo());

    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, channel);
    contact.put(BODY_PARAM_IDENTIFIER, identifier);
    contact.put(BODY_PARAM_TEMPLATE, getTemplate());
    requestBody.put(BODY_PARAM_CONTACTS, List.of(contact));

    return requestBody;
  }

  /** Helper to get meta info for passwordless */
  private Map<String, Object> getMetaInfo() {
    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put("deviceName", "testDevice");
    metaInfo.put("location", "testLocation");
    return metaInfo;
  }

  /** Helper to get template for passwordless */
  private Map<String, Object> getTemplate() {
    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, "otp");
    return template;
  }

  /** Stub for getUser call during passwordless init */
  private StubMapping stubGetUserForPasswordless(
      String phoneNumber, String email, boolean isNewUser) {
    JsonObject responseBody = new JsonObject();
    if (isNewUser) {
      // For new users, return empty object or minimal data
      responseBody.put(BODY_PARAM_IS_NEW_USER, true);
    } else {
      // For existing users, return user with userId
      String userId = RandomStringUtils.randomAlphanumeric(10);
      responseBody.put(BODY_PARAM_USERID, userId);
      responseBody.put(BODY_PARAM_USERNAME, V2_SIGNIN_TEST_USERNAME_1);
      responseBody.put(BODY_PARAM_NAME, "John Doe");
      if (phoneNumber != null) {
        responseBody.put(BODY_PARAM_PHONE_NUMBER, phoneNumber);
      }
      if (email != null) {
        responseBody.put(BODY_PARAM_EMAIL, email);
      }
    }

    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody.encode())));
  }

  /** Stub for successful authentication of registered user */
  private StubMapping stubAuthenticateUserSuccess(
      String username, String email, String phoneNumber, String credentialType) {
    JsonObject responseBody =
        new JsonObject()
            .put("email", email)
            .put("phoneNumber", phoneNumber)
            .put("username", username)
            .put("passwordSet", V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD.equals(credentialType))
            .put("pinSet", V2_SIGNIN_CREDENTIAL_TYPE_PIN.equals(credentialType))
            .put("userId", RandomStringUtils.randomAlphanumeric(10));

    return wireMockServer.stubFor(
        post(urlPathMatching("/authenticateUser"))
            .willReturn(
                aResponse()
                    .withStatus(SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody.encode())));
  }

  /** Stub for registered user with incorrect credentials */
  private StubMapping stubAuthenticateUserIncorrectCredentials() {
    JsonObject error =
        new JsonObject()
            .put("code", "User-Error")
            .put("message", "Unauthorized")
            .put("cause", "Unauthorized")
            .put("details", new JsonObject());

    JsonObject responseBody = new JsonObject().put("error", error);

    return wireMockServer.stubFor(
        post(urlPathMatching("/authenticateUser"))
            .willReturn(
                aResponse()
                    .withStatus(SC_UNAUTHORIZED)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody.encode())));
  }

  /** Stub for successful getUser call */
  private StubMapping stubGetUserSuccess(String username, boolean passwordSet, boolean pinSet) {
    JsonObject responseBody =
        new JsonObject()
            .put("username", username)
            .put("email", V2_SIGNIN_TEST_EMAIL_1)
            .put("phoneNumber", V2_SIGNIN_TEST_PHONE_1)
            .put("passwordSet", passwordSet)
            .put("pinSet", pinSet)
            .put("userId", RandomStringUtils.randomAlphanumeric(10));

    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody.encode())));
  }

  /** Stub for successful updateUser call */
  private StubMapping stubUpdateUserSuccess(String username) {
    JsonObject responseBody =
        new JsonObject()
            .put("username", username)
            .put("email", V2_SIGNIN_TEST_EMAIL_1)
            .put("phoneNumber", V2_SIGNIN_TEST_PHONE_1)
            .put("userId", RandomStringUtils.randomAlphanumeric(10));

    return wireMockServer.stubFor(
        post(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody.encode())));
  }

  // Helper method to validate Set-Cookie values match response body tokens
  private void validateSetCookieTokens(Response response) {
    String accessToken = response.jsonPath().getString("access_token");
    String refreshToken = response.jsonPath().getString("refresh_token");
    String ssoToken = response.jsonPath().getString("sso_token");

    assertThat(
        "AT cookie should match access_token", response.getCookie("AT"), equalTo(accessToken));
    assertThat(
        "RT cookie should match refresh_token", response.getCookie("RT"), equalTo(refreshToken));
    assertThat("SSOT cookie should match sso_token", response.getCookie("SSOT"), equalTo(ssoToken));
  }

  // JWT validation method
  private void validateAccessTokenClaims(
      String accessToken,
      String scope,
      String tenantId,
      String clientId,
      int expiresIn,
      String credentialType) {
    Path publicKeyPath = Paths.get("src/test/resources/test-data/tenant1-public-key.pem");
    JWT jwt = decoder.decode(accessToken, RSAVerifier.newVerifier(publicKeyPath));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(claims.get(JWT_CLAIM_SUB), notNullValue());
    assertThat(claims.get(JWT_CLAIM_SCOPE), equalTo(scope));
    assertThat(claims.get(JWT_CLAIM_TENANT_ID), equalTo(tenantId));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(clientId));

    // Validate expiry matches expires_in from response
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo((long) expiresIn));

    // Validate AMR claim
    @SuppressWarnings("unchecked")
    List<String> amr = (List<String>) claims.get(JWT_CLAIMS_AMR);
    String expectedAmr =
        V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD.equals(credentialType)
            ? "pwd"
            : V2_SIGNIN_CREDENTIAL_TYPE_PIN;
    assertThat(amr.size(), equalTo(1));
    assertThat(amr.get(0), equalTo(expectedAmr));
  }

  private String generateRandomPhoneNumber() {
    return "9" + String.format("%09d", (new Random()).nextInt(1000000000));
  }

  private String generateRandomEmail() {
    return "test" + System.currentTimeMillis() + "@example.com";
  }
}
