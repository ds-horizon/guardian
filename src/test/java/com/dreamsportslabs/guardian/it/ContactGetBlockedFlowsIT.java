package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.*;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import io.restassured.response.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContactGetBlockedFlowsIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String Flow_1 = "passwordless";
  private static final String Flow_2 = "social_auth";

  @Test
  @DisplayName("Should get blocked Flows successfully")
  public void getBlockedFlows_success() {
    // Arrange - First block some Flows
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put("contact", contactId);
    blockRequestBody.put("blockFlows", new String[] {Flow_1, Flow_2});
    blockRequestBody.put("reason", randomAlphanumeric(10));
    blockRequestBody.put("operator", randomAlphanumeric(10));
    blockRequestBody.put("unblockedAt", unblockedAt);

    Response blockResponse = blockContactFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response = getBlockedFlows(TENANT_ID, contactId);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(2));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(2));
    assertThat(blockedFlows.contains(Flow_1), equalTo(true));
    assertThat(blockedFlows.contains(Flow_2), equalTo(true));
  }

  @Test
  @DisplayName("Should get blocked Flows successfully with email contact")
  public void getBlockedFlows_emailContact_success() {
    // Arrange - First block some Flows
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put("contact", EMAIL_CONTACT);
    blockRequestBody.put("blockFlows", new String[] {Flow_1});
    blockRequestBody.put("reason", randomAlphanumeric(10));
    blockRequestBody.put("operator", randomAlphanumeric(10));
    blockRequestBody.put("unblockedAt", unblockedAt);

    Response blockResponse = blockContactFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response = getBlockedFlows(TENANT_ID, EMAIL_CONTACT);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(EMAIL_CONTACT));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(1));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(1));
    assertThat(blockedFlows.contains(Flow_1), equalTo(true));
  }

  @Test
  @DisplayName("Should return empty list when no Flows are blocked")
  public void getBlockedFlows_noBlockedFlows_returnsEmptyList() {
    // Arrange
    String contactId = randomNumeric(10);

    // Act
    Response response = getBlockedFlows(TENANT_ID, contactId);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(0));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return error for unknown tenant")
  public void getBlockedFlows_unknownTenant_returnsError() {
    // Arrange
    String contactId = randomNumeric(10);

    // Act
    Response response = getBlockedFlows(randomAlphanumeric(8), contactId);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("No config found"));
  }

  @Test
  @DisplayName("Should handle very long contact ID")
  public void getBlockedFlows_longContactId_success() {
    // Arrange
    String longContactId = "a".repeat(1000);

    // Act
    Response response = getBlockedFlows(TENANT_ID, longContactId);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(longContactId));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(0));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return correct blocked Flows after partial unblock")
  public void getBlockedFlows_afterPartialUnblock_success() {
    // Arrange - First block some Flows
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put("contact", contactId);
    blockRequestBody.put("blockFlows", new String[] {Flow_1, Flow_2});
    blockRequestBody.put("reason", randomAlphanumeric(10));
    blockRequestBody.put("operator", randomAlphanumeric(10));
    blockRequestBody.put("unblockedAt", unblockedAt);

    Response blockResponse = blockContactFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Verify both Flows are blocked
    Response initialResponse = getBlockedFlows(TENANT_ID, contactId);
    initialResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(initialResponse.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(initialResponse.getBody().jsonPath().getInt("totalCount"), equalTo(2));

    // Unblock one Flow
    Map<String, Object> unblockRequestBody = new HashMap<>();
    unblockRequestBody.put("contact", contactId);
    unblockRequestBody.put("unblockFlows", new String[] {Flow_1});
    unblockRequestBody.put("operator", randomAlphanumeric(10));

    Response unblockResponse = unblockContactFlows(TENANT_ID, unblockRequestBody);
    unblockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act - Get blocked Flows again
    Response response = getBlockedFlows(TENANT_ID, contactId);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(1));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(1));
    assertThat(blockedFlows.contains(Flow_2), equalTo(true));
    assertThat(blockedFlows.contains(Flow_1), equalTo(false));
  }
}
