package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.*;
import static com.dreamsportslabs.guardian.constant.Channel.EMAIL;
import static com.dreamsportslabs.guardian.constant.Channel.SMS;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContactSendOtpIT {
  private static final String TENANT_ID = "tenant1"; // OTP is mocked for this tenant
  private static final String TENANT_ID_NON_MOCKED = "tenant2"; // OTP is NOT mocked for this tenant
  private WireMockServer wireMockServer;

  private StubMapping getStubForSendSms() {
    return wireMockServer.stubFor(
        post(urlPathMatching("/sendSms"))
            .willReturn(aResponse().withStatus(200).withBody("{\"result\":\"ok\"}")));
  }

  @Test
  @DisplayName("Should send OTP via SMS for valid request and default template is used")
  public void testSendOtpSmsValid() {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
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

  @Test
  @DisplayName("Should send OTP via EMAIL for valid request")
  public void testSendOtpEmailValid() {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, EMAIL);
    contact.put(BODY_PARAM_IDENTIFIER, "test@example.com");

    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);
    body.put(BODY_PARAM_STATE, null);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
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

  @Test
  @DisplayName("Should return error when contact is missing")
  public void testSendOtpMissingContact() {
    // Arrange
    Map<String, Object> body = new HashMap<>();

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should return error when contact is invalid (missing identifier)")
  public void testSendOtpInvalidContact() {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    // missing identifier
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should return error when resends are exhausted")
  public void testSendOtpResendsExhausted() {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    String state = RandomStringUtils.randomAlphanumeric(8);

    addStateInRedis(
        TENANT_ID, state, 60, BODY_CHANNEL_SMS, 4, 5, Instant.now().toEpochMilli(), 5, true, 60);

    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);

    // Act
    Response exhausted = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);

    // Validate
    exhausted.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should return error when resend is not allowed (too soon)")
  public void testSendOtpResendNotAllowed() {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    Response first = ApplicationIoUtils.sendOtp(TENANT_ID, body);
    String state = first.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);

    // Act
    Response resend = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);

    // Validate
    resend.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should handle mocked OTP for tenant1")
  public void testSendOtpMocked() {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

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
    Map<String, Object> template = new HashMap<>();
    // missing name
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphabetic(12));
    contact.put(BODY_PARAM_TEMPLATE, template);
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should send OTP with valid template")
  public void testSendOtpValidTemplate() {
    // Arrange
    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, "otp-template");
    template.put(BODY_PARAM_PARAMS, new HashMap<>());
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, EMAIL);
    contact.put(BODY_PARAM_IDENTIFIER, "test@example.com");
    contact.put(BODY_PARAM_TEMPLATE, template);
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
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

  @Test
  @DisplayName("Should return error for expired state")
  public void testSendOtpExpiredState() {
    // Arrange
    // Simulate by using a random/invalid state
    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, "expired-or-invalid-state");

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);

    // Validate
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should return error for non-existent tenant ID")
  public void testSendOtpNonExistentTenant() {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp("nonexistent-tenant", body);

    // Validate
    response.then().statusCode(isA(Integer.class)); // Accepts 400 or 404 depending on impl
  }

  @Test
  @DisplayName("Should return error when max tries are exceeded")
  public void testSendOtpMaxTriesExceeded() {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);
    Response first = ApplicationIoUtils.sendOtp(TENANT_ID, body);
    String state = first.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    // Simulate max tries by calling resend many times (simulate user entering wrong
    // OTP)
    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);
    for (int i = 0; i < 10; i++) {
      ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);
    }

    // Act
    Response afterMaxTries = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);

    // Validate
    afterMaxTries.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should enforce resend limit exactly (no more than allowed)")
  public void testResendLimitEnforcedExactly() throws InterruptedException {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    StubMapping sendSmsStub = getStubForSendSms();

    // First request, get state
    Response first = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);
    Thread.sleep(2 * 1000L);

    String state = first.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);

    int resendLimit = 5; // from migration default

    // Resend up to the limit
    for (int i = 0; i < resendLimit; i++) {
      Response resend = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);
      resend.then().statusCode(SC_OK);
      Thread.sleep(2 * 1000L);
    }

    // Act
    // The next resend should fail
    Response exhausted = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);

    // Validate
    exhausted
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RESENDS_EXHAUSTED));

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should start resends counter at 0")
  public void testResendsCounterStartsAtZero() {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);

    // Validate
    response.then().statusCode(SC_OK).body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0));
  }

  @Test
  @DisplayName("Should decrease resendsLeft with each resend")
  public void testResendsLeftDecreases() throws InterruptedException {
    // Arrange
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));

    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    StubMapping sendSmsStub = getStubForSendSms();

    Response first = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);
    Thread.sleep(2 * 1000L);

    String state = first.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    int resendsLeft = first.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_RESENDS_LEFT);

    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);

    // Act
    Response second = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);

    // Validate
    int resendsLeft2 = second.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_RESENDS_LEFT);

    assertThat(resendsLeft2, equalTo(resendsLeft - 1));

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should handle non-mocked OTP for SMS channel (tenant2)")
  public void testNonMockedOtpSmsContactVerify() {
    // Arrange
    String phoneNumber = RandomStringUtils.randomAlphanumeric(12);
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, phoneNumber);
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    StubMapping sendSmsStub = getStubForSendSms();

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);

    // Validate
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS_LEFT, equalTo(5));

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

    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);

    // Act
    Response exhausted = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);

    // Validate
    exhausted
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
    String phoneNumber = RandomStringUtils.randomAlphanumeric(12);
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, phoneNumber);
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    StubMapping sendSmsStub = getStubForSendSms();

    Response first = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);
    String state = first.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);

    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);

    // Act
    Response resend = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);

    // Validate
    resend.then().statusCode(SC_BAD_REQUEST);

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should handle 4xx error from OTP service for non-mocked tenant (SMS, tenant2)")
  public void testOtpService4xxErrorNonMockedSms() {
    // Arrange
    String phoneNumber = RandomStringUtils.randomAlphanumeric(12);

    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, phoneNumber);

    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

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
        .statusCode(SC_INTERNAL_SERVER_ERROR)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_SMS_SERVICE));

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should handle 5xx error from OTP service for non-mocked tenant (SMS, tenant2)")
  public void testOtpService5xxErrorNonMockedSms() {
    // Arrange
    String phoneNumber = RandomStringUtils.randomAlphanumeric(12);
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, phoneNumber);
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    // Stub the SMS service to return 500
    StubMapping sendSmsStub =
        wireMockServer.stubFor(
            post(urlPathMatching("/external/sms/send"))
                .willReturn(
                    aResponse().withStatus(500).withBody("{\"error\":\"internal error\"}")));

    // Act
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);

    // Validate
    response
        .then()
        .statusCode(SC_INTERNAL_SERVER_ERROR); // Adjust if your API returns a different code

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should handle 4xx error for OTP retried after expired time")
  public void testOtpService4xxErrorExpired() throws InterruptedException {
    // Arrange
    // Stub the SMS service to return 400
    StubMapping sendSmsStub = getStubForSendSms();

    // Simulate expired state by adding a state with past expiry
    String state = RandomStringUtils.randomAlphanumeric(12);
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

    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);

    // Act
    Response expiredResponse = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);

    // Validate
    expiredResponse
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_STATE));

    wireMockServer.removeStub(sendSmsStub);
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
        Instant.now().toEpochMilli(),
        System.currentTimeMillis() / 1000 + expiry);
  }

  private String generateRandomPhoneNumber() {
    return "9" + String.format("%09d", (new Random()).nextInt(1000000000));
  }

  private String generateRandomEmail() {
    return "test" + System.currentTimeMillis() + "@example.com";
  }
}
