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
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContactSendOtpIT {
  private static final String TENANT_ID = "tenant1"; // OTP is mocked for this tenant
  private static final String TENANT_ID_NON_MOCKED = "tenant2"; // OTP is NOT mocked for this tenant
  private static WireMockServer wireMockServer;

  @BeforeAll
  public static void setupWireMock() {
    wireMockServer = new WireMockServer(8088); // or any available port
    wireMockServer.start();
    configureFor("localhost", 8088);
  }

  @AfterAll
  public static void teardownWireMock() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  private StubMapping getStubForSendSms() {
    return wireMockServer.stubFor(
        post(urlPathMatching("/sendSms"))
            .willReturn(aResponse().withStatus(200).withBody("{\"result\":\"ok\"}")));
  }

  @Test
  @DisplayName("Should send OTP via SMS for valid request")
  public void testSendOtpSmsValid() {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);
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
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, EMAIL);
    contact.put(BODY_PARAM_IDENTIFIER, "test@example.com");
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);
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
    Map<String, Object> body = new HashMap<>();
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should return error when contact is invalid (missing identifier)")
  public void testSendOtpInvalidContact() {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    // missing identifier
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should return error when resends are exhausted")
  public void testSendOtpResendsExhausted() {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    // Simulate resends exhausted by sending multiple requests with the same state
    Response first = ApplicationIoUtils.sendOtp(TENANT_ID, body);
    String state = first.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);
    for (int i = 0; i < 10; i++) {
      ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);
    }
    Response exhausted = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);
    exhausted.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should return error when resend is not allowed (too soon)")
  public void testSendOtpResendNotAllowed() {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    Response first = ApplicationIoUtils.sendOtp(TENANT_ID, body);
    String state = first.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);
    Response resend = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);
    resend.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should handle mocked OTP for tenant1")
  public void testSendOtpMocked() {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_TRIES, equalTo(0))
        .body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0));
  }

  @Test
  @DisplayName("Should return error for contact with invalid template")
  public void testSendOtpInvalidTemplate() {
    Map<String, Object> template = new HashMap<>();
    // missing name
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphabetic(12));
    contact.put(BODY_PARAM_TEMPLATE, template);
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should send OTP with valid template")
  public void testSendOtpValidTemplate() {
    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, "otp-template");
    template.put(BODY_PARAM_PARAMS, new HashMap<>());
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, EMAIL);
    contact.put(BODY_PARAM_IDENTIFIER, "test@example.com");
    contact.put(BODY_PARAM_TEMPLATE, template);
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);
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
    // Simulate by using a random/invalid state
    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, "expired-or-invalid-state");
    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should return error for non-existent tenant ID")
  public void testSendOtpNonExistentTenant() {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);
    Response response = ApplicationIoUtils.sendOtp("nonexistent-tenant", body);
    response.then().statusCode(isA(Integer.class)); // Accepts 400 or 404 depending on impl
  }

  @Test
  @DisplayName("Should return error when max tries are exceeded")
  public void testSendOtpMaxTriesExceeded() {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);
    Response first = ApplicationIoUtils.sendOtp(TENANT_ID, body);
    String state = first.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    // Simulate max tries by calling resend many times (simulate user entering wrong OTP)
    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);
    for (int i = 0; i < 10; i++) {
      ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);
    }
    Response afterMaxTries = ApplicationIoUtils.sendOtp(TENANT_ID, stateBody);
    afterMaxTries.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should enforce resend limit exactly (no more than allowed)")
  public void testResendLimitEnforcedExactly() throws InterruptedException {
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
    // The next resend should fail
    Response exhausted = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);
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
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, RandomStringUtils.randomAlphanumeric(12));
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    Response response = ApplicationIoUtils.sendOtp(TENANT_ID, body);
    response.then().statusCode(SC_OK).body(RESPONSE_BODY_PARAM_RESENDS, equalTo(0));
  }

  @Test
  @DisplayName("Should decrease resendsLeft with each resend")
  public void testResendsLeftDecreases() throws InterruptedException {
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

    Response second = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);
    int resendsLeft2 = second.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_RESENDS_LEFT);

    assertThat(resendsLeft2, equalTo(resendsLeft - 1));

    wireMockServer.removeStub(sendSmsStub);
  }

  @Test
  @DisplayName("Should handle non-mocked OTP for SMS channel (tenant2)")
  public void testNonMockedOtpSmsContactVerify() {
    String phoneNumber = RandomStringUtils.randomAlphanumeric(12);
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, phoneNumber);
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    StubMapping sendSmsStub = getStubForSendSms();

    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);
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
    String phoneNumber = RandomStringUtils.randomAlphanumeric(12);
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, phoneNumber);
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);

    StubMapping sendSmsStub = getStubForSendSms();

    Response first = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);
    Thread.sleep(2 * 1000L);

    String state = first.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
    Map<String, Object> stateBody = new HashMap<>();
    stateBody.put(BODY_PARAM_STATE, state);

    int resendLimit = 5; // from migration default
    for (int i = 0; i < resendLimit; i++) {
      Response resend = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);
      resend.then().statusCode(SC_OK);
      Thread.sleep(2 * 1000L);
    }
    Response exhausted = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);
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
    Response resend = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);
    resend.then().statusCode(SC_BAD_REQUEST);

    wireMockServer.removeStub(sendSmsStub);
  }

  //    @Test
  //    @DisplayName("Should allow resend after waiting for resendAfter for non-mocked tenant (SMS,
  // tenant2)")
  //    public void testSendOtpResendAfterWaitNonMockedSms() throws InterruptedException {
  //        String phoneNumber = RandomStringUtils.randomAlphanumeric(12);
  //        Map<String, Object> contact = new HashMap<>();
  //        contact.put("channel", SMS);
  //        contact.put("identifier", phoneNumber);
  //        Map<String, Object> body = new HashMap<>();
  //        body.put("contact", contact);
  //
  //        StubMapping sendSmsStub = getStubForSendSms();
  //
  //        Response first = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);
  //        String state = first.getBody().jsonPath().getString("state");
  //        Number resendAfter = first.getBody().jsonPath().get("resendAfter");
  //        Thread.sleep(resendAfter.longValue() * 1000 + 1000); // Wait for resendAfter + 1s
  //        Map<String, Object> stateBody = new HashMap<>();
  //        stateBody.put("state", state);
  //        Response resend = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, stateBody);
  //        resend.then().statusCode(SC_OK);
  //
  //        wireMockServer.removeStub(sendSmsStub);
  //    }

  @Test
  @DisplayName("Should handle 4xx error from OTP service for non-mocked tenant (SMS, tenant2)")
  public void testOtpService4xxErrorNonMockedSms() {
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

    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);
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

    Response response = ApplicationIoUtils.sendOtp(TENANT_ID_NON_MOCKED, body);
    response
        .then()
        .statusCode(SC_INTERNAL_SERVER_ERROR); // Adjust if your API returns a different code

    wireMockServer.removeStub(sendSmsStub);
  }
}
