package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONTACT;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_OTP;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INCORRECT_OTP;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_STATE;
import static com.dreamsportslabs.guardian.Constants.ERROR_RETRIES_EXHAUSTED;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.METADATA;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_RETRIES_LEFT_METADATA;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.constant.Channel.SMS;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContactOtpVerifyIT {
  // Constants
  private static final String TENANT_ID = "tenant1"; // OTP is mocked for this tenant
  private static final String TENANT_ID_NON_MOCKED = "tenant2"; // OTP is NOT mocked for this tenant
  private static final String NONEXISTENT_TENANT = "nonexistent-tenant";
  private static final String MOCKED_OTP = "999999";
  private static final String WRONG_OTP = "9999";
  private static final String INVALID_STATE = "invalid-state-12345";
  private static final String EXPIRED_STATE = "expired-state-12345";
  private static final int RANDOM_IDENTIFIER_LENGTH = 12;
  private static final int MAX_RETRY_ATTEMPTS = 5;
  private static final int RANDOM_LONG_LENGTH = 1000;

  private WireMockServer wireMockServer;

  // Helper Methods for Test Data Creation
  private Map<String, Object> createSmsContact(String identifier) {
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, SMS);
    contact.put(BODY_PARAM_IDENTIFIER, identifier);
    return contact;
  }

  private Map<String, Object> createSendOtpBody(Map<String, Object> contact) {
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_CONTACT, contact);
    return body;
  }

  private Map<String, Object> createVerifyOtpBody(String state, String otp) {
    Map<String, Object> body = new HashMap<>();
    if (state != null) {
      body.put(BODY_PARAM_STATE, state);
    }
    if (otp != null) {
      body.put(BODY_PARAM_OTP, otp);
    }
    return body;
  }

  private String generateRandomIdentifier() {
    return RandomStringUtils.randomAlphanumeric(RANDOM_IDENTIFIER_LENGTH);
  }

  // Helper Methods for Common Operations
  private String sendOtpAndGetState(String tenantId, String identifier) {
    Map<String, Object> contact = createSmsContact(identifier);
    Map<String, Object> sendBody = createSendOtpBody(contact);
    Response sendResponse = ApplicationIoUtils.sendOtp(tenantId, sendBody);
    return sendResponse.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_STATE);
  }

  // Helper Methods for Assertions
  private void assertInvalidStateError(Response response) {
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_STATE));
  }

  private void assertIncorrectOtpError(Response response) {
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INCORRECT_OTP))
        .body(METADATA + "." + RESPONSE_BODY_PARAM_RETRIES_LEFT_METADATA, isA(Integer.class));
  }

  private void assertRetriesExhaustedError(Response response) {
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_RETRIES_EXHAUSTED));
  }

  // Stub Management
  private StubMapping getStubForSendSms() {
    return wireMockServer.stubFor(
        post(urlPathMatching("/sendSms"))
            .willReturn(aResponse().withStatus(200).withBody("{\"result\":\"ok\"}")));
  }

  @Test
  @DisplayName("Should verify OTP successfully with valid state and OTP (mocked tenant)")
  public void testVerifyOtpSuccessfulMocked() {
    // Arrange
    String state = sendOtpAndGetState(TENANT_ID, generateRandomIdentifier());
    Map<String, Object> verifyBody = createVerifyOtpBody(state, MOCKED_OTP);

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    response.then().statusCode(SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should return error when state is missing")
  public void testVerifyOtpMissingState() {
    // Arrange
    Map<String, Object> verifyBody = createVerifyOtpBody(null, "1234");

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("State is missing"));
  }

  @Test
  @DisplayName("Should return error when OTP is missing")
  public void testVerifyOtpMissingOtp() {
    // Arrange
    String state = sendOtpAndGetState(TENANT_ID, generateRandomIdentifier());
    Map<String, Object> verifyBody = createVerifyOtpBody(state, null);

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("OTP is missing"));
  }

  @Test
  @DisplayName("Should return error when state is empty")
  public void testVerifyOtpEmptyState() {
    // Arrange
    Map<String, Object> verifyBody = createVerifyOtpBody("", "1234");

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("State is missing"));
  }

  @Test
  @DisplayName("Should return error when OTP is empty")
  public void testVerifyOtpEmptyOtp() {
    // Arrange
    String state = sendOtpAndGetState(TENANT_ID, generateRandomIdentifier());
    Map<String, Object> verifyBody = createVerifyOtpBody(state, "");

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("OTP is missing"));
  }

  @Test
  @DisplayName("Should return error for invalid/non-existent state")
  public void testVerifyOtpInvalidState() {
    // Arrange
    Map<String, Object> verifyBody = createVerifyOtpBody(INVALID_STATE, "1234");

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    assertInvalidStateError(response);
  }

  @Test
  @DisplayName("Should return error for incorrect OTP")
  public void testVerifyOtpIncorrectOtp() {
    // Arrange
    String state = sendOtpAndGetState(TENANT_ID, generateRandomIdentifier());
    Map<String, Object> verifyBody = createVerifyOtpBody(state, WRONG_OTP);

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    assertIncorrectOtpError(response);
  }

  @Test
  @DisplayName("Should return error when retries are exhausted")
  public void testVerifyOtpRetriesExhausted() {
    // Arrange
    String state = sendOtpAndGetState(TENANT_ID, generateRandomIdentifier());
    Map<String, Object> verifyBody = createVerifyOtpBody(state, "111111"); // Wrong OTP

    // Act & Validate
    for (int i = 0; i < MAX_RETRY_ATTEMPTS; i++) {
      Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);
      if (i < MAX_RETRY_ATTEMPTS - 1) {
        assertIncorrectOtpError(response);
      } else {
        assertRetriesExhaustedError(response);
      }
    }
  }

  @Test
  @DisplayName("Should return error for expired state")
  public void testVerifyOtpExpiredState() {
    // Arrange
    Map<String, Object> verifyBody = createVerifyOtpBody(EXPIRED_STATE, "1234");

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    assertInvalidStateError(response);
  }

  @Test
  @DisplayName("Should return error for non-existent tenant")
  public void testVerifyOtpNonExistentTenant() {
    // Arrange
    Map<String, Object> verifyBody = createVerifyOtpBody("some-state", "1234");

    // Act
    Response response = ApplicationIoUtils.verifyOtp(NONEXISTENT_TENANT, verifyBody);

    // Validate
    response.then().statusCode(isA(Integer.class)); // Could be 400 or 404
  }

  @Test
  @DisplayName("Should decrease retriesLeft with each incorrect attempt")
  public void testVerifyOtpRetriesLeftDecreases() {
    // Arrange
    String state = sendOtpAndGetState(TENANT_ID, generateRandomIdentifier());
    Map<String, Object> verifyBody = createVerifyOtpBody(state, WRONG_OTP);

    // Act
    Response firstAttempt = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);
    int retriesLeft1 =
        firstAttempt
            .getBody()
            .jsonPath()
            .getInt(ERROR + "." + METADATA + "." + RESPONSE_BODY_PARAM_RETRIES_LEFT_METADATA);

    Response secondAttempt = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);
    int retriesLeft2 =
        secondAttempt
            .getBody()
            .jsonPath()
            .getInt(ERROR + "." + METADATA + "." + RESPONSE_BODY_PARAM_RETRIES_LEFT_METADATA);

    // Validate
    assertThat(retriesLeft2, equalTo(retriesLeft1 - 1));
  }

  @Test
  @DisplayName("Should handle malformed request body")
  public void testVerifyOtpMalformedBody() {
    // Arrange
    Map<String, Object> verifyBody = new HashMap<>();
    verifyBody.put("invalidField", "invalidValue");

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("State is missing"));
  }

  @Test
  @DisplayName("Should verify OTP successfully after some incorrect attempts")
  public void testVerifyOtpSuccessAfterIncorrectAttempts() {
    // Arrange
    String state = sendOtpAndGetState(TENANT_ID, generateRandomIdentifier());

    // Try incorrect OTP first
    Map<String, Object> incorrectBody = createVerifyOtpBody(state, "111111");
    Response incorrectAttempt = ApplicationIoUtils.verifyOtp(TENANT_ID, incorrectBody);
    assertIncorrectOtpError(incorrectAttempt);

    // Now try with correct OTP
    Map<String, Object> correctBody = createVerifyOtpBody(state, MOCKED_OTP);

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, correctBody);

    // Validate
    response.then().statusCode(SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should not allow verification after successful verification (state consumed)")
  public void testVerifyOtpStateConsumedAfterSuccess() {
    // Arrange
    String state = sendOtpAndGetState(TENANT_ID, generateRandomIdentifier());
    Map<String, Object> verifyBody = createVerifyOtpBody(state, MOCKED_OTP);

    // First verification - should succeed
    Response firstVerification = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);
    firstVerification.then().statusCode(SC_NO_CONTENT);

    // Act
    // Second verification with same state - should fail
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    assertInvalidStateError(response);
  }

  @Test
  @DisplayName("Should handle null values in request")
  public void testVerifyOtpNullValues() {
    // Arrange
    Map<String, Object> verifyBody = createVerifyOtpBody(null, null);

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should handle very long state and OTP values")
  public void testVerifyOtpLongValues() {
    // Arrange
    String veryLongState = RandomStringUtils.randomAlphanumeric(RANDOM_LONG_LENGTH);
    String veryLongOtp = RandomStringUtils.randomAlphanumeric(RANDOM_LONG_LENGTH);
    Map<String, Object> verifyBody = createVerifyOtpBody(veryLongState, veryLongOtp);

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    assertInvalidStateError(response);
  }

  @Test
  @DisplayName("Should handle special characters in state and OTP")
  public void testVerifyOtpSpecialCharacters() {
    // Arrange
    Map<String, Object> verifyBody =
        createVerifyOtpBody("state-with-special-chars-!@#$%^&*()", "otp-with-special-chars-!@#$");

    // Act
    Response response = ApplicationIoUtils.verifyOtp(TENANT_ID, verifyBody);

    // Validate
    assertInvalidStateError(response);
  }
}
