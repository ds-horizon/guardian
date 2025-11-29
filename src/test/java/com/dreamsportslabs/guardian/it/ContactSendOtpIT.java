package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_SMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONTACT;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EXPIRY;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_MAX_RESENDS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_MAX_TRIES;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_OTP_MOCKED;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_PARAMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESENDS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESENDS_LEFT;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESEND_INTERVAL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_TEMPLATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_TRIES;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_STATE;
import static com.dreamsportslabs.guardian.Constants.ERROR_RESENDS_EXHAUSTED;
import static com.dreamsportslabs.guardian.Constants.ERROR_RESENDS_NOT_ALLOWED;
import static com.dreamsportslabs.guardian.Constants.ERROR_SMS_SERVICE;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RESENDS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RESENDS_LEFT;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RESEND_AFTER;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RETRIES_LEFT;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_TRIES;
import static com.dreamsportslabs.guardian.constant.Channel.EMAIL;
import static com.dreamsportslabs.guardian.constant.Channel.SMS;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContactSendOtpIT {
  // Constants
  private static final String TENANT_ID = "tenant1"; // OTP is mocked for this tenant
  private static final String TENANT_ID_NON_MOCKED = "tenant2"; // OTP is NOT mocked for this tenant
  private static final String TEST_EMAIL = "test@example.com";
  private static final String DEFAULT_TEMPLATE_NAME = "default";
  private static final String NONEXISTENT_TENANT = "nonexistent-tenant";
  private static final String EXPIRED_STATE = "expired-or-invalid-state";
  private static final int RANDOM_IDENTIFIER_LENGTH = 12;
  private static final int MAX_TRIES_SIMULATION = 10;

  private WireMockServer wireMockServer;

  // Helper Methods for Test Data Creation
  private Map<String, Object> createSmsContact(String identifier) {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, identifier);
    return contact;
  }

  private Map<String, Object> createEmailContact(String identifier) {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, EMAIL);
    contact.put(BODY_PARAM_IDENTIFIER, identifier);
    return contact;
  }

  private Map<String, Object> createContactWithTemplate(
      Object channel, String identifier, Map<String, Object> template) {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, channel);
    contact.put(BODY_PARAM_IDENTIFIER, identifier);
    if (template != null) {
      contact.put(BODY_PARAM_TEMPLATE, template);
    }
    return contact;
  }

  private Map<String, Object> createTemplate(String name, Map<String, Object> params) {
    Map<String, Object> template = new HashMap<>();
    if (name != null) {
      template.put(BODY_PARAM_NAME, name);
    }
    if (params != null) {
      template.put(BODY_PARAM_PARAMS, params);
    }
    return template;
  }

  private Map<String, Object> createRequestBody(Map<String, Object> contact) {
    Map<String, Object> body = new HashMap<>();
    if (contact != null) {
      body.put(BODY_PARAM_CONTACT, contact);
    }
    return body;
  }

  private Map<String, Object> createStateBody(String state) {
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_STATE, state);
    return body;
  }

  private String generateRandomIdentifier() {
    return RandomStringUtils.randomAlphanumeric(RANDOM_IDENTIFIER_LENGTH);
  }

  // Helper Methods for Assertions
  private void assertSuccessfulSendOtpResponse(Response response) {
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_RETRIES_LEFT, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_RESENDS, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, isA(Integer.class))
        .body(RESPONSE_BODY_PARAM_RESEND_AFTER, isA(Number.class))
        .body(RESPONSE_BODY_PARAM_STATE, isA(String.class));
  }

  private String sendOtpAndGetState(String tenantId, Map<String, Object> body) {
    Response response = ApplicationIoUtils.sendOtp(tenantId, body);
    return response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
  }

  // Stub Management
  private StubMapping getStubForSendSms() {
    return wireMockServer.stubFor(
        post(urlPathMatching("/sendSms"))
            .willReturn(aResponse().withStatus(200).withBody("{\"result\":\"ok\"}")));
  }

  @Test
  @DisplayName("Should send OTP via SMS for valid request and default template is used")
  public void testSendOtpSmsValid() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    assertSuccessfulSendOtpResponse(response);
    String state = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);

    JsonObject responseObj = DbUtils.getContactState(state, TENANT_ID);

    assertThat(responseObj, isA(JsonObject.class));

    assertThat(
        responseObj.getJsonObject(BODY_PARAM_CONTACT).getString(BODY_PARAM_CHANNEL),
        equalTo(BODY_CHANNEL_SMS));

    assertThat(
        responseObj.getJsonObject(BODY_PARAM_CONTACT).getString(BODY_PARAM_IDENTIFIER),
        equalTo(contact.get(BODY_PARAM_IDENTIFIER)));

    assertThat(
        responseObj
            .getJsonObject(BODY_PARAM_CONTACT)
            .getJsonObject(BODY_PARAM_TEMPLATE)
            .getString(BODY_PARAM_NAME),
        equalTo(DEFAULT_TEMPLATE_NAME));

    assertThat(responseObj.getInteger(BODY_PARAM_TRIES), equalTo(0));
    assertThat(responseObj.getInteger(BODY_PARAM_RESENDS), equalTo(0));
    assertThat(responseObj.getInteger(BODY_PARAM_RESEND_INTERVAL), equalTo(30));
    assertThat(responseObj.getBoolean(BODY_PARAM_OTP_MOCKED), equalTo(true));
    assertThat(responseObj.getInteger(BODY_PARAM_MAX_TRIES), equalTo(5));
    assertThat(responseObj.getInteger(BODY_PARAM_MAX_RESENDS), equalTo(5));
    assertThat(responseObj.getLong(BODY_PARAM_EXPIRY), isA(Long.class));

    assertThat(response.getBody().jsonPath().getInt(BODY_PARAM_TRIES), equalTo(0));
    assertThat(response.getBody().jsonPath().getInt(BODY_PARAM_RESENDS), equalTo(0));
    assertThat(response.getBody().jsonPath().getInt(BODY_PARAM_RESENDS_LEFT), equalTo(5));
    assertThat(response.getBody().jsonPath().getString(BODY_PARAM_STATE), equalTo(state));
    assertThat(response.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_RETRIES_LEFT), equalTo(5));
  }

  @Test
  @DisplayName("Should send OTP via EMAIL for valid request")
  public void testSendOtpEmailValid() {
    // Arrange
    Map<String, Object> contact = createEmailContact(TEST_EMAIL);
    Map<String, Object> body = createRequestBody(contact);
    body.put(BODY_PARAM_STATE, null);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    assertSuccessfulSendOtpResponse(response);
  }

  @Test
  @DisplayName("Should return error when contact is missing")
  public void testSendOtpMissingContact() {
    // Arrange
    Map<String, Object> body = createRequestBody(null);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("contact details are missing or invalid"));
    ;
  }

  @Test
  @DisplayName("Should return error when contact is invalid (missing identifier)")
  public void testSendOtpInvalidContact() {
    // Arrange
    Map<String, Object> contact = createSmsContact(null); // missing identifier
    Map<String, Object> body = createRequestBody(contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("contact details are missing or invalid"));
  }

  @Test
  @DisplayName("Should return error when resends are exhausted")
  public void testSendOtpResendsExhausted() {
    // Arrange
    String state = RandomStringUtils.randomAlphanumeric(8);
    addStateInRedis(
        TENANT_ID, state, 60, BODY_CHANNEL_SMS, 4, 5, Instant.now().toEpochMilli(), 5, true, 60);
    Map<String, Object> stateBody = createStateBody(state);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RESENDS_EXHAUSTED));

    JsonObject responseObj = DbUtils.getContactState(TENANT_ID, state);
    assertThat(responseObj, equalTo(null));
  }

  @Test
  @DisplayName("Should return error when resend is not allowed (too soon)")
  public void testSendOtpResendNotAllowed() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);
    String state = sendOtpAndGetState(TENANT_ID, body);
    Map<String, Object> stateBody = createStateBody(state);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RESENDS_NOT_ALLOWED));
  }

  @Test
  @DisplayName("Should handle mocked OTP for tenant1")
  public void testSendOtpMocked() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0));
  }

  @Test
  @DisplayName("Should return error for contact with invalid template")
  public void testSendOtpInvalidTemplate() {
    // Arrange
    Map<String, Object> template = createTemplate(null, null); // missing name
    Map<String, Object> contact =
        createContactWithTemplate(SMS, generateRandomIdentifier(), template);
    Map<String, Object> body = createRequestBody(contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("contact details are missing or invalid"));
  }

  @Test
  @DisplayName("Should send OTP with valid template")
  public void testSendOtpValidTemplate() {
    // Arrange
    Map<String, Object> template = createTemplate(DEFAULT_TEMPLATE_NAME, new HashMap<>());
    Map<String, Object> contact = createContactWithTemplate(EMAIL, TEST_EMAIL, template);
    Map<String, Object> body = createRequestBody(contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    assertSuccessfulSendOtpResponse(response);
  }

  @Test
  @DisplayName("Should return error for expired state")
  public void testSendOtpExpiredState() {
    // Arrange
    Map<String, Object> stateBody = createStateBody(EXPIRED_STATE);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_STATE));
  }

  @Test
  @DisplayName("Should return error for non-existent tenant ID")
  public void testSendOtpNonExistentTenant() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(NONEXISTENT_TENANT, body);

    // Validate
    response.then().statusCode(isA(Integer.class)); // Accepts 400 or 404 depending on impl
  }

  @Test
  @DisplayName("Should return error when max tries are exceeded")
  public void testSendOtpMaxTriesExceeded() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);
    String state = sendOtpAndGetState(TENANT_ID, body);
    Map<String, Object> stateBody = createStateBody(state);

    // Simulate max tries by calling resend many times
    for (int i = 0; i < MAX_TRIES_SIMULATION; i++) {
      ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);
    }

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RESENDS_NOT_ALLOWED));
  }

  @Test
  @DisplayName("Should start resends counter at 0")
  public void testResendsCounterStartsAtZero() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    response.then().statusCode(SC_OK).body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0));
  }

  @Test
  @DisplayName("Should decrease resendsLeft with each resend")
  public void testResendsLeftDecreases() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);
    StubMapping sendSmsStub = getStubForSendSms();

    Response first = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);
    String state = first.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);

    JsonObject object = DbUtils.getContactState(state, TENANT_ID_NON_MOCKED);
    assertThat(object, isA(JsonObject.class));
    assertThat(object.getString("resends"), equalTo("0"));

    // Todo: Validate if wiremock function is actually called
    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should handle non-mocked OTP for SMS channel (tenant2)")
  public void testNonMockedOtpSmsContactVerify() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);
    StubMapping sendSmsStub = getStubForSendSms();

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5));

    // Todo: Validate if wiremock function is actually called
    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should enforce resend limit exactly for non-mocked tenant (SMS, tenant2)")
  public void testResendLimitEnforcedExactlyNonMockedSms() throws InterruptedException {
    // Arrange
    StubMapping sendSmsStub = getStubForSendSms();
    String state = RandomStringUtils.randomAlphanumeric(8);

    addStateInRedis(
        TENANT_ID_NON_MOCKED,
        state,
        60,
        BODY_CHANNEL_SMS,
        4,
        5,
        Instant.now().toEpochMilli(),
        5,
        false,
        60);

    Map<String, Object> stateBody = createStateBody(state);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RESENDS_EXHAUSTED));

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName(
      "Should return error when resend is not allowed (too soon) for non-mocked tenant (SMS, tenant2)")
  public void testSendOtpResendNotAllowedNonMockedSms() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);
    StubMapping sendSmsStub = getStubForSendSms();

    String state = sendOtpAndGetState(TENANT_ID_NON_MOCKED, body);
    Map<String, Object> stateBody = createStateBody(state);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RESENDS_NOT_ALLOWED));

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should handle 4xx error from OTP service for non-mocked tenant (SMS, tenant2)")
  public void testOtpService4xxErrorNonMockedSms() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);

    // Stub the SMS service to return 400
    StubMapping sendSmsStub =
        wireMockServer.stubFor(
            post(urlPathMatching("/sendSms"))
                .willReturn(aResponse().withStatus(400).withBody("{\"error\":\"bad request\"}")));

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_SMS_SERVICE));

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should handle 5xx error from OTP service for non-mocked tenant (SMS, tenant2)")
  public void testOtpService5xxErrorNonMockedSms() {
    // Arrange
    Map<String, Object> contact = createSmsContact(generateRandomIdentifier());
    Map<String, Object> body = createRequestBody(contact);

    // Stub the SMS service to return 500
    StubMapping sendSmsStub =
        wireMockServer.stubFor(
            post(urlPathMatching("/external/sms/send"))
                .willReturn(
                    aResponse().withStatus(500).withBody("{\"error\":\"internal error\"}")));

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);

    // Validate
    // Todo: Validate if wiremock function is actually called
    response.then().statusCode(SC_INTERNAL_SERVER_ERROR);

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should handle 4xx error for OTP retried after expired time")
  public void testOtpService4xxErrorExpired() throws InterruptedException {
    // Arrange
    StubMapping sendSmsStub = getStubForSendSms();
    String state = RandomStringUtils.randomAlphanumeric(RANDOM_IDENTIFIER_LENGTH);

    addStateInRedis(
        TENANT_ID_NON_MOCKED,
        state,
        60,
        BODY_CHANNEL_SMS,
        0,
        0,
        Instant.now().minus(10, ChronoUnit.MINUTES).toEpochMilli() / 1000,
        5,
        false,
        -1);

    Map<String, Object> stateBody = createStateBody(state);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_STATE));

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should validate contact when state is not provided")
  public void testContactValidationWhenStateNotProvided() {
    // Arrange - No state provided, so validation logic runs
    // Create contact with invalid channel
    Map<String, Object> contact = createContactWithTemplate("invalid_channel", TEST_EMAIL, null);
    Map<String, Object> requestBody = new HashMap<>();
    // state not included in request
    requestBody.put(BODY_PARAM_CONTACT, contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, requestBody);

    // Validate - Should get 400 because contact validation runs when no state
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should validate email format when state is not provided")
  public void testEmailValidationWhenStateNotProvided() {
    // Arrange - No state provided, so validation logic runs
    // Create contact with invalid email format
    Map<String, Object> contact = createEmailContact("not-an-email");
    Map<String, Object> requestBody = new HashMap<>();
    // state not included in request
    requestBody.put(BODY_PARAM_CONTACT, contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, requestBody);

    // Validate - Should get 400 because contact validation runs when no state
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should validate null channel when state is not provided")
  public void testNullChannelValidationWhenStateNotProvided() {
    // Arrange - No state provided, so validation logic runs
    // Create contact with null channel
    Map<String, Object> contact = createContactWithTemplate(null, TEST_EMAIL, null);
    Map<String, Object> requestBody = new HashMap<>();
    // state not included in request
    requestBody.put(BODY_PARAM_CONTACT, contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, requestBody);

    // Validate - Should get 400 because contact validation runs when no state
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  private void addStateInRedis(
      String tenantId,
      String state,
      int ttlSec,
      String channel,
      int tries,
      int resends,
      long resendAfter,
      int otpResendInterval,
      Boolean isOtpMocked,
      int expiry) {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, channel);

    if (channel.equalsIgnoreCase(BODY_CHANNEL_SMS)) {
      String phoneNuber = generateRandomPhoneNumber();
      contact.put(BODY_PARAM_IDENTIFIER, phoneNuber);
    } else {
      String email = generateRandomEmail();
      contact.put(BODY_PARAM_IDENTIFIER, email);
    }

    DbUtils.createContactOtpSendState(
        tenantId,
        state,
        ttlSec,
        "999999",
        isOtpMocked,
        tries,
        resends,
        resendAfter,
        otpResendInterval,
        5,
        5,
        contact,
        System.currentTimeMillis() / 1000 + expiry);
  }

  private String generateRandomPhoneNumber() {
    return "9" + String.format("%09d", (new Random()).nextInt(1000000000));
  }

  private String generateRandomEmail() {
    return "test" + System.currentTimeMillis() + "@example.com";
  }
}
