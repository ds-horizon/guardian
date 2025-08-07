package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_SMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONTACTS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DEVICE_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_FLOW;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_NEW_USER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_LOCATION;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_META_INFO;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_PARAMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_TEMPLATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERNAME;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_STATE;
import static com.dreamsportslabs.guardian.Constants.ERROR_RESENDS_EXHAUSTED;
import static com.dreamsportslabs.guardian.Constants.ERROR_RESENDS_NOT_ALLOWED;
import static com.dreamsportslabs.guardian.Constants.ERROR_USER_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_USER_NOT_EXISTS;
import static com.dreamsportslabs.guardian.Constants.METADATA;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNIN;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNINUP;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNUP;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_CONTACTS;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_CONTACTS_TEMPLATE;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_CONTACTS_TEMPLATE_NAME;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_CREATED_AT_EPOCH;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_EXPIRY;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_FLOW;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_IS_NEW_USER;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_IS_OTP_MOCKED;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_RESENDS;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_RESEND_AFTER;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_STATE;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_TRIES;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_MODEL_USER;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_IS_NEW_USER;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RESENDS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RESENDS_LEFT;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RESEND_AFTER;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RETRIES_LEFT;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_TRIES;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.passwordlessInit;
import static com.dreamsportslabs.guardian.utils.DbUtils.createState;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.utils.DbUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.CombinableMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PasswordlessInitIT {
  public static String tenant1 = "tenant1"; // OTP is mocked for this tenant
  public static String tenant2 = "tenant2"; // OTP is not mocked for this tenant
  private WireMockServer wireMockServer;

  @Test
  @DisplayName("Should return error when phone number is not provided")
  public void testPhoneNumberMissing() {
    // Arrange
    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_SMS, null, PASSWORDLESS_FLOW_SIGNINUP, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should return error when email is not provided")
  public void testEmailMissing() {
    // Arrange
    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_EMAIL, null, PASSWORDLESS_FLOW_SIGNINUP, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should create state for a new user (phoneNumber doesn't exist) in SIGNINUP flow")
  public void testNewUserPhoneSigninup() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(true));

    String state = response.then().extract().path(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForNewUser(state, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should handle existing user when valid phoneNumber exists in SIGNINUP flow")
  public void testExistingUserPhoneNumberSigninup() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(phoneNumber, null);

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, phoneNumber, null, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should create new user when valid SMS number doesn't exist in SIGNUP flow")
  public void testNewUserSmsSignup() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(true));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForNewUser(state, PASSWORDLESS_FLOW_SIGNUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return error when existing phone number is used in SIGNUP flow")
  public void testExistingPhoneNumberSignup() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> requestBodyInit =
        getRequestBodyInit(
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(phoneNumber, null);

    // Act
    Response response = passwordlessInit(tenant1, requestBodyInit);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_USER_EXISTS));

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return error when non-existent SMS number is used in SIGNIN flow")
  public void testNonExistentSmsSignin() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNIN,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_USER_NOT_EXISTS));

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should handle existing user when valid SMS number exists in SIGNIN flow")
  public void testExistingUserSmsSignin() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNIN,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(phoneNumber, null);

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, phoneNumber, null, PASSWORDLESS_FLOW_SIGNIN);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should handle existing user when valid SMS number exists without flow")
  public void testExistingUserSmsNoFlow() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> requestBody =
        getRequestBodyWithoutFlow(BODY_CHANNEL_SMS, phoneNumber, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(phoneNumber, null);

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, phoneNumber, null, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should create new user when valid SMS number doesn't exist without flow")
  public void testNewUserSmsNoFlow() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> requestBody =
        getRequestBodyWithoutFlow(BODY_CHANNEL_SMS, phoneNumber, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(true));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForNewUser(state, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return error when tenant-id header is missing")
  public void testMissingTenantId() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = passwordlessInit(null, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should return error when non-existent tenant-id is provided")
  public void testNonExistentTenantId() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = passwordlessInit("random", requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should create new user when valid email doesn't exist without flow")
  public void testNewUserEmailNoFlow() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> requestBody =
        getRequestBodyWithoutFlow(BODY_CHANNEL_EMAIL, email, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(true));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForNewUser(state, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should handle existing user when valid email exists without flow")
  public void testExistingUserEmailNoFlow() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> requestBody =
        getRequestBodyWithoutFlow(BODY_CHANNEL_EMAIL, email, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(null, email);

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, null, email, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should create new user when valid email doesn't exist in SIGNINUP flow")
  public void testNewUserEmailSigninup() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_EMAIL, email, PASSWORDLESS_FLOW_SIGNINUP, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(true));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForNewUser(state, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should handle existing user when valid email exists in SIGNINUP flow")
  public void testExistingUserEmailSigninup() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_EMAIL, email, PASSWORDLESS_FLOW_SIGNINUP, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(null, email);

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, null, email, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should create new user when valid email doesn't exist in SIGNUP flow")
  public void testNewUserEmailSignup() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_EMAIL, email, PASSWORDLESS_FLOW_SIGNUP, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(true));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForNewUser(state, PASSWORDLESS_FLOW_SIGNUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return error when existing email is used in SIGNUP flow")
  public void testExistingEmailSignup() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_EMAIL, email, PASSWORDLESS_FLOW_SIGNUP, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(null, email);

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_USER_EXISTS));

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return error when non-existent email is used in SIGNIN flow")
  public void testNonExistentEmailSignin() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_EMAIL, email, PASSWORDLESS_FLOW_SIGNIN, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_USER_NOT_EXISTS));

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should handle existing user when valid email exists in SIGNIN flow")
  public void testExistingUserEmailSignin() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_EMAIL, email, PASSWORDLESS_FLOW_SIGNIN, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(null, email);

    // Act
    Response response = passwordlessInit(tenant1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, null, email, PASSWORDLESS_FLOW_SIGNIN);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should decrease resendsLeft when request is made within OTP validity time")
  public void testResendsLeftDecrease() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    addStateInRedis(
        state,
        60,
        BODY_CHANNEL_EMAIL,
        0,
        0,
        System.currentTimeMillis() / 1000 - 30,
        0,
        PASSWORDLESS_FLOW_SIGNINUP,
        900);

    // Act
    Response response = passwordlessInit(tenant1, state);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(1))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(4))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, equalTo(state))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));
  }

  @Test
  @DisplayName("Should prevent API call within OTP resend timeframe")
  public void testPreventQuickResend() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_EMAIL, email, PASSWORDLESS_FLOW_SIGNINUP, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(null, email);

    Response response1 = passwordlessInit(tenant1, requestBody);
    response1
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));
    String state = response1.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);

    // Act
    Response response2 = passwordlessInit(tenant1, state);

    // Validate
    response2
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RESENDS_NOT_ALLOWED))
        .appendRootPath(METADATA)
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class));

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should reset resendsLeft after OTP validity time")
  public void testResendsLeftReset() throws InterruptedException {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    addStateInRedis(
        state,
        60,
        BODY_CHANNEL_EMAIL,
        0,
        0,
        System.currentTimeMillis() / 1000 - 30,
        0,
        PASSWORDLESS_FLOW_SIGNINUP,
        -10);

    // Act
    Response response = passwordlessInit(tenant1, state);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_STATE));
  }

  @Test
  @DisplayName("Should return error when resend attempts are exhausted")
  public void testResendsExhausted() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    addStateInRedis(
        state,
        60,
        BODY_CHANNEL_EMAIL,
        0,
        5,
        System.currentTimeMillis() / 1000 + 30,
        30,
        PASSWORDLESS_FLOW_SIGNINUP,
        5);

    // Act
    Response response = passwordlessInit(tenant1, state);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RESENDS_EXHAUSTED));
  }

  @Test
  @DisplayName("Should handle mocked OTP without template for email channel")
  public void testMockedOtpEmailNoTemplate() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> contacts = new HashMap<>();
    contacts.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_EMAIL);
    contacts.put(BODY_PARAM_IDENTIFIER, email);

    StubMapping stub = getStubForExistingUser(null, email);

    // Act
    Response response =
        passwordlessInit(
            tenant1,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN,
            List.of(contacts),
            new HashMap<>(),
            new HashMap<>());

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, null, email, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should handle mocked OTP without template for SMS channel")
  public void testMockedOtpSmsNoTemplate() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> contacts = new HashMap<>();
    contacts.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_SMS);
    contacts.put(BODY_PARAM_IDENTIFIER, phoneNumber);

    StubMapping stub = getStubForExistingUser(phoneNumber, null);

    // Act
    Response response =
        passwordlessInit(
            tenant1,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN,
            List.of(contacts),
            new HashMap<>(),
            new HashMap<>());

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, phoneNumber, null, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should handle non-mocked OTP with template for email channel")
  public void testNonMockedOtpEmailWithTemplate() {
    // Arrange
    String email = generateRandomEmail();
    Map<String, String> params = Map.of("hash", "1234");

    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, "otp");
    template.put(BODY_PARAM_PARAMS, params);

    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_EMAIL);
    contact.put(BODY_PARAM_IDENTIFIER, email);
    contact.put(BODY_PARAM_TEMPLATE, template);

    StubMapping userStub = getStubForExistingUser(null, email);
    StubMapping sendEmailStub = getStubForSendEmail();

    // Act
    Response response =
        passwordlessInit(
            tenant2,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN,
            List.of(contact),
            new HashMap<>(),
            new HashMap<>());
    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    JsonObject redisValue = DbUtils.getState(state, tenant2);
    assertThat(redisValue, notNullValue());
    assertThat(
        redisValue
            .getJsonArray(PASSWORDLESS_MODEL_CONTACTS)
            .getJsonObject(0)
            .getJsonObject(PASSWORDLESS_MODEL_CONTACTS_TEMPLATE)
            .getString(PASSWORDLESS_MODEL_CONTACTS_TEMPLATE_NAME),
        equalTo("otp"));

    // cleanup
    wireMockServer.removeStub(userStub);
    wireMockServer.removeStub(sendEmailStub);
  }

  @Test
  @DisplayName("Should handle non-mocked OTP with default template for email channel")
  public void testNonMockedOtpEmailDefaultTemplate() {
    // Arrange
    String email = generateRandomEmail();

    Map<String, Object> contacts = new HashMap<>();
    contacts.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_EMAIL);
    contacts.put(BODY_PARAM_IDENTIFIER, email);

    StubMapping userStub = getStubForExistingUser(null, email);
    StubMapping sendEmailStub = getStubForSendEmail();

    // Act
    Response response =
        passwordlessInit(
            tenant2,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN,
            List.of(contacts),
            new HashMap<>(),
            new HashMap<>());
    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    JsonObject redisValue = DbUtils.getState(state, tenant2);
    assertThat(redisValue, notNullValue());
    assertThat(
        redisValue
            .getJsonArray(PASSWORDLESS_MODEL_CONTACTS)
            .getJsonObject(0)
            .getJsonObject(PASSWORDLESS_MODEL_CONTACTS_TEMPLATE)
            .getString(PASSWORDLESS_MODEL_CONTACTS_TEMPLATE_NAME),
        equalTo("otp"));

    // cleanup
    wireMockServer.removeStub(userStub);
    wireMockServer.removeStub(sendEmailStub);
  }

  @Test
  @DisplayName("Should handle non-mocked OTP with template for SMS channel")
  public void testNonMockedOtpSmsWithTemplate() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, String> params = Map.of("hash", "1234");

    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, "otp_sms");
    template.put(BODY_PARAM_PARAMS, params);

    Map<String, Object> contacts = new HashMap<>();
    contacts.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_SMS);
    contacts.put(BODY_PARAM_IDENTIFIER, phoneNumber);
    contacts.put(BODY_PARAM_TEMPLATE, template);

    StubMapping userStub = getStubForExistingUser(phoneNumber, null);
    StubMapping sendEmailStub = getStubForSendSms();

    // Act
    Response response =
        passwordlessInit(
            tenant2,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN,
            List.of(contacts),
            new HashMap<>(),
            new HashMap<>());
    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    JsonObject redisValue = DbUtils.getState(state, tenant2);
    assertThat(
        redisValue
            .getJsonArray("contacts")
            .getJsonObject(0)
            .getJsonObject("template")
            .getString("name"),
        equalTo("otp_sms"));

    // cleanup
    wireMockServer.removeStub(userStub);
    wireMockServer.removeStub(sendEmailStub);
  }

  @Test
  @DisplayName("Should handle non-mocked OTP with default template for SMS channel")
  public void testNonMockedOtpSmsDefaultTemplate() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> contacts = new HashMap<>();
    contacts.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_SMS);
    contacts.put(BODY_PARAM_IDENTIFIER, phoneNumber);

    StubMapping userStub = getStubForExistingUser(phoneNumber, null);
    StubMapping sendEmailStub = getStubForSendSms();

    // Act
    Response response =
        passwordlessInit(
            tenant2,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN,
            List.of(contacts),
            new HashMap<>(),
            new HashMap<>());
    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    JsonObject redisValue = DbUtils.getState(state, tenant2);
    assertThat(
        redisValue
            .getJsonArray("contacts")
            .getJsonObject(0)
            .getJsonObject("template")
            .getString("name"),
        equalTo("otp"));

    // cleanup
    wireMockServer.removeStub(userStub);
    wireMockServer.removeStub(sendEmailStub);
  }

  @Test
  @DisplayName("Should handle multiple valid contacts with different channels")
  public void testMultipleContactsDifferentChannels() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    String email = generateRandomEmail();

    Map<String, Object> contactSms = new HashMap<>();
    contactSms.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_SMS);
    contactSms.put(BODY_PARAM_IDENTIFIER, phoneNumber);

    Map<String, Object> contactEmail = new HashMap<>();
    contactEmail.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_EMAIL);
    contactEmail.put(BODY_PARAM_IDENTIFIER, email);

    StubMapping userStub = getStubForExistingUser(phoneNumber, email);

    // Act
    Response response =
        passwordlessInit(
            tenant1,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN,
            List.of(contactSms, contactEmail),
            new HashMap<>(),
            new HashMap<>());
    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER, equalTo(false));

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, phoneNumber, email, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(userStub);
  }

  @Test
  @DisplayName("Should return error for multiple contacts with same channel")
  public void testMultipleContactsSameChannel() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();

    Map<String, Object> contactSms = new HashMap<>();
    contactSms.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_SMS);
    contactSms.put(BODY_PARAM_IDENTIFIER, phoneNumber);

    Map<String, Object> contactEmail = new HashMap<>();
    contactEmail.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_SMS);
    contactEmail.put(BODY_PARAM_IDENTIFIER, generateRandomPhoneNumber());

    StubMapping userStub = getStubForExistingUser(phoneNumber, null);

    // Act
    Response response =
        passwordlessInit(
            tenant1,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN,
            List.of(contactSms, contactEmail),
            new HashMap<>(),
            new HashMap<>());
    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));

    // cleanup
    wireMockServer.removeStub(userStub);
  }

  @Test
  @DisplayName("Should use configured TTL value instead of any other value")
  public void testConfiguredTtlValue() {
    // Arrange
    String email = generateRandomEmail();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            BODY_CHANNEL_EMAIL, email, PASSWORDLESS_FLOW_SIGNINUP, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(null, email);

    // Act
    Response response = passwordlessInit(tenant1, requestBody);
    response.then().statusCode(SC_OK);

    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);

    // validate
    long ttl = DbUtils.getStateTtl(state, tenant1);
    assertThat(ttl, greaterThanOrEqualTo(899L));
    assertThat(ttl, lessThanOrEqualTo(900L));

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should not extend TTL when resending OTP to existing state")
  public void testTtlNotExtendedOnResend() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);

    addStateInRedis(
        state,
        60,
        BODY_CHANNEL_EMAIL,
        0,
        0,
        System.currentTimeMillis() / 1000 - 30,
        0,
        PASSWORDLESS_FLOW_SIGNINUP,
        60);

    long initialTtl = DbUtils.getStateTtl(state, tenant1);
    assertThat(initialTtl, equalTo(60L));

    // Act
    Response response = passwordlessInit(tenant1, state);
    response.then().statusCode(SC_OK);

    long ttlAfterApiCall = DbUtils.getStateTtl(state, tenant1);

    // Validate
    assertThat(ttlAfterApiCall, greaterThanOrEqualTo(initialTtl - 1));
    assertThat(ttlAfterApiCall, lessThanOrEqualTo(initialTtl));
  }

  public Map<String, Object> getRequestBodyInit(
      String channel, String identifier, String flow, String responseType) {
    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put(BODY_PARAM_DEVICE_NAME, "testDevice");

    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, "otp");

    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, channel);
    contact.put(BODY_PARAM_IDENTIFIER, identifier);
    contact.put(BODY_PARAM_TEMPLATE, template);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_FLOW, flow);
    requestBody.put(BODY_PARAM_RESPONSE_TYPE, responseType);
    requestBody.put(BODY_PARAM_META_INFO, metaInfo);
    requestBody.put(BODY_PARAM_CONTACTS, List.of(contact));

    return requestBody;
  }

  public Map<String, Object> getRequestBodyWithoutFlow(
      String channel, String identifier, String responseType) {
    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put(BODY_PARAM_DEVICE_NAME, "testDevice");

    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, "otp");

    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, channel);
    contact.put(BODY_PARAM_IDENTIFIER, identifier);
    contact.put(BODY_PARAM_TEMPLATE, template);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_RESPONSE_TYPE, responseType);
    requestBody.put(BODY_PARAM_META_INFO, metaInfo);
    requestBody.put(BODY_PARAM_CONTACTS, List.of(contact));

    return requestBody;
  }

  private String generateRandomPhoneNumber() {
    return "9" + String.format("%09d", (new Random()).nextInt(1000000000));
  }

  private String generateRandomEmail() {
    return "test" + System.currentTimeMillis() + "@example.com";
  }

  private StubMapping getStubForExistingUser(String phoneNumber, String email) {
    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, "application/json")
                    .withBody(
                        "{\""
                            + BODY_PARAM_NAME
                            + "\": \"John Doe\", \""
                            + BODY_PARAM_EMAIL
                            + "\": \""
                            + email
                            + "\", \""
                            + BODY_PARAM_PHONE_NUMBER
                            + "\": \""
                            + phoneNumber
                            + "\", \""
                            + BODY_PARAM_USERID
                            + "\": \"1\", \""
                            + BODY_PARAM_USERNAME
                            + "\": \"user1\"}")));
  }

  private StubMapping getStubForNonExistingUser() {
    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, "application/json")
                    .withBody("{}")));
  }

  private StubMapping getStubForSendEmail() {
    return wireMockServer.stubFor(
        post(urlPathMatching("/sendEmail"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, "application/json")
                    .withBody("{}")));
  }

  private StubMapping getStubForSendSms() {
    return wireMockServer.stubFor(
        post(urlPathMatching("/sendSms"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, "application/json")
                    .withBody("{}")));
  }

  private void addStateInRedis(
      String state,
      int ttlSec,
      String channel,
      int tries,
      int resends,
      long resendAfter,
      int otpResendInterval,
      String flow,
      int expiry) {
    Map<String, Object> user = new HashMap<>();
    user.put(BODY_PARAM_NAME, "John Deo");
    user.put(BODY_PARAM_USERID, 1);
    user.put(BODY_PARAM_USERNAME, "TestUser");
    user.put(BODY_PARAM_IS_NEW_USER, false);

    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, channel);

    if (channel.equalsIgnoreCase(BODY_CHANNEL_SMS)) {
      String phoneNuber = generateRandomPhoneNumber();
      user.put(BODY_PARAM_PHONE_NUMBER, phoneNuber);
      contact.put(BODY_PARAM_IDENTIFIER, phoneNuber);
    } else {
      String email = generateRandomEmail();
      user.put(BODY_CHANNEL_EMAIL, email);
      contact.put(BODY_PARAM_IDENTIFIER, email);
    }

    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put(BODY_PARAM_DEVICE_NAME, "testDevice");
    metaInfo.put(BODY_PARAM_LOCATION, "testLocation");

    createState(
        tenant1,
        state,
        ttlSec,
        "999999",
        true,
        tries,
        resends,
        resendAfter,
        otpResendInterval,
        5,
        5,
        user,
        List.of(contact),
        flow,
        BODY_PARAM_RESPONSE_TYPE_TOKEN,
        metaInfo,
        new HashMap<>(),
        Instant.now().toEpochMilli(),
        System.currentTimeMillis() / 1000 + expiry);
  }

  private static CombinableMatcher<Long> isInRange(Long startRange, Long endRange) {
    return both(greaterThanOrEqualTo(startRange)).and(lessThanOrEqualTo(endRange));
  }

  private static void assertRedisStateForExistingUser(
      String state, String phoneNumber, String email, String flow) {
    JsonObject redisValue = DbUtils.getState(state, tenant1);

    long currentTimeInMilli = Instant.now().toEpochMilli();

    JsonObject user = new JsonObject();
    user.put(BODY_PARAM_NAME, "John Doe");
    user.put(BODY_PARAM_EMAIL, "" + email);
    user.put(BODY_PARAM_PHONE_NUMBER, "" + phoneNumber);
    user.put(BODY_PARAM_USERID, "1");
    user.put(BODY_PARAM_USERNAME, "user1");
    user.put(PASSWORDLESS_MODEL_IS_NEW_USER, false);

    assertThat(redisValue.getString(PASSWORDLESS_MODEL_STATE), equalTo(state));
    assertThat(redisValue.getBoolean(PASSWORDLESS_MODEL_IS_OTP_MOCKED), equalTo(true));
    assertThat(redisValue.getInteger(PASSWORDLESS_MODEL_TRIES), equalTo(0));
    assertThat(redisValue.getInteger(PASSWORDLESS_MODEL_RESENDS), equalTo(0));
    assertThat(
        redisValue.getLong(PASSWORDLESS_MODEL_RESEND_AFTER),
        isInRange(currentTimeInMilli / 1000, currentTimeInMilli / 1000 + 30));
    assertThat(redisValue.getString(PASSWORDLESS_MODEL_FLOW), equalTo(flow));
    assertThat(
        redisValue.getString(PASSWORDLESS_MODEL_RESPONSE_TYPE),
        equalTo(BODY_PARAM_RESPONSE_TYPE_TOKEN));
    assertThat(
        redisValue.getLong(PASSWORDLESS_MODEL_EXPIRY),
        isInRange(currentTimeInMilli / 1000, currentTimeInMilli / 1000 + 900));
    assertThat(
        redisValue.getLong(PASSWORDLESS_MODEL_CREATED_AT_EPOCH),
        isInRange(currentTimeInMilli - 5000, currentTimeInMilli));
    assertThat(redisValue.getJsonObject(PASSWORDLESS_MODEL_USER), equalTo(user));
  }

  private static void assertRedisStateForNewUser(String state, String flow) {
    JsonObject passwordlessModel = DbUtils.getState(state, tenant1);
    assertThat(passwordlessModel, notNullValue());

    long currentTimeInMilli = Instant.now().toEpochMilli();

    JsonObject user = new JsonObject();
    user.put(PASSWORDLESS_MODEL_IS_NEW_USER, true);

    assertThat(passwordlessModel.getString(PASSWORDLESS_MODEL_STATE), equalTo(state));
    assertThat(passwordlessModel.getBoolean(PASSWORDLESS_MODEL_IS_OTP_MOCKED), equalTo(true));
    assertThat(passwordlessModel.getInteger(PASSWORDLESS_MODEL_TRIES), equalTo(0));
    assertThat(passwordlessModel.getInteger(PASSWORDLESS_MODEL_RESENDS), equalTo(0));
    assertThat(
        passwordlessModel.getLong(PASSWORDLESS_MODEL_RESEND_AFTER),
        isInRange(currentTimeInMilli / 1000, currentTimeInMilli / 1000 + 30));
    assertThat(passwordlessModel.getString(PASSWORDLESS_MODEL_FLOW), equalTo(flow));
    assertThat(
        passwordlessModel.getString(PASSWORDLESS_MODEL_RESPONSE_TYPE),
        equalTo(BODY_PARAM_RESPONSE_TYPE_TOKEN));
    assertThat(
        passwordlessModel.getLong(PASSWORDLESS_MODEL_EXPIRY),
        isInRange(currentTimeInMilli / 1000, currentTimeInMilli / 1000 + 900));
    assertThat(
        passwordlessModel.getLong(PASSWORDLESS_MODEL_CREATED_AT_EPOCH),
        isInRange(currentTimeInMilli - 5000, currentTimeInMilli));
    assertThat(passwordlessModel.getJsonObject(PASSWORDLESS_MODEL_USER), equalTo(user));
  }
}
