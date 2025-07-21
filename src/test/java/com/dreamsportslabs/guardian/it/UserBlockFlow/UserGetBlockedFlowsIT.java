package com.dreamsportslabs.guardian.it.UserBlockFlow;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_BLOCK_FLOWS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_REASON;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_UNBLOCKED_AT;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_UNBLOCK_FLOWS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USER_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_BLOCKED_FLOWS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_TOTAL_COUNT;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.blockUserFlows;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getBlockedFlows;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.unblockUserFlows;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserGetBlockedFlowsIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String PASSWORDLESS = "passwordless";
  private static final String SOCIAL_AUTH = "social_auth";
  private static final String PASSWORD = "password";
  private static final String OTP_VERIFY = "otp_verify";

  @Test
  @DisplayName("Should get blocked Flows successfully")
  public void getBlockedFlows_success() {
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
    Response response = getBlockedFlows(TENANT_ID, contact);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(contact));
    assertThat(response.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT), equalTo(2));

    List<String> blockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_BLOCKED_FLOWS);
    assertThat(blockedFlows.size(), equalTo(2));
    assertThat(blockedFlows.contains(PASSWORDLESS), equalTo(true));
    assertThat(blockedFlows.contains(SOCIAL_AUTH), equalTo(true));
  }

  @Test
  @DisplayName("Should get blocked Flows successfully with email userIdentifier")
  public void getBlockedFlows_emailContact_success() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, EMAIL_CONTACT);
    blockRequestBody.put(BODY_PARAM_BLOCK_FLOWS, new String[] {PASSWORDLESS});
    blockRequestBody.put(BODY_PARAM_REASON, randomAlphanumeric(10));
    blockRequestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response = getBlockedFlows(TENANT_ID, EMAIL_CONTACT);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER),
        equalTo(EMAIL_CONTACT));
    assertThat(response.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT), equalTo(1));

    List<String> blockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_BLOCKED_FLOWS);
    assertThat(blockedFlows.size(), equalTo(1));
    assertThat(blockedFlows.contains(PASSWORDLESS), equalTo(true));
  }

  @Test
  @DisplayName("Should return empty list when no Flows are blocked")
  public void getBlockedFlows_noBlockedFlows_returnsEmptyList() {
    // Arrange
    String contact = randomNumeric(10);

    // Act
    Response response = getBlockedFlows(TENANT_ID, contact);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(contact));
    assertThat(response.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT), equalTo(0));

    List<String> blockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_BLOCKED_FLOWS);
    assertThat(blockedFlows.size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return error for unknown tenant")
  public void getBlockedFlows_unknownTenant_returnsError() {
    // Arrange
    String contact = randomNumeric(10);

    // Act
    Response response = getBlockedFlows(randomAlphanumeric(8), contact);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("invalid_request"))
        .body(MESSAGE, equalTo("No config found"));
  }

  @Test
  @DisplayName("Should handle very long userIdentifier ID")
  public void getBlockedFlows_longcontact_success() {
    // Arrange
    String longcontact = "a".repeat(1000);

    // Act
    Response response = getBlockedFlows(TENANT_ID, longcontact);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(longcontact));
    assertThat(response.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT), equalTo(0));

    List<String> blockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_BLOCKED_FLOWS);
    assertThat(blockedFlows.size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return correct blocked Flows after partial unblock")
  public void getBlockedFlows_afterPartialUnblock_success() {
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

    // Verify
    Response initialResponse = getBlockedFlows(TENANT_ID, contact);
    initialResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(
        initialResponse.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER),
        equalTo(contact));
    assertThat(
        initialResponse.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT), equalTo(2));

    // Act
    Map<String, Object> unblockRequestBody = new HashMap<>();
    unblockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    unblockRequestBody.put(BODY_PARAM_UNBLOCK_FLOWS, new String[] {PASSWORDLESS});

    // verify
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);
    unblockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response = getBlockedFlows(TENANT_ID, contact);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(contact));
    assertThat(response.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT), equalTo(1));

    List<String> blockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_BLOCKED_FLOWS);
    assertThat(blockedFlows.size(), equalTo(1));
    assertThat(blockedFlows.contains(SOCIAL_AUTH), equalTo(true));
    assertThat(blockedFlows.contains(PASSWORDLESS), equalTo(false));
  }

  @Test
  @DisplayName("Should return error for missing userIdentifier parameter")
  public void getBlockedFlows_missingUserIdentifier() {
    // Act
    Response response = getBlockedFlows(TENANT_ID, null);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("invalid_query_param"))
        .body(MESSAGE, equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should return error for empty userIdentifier parameter")
  public void getBlockedFlows_emptyUserIdentifier() {
    // Act
    Response response = getBlockedFlows(TENANT_ID, "");

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("invalid_query_param"))
        .body(MESSAGE, equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should return empty list for expired blocks")
  public void getBlockedFlows_expiredBlocks() {
    // Arrange
    String contact = randomNumeric(10);
    String reason = randomAlphanumeric(10);

    DbUtils.createUserFlowBlockWithImmediateExpiry(TENANT_ID, contact, PASSWORDLESS, reason);
    DbUtils.createUserFlowBlockWithImmediateExpiry(TENANT_ID, contact, SOCIAL_AUTH, reason);

    // Act
    Response response = getBlockedFlows(TENANT_ID, contact);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(contact));
    assertThat(response.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT), equalTo(0));

    List<String> blockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_BLOCKED_FLOWS);
    assertThat(blockedFlows.size(), equalTo(0));
  }

  @Test
  @DisplayName("Should handle permanently blocked flows")
  public void getBlockedFlows_permanentlyBlocked() {
    // Arrange
    String contact = randomNumeric(10);

    Long unblockedAt = Instant.now().plusSeconds(31536000).toEpochMilli() / 1000; // 1 year
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put(BODY_PARAM_USER_IDENTIFIER, contact);
    blockRequestBody.put(BODY_PARAM_BLOCK_FLOWS, new String[] {PASSWORDLESS, SOCIAL_AUTH});
    blockRequestBody.put(BODY_PARAM_REASON, randomAlphanumeric(10));
    blockRequestBody.put(BODY_PARAM_UNBLOCKED_AT, unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response = getBlockedFlows(TENANT_ID, contact);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(contact));
    assertThat(response.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT), equalTo(2));

    List<String> blockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_BLOCKED_FLOWS);
    assertThat(blockedFlows.size(), equalTo(2));
    assertThat(blockedFlows.contains(PASSWORDLESS), equalTo(true));
    assertThat(blockedFlows.contains(SOCIAL_AUTH), equalTo(true));
  }

  @Test
  @DisplayName("Should handle large number of blocked flows")
  public void getBlockedFlows_largeNumberOfFlows() {
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
    Response response = getBlockedFlows(TENANT_ID, contact);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(
        response.getBody().jsonPath().getString(BODY_PARAM_USER_IDENTIFIER), equalTo(contact));
    assertThat(response.getBody().jsonPath().getInt(RESPONSE_BODY_PARAM_TOTAL_COUNT), equalTo(4));

    List<String> blockedFlows =
        response.getBody().jsonPath().getList(RESPONSE_BODY_PARAM_BLOCKED_FLOWS);
    assertThat(blockedFlows.size(), equalTo(4));
    assertThat(blockedFlows.contains(PASSWORDLESS), equalTo(true));
    assertThat(blockedFlows.contains(SOCIAL_AUTH), equalTo(true));
    assertThat(blockedFlows.contains(PASSWORD), equalTo(true));
    assertThat(blockedFlows.contains(OTP_VERIFY), equalTo(true));
  }
}
