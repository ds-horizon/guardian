package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_UNAUTHORIZED;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.constant.Constants.ACCESS_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.utils.DbUtils.addDefaultClientScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.addFirstPartyClient;
import static com.dreamsportslabs.guardian.utils.DbUtils.addScope;
import static com.dreamsportslabs.guardian.utils.DbUtils.addThirdPartyClient;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class V2LogoutIT {

  private static final String INVALID_tenant1 = "invalid-tenant";
  private static final String VALID_USER_ID = "test-user-123";
  public static String tenant1 = "tenant1";
  private static final String TEST_SCOPE_1 = "scope1";
  private static final String TEST_SCOPE_2 = "scope2";
  static String client1;
  static String client2;

  @BeforeAll
  static void setup() {

    addScope(tenant1, TEST_SCOPE_1);
    addScope(tenant1, TEST_SCOPE_2);

    client1 = addFirstPartyClient(tenant1);
    client2 = addThirdPartyClient(tenant1);

    addDefaultClientScopes(tenant1, client1, TEST_SCOPE_1);
    addDefaultClientScopes(tenant1, client2, TEST_SCOPE_2);
  }

  @Test
  @DisplayName("Should successfully logout with refresh token in request body - TOKEN type")
  void testSuccessfulLogoutWithBodyTokenType() {
    // Arrange
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client1,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("refresh_token", refreshToken);
    requestBody.put("logout_type", "TOKEN");

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, requestBody, null);

    // Assert
    response
        .then()
        .statusCode(SC_NO_CONTENT)
        .cookie(ACCESS_TOKEN_COOKIE_NAME, "")
        .cookie(REFRESH_TOKEN_COOKIE_NAME, "");

    assertThat(
        "Refresh token should be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken, tenant1),
        equalTo(true));
  }

  @Test
  @DisplayName("Should successfully logout with refresh token from cookie")
  void testSuccessfulLogoutWithCookieToken() {
    // Arrange
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client1,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, new HashMap<>(), refreshToken);

    // Assert
    response
        .then()
        .statusCode(SC_NO_CONTENT)
        .cookie(ACCESS_TOKEN_COOKIE_NAME, "")
        .cookie(REFRESH_TOKEN_COOKIE_NAME, "");

    assertThat(
        "Refresh token should be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken, tenant1),
        equalTo(true));
  }

  @Test
  @DisplayName("Should successfully logout with CLIENT logout type")
  void testSuccessfulLogoutWithClientType() {
    // Arrange
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client1,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");

    String refreshToken2 =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client1,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");

    String refreshToken3 =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client2,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("refresh_token", refreshToken);
    requestBody.put("logout_type", "CLIENT");
    requestBody.put("client1", client1);

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, requestBody, null);

    // Assert
    response
        .then()
        .statusCode(SC_NO_CONTENT)
        .cookie(ACCESS_TOKEN_COOKIE_NAME, "")
        .cookie(REFRESH_TOKEN_COOKIE_NAME, "");

    assertThat(
        "Refresh token should be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken, tenant1),
        equalTo(true));

    assertThat(
        "Refresh token should be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken2, tenant1),
        equalTo(true));

    assertThat(
        "Refresh token should not be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken3, tenant1),
        equalTo(false));
  }

  @Test
  @DisplayName("Should successfully logout with TENANT logout type")
  void testSuccessfulLogoutWithTenantType() {
    // Arrange
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client1,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");

    String refreshToken2 =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client2,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("refresh_token", refreshToken);
    requestBody.put("logout_type", "TENANT");

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, requestBody, null);

    // Assert
    response
        .then()
        .statusCode(SC_NO_CONTENT)
        .cookie(ACCESS_TOKEN_COOKIE_NAME, "")
        .cookie(REFRESH_TOKEN_COOKIE_NAME, "");

    assertThat(
        "Refresh token should be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken, tenant1),
        equalTo(true));

    assertThat(
        "Refresh token should be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken2, tenant1),
        equalTo(true));
  }

  @Test
  @DisplayName("Should return 401 when TENANT logout type with third party client token")
  void testTenantLogoutShouldFailWhenClientIsNotFirstParty() {
    // Arrange
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client1,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");

    String refreshToken2 =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client2,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("refresh_token", refreshToken2);
    requestBody.put("logout_type", "TENANT");

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, requestBody, null);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_UNAUTHORIZED));

    assertThat(
        "Refresh token should not be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken, tenant1),
        equalTo(false));

    assertThat(
        "Refresh token should not be revoked",
        DbUtils.isRefreshTokenRevoked(refreshToken2, tenant1),
        equalTo(false));
  }

  @Test
  @DisplayName("Should prioritize body token over cookie token")
  void testBodyTokenPriorityOverCookie() {
    // Arrange
    String bodyToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client1,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");
    String cookieToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client1,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("refresh_token", bodyToken);

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, requestBody, cookieToken);

    // Assert
    response.then().statusCode(SC_NO_CONTENT);
    assertThat(
        "Body token should be revoked",
        DbUtils.isRefreshTokenRevoked(bodyToken, tenant1),
        equalTo(true));
    assertThat(
        "Cookie token should not be revoked",
        DbUtils.isRefreshTokenRevoked(cookieToken, tenant1),
        equalTo(false));
  }

  @Test
  @DisplayName("Should return 401 when refresh token is invalid")
  void testLogoutWithInvalidTokenFormat() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("refresh_token", "invalid-token-format");

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, requestBody, null);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_UNAUTHORIZED));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing")
  void testLogoutWithMissingTenantId() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("refresh_token", "some-token");

    // Act & Assert
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/v2/logout")
        .then()
        .statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 400 when tenant-id is invalid")
  void testLogoutWithInvalidTenantId() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("refresh_token", "some-token");

    // Act
    Response response = ApplicationIoUtils.v2Logout(INVALID_tenant1, requestBody, null);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("No config found"));
  }

  @Test
  @DisplayName("Should return 401 when refresh token is missing from both body and cookie")
  void testLogoutWithMissingRefreshToken() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, requestBody, null);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_UNAUTHORIZED));
  }

  @Test
  @DisplayName("Should return 400 when logout_type is invalid")
  void testLogoutWithInvalidLogoutType() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("refresh_token", "some-token");
    requestBody.put("logout_type", "INVALID_TYPE");

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, requestBody, null);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should handle empty request body with no cookie")
  void testLogoutWithEmptyBodyAndNoCookie() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, requestBody, null);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body("code", equalTo(ERROR_UNAUTHORIZED));
  }

  @Test
  @DisplayName("Should use cookie token when body token is missing")
  void testCookieTokenWhenBodyTokenMissing() {
    // Arrange
    String cookieToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            client1,
            VALID_USER_ID,
            1800L,
            "[\"openid\", \"profile\"]",
            "test-device",
            "127.0.0.1",
            "test-source",
            "test-location",
            "[\"PASSWORD\"]");
    Map<String, Object> requestBody = new HashMap<>();

    // Act
    Response response = ApplicationIoUtils.v2Logout(tenant1, requestBody, cookieToken);

    // Assert
    response.then().statusCode(SC_NO_CONTENT);
    assertThat(
        "Cookie token should be revoked",
        DbUtils.isRefreshTokenRevoked(cookieToken, tenant1),
        equalTo(true));
  }
}
