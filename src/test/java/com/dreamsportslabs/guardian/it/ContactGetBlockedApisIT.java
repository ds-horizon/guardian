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

public class ContactGetBlockedApisIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String API_ENDPOINT_1 = "/api/v1/test/1";
  private static final String API_ENDPOINT_2 = "/api/v2/test/2";

  @Test
  @DisplayName("Should get blocked APIs successfully")
  public void getBlockedApis_success() {
    // Arrange - First block some APIs
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put("contact", contactId);
    blockRequestBody.put("blockApis", new String[] {API_ENDPOINT_1, API_ENDPOINT_2});
    blockRequestBody.put("reason", randomAlphanumeric(10));
    blockRequestBody.put("operator", randomAlphanumeric(10));
    blockRequestBody.put("unblockedAt", unblockedAt);

    Response blockResponse = blockContactApis(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response = getBlockedApis(TENANT_ID, contactId);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(2));

    List<String> blockedApis = response.getBody().jsonPath().getList("blockedApis");
    assertThat(blockedApis.size(), equalTo(2));
    assertThat(blockedApis.contains(API_ENDPOINT_1), equalTo(true));
    assertThat(blockedApis.contains(API_ENDPOINT_2), equalTo(true));
  }

  @Test
  @DisplayName("Should get blocked APIs successfully with email contact")
  public void getBlockedApis_emailContact_success() {
    // Arrange - First block some APIs
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put("contact", EMAIL_CONTACT);
    blockRequestBody.put("blockApis", new String[] {API_ENDPOINT_1});
    blockRequestBody.put("reason", randomAlphanumeric(10));
    blockRequestBody.put("operator", randomAlphanumeric(10));
    blockRequestBody.put("unblockedAt", unblockedAt);

    Response blockResponse = blockContactApis(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response = getBlockedApis(TENANT_ID, EMAIL_CONTACT);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(EMAIL_CONTACT));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(1));

    List<String> blockedApis = response.getBody().jsonPath().getList("blockedApis");
    assertThat(blockedApis.size(), equalTo(1));
    assertThat(blockedApis.contains(API_ENDPOINT_1), equalTo(true));
  }

  @Test
  @DisplayName("Should return empty list when no APIs are blocked")
  public void getBlockedApis_noBlockedApis_returnsEmptyList() {
    // Arrange
    String contactId = randomNumeric(10);

    // Act
    Response response = getBlockedApis(TENANT_ID, contactId);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(0));

    List<String> blockedApis = response.getBody().jsonPath().getList("blockedApis");
    assertThat(blockedApis.size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return error for unknown tenant")
  public void getBlockedApis_unknownTenant_returnsError() {
    // Arrange
    String contactId = randomNumeric(10);

    // Act
    Response response = getBlockedApis(randomAlphanumeric(8), contactId);

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
  public void getBlockedApis_longContactId_success() {
    // Arrange
    String longContactId = "a".repeat(1000);

    // Act
    Response response = getBlockedApis(TENANT_ID, longContactId);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(longContactId));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(0));

    List<String> blockedApis = response.getBody().jsonPath().getList("blockedApis");
    assertThat(blockedApis.size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return correct blocked APIs after partial unblock")
  public void getBlockedApis_afterPartialUnblock_success() {
    // Arrange - First block some APIs
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put("contact", contactId);
    blockRequestBody.put("blockApis", new String[] {API_ENDPOINT_1, API_ENDPOINT_2});
    blockRequestBody.put("reason", randomAlphanumeric(10));
    blockRequestBody.put("operator", randomAlphanumeric(10));
    blockRequestBody.put("unblockedAt", unblockedAt);

    Response blockResponse = blockContactApis(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Verify both APIs are blocked
    Response initialResponse = getBlockedApis(TENANT_ID, contactId);
    initialResponse.then().statusCode(HttpStatus.SC_OK);
    assertThat(initialResponse.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(initialResponse.getBody().jsonPath().getInt("totalCount"), equalTo(2));

    // Unblock one API
    Map<String, Object> unblockRequestBody = new HashMap<>();
    unblockRequestBody.put("contact", contactId);
    unblockRequestBody.put("unblockApis", new String[] {API_ENDPOINT_1});
    unblockRequestBody.put("operator", randomAlphanumeric(10));

    Response unblockResponse = unblockContactApis(TENANT_ID, unblockRequestBody);
    unblockResponse.then().statusCode(HttpStatus.SC_OK);

    // Act - Get blocked APIs again
    Response response = getBlockedApis(TENANT_ID, contactId);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(response.getBody().jsonPath().getInt("totalCount"), equalTo(1));

    List<String> blockedApis = response.getBody().jsonPath().getList("blockedApis");
    assertThat(blockedApis.size(), equalTo(1));
    assertThat(blockedApis.contains(API_ENDPOINT_2), equalTo(true));
    assertThat(blockedApis.contains(API_ENDPOINT_1), equalTo(false));
  }
}
