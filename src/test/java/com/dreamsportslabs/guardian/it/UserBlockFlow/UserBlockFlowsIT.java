package com.dreamsportslabs.guardian.it.UserBlockFlow;

import static com.dreamsportslabs.guardian.Constants.*;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.Setup;
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
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Setup.class)
public class UserBlockFlowsIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String PASSWORDLESS_FLOW = "passwordless";
  private static final String SOCIAL_AUTH_FLOW = "social_auth";
  private static final String OTP_VERIFY_FLOW = "otp_verify";
  private static final String PASSWORD_FLOW = "password";
  private WireMockServer wireMockServer;

  private Map<String, Object> generateBlockRequestBody(
      String userIdentifier, String[] blockFlows, String reason, Long unblockedAt) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", userIdentifier);
    requestBody.put("blockFlows", blockFlows);
    requestBody.put("reason", reason);
    requestBody.put("unblockedAt", unblockedAt);

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

    // Add the required template field
    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, "otp");
    contact.put(BODY_PARAM_TEMPLATE, template);

    contacts.add(contact);
    requestBody.put(BODY_PARAM_CONTACTS, contacts);

    // Add metaInfo with device name like in PasswordlessInitIT
    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put(BODY_PARAM_DEVICE_NAME, "testDevice");
    requestBody.put(BODY_PARAM_META_INFO, metaInfo);
    requestBody.put(BODY_PARAM_ADDITIONAL_INFO, new HashMap<>());

    return requestBody;
  }

  private Map<String, Object> createSendOtpBody(String email) {
    Map<String, Object> requestBody = new HashMap<>();
    Map<String, Object> contact = new HashMap<>();
    contact.put("channel", "email");
    contact.put("identifier", email);
    requestBody.put("contact", contact);
    requestBody.put("metaInfo", new HashMap<>());
    return requestBody;
  }

  private Map<String, Object> createVerifyOtpBody(String state, String otp) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("state", state);
    requestBody.put("otp", otp);
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
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contact,
            new String[] {PASSWORDLESS_FLOW, SOCIAL_AUTH_FLOW},
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(2));
    assertThat(blockedFlows.contains(PASSWORDLESS_FLOW), equalTo(true));
    assertThat(blockedFlows.contains(SOCIAL_AUTH_FLOW), equalTo(true));
  }

  @Test
  @DisplayName("Should block Flows successfully with email userIdentifier")
  public void blockFlow_emailContact_success() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            EMAIL_CONTACT, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(EMAIL_CONTACT));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(1));
    assertThat(blockedFlows.contains(PASSWORDLESS_FLOW), equalTo(true));
  }

  @Test
  @DisplayName("Should update existing block successfully")
  public void blockFlow_updateExisting_success() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt1 = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody1 =
        generateBlockRequestBody(
            contact, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), unblockedAt1);

    Response response1 = blockUserFlows(TENANT_ID, requestBody1);
    response1.then().statusCode(HttpStatus.SC_OK);

    List<String> blockedFlows1 = response1.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows1.size(), equalTo(1));
    assertThat(blockedFlows1.contains(PASSWORDLESS_FLOW), equalTo(true));

    Long unblockedAt2 = Instant.now().plusSeconds(7200).toEpochMilli() / 1000;
    Map<String, Object> requestBody2 =
        generateBlockRequestBody(
            contact,
            new String[] {PASSWORDLESS_FLOW, SOCIAL_AUTH_FLOW},
            randomAlphanumeric(10),
            unblockedAt2);

    // Act
    Response response2 = blockUserFlows(TENANT_ID, requestBody2);

    // Assert
    response2.then().statusCode(HttpStatus.SC_OK);

    assertThat(response2.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));

    List<String> blockedFlows2 = response2.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows2.size(), equalTo(2));
    assertThat(blockedFlows2.contains(PASSWORDLESS_FLOW), equalTo(true));
    assertThat(blockedFlows2.contains(SOCIAL_AUTH_FLOW), equalTo(true));
  }

  @Test
  @DisplayName("Should return error for missing userIdentifier")
  public void blockFlow_missingContact() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("blockFlows", new String[] {PASSWORDLESS_FLOW});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should return error for empty userIdentifier")
  public void blockFlow_emptyContact() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            "", new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should return error for missing blockFlows")
  public void blockFlow_missingBlockFlows() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", contact);
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for empty blockFlows array")
  public void blockFlow_emptyBlockFlows() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(contact, new String[] {}, randomAlphanumeric(10), unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for missing reason")
  public void blockFlow_missingReason() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", contact);
    requestBody.put("blockFlows", new String[] {PASSWORDLESS_FLOW});
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Reason is required"));
  }

  @Test
  @DisplayName("Should return error for empty reason")
  public void blockFlow_emptyReason() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(contact, new String[] {PASSWORDLESS_FLOW}, "", unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Reason is required"));
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
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("unblockedAt must be a future timestamp"));
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
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("unblockedAt must be a future timestamp"));
  }

  @Test
  @DisplayName("Should return error for unknown tenant")
  public void blockFlow_unknownTenant() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contact, new String[] {PASSWORDLESS_FLOW}, randomAlphanumeric(10), unblockedAt);

    // Act
    Response response = blockUserFlows(randomAlphanumeric(8), requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("No config found"));
  }

  @Test
  @DisplayName("Should return error for null blockFlows")
  public void blockFlow_nullBlockFlows() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", contact);
    requestBody.put("blockFlows", null);
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for null reason")
  public void blockFlow_nullReason() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", contact);
    requestBody.put("blockFlows", new String[] {PASSWORDLESS_FLOW});
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Reason is required"));
  }

  @Test
  @DisplayName("Should return error for null userIdentifier")
  public void blockFlow_nullContact() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", null);
    requestBody.put("blockFlows", new String[] {PASSWORDLESS_FLOW});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should verify passwordless flow is blocked after blocking")
  public void verifyPasswordlessFlowBlocked() {
    // Arrange
    String contact = randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    String reason = randomAlphanumeric(10);
    Map<String, Object> requestBody =
        generateBlockRequestBody(contact, new String[] {PASSWORDLESS_FLOW}, reason, unblockedAt);

    // Set up WireMock stubs
    StubMapping userStub = getStubForNonExistingUser();
    StubMapping emailStub = getStubForSendEmail();

    // Act
    Response blockResponse = blockUserFlows(TENANT_ID, requestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Verify
    assertThat(blockResponse.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));

    // Act
    Map<String, Object> passwordlessInitBody = generatePasswordlessInitRequestBody(contact);
    Response passwordlessInitResponse = passwordlessInit(TENANT_ID, passwordlessInitBody);

    // Assert
    passwordlessInitResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_FLOW_BLOCKED))
        .body("message", equalTo(reason));

    // Cleanup
    wireMockServer.removeStub(userStub);
    wireMockServer.removeStub(emailStub);
  }

  // TODO: implement the social auth flow blocking test for both facebook and google

  @Test
  @DisplayName("Should verify OTP verify flow is blocked after blocking")
  public void verifyOtpVerifyFlowBlocked() {
    // Arrange
    String testEmail = randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    String reason = randomAlphanumeric(10);
    Map<String, Object> blockRequestBody =
        generateBlockRequestBody(testEmail, new String[] {OTP_VERIFY_FLOW}, reason, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    assertThat(blockResponse.getBody().jsonPath().getString("userIdentifier"), equalTo(testEmail));

    // Act
    Map<String, Object> sendOtpBody = createSendOtpBody(testEmail);
    Response sendOtpResponse = sendOtp(TENANT_ID, sendOtpBody);

    // Verify
    sendOtpResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_FLOW_BLOCKED))
        .body("message", equalTo(reason));
  }

  @Test
  @DisplayName("Should verify OTP verify flow is blocked for existing OTP state")
  public void verifyOtpVerifyFlowBlockedForExistingState() {
    // Arrange
    String testEmail = randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
    Map<String, Object> sendOtpBody = createSendOtpBody(testEmail);
    Response sendOtpResponse = sendOtp(TENANT_ID, sendOtpBody);

    if (sendOtpResponse.getStatusCode() == HttpStatus.SC_OK) {
      String state = sendOtpResponse.getBody().jsonPath().getString("state");

      Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
      String reason = randomAlphanumeric(10);
      Map<String, Object> blockRequestBody =
          generateBlockRequestBody(testEmail, new String[] {OTP_VERIFY_FLOW}, reason, unblockedAt);

      Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
      blockResponse.then().statusCode(HttpStatus.SC_OK);

      // Act
      Map<String, Object> verifyOtpBody = createVerifyOtpBody(state, "123456");
      Response verifyOtpResponse = verifyOtp(TENANT_ID, verifyOtpBody);

      // Assert
      verifyOtpResponse
          .then()
          .statusCode(HttpStatus.SC_FORBIDDEN)
          .rootPath(ERROR)
          .body("code", equalTo(ERROR_FLOW_BLOCKED))
          .body("message", equalTo(reason));
    }
  }

  @Test
  @DisplayName("Should verify password signin flow is blocked after blocking")
  public void verifyPasswordSignInFlowBlocked() {
    // Arrange
    String username = randomAlphanumeric(10);
    String password = "password@123";
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    String reason = randomAlphanumeric(10);
    Map<String, Object> requestBody =
        generateBlockRequestBody(username, new String[] {PASSWORD_FLOW}, reason, unblockedAt);

    // Act
    Response blockResponse = blockUserFlows(TENANT_ID, requestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Assert
    assertThat(blockResponse.getBody().jsonPath().getString("userIdentifier"), equalTo(username));

    // Act
    Response signInResponse = signIn(TENANT_ID, username, password, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Assert
    signInResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_FLOW_BLOCKED))
        .body("message", equalTo(reason));
  }

  @Test
  @DisplayName("Should verify password signup flow is blocked after blocking")
  public void verifyPasswordSignUpFlowBlocked() {
    // Arrange
    String username = randomAlphanumeric(10);
    String password = "password@123";
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    String reason = randomAlphanumeric(10);
    Map<String, Object> requestBody =
        generateBlockRequestBody(username, new String[] {PASSWORD_FLOW}, reason, unblockedAt);

    // Act
    Response blockResponse = blockUserFlows(TENANT_ID, requestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Assert
    assertThat(blockResponse.getBody().jsonPath().getString("userIdentifier"), equalTo(username));

    // Act
    Response signUpResponse = signUp(TENANT_ID, username, password, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Assert
    signUpResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_FLOW_BLOCKED))
        .body("message", equalTo(reason));
  }
}
