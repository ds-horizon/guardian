package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.blockContactApis;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.unblockContactApis;
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

public class ContactUnblockApiIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String API_ENDPOINT_1 = "/api/v1/test/1";
  private static final String API_ENDPOINT_2 = "/api/v2/test/2";

  /** Common function to generate request body for unblock API */
  private Map<String, Object> generateUnblockRequestBody(
      String contact, String[] unblockApis, String operator) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contact);
    requestBody.put("unblockApis", unblockApis);
    requestBody.put("operator", operator);

    return requestBody;
  }

  @Test
  @DisplayName("Should unblock APIs successfully")
  public void unblockApi_success() {
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

    // Arrange - Prepare unblock request
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(
            contactId, new String[] {API_ENDPOINT_1}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response.getBody().jsonPath().getString("message"), equalTo("APIs unblocked successfully"));

    List<String> unblockedApis = response.getBody().jsonPath().getList("unblockedApis");
    assertThat(unblockedApis.size(), equalTo(1));
    assertThat(unblockedApis.contains(API_ENDPOINT_1), equalTo(true));
  }

  @Test
  @DisplayName("Should unblock APIs successfully with email contact")
  public void unblockApi_emailContact_success() {
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

    // Arrange - Prepare unblock request
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(
            EMAIL_CONTACT, new String[] {API_ENDPOINT_1}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(EMAIL_CONTACT));
    assertThat(
        response.getBody().jsonPath().getString("message"), equalTo("APIs unblocked successfully"));

    List<String> unblockedApis = response.getBody().jsonPath().getList("unblockedApis");
    assertThat(unblockedApis.size(), equalTo(1));
    assertThat(unblockedApis.contains(API_ENDPOINT_1), equalTo(true));
  }

  @Test
  @DisplayName("Should unblock all APIs successfully")
  public void unblockApi_allApis_success() {
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

    // Arrange - Prepare unblock request for all APIs
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(
            contactId, new String[] {API_ENDPOINT_1, API_ENDPOINT_2}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response.getBody().jsonPath().getString("message"), equalTo("APIs unblocked successfully"));

    List<String> unblockedApis = response.getBody().jsonPath().getList("unblockedApis");
    assertThat(unblockedApis.size(), equalTo(2));
    assertThat(unblockedApis.contains(API_ENDPOINT_1), equalTo(true));
    assertThat(unblockedApis.contains(API_ENDPOINT_2), equalTo(true));
  }

  @Test
  @DisplayName("Should handle unblocking non-blocked APIs gracefully")
  public void unblockApi_nonBlockedApis_success() {
    // Arrange - Prepare unblock request for APIs that were never blocked
    String contactId = randomNumeric(10);
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(
            contactId, new String[] {API_ENDPOINT_1}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response.getBody().jsonPath().getString("message"), equalTo("APIs unblocked successfully"));

    List<String> unblockedApis = response.getBody().jsonPath().getList("unblockedApis");
    assertThat(unblockedApis.size(), equalTo(1));
    assertThat(unblockedApis.contains(API_ENDPOINT_1), equalTo(true));
  }

  @Test
  @DisplayName("Should return error for missing contact")
  public void unblockApi_missingContact() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("unblockApis", new String[] {API_ENDPOINT_1});
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(TENANT_ID, requestBody);

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
  public void unblockApi_emptyContact() {
    // Arrange
    Map<String, Object> requestBody =
        generateUnblockRequestBody("", new String[] {API_ENDPOINT_1}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Contact is required"));
  }

  @Test
  @DisplayName("Should return error for missing unblockApis")
  public void unblockApi_missingUnblockApis() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one API must be provided"));
  }

  @Test
  @DisplayName("Should return error for empty unblockApis array")
  public void unblockApi_emptyUnblockApis() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody =
        generateUnblockRequestBody(contactId, new String[] {}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one API must be provided"));
  }

  @Test
  @DisplayName("Should return error for missing operator")
  public void unblockApi_missingOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("unblockApis", new String[] {API_ENDPOINT_1});

    // Act
    Response response = unblockContactApis(TENANT_ID, requestBody);

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
  public void unblockApi_emptyOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody =
        generateUnblockRequestBody(contactId, new String[] {API_ENDPOINT_1}, "");

    // Act
    Response response = unblockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Operator is required"));
  }

  @Test
  @DisplayName("Should return error for unknown tenant")
  public void unblockApi_unknownTenant() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody =
        generateUnblockRequestBody(
            contactId, new String[] {API_ENDPOINT_1}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(randomAlphanumeric(8), requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("No config found"));
  }

  @Test
  @DisplayName("Should return error for null unblockApis")
  public void unblockApi_nullUnblockApis() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("unblockApis", null);
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one API must be provided"));
  }

  @Test
  @DisplayName("Should return error for null operator")
  public void unblockApi_nullOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("unblockApis", new String[] {API_ENDPOINT_1});
    requestBody.put("operator", null);

    // Act
    Response response = unblockContactApis(TENANT_ID, requestBody);

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
  public void unblockApi_nullContact() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", null);
    requestBody.put("unblockApis", new String[] {API_ENDPOINT_1});
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response = unblockContactApis(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Contact is required"));
  }

  @Test
  @DisplayName("Should handle unblocking already unblocked APIs gracefully")
  public void unblockApi_unblocking_Already_UnblockedAPi() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("unblockApis", new String[] {API_ENDPOINT_1});
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response1 = unblockContactApis(TENANT_ID, requestBody);
    response1.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response2 = unblockContactApis(TENANT_ID, requestBody);

    // Assert
    response2.then().statusCode(HttpStatus.SC_OK);
    assertThat(response2.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response2.getBody().jsonPath().getString("message"),
        equalTo("APIs unblocked successfully"));
  }
}
