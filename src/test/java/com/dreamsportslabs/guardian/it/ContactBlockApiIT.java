package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.blockContactApis;
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

public class ContactBlockApiIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String API_ENDPOINT_1 = "/api/v1/test/1";
  private static final String API_ENDPOINT_2 = "/api/v2/test/2";

  /** Common function to generate request body for block API */
  private Map<String, Object> generateBlockRequestBody(
      String contact, String[] blockApis, String reason, String operator, Long unblockedAt) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contact);
    requestBody.put("blockApis", blockApis);
    requestBody.put("reason", reason);
    requestBody.put("operator", operator);
    requestBody.put("unblockedAt", unblockedAt);

    return requestBody;
  }

  @Test
  @DisplayName("Should block APIs successfully")
  public void blockApi_success() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {API_ENDPOINT_1, API_ENDPOINT_2},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response.getBody().jsonPath().getString("message"), equalTo("APIs blocked successfully"));

    List<String> blockedApis = response.getBody().jsonPath().getList("blockedApis");
    assertThat(blockedApis.size(), equalTo(2));
    assertThat(blockedApis.contains(API_ENDPOINT_1), equalTo(true));
    assertThat(blockedApis.contains(API_ENDPOINT_2), equalTo(true));
  }

  @Test
  @DisplayName("Should block APIs successfully with email contact")
  public void blockApi_emailContact_success() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            EMAIL_CONTACT,
            new String[] {API_ENDPOINT_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(EMAIL_CONTACT));
    assertThat(
        response.getBody().jsonPath().getString("message"), equalTo("APIs blocked successfully"));

    List<String> blockedApis = response.getBody().jsonPath().getList("blockedApis");
    assertThat(blockedApis.size(), equalTo(1));
    assertThat(blockedApis.contains(API_ENDPOINT_1), equalTo(true));
  }

  @Test
  @DisplayName("Should update existing block successfully")
  public void blockApi_updateExisting_success() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt1 = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody1 =
        generateBlockRequestBody(
            contactId,
            new String[] {API_ENDPOINT_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt1);

    Response response1 = blockContactApis(TENANT_ID, requestBody1);
    response1.then().statusCode(HttpStatus.SC_OK);

    List<String> blockedApis1 = response1.getBody().jsonPath().getList("blockedApis");
    assertThat(blockedApis1.size(), equalTo(1));
    assertThat(blockedApis1.contains(API_ENDPOINT_1), equalTo(true));

    Long unblockedAt2 = Instant.now().plusSeconds(7200).toEpochMilli();
    Map<String, Object> requestBody2 =
        generateBlockRequestBody(
            contactId,
            new String[] {API_ENDPOINT_1, API_ENDPOINT_2},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt2);

    // Act
    Response response2 = blockContactApis(TENANT_ID, requestBody2);

    // Assert
    response2.then().statusCode(HttpStatus.SC_OK);

    assertThat(response2.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response2.getBody().jsonPath().getString("message"), equalTo("APIs blocked successfully"));

    List<String> blockedApis2 = response2.getBody().jsonPath().getList("blockedApis");
    assertThat(blockedApis2.size(), equalTo(2));
    assertThat(blockedApis2.contains(API_ENDPOINT_1), equalTo(true));
    assertThat(blockedApis2.contains(API_ENDPOINT_2), equalTo(true));
  }

  @Test
  @DisplayName("Should return error for missing contact")
  public void blockApi_missingContact() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("blockApis", new String[] {API_ENDPOINT_1});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

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
  public void blockApi_emptyContact() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();

    Map<String, Object> requestBody =
        generateBlockRequestBody(
            "",
            new String[] {API_ENDPOINT_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Contact is required"));
  }

  @Test
  @DisplayName("Should return error for missing blockApis")
  public void blockApi_missingBlockApis() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one API must be provided"));
  }

  @Test
  @DisplayName("Should return error for empty blockApis array")
  public void blockApi_emptyBlockApis() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one API must be provided"));
  }

  @Test
  @DisplayName("Should return error for missing reason")
  public void blockApi_missingReason() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockApis", new String[] {API_ENDPOINT_1});
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

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
  public void blockApi_emptyReason() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId, new String[] {API_ENDPOINT_1}, "", randomAlphanumeric(10), unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

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
  public void blockApi_missingOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockApis", new String[] {API_ENDPOINT_1});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

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
  public void blockApi_emptyOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId, new String[] {API_ENDPOINT_1}, randomAlphanumeric(10), "", unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

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
  public void blockApi_pastUnblockedAt() {
    // Arrange
    String contactId = randomNumeric(10);
    Long pastTime = Instant.now().minusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {API_ENDPOINT_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            pastTime);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

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
  public void blockApi_currentUnblockedAt() {
    // Arrange
    String contactId = randomNumeric(10);
    Long currentTime = Instant.now().toEpochMilli();
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {API_ENDPOINT_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            currentTime);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

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
  public void blockApi_unknownTenant() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody =
        generateBlockRequestBody(
            contactId,
            new String[] {API_ENDPOINT_1},
            randomAlphanumeric(10),
            randomAlphanumeric(10),
            unblockedAt);

    // Act
    Response response = blockContactApis(randomAlphanumeric(8), requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("No config found"));
  }

  @Test
  @DisplayName("Should return error for null blockApis")
  public void blockApi_nullBlockApis() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockApis", null);
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one API must be provided"));
  }

  @Test
  @DisplayName("Should return error for null reason")
  public void blockApi_nullReason() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockApis", new String[] {API_ENDPOINT_1});
    requestBody.put("reason", null);
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

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
  public void blockApi_nullOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("blockApis", new String[] {API_ENDPOINT_1});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", null);
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

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
  public void blockApi_nullContact() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", null);
    requestBody.put("blockApis", new String[] {API_ENDPOINT_1});
    requestBody.put("reason", randomAlphanumeric(10));
    requestBody.put("operator", randomAlphanumeric(10));
    requestBody.put("unblockedAt", unblockedAt);

    // Act
    Response response = blockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Contact is required"));
  }
}
