package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.blockContactFlows;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.unblockContactFlows;
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

public class ContactUnblockFlowsIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String Flow_1 = "passwordless";
  private static final String Flow_2 = "social_auth";

  /** Common function to generate request body for unblock Flow */
  private Map<String, Object> generateUnblockRequestBody(
      String contact, String[] unblockFlows, String operator) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contact);
    requestBody.put("unblockFlows", unblockFlows);
    requestBody.put("operator", operator);

    return requestBody;
  }

  @Test
  @DisplayName("Should unblock Flows successfully")
  public void unblockFlows_success() {
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

    // Arrange - Prepare unblock request
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contactId, new String[] {Flow_1}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response.getBody().jsonPath().getString("message"),
        equalTo("Flows unblocked successfully"));

    List<String> unblockedFlows = response.getBody().jsonPath().getList("unblockedFlows");
    assertThat(unblockedFlows.size(), equalTo(1));
    assertThat(unblockedFlows.contains(Flow_1), equalTo(true));
  }

  @Test
  @DisplayName("Should unblock Flows successfully with email contact")
  public void unblockFlows_emailContact_success() {
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

    // Arrange - Prepare unblock request
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(EMAIL_CONTACT, new String[] {Flow_1}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(EMAIL_CONTACT));
    assertThat(
        response.getBody().jsonPath().getString("message"),
        equalTo("Flows unblocked successfully"));

    List<String> unblockedFlows = response.getBody().jsonPath().getList("unblockedFlows");
    assertThat(unblockedFlows.size(), equalTo(1));
    assertThat(unblockedFlows.contains(Flow_1), equalTo(true));
  }

  @Test
  @DisplayName("Should unblock all Flows successfully")
  public void unblockFlows_allFlows_success() {
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

    // Arrange - Prepare unblock request for all Flows
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(
            contactId, new String[] {Flow_1, Flow_2}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response.getBody().jsonPath().getString("message"),
        equalTo("Flows unblocked successfully"));

    List<String> unblockedFlows = response.getBody().jsonPath().getList("unblockedFlows");
    assertThat(unblockedFlows.size(), equalTo(2));
    assertThat(unblockedFlows.contains(Flow_1), equalTo(true));
    assertThat(unblockedFlows.contains(Flow_2), equalTo(true));
  }

  @Test
  @DisplayName("Should handle unblocking non-blocked Flows gracefully")
  public void unblockFlows_nonBlockedFlows_success() {
    // Arrange - Prepare unblock request for Flows that were never blocked
    String contactId = randomNumeric(10);
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contactId, new String[] {Flow_1}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response.getBody().jsonPath().getString("message"),
        equalTo("Flows unblocked successfully"));

    List<String> unblockedFlows = response.getBody().jsonPath().getList("unblockedFlows");
    assertThat(unblockedFlows.size(), equalTo(1));
    assertThat(unblockedFlows.contains(Flow_1), equalTo(true));
  }

  @Test
  @DisplayName("Should return error for missing contact")
  public void unblockFlows_missingContact() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("unblockFlows", new String[] {Flow_1});
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(TENANT_ID, requestBody);

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
  public void unblockFlows_emptyContact() {
    // Arrange
    Map<String, Object> requestBody =
        generateUnblockRequestBody("", new String[] {Flow_1}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Contact is required"));
  }

  @Test
  @DisplayName("Should return error for missing unblockFlows")
  public void unblockFlows_missingunblockFlows() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for empty unblockFlows array")
  public void unblockFlows_emptyunblockFlows() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody =
        generateUnblockRequestBody(contactId, new String[] {}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for missing operator")
  public void unblockFlows_missingOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("unblockFlows", new String[] {Flow_1});

    // Act
    Response response = unblockContactFlows(TENANT_ID, requestBody);

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
  public void unblockFlows_emptyOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody =
        generateUnblockRequestBody(contactId, new String[] {Flow_1}, "");

    // Act
    Response response = unblockContactFlows(TENANT_ID, requestBody);

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
  public void unblockFlows_unknownTenant() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody =
        generateUnblockRequestBody(contactId, new String[] {Flow_1}, randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(randomAlphanumeric(8), requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("No config found"));
  }

  @Test
  @DisplayName("Should return error for null unblockFlows")
  public void unblockFlows_nullunblockFlows() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("unblockFlows", null);
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for null operator")
  public void unblockFlows_nullOperator() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("unblockFlows", new String[] {Flow_1});
    requestBody.put("operator", null);

    // Act
    Response response = unblockContactFlows(TENANT_ID, requestBody);

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
  public void unblockFlows_nullContact() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", null);
    requestBody.put("unblockFlows", new String[] {Flow_1});
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response = unblockContactFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("Contact is required"));
  }

  @Test
  @DisplayName("Should handle unblocking already unblocked Flows gracefully")
  public void unblockFlows_unblocking_Already_UnblockedFlow() {
    // Arrange
    String contactId = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("contact", contactId);
    requestBody.put("unblockFlows", new String[] {Flow_1});
    requestBody.put("operator", randomAlphanumeric(10));

    // Act
    Response response1 = unblockContactFlows(TENANT_ID, requestBody);
    response1.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response2 = unblockContactFlows(TENANT_ID, requestBody);

    // Assert
    response2.then().statusCode(HttpStatus.SC_OK);
    assertThat(response2.getBody().jsonPath().getString("contact"), equalTo(contactId));
    assertThat(
        response2.getBody().jsonPath().getString("message"),
        equalTo("Flows unblocked successfully"));
  }
}
