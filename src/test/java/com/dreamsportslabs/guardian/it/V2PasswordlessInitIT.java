package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_SMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONTACTS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DEVICE_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_FLOW;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_NEW_USER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_LOCATION;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_META_INFO;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_META_INFO_V2;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE_V2;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPES;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_TEMPLATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERNAME;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_STATE;
import static com.dreamsportslabs.guardian.Constants.ERROR_RESENDS_EXHAUSTED;
import static com.dreamsportslabs.guardian.Constants.ERROR_RESENDS_NOT_ALLOWED;
import static com.dreamsportslabs.guardian.Constants.ERROR_UNAUTHORIZED;
import static com.dreamsportslabs.guardian.Constants.ERROR_USER_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_USER_NOT_EXISTS;
import static com.dreamsportslabs.guardian.Constants.HEADER_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNIN;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNINUP;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNUP;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_IS_NEW_USER_V2;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RESENDS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RESENDS_LEFT_V2;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RESEND_AFTER_V2;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RETRIES_LEFT_V2;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_TRIES;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.utils.DbUtils.addDefaultClientScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.addFirstPartyClient;
import static com.dreamsportslabs.guardian.utils.DbUtils.addScope;
import static com.dreamsportslabs.guardian.utils.DbUtils.createState;
import static com.dreamsportslabs.guardian.utils.DbUtils.getState;
import static com.dreamsportslabs.guardian.utils.DbUtils.getStateTtl;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

@Order(5)
public class V2PasswordlessInitIT {
  private static final String TEST_SCOPE_1 = "scope1";
  private static String client1;
  private WireMockServer wireMockServer;

  @BeforeAll
  static void setup() {
    addScope(TENANT_1, TEST_SCOPE_1);
    client1 = addFirstPartyClient(TENANT_1);
    addDefaultClientScopes(TENANT_1, client1, TEST_SCOPE_1);
  }

  @Test
  @DisplayName("Should return error when client_id is missing")
  public void testClientIdMissing() {
    // Arrange
    Map<String, Object> requestBody =
        getRequestBodyInit(
            null,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_SMS,
            generateRandomPhoneNumber(),
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should return error when contacts are missing")
  public void testContactsMissing() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, client1);
    requestBody.put(BODY_PARAM_SCOPES, List.of(TEST_SCOPE_1));
    requestBody.put(BODY_PARAM_FLOW, PASSWORDLESS_FLOW_SIGNINUP);
    requestBody.put(BODY_PARAM_RESPONSE_TYPE, BODY_PARAM_RESPONSE_TYPE_TOKEN);
    requestBody.put(BODY_PARAM_META_INFO, getMetaInfo());

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should create state for new user with phone number in SIGNINUP flow")
  public void testNewUserPhoneSigninup() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER_V2, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER_V2, equalTo(true));

    String state = response.then().extract().path(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForNewUser(state, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should create state for new user with email in SIGNINUP flow")
  public void testNewUserEmailSigninup() {
    // Arrange
    String email = generateRandomEmail();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_EMAIL,
            email,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER_V2, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER_V2, equalTo(true));

    String state = response.then().extract().path(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForNewUser(state, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should handle existing user in SIGNINUP flow")
  public void testExistingUserSigninup() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(phoneNumber, null);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER_V2, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER_V2, equalTo(false));

    String state = response.then().extract().path(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, PASSWORDLESS_FLOW_SIGNINUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return error for existing user in SIGNUP flow")
  public void testExistingUserSignup() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(phoneNumber, null);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

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
  @DisplayName("Should return error for non-existing user in SIGNIN flow")
  public void testNonExistingUserSignin() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNIN,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

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
  @DisplayName("Should handle resend with valid state")
  public void testResendWithValidState() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    addStateInRedis(
        state,
        300,
        BODY_CHANNEL_SMS,
        0,
        0,
        System.currentTimeMillis() / 1000 - 30,
        60,
        PASSWORDLESS_FLOW_SIGNINUP,
        300);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, client1);
    requestBody.put(BODY_PARAM_SCOPES, List.of(TEST_SCOPE_1));
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_RESPONSE_TYPE, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(1))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT_V2, equalTo(4))
        .body(RESPONSE_BODY_PARAM_STATE, equalTo(state));
  }

  @Test
  @DisplayName("Should return error for invalid state")
  public void testInvalidState() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, client1);
    requestBody.put(BODY_PARAM_SCOPES, List.of(TEST_SCOPE_1));
    requestBody.put(BODY_PARAM_STATE, "invalid_state");
    requestBody.put(BODY_PARAM_RESPONSE_TYPE, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_STATE));
  }

  @Test
  @DisplayName("Should return error when resends are exhausted")
  public void testResendsExhausted() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    addStateInRedis(
        state,
        300,
        BODY_CHANNEL_SMS,
        0,
        5,
        System.currentTimeMillis() / 1000 - 30,
        60,
        PASSWORDLESS_FLOW_SIGNINUP,
        300);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, client1);
    requestBody.put(BODY_PARAM_SCOPES, List.of(TEST_SCOPE_1));
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_RESPONSE_TYPE, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RESENDS_EXHAUSTED));
  }

  @Test
  @DisplayName("Should return error when resend not allowed due to time restriction")
  public void testResendNotAllowed() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    addStateInRedis(
        state,
        300,
        BODY_CHANNEL_SMS,
        0,
        1,
        System.currentTimeMillis() / 1000 + 30,
        60,
        PASSWORDLESS_FLOW_SIGNINUP,
        300);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, client1);
    requestBody.put(BODY_PARAM_SCOPES, List.of(TEST_SCOPE_1));
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_RESPONSE_TYPE, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RESENDS_NOT_ALLOWED));
  }

  @Test
  @DisplayName("Should return error when tenant-id header is missing")
  public void testMissingTenantId() {
    // Arrange
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_SMS,
            generateRandomPhoneNumber(),
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = v2PasswordlessInit(null, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));
  }

  @Test
  @DisplayName("Should use configured TTL value")
  public void testConfiguredTtlValue() {
    // Arrange
    String email = generateRandomEmail();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_EMAIL,
            email,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);
    response.then().statusCode(SC_OK);

    String state = response.then().extract().path(RESPONSE_BODY_PARAM_STATE);

    // Validate
    long ttl = getStateTtl(state, TENANT_1);
    assertThat(ttl, greaterThanOrEqualTo(899L));
    assertThat(ttl, lessThanOrEqualTo(900L));

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return error when phone number is not provided")
  public void testPhoneNumberMissing() {
    // Arrange
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_SMS,
            null,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

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
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_EMAIL,
            null,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should create new user when valid phone number doesn't exist in SIGNUP flow")
  public void testNewUserPhoneSignup() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER_V2, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER_V2, equalTo(true));

    String state = response.then().extract().path(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForNewUser(state, PASSWORDLESS_FLOW_SIGNUP);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should handle existing user when valid phone number exists in SIGNIN flow")
  public void testExistingUserPhoneSignin() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_SMS,
            phoneNumber,
            PASSWORDLESS_FLOW_SIGNIN,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(phoneNumber, null);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER_V2, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER_V2, equalTo(false));

    String state = response.then().extract().path(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, PASSWORDLESS_FLOW_SIGNIN);

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
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_EMAIL,
            email,
            PASSWORDLESS_FLOW_SIGNUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER_V2, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER_V2, equalTo(true));

    String state = response.then().extract().path(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForNewUser(state, PASSWORDLESS_FLOW_SIGNUP);

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
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_EMAIL,
            email,
            PASSWORDLESS_FLOW_SIGNIN,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(null, email);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT_V2, equalTo(5))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER_V2, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class))
        .body(RESPONSE_BODY_PARAM_IS_NEW_USER_V2, equalTo(false));

    String state = response.then().extract().path(RESPONSE_BODY_PARAM_STATE);
    assertRedisStateForExistingUser(state, PASSWORDLESS_FLOW_SIGNIN);

    // cleanup
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return error for non-existing email in SIGNIN flow")
  public void testNonExistingEmailSignin() {
    // Arrange
    String email = generateRandomEmail();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_EMAIL,
            email,
            PASSWORDLESS_FLOW_SIGNIN,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForNonExistingUser();

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

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
  @DisplayName("Should return error for existing email in SIGNUP flow")
  public void testExistingEmailSignup() {
    // Arrange
    String email = generateRandomEmail();
    Map<String, Object> requestBody =
        getRequestBodyInit(
            client1,
            List.of(TEST_SCOPE_1),
            BODY_CHANNEL_EMAIL,
            email,
            PASSWORDLESS_FLOW_SIGNUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN);

    StubMapping stub = getStubForExistingUser(null, email);

    // Act
    Response response = v2PasswordlessInit(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_USER_EXISTS));

    // cleanup
    wireMockServer.removeStub(stub);
  }

  private Map<String, Object> getRequestBodyInit(
      String clientId,
      List<String> scopes,
      String channel,
      String identifier,
      String flow,
      String responseType) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, clientId);
    requestBody.put(BODY_PARAM_SCOPES, scopes);
    requestBody.put(BODY_PARAM_FLOW, flow);
    requestBody.put(BODY_PARAM_RESPONSE_TYPE_V2, responseType);
    requestBody.put(BODY_PARAM_META_INFO_V2, getMetaInfo());

    if (identifier != null) {
      Map<String, Object> contact = new HashMap<>();
      contact.put(BODY_PARAM_CHANNEL, channel);
      contact.put(BODY_PARAM_IDENTIFIER, identifier);
      contact.put(BODY_PARAM_TEMPLATE, getTemplate());
      requestBody.put(BODY_PARAM_CONTACTS, List.of(contact));
    } else {
      Map<String, Object> contact = new HashMap<>();
      contact.put(BODY_PARAM_CHANNEL, channel);
      contact.put(BODY_PARAM_TEMPLATE, getTemplate());
      requestBody.put(BODY_PARAM_CONTACTS, List.of(contact));
    }

    return requestBody;
  }

  private Map<String, Object> getMetaInfo() {
    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put(BODY_PARAM_DEVICE_NAME, "testDevice");
    metaInfo.put(BODY_PARAM_LOCATION, "testLocation");
    return metaInfo;
  }

  private Map<String, Object> getTemplate() {
    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, "otp");
    return template;
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

  private void assertRedisStateForNewUser(String state, String flow) {
    JsonObject stateData = getState(state, TENANT_1);
    assertThat(stateData, notNullValue());
    assertThat(stateData.getString("flow"), equalTo(flow));
    assertThat(stateData.getJsonObject("user").getString("userId"), equalTo(null));
  }

  private void assertRedisStateForExistingUser(String state, String flow) {
    JsonObject stateData = getState(state, TENANT_1);
    assertThat(stateData, notNullValue());
    assertThat(stateData.getString("flow"), equalTo(flow));
    assertThat(stateData.getJsonObject("user").getString("userId"), notNullValue());
  }

  private Response v2PasswordlessInit(String tenantId, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    if (tenantId != null) {
      headers.put(HEADER_TENANT_ID, tenantId);
    }
    headers.put(CONTENT_TYPE, "application/json");

    return given().headers(headers).body(body).post("/v2/passwordless/init");
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
    user.put(BODY_PARAM_NAME, "John Doe");
    user.put(BODY_PARAM_USERID, 1);
    user.put(BODY_PARAM_USERNAME, "TestUser");
    user.put(BODY_PARAM_IS_NEW_USER, false);

    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, channel);

    if (channel.equalsIgnoreCase(BODY_CHANNEL_SMS)) {
      String phoneNumber = generateRandomPhoneNumber();
      user.put(BODY_PARAM_PHONE_NUMBER, phoneNumber);
      contact.put(BODY_PARAM_IDENTIFIER, phoneNumber);
    } else {
      String email = generateRandomEmail();
      user.put(BODY_PARAM_EMAIL, email);
      contact.put(BODY_PARAM_IDENTIFIER, email);
    }

    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put(BODY_PARAM_DEVICE_NAME, "testDevice");
    metaInfo.put(BODY_PARAM_LOCATION, "testLocation");

    // Create state using existing method
    createState(
        TENANT_1,
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

    // Update the state with clientId and scopes
    updateStateWithScopes(state, client1, List.of(TEST_SCOPE_1));
  }

  private void updateStateWithScopes(String state, String clientId, List<String> scopes) {
    try {
      JsonObject stateData = getState(state, TENANT_1);
      if (stateData != null) {
        stateData.put("clientId", clientId);
        stateData.put("scopes", scopes);

        // Re-save the updated state
        String key = "STATE" + "_" + TENANT_1 + "_" + state;
        try (Jedis jedis = new Jedis("localhost", 6379)) {
          jedis.set(key, stateData.toString());
        }
      }
    } catch (Exception e) {
      // Handle error silently for test
    }
  }
}
