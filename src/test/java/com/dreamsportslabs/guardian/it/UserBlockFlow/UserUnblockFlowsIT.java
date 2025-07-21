package com.dreamsportslabs.guardian.it.UserBlockFlow;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_ADDITIONAL_INFO;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_BLOCK_FLOWS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONTACTS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_FLOW;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_META_INFO;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_REASON;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_UNBLOCKED_AT;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_UNBLOCK_FLOWS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USER_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNINUP;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_BLOCKED_FLOWS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_TOTAL_COUNT;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_UNBLOCKED_FLOWS;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.blockUserFlows;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getBlockedFlows;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.passwordlessInit;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.unblockUserFlows;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserUnblockFlowsIT {

  private static final String TENANT_ID = "tenant1";
  private WireMockServer wireMockServer;
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String PASSWORDLESS = "passwordless";
  private static final String SOCIAL_AUTH = "social_auth";
  private static final String PASSWORD = "password";
  private static final String OTP_VERIFY = "otp_verify";

  private Map<String, Object> generateUnblockRequestBody(String contact, String[] unblockFlows) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    requestBody.put(BODY_PARAM_UNBLOCK_FLOWS, unblockFlows);

    return requestBody;
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

  @Test
  @DisplayName("Should unblock Flows successfully")
  public void unblockFlows_success() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    blockRequestBody.put(BODY_PARAM_BLOCK_FLOWS, new String[] {PASSWORDLESS, SOCIAL_AUTH});
    blockRequestBody.put(BODY_PARAM_REASON, randomAlphanumeric(10));
    blockRequestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Arrange
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(contact));

    List<String> unblockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_UNBLOCKED_FLOWS);
    assertThat(unblockedFlows.size(), equalTo(1));
    assertThat(unblockedFlows.contains(PASSWORDLESS), equalTo(true));
  }

  @Test
  @DisplayName("Should unblock Flows successfully with email userIdentifier")
  public void unblockFlows_emailContact_success() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, EMAIL_CONTACT);
    blockRequestBody.put(BODY_PARAM_BLOCK_FLOWS, new String[] {PASSWORDLESS});
    blockRequestBody.put(BODY_PARAM_REASON, randomAlphanumeric(10));
    blockRequestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Arrange
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(EMAIL_CONTACT, new String[] {PASSWORDLESS});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER),
        equalTo(EMAIL_CONTACT));

    List<String> unblockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_UNBLOCKED_FLOWS);
    assertThat(unblockedFlows.size(), equalTo(1));
    assertThat(unblockedFlows.contains(PASSWORDLESS), equalTo(true));
  }

  @Test
  @DisplayName("Should unblock all Flows successfully")
  public void unblockFlows_allFlows_success() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    blockRequestBody.put(BODY_PARAM_BLOCK_FLOWS, new String[] {PASSWORDLESS, SOCIAL_AUTH});
    blockRequestBody.put(BODY_PARAM_REASON, randomAlphanumeric(10));
    blockRequestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Arrange
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS, SOCIAL_AUTH});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(contact));

    List<String> unblockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_UNBLOCKED_FLOWS);
    assertThat(unblockedFlows.size(), equalTo(2));
    assertThat(unblockedFlows.contains(PASSWORDLESS), equalTo(true));
    assertThat(unblockedFlows.contains(SOCIAL_AUTH), equalTo(true));
  }

  @Test
  @DisplayName("Should handle unblocking non-blocked Flows gracefully")
  public void unblockFlows_nonBlockedFlows_success() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(contact));

    List<String> unblockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_UNBLOCKED_FLOWS);
    assertThat(unblockedFlows.size(), equalTo(1));
    assertThat(unblockedFlows.contains(PASSWORDLESS), equalTo(true));
  }

  @Test
  @DisplayName("Should return error for missing userIdentifier")
  public void unblockFlows_missingContact() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_UNBLOCK_FLOWS, new String[] {PASSWORDLESS});

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should return error for empty userIdentifier")
  public void unblockFlows_emptyContact() {
    // Arrange
    Map<String, Object> requestBody = generateUnblockRequestBody("", new String[] {PASSWORDLESS});

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should return error for missing unblockFlows")
  public void unblockFlows_missingunblockFlows() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for empty unblockFlows array")
  public void unblockFlows_emptyUnblockFlows() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody = generateUnblockRequestBody(contact, new String[] {});

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for unknown tenant")
  public void unblockFlows_unknownTenant() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});

    // Act
    Response response = unblockUserFlows(randomAlphanumeric(8), requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("No config found"));
  }

  @Test
  @DisplayName("Should return error for null unblockFlows")
  public void unblockFlows_nullunblockFlows() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    requestBody.put(BODY_PARAM_UNBLOCK_FLOWS, null);

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for null userIdentifier")
  public void unblockFlows_nullContact() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_USER_IDENTIFIER, null);
    requestBody.put(BODY_PARAM_UNBLOCK_FLOWS, new String[] {PASSWORDLESS});

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should handle unblocking already unblocked Flows gracefully")
  public void unblockFlows_unblocking_Already_UnblockedFlow() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    requestBody.put(BODY_PARAM_UNBLOCK_FLOWS, new String[] {PASSWORDLESS});

    // Act
    Response response1 = unblockUserFlows(TENANT_ID, requestBody);
    response1.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response2 = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response2.then().statusCode(HttpStatus.SC_OK);
    assertThat(
        response2.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(contact));
  }

  @Test
  @DisplayName("Should verify that unblocked flows are actually accessible after unblocking")
  public void unblockFlows_verifyAccessibilityAfterUnblocking() {
    // Arrange
    String contact = randomNumeric(10) + "@example.com";
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    blockRequestBody.put(BODY_PARAM_BLOCK_FLOWS, new String[] {PASSWORDLESS});
    blockRequestBody.put(BODY_PARAM_REASON, randomAlphanumeric(10));
    blockRequestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Verify
    Response blockedFlowsResponse = getBlockedFlows(TENANT_ID, contact);
    blockedFlowsResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(
        blockedFlowsResponse.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT),
        equalTo(1));

    // Act
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);
    unblockResponse.then().statusCode(HttpStatus.SC_OK);

    // Verify
    Response unblockedFlowsResponse = getBlockedFlows(TENANT_ID, contact);
    unblockedFlowsResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(
        unblockedFlowsResponse.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT),
        equalTo(0));

    StubMapping userStub = getStubForNonExistingUser();
    StubMapping emailStub = getStubForSendEmail();

    // Act
    Map<String, Object> passwordlessInitBody = new HashMap<>();
    passwordlessInitBody.put(BODY_PARAM_FLOW, PASSWORDLESS_FLOW_SIGNINUP);
    passwordlessInitBody.put(BODY_PARAM_RESPONSE_TYPE, BODY_PARAM_RESPONSE_TYPE_TOKEN);

    // Add a contact (email) to the contacts array
    Map<String, Object> contactInfo = new HashMap<>();
    contactInfo.put(BODY_PARAM_CHANNEL, BODY_PARAM_EMAIL);
    contactInfo.put(BODY_PARAM_IDENTIFIER, contact);
    passwordlessInitBody.put(BODY_PARAM_CONTACTS, List.of(contactInfo));

    passwordlessInitBody.put(BODY_PARAM_META_INFO, new HashMap<>());
    passwordlessInitBody.put(BODY_PARAM_ADDITIONAL_INFO, new HashMap<>());

    Response passwordlessResponse = passwordlessInit(TENANT_ID, passwordlessInitBody);

    // Assert
    passwordlessResponse.then().statusCode(HttpStatus.SC_OK);

    // Cleanup
    wireMockServer.removeStub(userStub);
    wireMockServer.removeStub(emailStub);
  }

  @Test
  @DisplayName("Should return error for invalid flow names in unblockFlows array")
  public void unblockFlows_invalidFlowNames() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody =
        generateUnblockRequestBody(contact, new String[] {"invalid_flow"});

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

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
  @DisplayName("Should verify that other flows remain blocked after partial unblock")
  public void unblockFlows_partialUnblockKeepsOthersBlocked() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    blockRequestBody.put(BODY_PARAM_BLOCK_FLOWS, new String[] {PASSWORDLESS, SOCIAL_AUTH});
    blockRequestBody.put(BODY_PARAM_REASON, randomAlphanumeric(10));
    blockRequestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);
    unblockResponse.then().statusCode(HttpStatus.SC_OK);

    // Verify
    Response blockedFlowsResponse = getBlockedFlows(TENANT_ID, contact);
    blockedFlowsResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(
        blockedFlowsResponse.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT),
        equalTo(1));

    List<String> remainingBlockedFlows =
        blockedFlowsResponse.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_BLOCKED_FLOWS);
    assertThat(remainingBlockedFlows.size(), equalTo(1));
    assertThat(remainingBlockedFlows.contains(SOCIAL_AUTH), equalTo(true));
    assertThat(remainingBlockedFlows.contains(PASSWORDLESS), equalTo(false));
  }

  @Test
  @DisplayName("Should handle unblocking flows that have already expired")
  public void unblockFlows_expiredFlows() {
    // Arrange
    String contact = randomNumeric(10);
    String reason = randomAlphanumeric(10);

    DbUtils.createUserFlowBlockWithImmediateExpiry(TENANT_ID, contact, PASSWORDLESS, reason);

    // Act
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    unblockResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(
        unblockResponse.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER),
        equalTo(contact));
  }

  @Test
  @DisplayName("Should handle case sensitivity in flow names")
  public void unblockFlows_caseSensitivity() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    blockRequestBody.put(BODY_PARAM_BLOCK_FLOWS, new String[] {PASSWORDLESS});
    blockRequestBody.put(BODY_PARAM_REASON, randomAlphanumeric(10));
    blockRequestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {"PASSWORDLESS"});
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    unblockResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(
        unblockResponse.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER),
        equalTo(contact));
  }

  @Test
  @DisplayName("Should handle unblocking large number of flows")
  public void unblockFlows_largeNumberOfFlows() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    blockRequestBody.put(
        BODY_PARAM_BLOCK_FLOWS, new String[] {PASSWORDLESS, SOCIAL_AUTH, PASSWORD, OTP_VERIFY});
    blockRequestBody.put(BODY_PARAM_REASON, randomAlphanumeric(10));
    blockRequestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(
            contact, new String[] {PASSWORDLESS, SOCIAL_AUTH, PASSWORD, OTP_VERIFY});
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    unblockResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(
        unblockResponse.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER),
        equalTo(contact));

    List<String> unblockedFlows =
        unblockResponse.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_UNBLOCKED_FLOWS);
    assertThat(unblockedFlows.size(), equalTo(4));
    assertThat(unblockedFlows.contains(PASSWORDLESS), equalTo(true));
    assertThat(unblockedFlows.contains(SOCIAL_AUTH), equalTo(true));
    assertThat(unblockedFlows.contains(PASSWORD), equalTo(true));
    assertThat(unblockedFlows.contains(OTP_VERIFY), equalTo(true));
  }

  @Test
  @DisplayName("Should handle unblocking with mixed case flow names")
  public void unblockFlows_mixedCaseFlowNames() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    blockRequestBody.put(BODY_PARAM_BLOCK_FLOWS, new String[] {PASSWORDLESS, SOCIAL_AUTH});
    blockRequestBody.put(BODY_PARAM_REASON, randomAlphanumeric(10));
    blockRequestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {"PASSWORDLESS", "SOCIAL_auth"});
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    unblockResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(
        unblockResponse.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER),
        equalTo(contact));

    List<String> unblockedFlows =
        unblockResponse.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_UNBLOCKED_FLOWS);
    assertThat(unblockedFlows.size(), equalTo(2));
  }
}
