package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.*;
import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;

import com.dreamsportslabs.guardian.Setup;
import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Setup.class)
class AdminLogoutIT {

  private static final String TENANT_ID = "tenant1";
  private static final String ADMIN_USERNAME = "admin";
  private static final String ADMIN_PASSWORD = "admin123";
  private static final String VALID_USER_ID = "test-user-123";
  private static final String INVALID_USER_ID = randomAlphanumeric(10);
  private static final String SECOND_TENANT_ADMIN_USERNAME = "admin2";
  private static final String SECOND_TENANT_ADMIN_PASSWORD = "admin456";

  private String validAuthHeader;
  private String invalidAuthHeader;
  private String otherTenantAuthHeader;

  @BeforeEach
  void setUp() {
    // Create Basic Auth headers
    String validCredentials = ADMIN_USERNAME + ":" + ADMIN_PASSWORD;
    validAuthHeader = "Basic " + Base64.getEncoder().encodeToString(validCredentials.getBytes());

    String invalidCredentials = ADMIN_USERNAME + ":wrongpassword";
    invalidAuthHeader =
        "Basic " + Base64.getEncoder().encodeToString(invalidCredentials.getBytes());

    String otherTenantCredentials =
        SECOND_TENANT_ADMIN_USERNAME + ":" + SECOND_TENANT_ADMIN_PASSWORD;
    otherTenantAuthHeader =
        "Basic " + Base64.getEncoder().encodeToString(otherTenantCredentials.getBytes());
  }

  @Test
  @DisplayName("Should successfully logout user and invalidate all tokens")
  void testSuccessfulAdminLogout() {
    // Arrange
    String refreshToken =
        DbUtils.insertRefreshToken(
            TENANT_ID,
            VALID_USER_ID,
            1800L, // 30 minutes expiry
            "test-source",
            "test-device",
            "test-location",
            "127.0.0.1");

    // Act 1
    Response refreshResponse = ApplicationIoUtils.refreshToken(TENANT_ID, refreshToken);
    refreshResponse
        .then()
        .statusCode(200)
        .body("accessToken", org.hamcrest.Matchers.notNullValue());

    // Act 2
    Response logoutResponse =
        ApplicationIoUtils.adminLogout(TENANT_ID, validAuthHeader, VALID_USER_ID);
    logoutResponse.then().statusCode(204).body(isEmptyString());

    // Act 3
    Response invalidRefreshResponse = ApplicationIoUtils.refreshToken(TENANT_ID, refreshToken);
    invalidRefreshResponse
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));
  }

  @Test
  @DisplayName("Should return 401 when admin credentials are invalid")
  void testAdminLogoutWithInvalidCredentials() {
    // Act & Assert
    ApplicationIoUtils.adminLogout(TENANT_ID, invalidAuthHeader, VALID_USER_ID)
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));
  }

  @Test
  @DisplayName("Should return 401 when admin credentials are missing")
  void testAdminLogoutWithoutCredentials() {
    // Act & Assert
    ApplicationIoUtils.adminLogout(TENANT_ID, null, VALID_USER_ID)
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));
  }

  @Test
  @DisplayName("Should return 400 when tenant-id is invalid")
  void testAdminLogoutWithInvalidTenantId() {
    // Act & Assert
    ApplicationIoUtils.adminLogout("invalid-tenant", validAuthHeader, VALID_USER_ID)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("No config found"));
  }

  @Test
  @DisplayName("Should return 400 when request body is empty")
  void testAdminLogoutWithEmptyBody() {
    // Act & Assert
    given()
        .contentType(ContentType.JSON)
        .header("tenant-id", TENANT_ID)
        .header("Authorization", validAuthHeader)
        .body("{}")
        .when()
        .post("/v1/admin/logout")
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("userId is required"));
  }

  @Test
  @DisplayName("Should return 400 when userId is missing")
  void testAdminLogoutWithMissingUserId() {
    // Arrange
    String userId = null;

    // Act & Assert
    ApplicationIoUtils.adminLogout(TENANT_ID, validAuthHeader, userId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("userId is required"));
  }

  @Test
  @DisplayName("Should successfully logout user even if user doesn't exist")
  void testAdminLogoutForNonExistentUser() {
    // Act & Assert
    ApplicationIoUtils.adminLogout(TENANT_ID, validAuthHeader, INVALID_USER_ID)
        .then()
        .statusCode(204)
        .body(isEmptyString());
  }

  @Test
  @DisplayName("Should return 401 when using admin credentials from different tenant")
  void testAdminLogoutWithWrongTenantCredentials() {
    // Act & Assert - Using tenant2 credentials to access tenant1 should fail
    ApplicationIoUtils.adminLogout(TENANT_ID, otherTenantAuthHeader, VALID_USER_ID)
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));
  }

  @Test
  @DisplayName("Should successfully logout multiple users and invalidate their tokens")
  void testAdminLogoutMultipleUsers() {
    // Arrange - Create multiple users with refresh tokens
    String user1Id = "user1";
    String user2Id = "user2";

    String refreshToken1 =
        DbUtils.insertRefreshToken(
            TENANT_ID, user1Id, 1800L, "test-source", "test-device", "test-location", "127.0.0.1");

    String refreshToken2 =
        DbUtils.insertRefreshToken(
            TENANT_ID, user2Id, 1800L, "test-source", "test-device", "test-location", "127.0.0.1");

    // Act 1 - Verify both refresh tokens work before logout
    ApplicationIoUtils.refreshToken(TENANT_ID, refreshToken1).then().statusCode(200);

    ApplicationIoUtils.refreshToken(TENANT_ID, refreshToken2).then().statusCode(200);

    // Act 2 - Logout user1
    ApplicationIoUtils.adminLogout(TENANT_ID, validAuthHeader, user1Id).then().statusCode(204);

    // Act 3 - Verify user1's token is invalid but user2's token still works
    ApplicationIoUtils.refreshToken(TENANT_ID, refreshToken1)
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));

    ApplicationIoUtils.refreshToken(TENANT_ID, refreshToken2).then().statusCode(200);

    // Act 4 - Logout user2
    ApplicationIoUtils.adminLogout(TENANT_ID, validAuthHeader, user2Id).then().statusCode(204);

    // Act 5 - Verify user2's token is now also invalid
    ApplicationIoUtils.refreshToken(TENANT_ID, refreshToken2)
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));
  }
}
