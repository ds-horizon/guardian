package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_SMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DEVICE_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_NEW_USER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_LOCATION;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_OTP;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERNAME;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INCORRECT_OTP;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_STATE;
import static com.dreamsportslabs.guardian.Constants.ERROR_RETRIES_EXHAUSTED;
import static com.dreamsportslabs.guardian.Constants.ERROR_UNAUTHORIZED;
import static com.dreamsportslabs.guardian.Constants.HEADER_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.METADATA;
import static com.dreamsportslabs.guardian.Constants.MFA_FACTORS;
import static com.dreamsportslabs.guardian.Constants.MFA_FACTOR_EMAIL_OTP;
import static com.dreamsportslabs.guardian.Constants.MFA_FACTOR_PASSWORD;
import static com.dreamsportslabs.guardian.Constants.MFA_FACTOR_PIN;
import static com.dreamsportslabs.guardian.Constants.MFA_FACTOR_SMS_OTP;
import static com.dreamsportslabs.guardian.Constants.MFA_POLICY_MANDATORY;
import static com.dreamsportslabs.guardian.Constants.MFA_POLICY_NOT_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNINUP;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_EXPIRES_IN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_TOKEN_TYPE;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_HEADER_PARAM_SET_COOKIE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TOKEN_TYPE_BEARER;
import static com.dreamsportslabs.guardian.utils.DbUtils.addDefaultClientScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.addFirstPartyClient;
import static com.dreamsportslabs.guardian.utils.DbUtils.addScope;
import static com.dreamsportslabs.guardian.utils.DbUtils.createState;
import static com.dreamsportslabs.guardian.utils.DbUtils.getState;
import static com.dreamsportslabs.guardian.utils.DbUtils.updateClientAllowedMfaMethods;
import static com.dreamsportslabs.guardian.utils.DbUtils.updateClientMfaPolicy;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.time.Instant;
import java.util.ArrayList;
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

@Order(6)
public class V2PasswordlessCompleteIT {
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
  @DisplayName("Should return error when state is missing")
  public void testStateMissing() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_OTP, "999999");

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should return error when OTP is missing")
  public void testOtpMissing() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_STATE, "valid_state");

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should return error for invalid state")
  public void testInvalidState() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_STATE, "invalid_state");
    requestBody.put(BODY_PARAM_OTP, "999999");

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_STATE));
  }

  @Test
  @DisplayName("Should return error for incorrect OTP")
  public void testIncorrectOtp() {
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
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_OTP, "123456"); // Wrong OTP

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INCORRECT_OTP))
        .body(METADATA, notNullValue());
  }

  @Test
  @DisplayName("Should return error when retries are exhausted")
  public void testRetriesExhausted() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    addStateInRedis(
        state,
        300,
        BODY_CHANNEL_SMS,
        5,
        0,
        System.currentTimeMillis() / 1000 - 30,
        60,
        PASSWORDLESS_FLOW_SIGNINUP,
        300);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_OTP, "123456");

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RETRIES_EXHAUSTED));
  }

  @Test
  @DisplayName("Should complete authentication for existing user with correct OTP")
  public void testCompleteAuthenticationExistingUser() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    addStateInRedisForExistingUser(
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
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_OTP, "999999"); // Correct OTP

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, isA(Integer.class))
        .header(RESPONSE_HEADER_PARAM_SET_COOKIE, notNullValue());

    // Verify state is cleaned up
    JsonObject stateData = getState(state, TENANT_1);
    assertThat(stateData, nullValue());
  }

  @Test
  @DisplayName("Should complete authentication for new user with correct OTP")
  public void testCompleteAuthenticationNewUser() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    addStateInRedisForNewUser(
        state,
        300,
        BODY_CHANNEL_SMS,
        0,
        0,
        System.currentTimeMillis() / 1000 - 30,
        60,
        PASSWORDLESS_FLOW_SIGNINUP,
        300);

    StubMapping createUserStub = getStubForCreateUser();

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_OTP, "999999");

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, isA(Integer.class))
        .header(RESPONSE_HEADER_PARAM_SET_COOKIE, notNullValue());

    // Verify state is cleaned up
    JsonObject stateData = getState(state, TENANT_1);
    assertThat(stateData, nullValue());

    // cleanup
    wireMockServer.removeStub(createUserStub);
  }

  @Test
  @DisplayName("Should complete authentication with email contact")
  public void testCompleteAuthenticationWithEmail() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    addStateInRedisForNewUser(
        state,
        300,
        BODY_CHANNEL_EMAIL,
        0,
        0,
        System.currentTimeMillis() / 1000 - 30,
        60,
        PASSWORDLESS_FLOW_SIGNINUP,
        300);

    StubMapping createUserStub = getStubForCreateUser();

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_OTP, "999999");

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, isA(Integer.class));

    // cleanup
    wireMockServer.removeStub(createUserStub);
  }

  @Test
  @DisplayName("Should return error when tenant-id header is missing")
  public void testMissingTenantId() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_STATE, "valid_state");
    requestBody.put(BODY_PARAM_OTP, "999999");

    // Act
    Response response = v2PasswordlessComplete(null, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));
  }

  @Test
  @DisplayName(
      "Should return MFA factors when mfa_policy is mandatory and client has allowed MFA methods")
  public void testMfaFactorsReturnedWhenPolicyIsMandatory() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    String phoneNumber = generateRandomPhoneNumber();
    String email = generateRandomEmail();

    updateClientMfaPolicyHelper(client1, MFA_POLICY_MANDATORY);
    updateClientWithAllowedMfaMethods(client1, List.of(MFA_FACTOR_PASSWORD, MFA_FACTOR_PIN, MFA_FACTOR_SMS_OTP, MFA_FACTOR_EMAIL_OTP));

    addStateInRedisForExistingUserWithMfaFields(
        state,
        300,
        BODY_CHANNEL_SMS,
        0,
        0,
        System.currentTimeMillis() / 1000 - 30,
        60,
        PASSWORDLESS_FLOW_SIGNINUP,
        300,
        true,
        true,
        phoneNumber,
        email);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_OTP, "999999");

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    // Note: OTP factors (sms-otp, email-otp) are filtered out because passwordless uses
    // ONE_TIME_PASSWORD (POSSESSION category), and OTP factors are also POSSESSION category.
    // Only factors from different categories (KNOWLEDGE: password, pin) are returned.
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, isA(Integer.class))
        .body(MFA_FACTORS, notNullValue())
        .body(MFA_FACTORS, hasSize(2))
        .body("mfa_factors[0].factor", equalTo(MFA_FACTOR_PASSWORD))
        .body("mfa_factors[0].is_enabled", equalTo(true))
        .body("mfa_factors[1].factor", equalTo(MFA_FACTOR_PIN))
        .body("mfa_factors[1].is_enabled", equalTo(true))
        .header(RESPONSE_HEADER_PARAM_SET_COOKIE, notNullValue());

    JsonObject stateData = getState(state, TENANT_1);
    assertThat(stateData, nullValue());

    // Cleanup
    updateClientMfaPolicyHelper(client1, MFA_POLICY_NOT_REQUIRED);
    updateClientWithAllowedMfaMethods(client1, null);
  }

  @Test
  @DisplayName("Should return empty MFA factors when mfa_policy is not_required")
  public void testNoMfaFactorsWhenPolicyIsNotRequired() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    String phoneNumber = generateRandomPhoneNumber();
    String email = generateRandomEmail();

    updateClientMfaPolicyHelper(client1, MFA_POLICY_NOT_REQUIRED);
    updateClientWithAllowedMfaMethods(client1, List.of(MFA_FACTOR_PASSWORD, MFA_FACTOR_PIN, "otp"));

    addStateInRedisForExistingUserWithMfaFields(
        state,
        300,
        BODY_CHANNEL_SMS,
        0,
        0,
        System.currentTimeMillis() / 1000 - 30,
        60,
        PASSWORDLESS_FLOW_SIGNINUP,
        300,
        true,
        true,
        phoneNumber,
        email);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_OTP, "999999");

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, isA(Integer.class))
        .body(MFA_FACTORS, notNullValue())
        .body(MFA_FACTORS, hasSize(0))
        .header(RESPONSE_HEADER_PARAM_SET_COOKIE, notNullValue());

    JsonObject stateData = getState(state, TENANT_1);
    assertThat(stateData, nullValue());

    // Cleanup
    updateClientMfaPolicyHelper(client1, MFA_POLICY_NOT_REQUIRED);
    updateClientWithAllowedMfaMethods(client1, null);
  }

  @Test
  @DisplayName(
      "Should return empty MFA factors when mfa_policy is mandatory but no allowed methods")
  public void testEmptyMfaFactorsWhenMandatoryButNoMethods() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);

    updateClientMfaPolicyHelper(client1, MFA_POLICY_MANDATORY);
    updateClientWithAllowedMfaMethods(client1, new ArrayList<>());

    addStateInRedisForExistingUser(
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
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_OTP, "999999");

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, isA(Integer.class))
        .body(MFA_FACTORS, notNullValue())
        .body(MFA_FACTORS, hasSize(0));

    JsonObject stateData = getState(state, TENANT_1);
    assertThat(stateData, nullValue());

    // Cleanup
    updateClientMfaPolicyHelper(client1, MFA_POLICY_NOT_REQUIRED);
    updateClientWithAllowedMfaMethods(client1, null);
  }

  @Test
  @DisplayName("Should return MFA factors with correct enabled status when policy is mandatory")
  public void testMfaFactorsWithCorrectEnabledStatusWhenMandatory() {
    // Arrange
    String state = RandomStringUtils.randomAlphabetic(10);
    String phoneNumber = generateRandomPhoneNumber();
    String email = generateRandomEmail();

    updateClientMfaPolicyHelper(client1, MFA_POLICY_MANDATORY);
    updateClientWithAllowedMfaMethods(client1, List.of(MFA_FACTOR_PASSWORD, MFA_FACTOR_PIN, MFA_FACTOR_SMS_OTP, MFA_FACTOR_EMAIL_OTP));

    addStateInRedisForExistingUserWithMfaFields(
        state,
        300,
        BODY_CHANNEL_SMS,
        0,
        0,
        System.currentTimeMillis() / 1000 - 30,
        60,
        PASSWORDLESS_FLOW_SIGNINUP,
        300,
        true,
        false,
        phoneNumber,
        email);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_OTP, "999999");

    // Act
    Response response = v2PasswordlessComplete(TENANT_1, requestBody);

    // Validate
    // Note: OTP factors (sms-otp, email-otp) are filtered out because passwordless uses
    // ONE_TIME_PASSWORD (POSSESSION category), and OTP factors are also POSSESSION category.
    // Only factors from different categories (KNOWLEDGE: password, pin) are returned.
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(MFA_FACTORS, notNullValue())
        .body(MFA_FACTORS, hasSize(2))
        .body("mfa_factors[0].factor", equalTo(MFA_FACTOR_PASSWORD))
        .body("mfa_factors[0].is_enabled", equalTo(true))
        .body("mfa_factors[1].factor", equalTo(MFA_FACTOR_PIN))
        .body("mfa_factors[1].is_enabled", equalTo(false));

    JsonObject stateData = getState(state, TENANT_1);
    assertThat(stateData, nullValue());

    // Cleanup
    updateClientMfaPolicyHelper(client1, MFA_POLICY_NOT_REQUIRED);
    updateClientWithAllowedMfaMethods(client1, null);
  }

  private String generateRandomPhoneNumber() {
    return "9" + String.format("%09d", (new Random()).nextInt(1000000000));
  }

  private String generateRandomEmail() {
    return "test" + System.currentTimeMillis() + "@example.com";
  }

  private StubMapping getStubForCreateUser() {
    return wireMockServer.stubFor(
        post(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, "application/json")
                    .withBody(
                        "{\""
                            + BODY_PARAM_NAME
                            + "\": \"John Doe\", \""
                            + BODY_PARAM_USERID
                            + "\": \"123\", \""
                            + BODY_PARAM_USERNAME
                            + "\": \"newuser\"}")));
  }

  private Response v2PasswordlessComplete(String tenantId, Map<String, Object> body) {
    Map<String, String> headers = new HashMap<>();
    if (tenantId != null) {
      headers.put(HEADER_TENANT_ID, tenantId);
    }
    headers.put(CONTENT_TYPE, "application/json");

    return given().headers(headers).body(body).post("/v2/passwordless/complete");
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
    addStateInRedis(
        state,
        ttlSec,
        channel,
        tries,
        resends,
        resendAfter,
        otpResendInterval,
        flow,
        expiry,
        null,
        null,
        null,
        null);
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
      int expiry,
      Boolean isPasswordSet,
      Boolean isPinSet,
      String phoneNumber,
      String email) {
    Map<String, Object> user = new HashMap<>();
    user.put(BODY_PARAM_NAME, "John Doe");
    user.put(BODY_PARAM_USERID, 1);
    user.put(BODY_PARAM_USERNAME, "TestUser");
    user.put(BODY_PARAM_IS_NEW_USER, false);

    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, channel);

    // Set contact identifier based on channel
    if (channel.equalsIgnoreCase(BODY_CHANNEL_SMS)) {
      String phone = phoneNumber != null ? phoneNumber : generateRandomPhoneNumber();
      user.put(BODY_PARAM_PHONE_NUMBER, phone);
      contact.put(BODY_PARAM_IDENTIFIER, phone);
    } else {
      String emailAddr = email != null ? email : generateRandomEmail();
      user.put(BODY_PARAM_EMAIL, emailAddr);
      contact.put(BODY_PARAM_IDENTIFIER, emailAddr);
    }

    // Add MFA-related fields if provided
    if (isPasswordSet != null) {
      user.put("isPasswordSet", isPasswordSet);
    }
    if (isPinSet != null) {
      user.put("isPinSet", isPinSet);
    }
    // Ensure both email and phoneNumber are in user object for OTP factors (if provided)
    // This allows both SMS_OTP and EMAIL_OTP to be evaluated
    if (email != null) {
      user.put(BODY_PARAM_EMAIL, email);
    }
    if (phoneNumber != null) {
      user.put(BODY_PARAM_PHONE_NUMBER, phoneNumber);
    }

    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put(BODY_PARAM_DEVICE_NAME, "testDevice");
    metaInfo.put(BODY_PARAM_LOCATION, "testLocation");

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

  private void addStateInRedisForExistingUser(
      String state,
      int ttlSec,
      String channel,
      int tries,
      int resends,
      long resendAfter,
      int otpResendInterval,
      String flow,
      int expiry) {
    addStateInRedis(
        state, ttlSec, channel, tries, resends, resendAfter, otpResendInterval, flow, expiry);
  }

  private void addStateInRedisForExistingUserWithMfaFields(
      String state,
      int ttlSec,
      String channel,
      int tries,
      int resends,
      long resendAfter,
      int otpResendInterval,
      String flow,
      int expiry,
      Boolean isPasswordSet,
      Boolean isPinSet,
      String phoneNumber,
      String email) {
    addStateInRedis(
        state,
        ttlSec,
        channel,
        tries,
        resends,
        resendAfter,
        otpResendInterval,
        flow,
        expiry,
        isPasswordSet,
        isPinSet,
        phoneNumber,
        email);
  }

  private void addStateInRedisForNewUser(
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
    user.put(BODY_PARAM_IS_NEW_USER, true);

    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, channel);

    if (channel.equalsIgnoreCase(BODY_CHANNEL_SMS)) {
      String phoneNumber = generateRandomPhoneNumber();
      contact.put(BODY_PARAM_IDENTIFIER, phoneNumber);
    } else {
      String email = generateRandomEmail();
      contact.put(BODY_PARAM_IDENTIFIER, email);
    }

    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put(BODY_PARAM_DEVICE_NAME, "testDevice");
    metaInfo.put(BODY_PARAM_LOCATION, "testLocation");

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

  private void updateClientWithAllowedMfaMethods(String clientId, List<String> allowedMfaMethods) {
    updateClientAllowedMfaMethods(TENANT_1, clientId, allowedMfaMethods);
  }

  private void updateClientMfaPolicyHelper(String clientId, String mfaPolicy) {
    updateClientMfaPolicy(TENANT_1, clientId, mfaPolicy);
  }
}
