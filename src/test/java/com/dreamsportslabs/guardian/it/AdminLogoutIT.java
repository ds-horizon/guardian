package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_UNAUTHORIZED;
import static com.dreamsportslabs.guardian.Constants.HEADER_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminLogoutIT {

  private static final String TENANT_ID = "tenant1";
  private static final String ADMIN_USERNAME = "admin";
  private static final String ADMIN_PASSWORD = "admin123";
  private static final String VALID_USER_ID = "test-user-123";
  private static final String INVALID_USER_ID = randomAlphanumeric(10);
  private static final String SECOND_TENANT_ADMIN_USERNAME = "admin2";
  private static final String SECOND_TENANT_ADMIN_PASSWORD = "admin456";
  private static final String validCredentials = ADMIN_USERNAME + ":" + ADMIN_PASSWORD;
  private static final String validAuthHeader =
      "Basic " + Base64.getEncoder().encodeToString(validCredentials.getBytes());
  private static final String invalidCredentials = ADMIN_USERNAME + ":wrongPassword";
  private static final String invalidAuthHeader =
      "Basic " + Base64.getEncoder().encodeToString(invalidCredentials.getBytes());
  private static final String otherTenantCredentials =
      SECOND_TENANT_ADMIN_USERNAME + ":" + SECOND_TENANT_ADMIN_PASSWORD;
  private static final String otherTenantAuthHeader =
      "Basic " + Base64.getEncoder().encodeToString(otherTenantCredentials.getBytes());

  @Test
  @DisplayName("Should successfully logout user and invalidate all tokens")
  void testSuccessfulAdminLogout() {
    // Arrange
    String refreshToken1 =
        DbUtils.insertRefreshToken(
            TENANT_ID,
            VALID_USER_ID,
            1800L,
            "test-source",
            "test-device",
            "test-location",
            "127.0.0.1");

    String refreshToken2 =
        DbUtils.insertRefreshToken(
            TENANT_ID,
            VALID_USER_ID,
            1800L,
            "test-source",
            "test-device",
            "test-location",
            "127.0.0.1");

    // Act 2
    Response logoutResponse =
        ApplicationIoUtils.adminLogout(TENANT_ID, validAuthHeader, VALID_USER_ID);
    logoutResponse.then().statusCode(204);

    // Act 3
    Response invalidRefreshTokenResponse1 =
        ApplicationIoUtils.refreshToken(TENANT_ID, refreshToken1);
    invalidRefreshTokenResponse1
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));

    Response invalidRefreshTokenResponse2 =
        ApplicationIoUtils.refreshToken(TENANT_ID, refreshToken2);
    invalidRefreshTokenResponse2
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));

    // verify
    assertThat(
        "Refresh token 1 should be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken1, TENANT_ID),
        equalTo(true));
    assertThat(
        "Refresh token 2 should be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken2, TENANT_ID),
        equalTo(true));
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
        .header(HEADER_TENANT_ID, TENANT_ID)
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
        .statusCode(204);
  }

  @Test
  @DisplayName("Should return 401 when using admin credentials from different tenant")
  void testAdminLogoutWithWrongTenantCredentials() {
    // Act & Assert
    ApplicationIoUtils.adminLogout(TENANT_ID, otherTenantAuthHeader, VALID_USER_ID)
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED));
  }
}
