package com.dreamsportslabs.guardian.it.UserBlockFlow;

import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.blockUserFlows;
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

public class UserUnblockFlowsIT {

  private static final String TENANT_ID = "tenant1";
  private static final String EMAIL_CONTACT =
      randomAlphanumeric(10) + "@" + randomAlphanumeric(5) + ".com";
  private static final String Flow_1 = "passwordless";
  private static final String Flow_2 = "social_auth";

  /** Common function to generate request body for unblock Flow */
  private Map<String, Object> generateUnblockRequestBody(String contact, String[] unblockFlows) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", contact);
    requestBody.put("unblockFlows", unblockFlows);

    return requestBody;
  }

  @Test
  @DisplayName("Should unblock Flows successfully")
  public void unblockFlows_success() {
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

    // Arrange
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {Flow_1});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));

    List<String> unblockedFlows = response.getBody().jsonPath().getList("unblockedFlows");
    assertThat(unblockedFlows.size(), equalTo(1));
    assertThat(unblockedFlows.contains(Flow_1), equalTo(true));
  }

  @Test
  @DisplayName("Should unblock Flows successfully with email userIdentifier")
  public void unblockFlows_emailContact_success() {
    // Arrange
    Long unblockedAt = Instant.now().plusSeconds(3600).toEpochMilli() / 1000;
    Map<String, Object> blockRequestBody = new HashMap<>();
    blockRequestBody.put("userIdentifier", EMAIL_CONTACT);
    blockRequestBody.put("blockFlows", new String[] {Flow_1});
    blockRequestBody.put("reason", randomAlphanumeric(10));
    blockRequestBody.put("unblockedAt", unblockedAt);

    Response blockResponse = blockUserFlows(TENANT_ID, blockRequestBody);
    blockResponse.then().statusCode(HttpStatus.SC_OK);

    // Arrange - Prepare unblock request
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(EMAIL_CONTACT, new String[] {Flow_1});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(EMAIL_CONTACT));

    List<String> unblockedFlows = response.getBody().jsonPath().getList("unblockedFlows");
    assertThat(unblockedFlows.size(), equalTo(1));
    assertThat(unblockedFlows.contains(Flow_1), equalTo(true));
  }

  @Test
  @DisplayName("Should unblock all Flows successfully")
  public void unblockFlows_allFlows_success() {
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

    // Arrange
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {Flow_1, Flow_2});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));

    List<String> unblockedFlows = response.getBody().jsonPath().getList("unblockedFlows");
    assertThat(unblockedFlows.size(), equalTo(2));
    assertThat(unblockedFlows.contains(Flow_1), equalTo(true));
    assertThat(unblockedFlows.contains(Flow_2), equalTo(true));
  }

  @Test
  @DisplayName("Should handle unblocking non-blocked Flows gracefully")
  public void unblockFlows_nonBlockedFlows_success() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> unblockRequestBody =
        generateUnblockRequestBody(contact, new String[] {Flow_1});

    // Act
    Response response = unblockUserFlows(TENANT_ID, unblockRequestBody);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    assertThat(response.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));

    List<String> unblockedFlows = response.getBody().jsonPath().getList("unblockedFlows");
    assertThat(unblockedFlows.size(), equalTo(1));
    assertThat(unblockedFlows.contains(Flow_1), equalTo(true));
  }

  @Test
  @DisplayName("Should return error for missing userIdentifier")
  public void unblockFlows_missingContact() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("unblockFlows", new String[] {Flow_1});

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should return error for empty userIdentifier")
  public void unblockFlows_emptyContact() {
    // Arrange
    Map<String, Object> requestBody = generateUnblockRequestBody("", new String[] {Flow_1});

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should return error for missing unblockFlows")
  public void unblockFlows_missingunblockFlows() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", contact);

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

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
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for unknown tenant")
  public void unblockFlows_unknownTenant() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody = generateUnblockRequestBody(contact, new String[] {Flow_1});

    // Act
    Response response = unblockUserFlows(randomAlphanumeric(8), requestBody);

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
    String contact = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", contact);
    requestBody.put("unblockFlows", null);

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("At least one flow must be provided"));
  }

  @Test
  @DisplayName("Should return error for null userIdentifier")
  public void unblockFlows_nullContact() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", null);
    requestBody.put("unblockFlows", new String[] {Flow_1});

    // Act
    Response response = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .rootPath("error")
        .body("code", equalTo("invalid_request"))
        .body("message", equalTo("userIdentifier is required"));
  }

  @Test
  @DisplayName("Should handle unblocking already unblocked Flows gracefully")
  public void unblockFlows_unblocking_Already_UnblockedFlow() {
    // Arrange
    String contact = randomNumeric(10);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("userIdentifier", contact);
    requestBody.put("unblockFlows", new String[] {Flow_1});

    // Act
    Response response1 = unblockUserFlows(TENANT_ID, requestBody);
    response1.then().statusCode(HttpStatus.SC_OK);

    // Act
    Response response2 = unblockUserFlows(TENANT_ID, requestBody);

    // Assert
    response2.then().statusCode(HttpStatus.SC_OK);
    assertThat(response2.getBody().jsonPath().getString("userIdentifier"), equalTo(contact));
  }
}
