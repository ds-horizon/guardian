package com.dreamsportslabs.guardian.it.UserBlockFlow;

import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.blockUserFlows;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getBlockedFlows;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.unblockUserFlows;
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

public class UserGetBlockedFlowsIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String Flow_1 = "passwordless";
  private static final String Flow_2 = "social_auth";

  @Test
  @DisplayName("Should get blocked Flows successfully")
  public void getBlockedFlows_success() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put("userIdentifier", contact);
    blockRequestBody.put("blockFlows", new String[] {Flow_1, Flow_2});
    blockRequestBody.put("reason", randomAlphanumeric(10));
    blockRequestBody.put("unblockedAt", unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response = getBlockedFlows(TENANT_ID, contact);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(2));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(2));
    assertThat(blockedFlows.contains(Flow_1), equalTo(true));
    assertThat(blockedFlows.contains(Flow_2), equalTo(true));
  }

  @Test
  @DisplayName("Should get blocked Flows successfully with email userIdentifier")
  public void getBlockedFlows_emailContact_success() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put("userIdentifier", EMAIL_CONTACT);
    blockRequestBody.put("blockFlows", new String[] {Flow_1});
    blockRequestBody.put("reason", randomAlphanumeric(10));
    blockRequestBody.put("unblockedAt", unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response = getBlockedFlows(TENANT_ID, EMAIL_CONTACT);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(EMAIL_CONTACT));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(1));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(1));
    assertThat(blockedFlows.contains(Flow_1), equalTo(true));
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

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(0));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
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
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("No config found"));
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

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(longcontact));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(0));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return correct blocked Flows after partial unblock")
  public void getBlockedFlows_afterPartialUnblock_success() {
    // Arrange
    String contact = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put("userIdentifier", contact);
    blockRequestBody.put("blockFlows", new String[] {Flow_1, Flow_2});
    blockRequestBody.put("reason", randomAlphanumeric(10));
    blockRequestBody.put("unblockedAt", unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Verify
    Response initialResponse = getBlockedFlows(TENANT_ID, contact);
    initialResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(initialResponse.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));
    assertThat(initialResponse.getBody().jsonPath().getInt("totalCount"), equalTo(2));

    // Act
    Map<String, Object> unblockRequestBody = new HashMap<>();
    unblockRequestBody.put("userIdentifier", contact);
    unblockRequestBody.put("unblockFlows", new String[] {Flow_1});

    // verify
    Response unblockResponse = unblockUserFlows(TENANT_ID, unblockRequestBody);
    unblockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response = getBlockedFlows(TENANT_ID, contact);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(1));

    List<String> blockedFlows = response.getBody().jsonPath().getList("blockedFlows");
    assertThat(blockedFlows.size(), equalTo(1));
    assertThat(blockedFlows.contains(Flow_2), equalTo(true));
    assertThat(blockedFlows.contains(Flow_1), equalTo(false));
  }
}
