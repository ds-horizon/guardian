package com.dreamsportslabs.guardian.it.userBlockFlow;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_BLOCK_FLOWS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_REASON;
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
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.blockUserFlows;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getBlockedFlows;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.unblockUserFlows;
import static com.dreamsportslabs.guardian.utils.DbUtils.addDefaultClientScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.addFirstPartyClient;
import static com.dreamsportslabs.guardian.utils.DbUtils.addScope;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@Order(3)
class UserUnblockFlowsIT {

  private static final String TENANT_ID = "tenant1";
  private static final String TEST_SCOPE_1 = "testScope1";
  private WireMockServer wireMockServer;
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String PASSWORDLESS = "passwordless";
  private static final String SOCIAL_AUTH = "social_auth";
  private static final String PASSWORD = "password";
  private static final String OTP_VERIFY = "otp_verify";
  private static final Long UNBLOCKED_AT = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;

  private Map<String, Object> generateUnblockRequestBody(String contact, String[] unblockFlows) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    requestBody.put(BODY_PARAM_UNBLOCK_FLOWS, unblockFlows);

    return requestBody;
  }

  private Map<String, Object> generateBlockRequestBody(
      String userIdentifier, String[] blockFlows, String reason, Long unblockedAt) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_USER_IDENTIFIER, userIdentifier);
    requestBody.put(BODY_PARAM_BLOCK_FLOWS, blockFlows);
    requestBody.put(BODY_PARAM_REASON, reason);
    requestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

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

  @BeforeAll
  static void setup() {

    addScope(TENANT_ID, TEST_SCOPE_1);

    String client1 = addFirstPartyClient(TENANT_ID);

    addDefaultClientScopes(TENANT_ID, client1, TEST_SCOPE_1);
  }

  @Test
  @DisplayName("Should unblock Flows successfully")
  public void testUnblockFlowsSuccess() {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> blockRequestBody =
        generateBlockRequestBody(
            contact,
            new String[] {PASSWORDLESS, SOCIAL_AUTH},
            randomAlphanumeric(10),
            UNBLOCKED_AT);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Arrange
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should unblock Flows successfully with email userIdentifier")
  public void testUnblockFlowsWithEmailContactSuccess() {
    // Arrange

    Map<String, Object> blockRequestBody =
        generateBlockRequestBody(
            EMAIL_CONTACT, new String[] {PASSWORDLESS}, randomAlphanumeric(10), UNBLOCKED_AT);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Arrange
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(EMAIL_CONTACT, new String[] {PASSWORDLESS});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should unblock all Flows successfully")
  public void testUnblockAllFlowsSuccess() {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> blockRequestBody =
        generateBlockRequestBody(
            contact,
            new String[] {PASSWORDLESS, SOCIAL_AUTH},
            randomAlphanumeric(10),
            UNBLOCKED_AT);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Arrange
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS, SOCIAL_AUTH});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should handle unblocking non-blocked Flows gracefully")
  public void testUnblockNonBlockedFlowsSuccess() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should return error for missing userIdentifier")
  public void testMissingUserIdentifier() {
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

  @ParameterizedTest
  @DisplayName("Should return error for empty and Null userIdentifier")
  @NullAndEmptySource
  public void testEmptyAndNullUserIdentifier(String contact) {
    // Arrange
    Map<String, Object> requestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});

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
  public void testMissingUnblockFlows() {
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

  @ParameterizedTest
  @DisplayName("Should return error for empty and null unblockFlows array")
  @NullAndEmptySource
  public void testEmptyUnblockFlows(String[] unblockFlows) {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody = generateUnblockRequestBody(contact, unblockFlows);

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
  public void testUnknownTenant() {
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
  @DisplayName("Should handle unblocking already unblocked Flows gracefully")
  public void testUnblockingAlreadyUnblockedFlow() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    requestBody.put(BODY_PARAM_UNBLOCK_FLOWS, new String[] {PASSWORDLESS});

    // Act
    Response response1 = unblockUserFlows(TENANT_ID, requestBody);
    response1.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Act
    Response response2 = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response2.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should verify that unblocked flows are actually accessible after unblocking")
  public void testVerifyAccessibilityAfterUnblocking() {
    // Arrange
    String contact = randomNumeric(10) + "@example.com";

    Map<String, Object> blockRequestBody =
        generateBlockRequestBody(
            contact, new String[] {PASSWORDLESS}, randomAlphanumeric(10), UNBLOCKED_AT);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

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
    unblockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    StubMapping userStub = getStubForNonExistingUser();
    StubMapping emailStub = getStubForSendEmail();

    // Act

    Map<String, Object> contactInfo = new HashMap<>();
    contactInfo.put(BODY_PARAM_CHANNEL, BODY_PARAM_EMAIL);
    contactInfo.put(BODY_PARAM_IDENTIFIER, contact);

    Response passwordlessResponse =
        ApplicationIoUtils.passwordlessInit(
            TENANT_ID,
            PASSWORDLESS_FLOW_SIGNINUP,
            BODY_PARAM_RESPONSE_TYPE_TOKEN,
            List.of(contactInfo),
            new HashMap<>(),
            new HashMap<>());

    // Assert
    passwordlessResponse.then().statusCode(HttpStatus.SC_OK);

    // Cleanup
    wireMockServer.removeStub(userStub);
    wireMockServer.removeStub(emailStub);
  }

  @Test
  @DisplayName("Should return error for invalid flow names in unblockFlows array")
  public void testInvalidFlowNames() {
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
  public void testPartialUnblockKeepsOthersBlocked() {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> blockRequestBody =
        generateBlockRequestBody(
            contact,
            new String[] {PASSWORDLESS, SOCIAL_AUTH},
            randomAlphanumeric(10),
            UNBLOCKED_AT);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Act
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);
    unblockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

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
  public void testUnblockExpiredFlows() {
    // Arrange
    String contact = randomNumeric(10);
    String reason = randomAlphanumeric(10);

    DbUtils.createUserFlowBlockWithImmediateExpiry(TENANT_ID, contact, PASSWORDLESS, reason);

    // Act
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {PASSWORDLESS});
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    unblockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  @DisplayName("Should throw error in unblocking mixed case flow names")
  public void testMixedCaseFlowNames() {
    // Arrange
    String contact = randomNumeric(10);

    Map<String, Object> blockRequestBody =
        generateBlockRequestBody(
            contact,
            new String[] {PASSWORDLESS, SOCIAL_AUTH},
            randomAlphanumeric(10),
            UNBLOCKED_AT);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_NO_CONTENT);

    // Act
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {"PASSWORDLESS", "SOCIAL_auth"});
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    unblockResponse
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(
            MESSAGE,
            equalTo(
                "Invalid flow: PASSWORDLESS. Valid flows are: [passwordless, password, social_auth, otp_verify]"));
  }
}
