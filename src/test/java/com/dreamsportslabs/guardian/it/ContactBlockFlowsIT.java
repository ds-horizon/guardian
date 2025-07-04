package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_FLOW_BLOCKED;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNINUP;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.*;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.signIn;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.Setup;
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
public class ContactBlockFlowsIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String Flow_1 = "passwordless";
  private static final String Flow_2 = "social_auth";

  /** Common function to generate request body for block Flow */
  private Map<String, Object> generateBlockRequestBody(
      String contact, String[] blockFlows, String reason, String operator, Long unblockedAt) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contact);
    requestBody.put("blockFlows", blockFlows);
    requestBody.put("reason", reason);
    requestBody.put("operator", operator);
    requestBody.put("unblockedAt", unblockedAt);

    return requestBody;
  }

  /** Common function to generate passwordless init request body */
  private Map<String, Object> generatePasswordlessInitRequestBody(String email) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("flow", PASSWORDLESS_FLOW_SIGNINUP);
    requestBody.put("responseType", BODY_PARAM_RESPONSE_TYPE_TOKEN);

    List<Map<String, Object>> contacts = new ArrayList<>();
    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, BODY_CHANNEL_EMAIL);
    contact.put(BODY_PARAM_IDENTIFIER, email);
    contacts.add(contact);
    requestBody.put("contacts", contacts);

    requestBody.put("metaInfo", new HashMap<>());
    requestBody.put("additionalInfo", new HashMap<>());

    return requestBody;
  }

  /** Helper method to create OTP send request body */
  private Map<String, Object> createSendOtpBody(String email) {
    Map<String, Object> requestBody = new HashMap<>();
    Map<String, Object> contact = new HashMap<>();
    contact.put("channel", "email");
    contact.put("identifier", email);
    requestBody.put("contact", contact);
    requestBody.put("metaInfo", new HashMap<>());
    return requestBody;
  }

  /** Helper method to create OTP verify request body */
  private Map<String, Object> createVerifyOtpBody(String state, String otp) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("state", state);
    requestBody.put("otp", otp);
    return requestBody;
  }

  @Test
  @DisplayName("Should block Flows successfully")
  public void blockFlow_success() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {Flow_1, Flow_2},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response.getBody().jsonPath().getString("message"), equalTo("Flows blocked successfully"));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(2));
    assertThat(blockedFlows.contains(Flow_1), equalTo(true));
    assertThat(blockedFlows.contains(Flow_2), equalTo(true));
  }

  @Test
  @DisplayName("Should block Flows successfully with email contact")
  public void blockFlow_emailContact_success() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            EMAIL_CONTACT,
            new String[] {Flow_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(EMAIL_CONTACT));
    assertThat(
        response.getBody().jsonPath().getString("message"), equalTo("Flows blocked successfully"));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(1));
    assertThat(blockedFlows.contains(Flow_1), equalTo(true));
  }

  @Test
  @DisplayName("Should update existing block successfully")
  public void blockFlow_updateExisting_success() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt1 = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody1 =
        generateBlockRequestBody(
            contactId,
            new String[] {Flow_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt1);

    Response response1 = blockContactFlows(TENANT_ID, requestBody1);
    response1.then().statusCode(HttpStatus.SC_OK);

    List<String> blockedFlows1 = response1.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows1.size(), equalTo(1));
    assertThat(blockedFlows1.contains(Flow_1), equalTo(true));

    Long unblockedAt2 = Instant.now().plusSeconds(7200).toEpochMilli() / 1000;
    Map<String, Object> requestBody2 =
        generateBlockRequestBody(
            contactId,
            new String[] {Flow_1, Flow_2},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt2);

    // Act
    Response response2 = blockContactFlows(TENANT_ID, requestBody2);

    // Assert
    response2.then().statusCode(HttpStatus.SC_OK);

    assertThat(response2.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response2.getBody().jsonPath().getString("message"), equalTo("Flows blocked successfully"));

    List<String> blockedFlows2 = response2.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows2.size(), equalTo(2));
    assertThat(blockedFlows2.contains(Flow_1), equalTo(true));
    assertThat(blockedFlows2.contains(Flow_2), equalTo(true));
  }

  @Test
  @DisplayName("Should return error for missing contact")
  public void blockFlow_missingContact() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("blockFlows", new String[] {Flow_1});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Contact is required"));
  }

  @Test
  @DisplayName("Should return error for empty contact")
  public void blockFlow_emptyContact() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            "", new String[] {Flow_1}, randomAlphanumeric(10), randomAlphanumeric(10), unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Contact is required"));
  }

  @Test
  @DisplayName("Should return error for missing blockFlows")
  public void blockFlow_missingBlockFlows() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

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
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

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
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockFlows", new String[] {Flow_1});
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

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
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId, new String[] {Flow_1}, "", randomAlphanumeric(10), unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Reason is required"));
  }

  @Test
  @DisplayName("Should return error for missing operator")
  public void blockFlow_missingOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockFlows", new String[] {Flow_1});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Operator is required"));
  }

  @Test
  @DisplayName("Should return error for empty operator")
  public void blockFlow_emptyOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId, new String[] {Flow_1}, randomAlphanumeric(10), "", unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Operator is required"));
  }

  @Test
  @DisplayName("Should return error for past unblockedAt")
  public void blockFlow_pastUnblockedAt() {
    // Arrange
    String contactId = randomNumeric(10);
    Long pastTime = Instant.now().minusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {Flow_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            pastTime);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

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
    String contactId = randomNumeric(10);
    Long currentTime = Instant.now().toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {Flow_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            currentTime);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

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
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {Flow_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response response = blockContactFlows(randomAlphanumeric(8), requestBody);

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
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockFlows", null);
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

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
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockFlows", new String[] {Flow_1});
    requestBody.put("reason", null);
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Reason is required"));
  }

  @Test
  @DisplayName("Should return error for null operator")
  public void blockFlow_nullOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockFlows", new String[] {Flow_1});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", null);
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Operator is required"));
  }

  @Test
  @DisplayName("Should return error for null contact")
  public void blockFlow_nullContact() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", null);
    requestBody.put("blockFlows", new String[] {Flow_1});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Contact is required"));
  }

  @Test
  @DisplayName("Should return error for missing operator")
  public void blockFlow_missingUnblockedAt() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockFlows", new String[] {Flow_1});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response = blockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("unblockedAt is required"));
  }

  @Test
  @DisplayName("Should verify passwordless flow is blocked after blocking")
  public void verifyPasswordlessFlowBlocked() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {Flow_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response blockResponse = blockContactFlows(TENANT_ID, requestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Verify
    assertThat(blockResponse.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        blockResponse.getBody().jsonPath().getString("message"),
        equalTo("Flows blocked successfully"));

    // Act
    Map<String, Object> passwordlessInitBody = generatePasswordlessInitRequestBody(contactId);
    Response passwordlessInitResponse = passwordlessInit(TENANT_ID, passwordlessInitBody);

    // Assert
    passwordlessInitResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_FLOW_BLOCKED))
        .body("message", equalTo("Passwordless flow is blocked for this contact"));
  }

  // TODO: implement the social auth flow blocking test for both facebook and google

  @Test
  @DisplayName("Should verify OTP verify flow is blocked after blocking")
  public void verifyOtpVerifyFlowBlocked() {
    // Arrange - Block OTP verify flow for a test email
    String testEmail = randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody =
        generateBlockRequestBody(
            testEmail,
            new String[] {"otp_verify"},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    Response blockResponse = blockContactFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    assertThat(blockResponse.getBody().jsonPath().getString("contact"), equalTo(testEmail));
    assertThat(
        blockResponse.getBody().jsonPath().getString("message"),
        equalTo("Flows blocked successfully"));

    // Act - Try to hit OTP send API
    Map<String, Object> sendOtpBody = createSendOtpBody(testEmail);
    Response sendOtpResponse = sendOtp(TENANT_ID, sendOtpBody);

    // Verify - The response should indicate that the flow is blocked
    sendOtpResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_FLOW_BLOCKED))
        .body("message", equalTo("OTP verify flow is blocked for this contact"));
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
      Map<String, Object> blockRequestBody =
          generateBlockRequestBody(
              testEmail,
              new String[] {"otp_verify"},
              randomAlphanumeric(10),
              randomAlphanumeric(10),
              unblockedAt);

      Response blockResponse = blockContactFlows(TENANT_ID, blockRequestBody);
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
          .body("message", equalTo("OTP verify flow is blocked for this contact"));
    }
  }

  @Test
  @DisplayName("Should verify password signin flow is blocked after blocking")
  public void verifyPasswordSignInFlowBlocked() {
    // Arrange
    String username = randomAlphanumeric(10);
    String password = "password@123";
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            username,
            new String[] {"password"},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response blockResponse = blockContactFlows(TENANT_ID, requestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Assert
    assertThat(blockResponse.getBody().jsonPath().getString("contact"), equalTo(username));
    assertThat(
        blockResponse.getBody().jsonPath().getString("message"),
        equalTo("Flows blocked successfully"));

    // Act
    Response signInResponse = signIn(TENANT_ID, username, password, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Assert
    signInResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_FLOW_BLOCKED))
        .body("message", equalTo("Password signin flow is blocked for this contact"));
  }

  @Test
  @DisplayName("Should verify password signup flow is blocked after blocking")
  public void verifyPasswordSignUpFlowBlocked() {
    // Arrange
    String username = randomAlphanumeric(10);
    String password = "password@123";
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            username,
            new String[] {"password"},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response blockResponse = blockContactFlows(TENANT_ID, requestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Assert
    assertThat(blockResponse.getBody().jsonPath().getString("contact"), equalTo(username));
    assertThat(
        blockResponse.getBody().jsonPath().getString("message"),
        equalTo("Flows blocked successfully"));

    // Act
    Response signUpResponse = signUp(TENANT_ID, username, password, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Assert
    signUpResponse
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_FLOW_BLOCKED))
        .body("message", equalTo("Password signup flow is blocked for this contact"));
  }
}
