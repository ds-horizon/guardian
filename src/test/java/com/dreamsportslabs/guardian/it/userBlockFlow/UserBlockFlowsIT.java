package com.dreamsportslabs.guardian.it.userBlockFlow;

import static com.dreamsportslabs.guardian.Constants.*;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;

import com.dreamsportslabs.guardian.utils.DbUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class UserBlockFlowsIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String PASSWORD = "password@123";
  private static final String PASSWORDLESS_FLOW = "passwordless";
  private static final String SOCIAL_AUTH_FLOW = "social_auth";
  private static final String OTP_VERIFY_FLOW = "otp_verify";
  private static final String PASSWORD_FLOW = "password";
  private static final Long UNBLOCKED_AT = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
  private WireMockServer wireMockServer;

  private Map<String, Object> generateBlockRequestBody(
      String userIdentifier, String[] blockFlows, String reason, Long unblockedAt) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_USER_IDENTIFIER, userIdentifier);
    requestBody.put(BODY_PARAM_BLOCK_FLOWS, blockFlows);
    requestBody.put(BODY_PARAM_REASON, reason);
    requestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    return requestBody;
  }

  private Map<String, Object> generatePasswordlessInitRequestBody(String email) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_FLOW, PASSWORDLESS_FLOW_SIGNINUP);
    requestBody.put(BODY_PARAM_RESPONSE_TYPE, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    List<Map<String, Object>> contacts = new ArrayList<>();
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_EMAIL);
    contact.put(BODY_PARAM_IDENTIFIER, email);

    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, BODY_PARAM_OTP);
    contact.put(BODY_PARAM_TEMPLATE, template);

    contacts.add(contact);
    requestBody.put(BODY_PARAM_CONTACTS, contacts);

    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put(BODY_PARAM_DEVICE_NAME, "testDevice");
    requestBody.put(BODY_PARAM_META_INFO, metaInfo);
    requestBody.put(BODY_PARAM_ADDITIONAL_INFO, new HashMap<>());

    return requestBody;
  }

  private Map<String, Object> createSendOtpBody(String email) {
    Map<String, Object> requestBody = new HashMap<>();
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, BODY_PARAM_EMAIL);
    contact.put(BODY_PARAM_IDENTIFIER, email);
    requestBody.put(BODY_PARAM_CONTACT, contact);
    requestBody.put(BODY_PARAM_META_INFO, new HashMap<>());
    return requestBody;
  }

  private Map<String, Object> createVerifyOtpBody(String state, String otp) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_STATE, state);
    requestBody.put(BODY_PARAM_OTP, otp);
    return requestBody;
  }

  private StubMapping getStubForNonExistingUser() {
    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));
  }

  private StubMapping getStubForSendEmail() {
    return wireMockServer.stubFor(
        post(urlPathMatching("/sendEmail"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));
  }

  @Test
  @DisplayName("Should block Flows successfully")
  public void blockFlow_success() {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contact,
            new String[] {PASSWORDLESS_FLOW, SOCIAL_AUTH_FLOW},
            randomAlphanumeric(10),
            UNBLOCKED_AT);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should block Flows successfully with email userIdentifier")
  public void blockFlow_emailContact_success() {
    // Arrange

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            EMAIL_CONTACT, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), UNBLOCKED_AT);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should update existing block successfully")
  public void blockFlow_updateExisting_success() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody1 =
        generateBlockRequestBody(
            contact, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), UNBLOCKED_AT);

    Response response1 = blockUserFlows(TENANT_ID, requestBody1);
    response1.then().statusCode(HttpStatus.SC_NO_CONTENT);

    Long unblockedAt1 = Instant.now().plusSeconds(7200).toEpochMilli() / 1000;
    Map<String, Object> requestBody2 =
        generateBlockRequestBody(
            contact,
            new String[] {PASSWORDLESS_FLOW, SOCIAL_AUTH_FLOW},
            randomAlphanumeric(10),
            unblockedAt1);

    // Act
    Response response2 = blockUserFlows(TENANT_ID, requestBody2);

    // Assert
    response2.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should return error for missing userIdentifier")
  public void blockFlow_missingContact() {
    // Arrange

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            "", new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), UNBLOCKED_AT);
    requestBody.remove(BODY_PARAM_USER_IDENTIFIER);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("userIdentifier is required"));
  }

  @ParameterizedTest
  @DisplayName("Should return error for empty and null userIdentifier")
  @NullAndEmptySource
  public void blockFlow_emptyAndNullContact(String userIdentifier) {
    // Arrange

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            userIdentifier, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), UNBLOCKED_AT);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should return error for missing blockFlows")
  public void blockFlow_missingBlockFlows() {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> requestBody =
        generateBlockRequestBody(contact, new String[] {}, randomAlphanumeric(10), UNBLOCKED_AT);
    requestBody.remove(BODY_PARAM_BLOCK_FLOWS);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("At least one flow must be provided"));
  }

  @ParameterizedTest
  @DisplayName("Should return error for empty and null blockFlows array")
  @NullAndEmptySource
  public void blockFlow_emptyAndNullBlockFlows(String[] blockFlows) {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> requestBody =
        generateBlockRequestBody(contact, blockFlows, randomAlphanumeric(10), UNBLOCKED_AT);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for missing reason")
  public void blockFlow_missingReason() {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contact, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), UNBLOCKED_AT);

    requestBody.remove(BODY_PARAM_REASON);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("Reason is required"));
  }

  @ParameterizedTest
  @DisplayName("Should return error for empty and null reason")
  @NullAndEmptySource
  public void blockFlow_emptyAndNullReason(String reason) {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> requestBody =
        generateBlockRequestBody(contact, new String[] {PASSWORDLESS_FLOW}, reason, UNBLOCKED_AT);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("Reason is required"));
  }

  @Test
  @DisplayName("Should return error for past unblockedAt")
  public void blockFlow_pastUnblockedAt() {
    // Arrange
    String contact = randomNumeric(10);
    Long pastTime = Instant.now().minusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contact, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), pastTime);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("unblockedAt must be a future timestamp"));
  }

  @Test
  @DisplayName("Should return error for current unblockedAt")
  public void blockFlow_currentUnblockedAt() {
    // Arrange
    String contact = randomNumeric(10);
    Long currentTime = Instant.now().toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contact, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), currentTime);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("unblockedAt must be a future timestamp"));
  }

  @Test
  @DisplayName("Should return error for unknown tenant")
  public void blockFlow_unknownTenant() {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contact, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), UNBLOCKED_AT);

    // Act
    Response response = blockUserFlows(randomAlphanumeric(8), requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("No config found"));
  }

  @Test
  @DisplayName("Should verify passwordless flow is blocked after blocking")
  public void verifyPasswordlessFlowBlocked() {
    // Arrange
    String contact = randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";

    String reason = randomAlphanumeric(10);
    Map<String, Object> requestBody =
        generateBlockRequestBody(contact, new String[] {PASSWORDLESS_FLOW}, reason, UNBLOCKED_AT);

    StubMapping userStub = getStubForNonExistingUser();
    StubMapping emailStub = getStubForSendEmail();

    // Act
    Response blockResponse = blockUserFlows(TENANT_ID, requestBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Act
    Map<String, Object> passwordlessInitBody = generatePasswordlessInitRequestBody(contact);
    Response passwordlessInitResponse = passwordlessInit(TENANT_ID, passwordlessInitBody);

    // Assert
    passwordlessInitResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_FLOW_BLOCKED))
        .body(MESSAGE, equalTo(reason));

    wireMockServer.removeStub(userStub);
    wireMockServer.removeStub(emailStub);
  }

  // TODO: implement the social auth flow blocking test for both facebook and google

  @Test
  @DisplayName("Should verify OTP verify flow is blocked after blocking")
  public void verifyOtpVerifyFlowBlocked() {
    // Arrange
    String testEmail = randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";

    String reason = randomAlphanumeric(10);
    Map<String, Object> blockRequestBody =
        generateBlockRequestBody(testEmail, new String[] {OTP_VERIFY_FLOW}, reason, UNBLOCKED_AT);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Act
    Map<String, Object> sendOtpBody = createSendOtpBody(testEmail);
    Response sendOtpResponse = sendOtp(TENANT_ID, sendOtpBody);

    // Verify
    sendOtpResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_FLOW_BLOCKED))
        .body(MESSAGE, equalTo(reason));
  }

  @Test
  @DisplayName("Should verify OTP verify flow is blocked for existing OTP state")
  public void verifyOtpVerifyFlowBlockedForExistingState() {
    // Arrange
    String testEmail = randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
    Map<String, Object> sendOtpBody = createSendOtpBody(testEmail);
    Response sendOtpResponse = sendOtp(TENANT_ID, sendOtpBody);

    if (sendOtpResponse.getStatusCode() == HttpStatus.SC_OK) {
      String state = sendOtpResponse.getBody().jsonPath().getString(BODY_PARAM_STATE);

      String reason = randomAlphanumeric(10);
      Map<String, Object> blockRequestBody =
          generateBlockRequestBody(testEmail, new String[] {OTP_VERIFY_FLOW}, reason, UNBLOCKED_AT);

      Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
      blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

      // Act
      Map<String, Object> verifyOtpBody = createVerifyOtpBody(state, "123456");
      Response verifyOtpResponse = verifyOtp(TENANT_ID, verifyOtpBody);

      // Assert
      verifyOtpResponse
          .then()
          .statusCode(HttpStatus.SC_FORBIDDEN)
          .rootPath(ERROR)
          .body(CODE, equalTo(ERROR_FLOW_BLOCKED))
          .body(MESSAGE, equalTo(reason));
    }
  }

  @Test
  @DisplayName("Should verify password signin flow is blocked after blocking")
  public void verifyPasswordSignInFlowBlocked() {
    // Arrange
    String username = randomAlphanumeric(10);

    String reason = randomAlphanumeric(10);
    Map<String, Object> requestBody =
        generateBlockRequestBody(username, new String[] {PASSWORD_FLOW}, reason, UNBLOCKED_AT);

    // Act
    Response blockResponse = blockUserFlows(TENANT_ID, requestBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Act
    Response signInResponse = signIn(TENANT_ID, username, PASSWORD, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Assert
    signInResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_FLOW_BLOCKED))
        .body(MESSAGE, equalTo(reason));
  }

  @Test
  @DisplayName("Should verify password signup flow is blocked after blocking")
  public void verifyPasswordSignUpFlowBlocked() {
    // Arrange
    String username = randomAlphanumeric(10);

    String reason = randomAlphanumeric(10);
    Map<String, Object> requestBody =
        generateBlockRequestBody(username, new String[] {PASSWORD_FLOW}, reason, UNBLOCKED_AT);

    // Act
    Response blockResponse = blockUserFlows(TENANT_ID, requestBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Act
    Response signUpResponse = signUp(TENANT_ID, username, PASSWORD, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Assert
    signUpResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_FLOW_BLOCKED))
        .body(MESSAGE, equalTo(reason));
  }

  @Test
  @DisplayName("Should verify block is automatically lifted after unblockedAt time")
  public void verifyBlockAutomaticallyLiftedAfterUnblockedAt() {
    // Arrange
    String contact = randomNumeric(10);
    String reason = randomAlphanumeric(10);

    DbUtils.createUserFlowBlockWithImmediateExpiry(TENANT_ID, contact, PASSWORDLESS_FLOW, reason);

    StubMapping userStub = getStubForNonExistingUser();
    StubMapping emailStub = getStubForSendEmail();

    // Act
    Map<String, Object> passwordlessInitBody = generatePasswordlessInitRequestBody(contact);
    Response passwordlessInitResponse = passwordlessInit(TENANT_ID, passwordlessInitBody);

    // Assert
    passwordlessInitResponse.then().statusCode(HttpStatus.SC_OK);

    wireMockServer.removeStub(userStub);
    wireMockServer.removeStub(emailStub);
  }

  @Test
  @DisplayName("Should block all available flow types at once")
  public void blockAllAvailableFlowTypes() {
    // Arrange
    String contact = randomNumeric(10);

    String reason = randomAlphanumeric(10);
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contact,
            new String[] {PASSWORDLESS_FLOW, PASSWORD_FLOW, SOCIAL_AUTH_FLOW, OTP_VERIFY_FLOW},
            reason,
            UNBLOCKED_AT);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should throw error in case-insensitive flow names")
  public void blockFlowCaseInsensitive() {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contact,
            new String[] {"PASSWORDLESS", "social_AUTH", "Password", "OTP_verify"},
            randomAlphanumeric(10),
            UNBLOCKED_AT);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(
            MESSAGE,
            equalTo(
                "Invalid flow: PASSWORDLESS. Valid flows are: [passwordless, password, social_auth, otp_verify]"));
  }

  @Test
  @DisplayName("Should return error for invalid flow names")
  public void blockFlowInvalidFlowNames() {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contact, new String[] {"invalid_flow"}, randomAlphanumeric(10), UNBLOCKED_AT);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(
            MESSAGE,
            equalTo(
                "Invalid flow: invalid_flow. Valid flows are: [passwordless, password, social_auth, otp_verify]"));
  }

  @Test
  @DisplayName("Should handle blocking already blocked user")
  public void blockAlreadyBlockedUser() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody1 =
        generateBlockRequestBody(
            contact, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), UNBLOCKED_AT);

    Response response1 = blockUserFlows(TENANT_ID, requestBody1);
    response1.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Act
    Long unblockedAt1 = Instant.now().plusSeconds(7200).toEpochMilli() / 1000;
    Map<String, Object> requestBody2 =
        generateBlockRequestBody(
            contact, new String[] {SOCIAL_AUTH_FLOW}, randomAlphanumeric(10), unblockedAt1);

    Response response2 = blockUserFlows(TENANT_ID, requestBody2);

    // Assert
    response2.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should handle multiple user identifiers for same user")
  public void blockMultipleUserIdentifiers() {
    // Arrange
    String email = randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
    String phone = randomNumeric(10);

    String reason = randomAlphanumeric(10);

    Map<String, Object> emailBlockBody =
        generateBlockRequestBody(email, new String[] {PASSWORDLESS_FLOW}, reason, UNBLOCKED_AT);
    Response emailBlockResponse = blockUserFlows(TENANT_ID, emailBlockBody);
    emailBlockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    Map<String, Object> phoneBlockBody =
        generateBlockRequestBody(phone, new String[] {PASSWORDLESS_FLOW}, reason, UNBLOCKED_AT);
    Response phoneBlockResponse = blockUserFlows(TENANT_ID, phoneBlockBody);
    phoneBlockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should verify blocking one identifier doesn't affect other identifiers")
  public void blockOneIdentifierDoesNotAffectOthers() {
    // Arrange
    String email = randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
    String phoneNumber = randomAlphanumeric(10);

    String reason = randomAlphanumeric(10);

    Map<String, Object> blockBody =
        generateBlockRequestBody(email, new String[] {PASSWORDLESS_FLOW}, reason, UNBLOCKED_AT);
    Response blockResponse = blockUserFlows(TENANT_ID, blockBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    StubMapping userStub = getStubForNonExistingUser();
    StubMapping emailStub = getStubForSendEmail();

    // Act
    Map<String, Object> passwordlessInitBody = generatePasswordlessInitRequestBody(phoneNumber);
    Response passwordlessInitResponse = passwordlessInit(TENANT_ID, passwordlessInitBody);

    // Assert
    passwordlessInitResponse.then().statusCode(HttpStatus.SC_OK);

    wireMockServer.removeStub(userStub);
    wireMockServer.removeStub(emailStub);
  }
}
