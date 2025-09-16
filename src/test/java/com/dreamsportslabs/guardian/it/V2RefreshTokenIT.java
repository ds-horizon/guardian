package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERID;
import static com.dreamsportslabs.guardian.Constants.CLAIM_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.CLAIM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_ISS;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_RFT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_HEADER_KID;
import static com.dreamsportslabs.guardian.constant.Constants.ACCESS_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.v2RefreshToken;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.restassured.response.Response;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class V2RefreshTokenIT {
  public static String tenant1 = "tenant1";
  public static String tenant3 = "tenant3";
  public static String clientId = "test-client";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private WireMockServer wireMockServer;

  @Test()
  @DisplayName("Should generate access token for a valid OIDC refresh token")
  public void testValidOidcRefreshToken() {
    // Arrange
    String userId = "1234";
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            1800L,
            "[\"openid\", \"profile\"]",
            "device1",
            "1.2.3.4",
            "app",
            "location",
            "[\"PASSWORD\"]");

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("access_token", isA(String.class))
        .body("token_type", equalTo("Bearer"))
        .body("expires_in", isA(Integer.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get("src/test/resources/test-data/tenant1-public-key.pem");

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(jwt.getHeaderClaim(JWT_HEADER_KID), equalTo("test-kid"));
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(userId));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo("https://test.com"));
    assertThat(claims.get(JWT_CLAIM_TENANT_ID), equalTo(tenant1));
    assertThat(
        claims.get(JWT_CLAIM_RFT_ID), equalTo(DigestUtils.md5Hex(refreshToken).toUpperCase()));
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(accessToken));
  }

  @Test()
  @DisplayName("Should generate access token without client_id validation")
  public void testValidOidcRefreshTokenWithoutClientId() {
    // Arrange
    String userId = "1234";
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            1800L,
            "[\"openid\", \"profile\"]",
            "device1",
            "1.2.3.4",
            "app",
            "location",
            "[\"PASSWORD\"]");

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, null);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));
  }

  @Test()
  @DisplayName("Should return error for invalid refresh token")
  public void testInvalidRefreshToken() {
    // Arrange
    String refreshToken = "invalid-refresh-token";

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookies().containsKey(REFRESH_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(""));
    assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME), equalTo(""));
  }

  @Test()
  @DisplayName("Should return error for wrong client_id")
  public void testWrongClientId() {
    // Arrange
    String userId = "1234";
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            1800L,
            "[\"openid\", \"profile\"]",
            "device1",
            "1.2.3.4",
            "app",
            "location",
            "[\"PASSWORD\"]");

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, "wrong-client");

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test()
  @DisplayName("Should return error for expired refresh token")
  public void testExpiredRefreshToken() {
    // Arrange
    String userId = "1234";
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            -1800L,
            "[\"openid\", \"profile\"]",
            "device1",
            "1.2.3.4",
            "app",
            "location",
            "[\"PASSWORD\"]");

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test()
  @DisplayName("Should return error for refresh token from different tenant")
  public void testDifferentTenantRefreshToken() {
    // Arrange
    String userId = "1234";
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            "tenant2",
            clientId,
            userId,
            1800L,
            "[\"openid\", \"profile\"]",
            "device1",
            "1.2.3.4",
            "app",
            "location",
            "[\"PASSWORD\"]");

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test()
  @DisplayName("Should add additional claims in Access Token if setting is enabled")
  public void testAdditionalClaimsEnabled() {
    // Arrange
    String userId = "1234";
    StubMapping stub = getStubForUserInfoWithAdditionalClaims(userId);
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            clientId,
            userId,
            1800L,
            "[\"openid\", \"profile\"]",
            "device1",
            "1.2.3.4",
            "app",
            "location",
            "[\"PASSWORD\"]");

    // Act
    Response response = v2RefreshToken(tenant3, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get("src/test/resources/test-data/tenant3-public-key.pem");

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(claims.get("item1"), equalTo("a"));
    assertThat(claims.get("item2"), equalTo("b"));
    wireMockServer.removeStub(stub);
  }

  @Test()
  @DisplayName("Should handle missing additional claims field gracefully")
  public void testMissingAdditionalClaimsField() {
    // Arrange
    String userId = "1234";
    StubMapping stub = getStubForUserInfoWithoutAdditionalClaims(userId);
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            clientId,
            userId,
            1800L,
            "[\"openid\", \"profile\"]",
            "device1",
            "1.2.3.4",
            "app",
            "location",
            "[\"PASSWORD\"]");

    // Act
    Response response = v2RefreshToken(tenant3, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get("src/test/resources/test-data/tenant3-public-key.pem");

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(claims.containsKey("item1"), equalTo(false));
    assertThat(claims.containsKey("item2"), equalTo(false));
    assertThat(claims.get("sub"), equalTo(userId));
    assertThat(claims.get("iss"), equalTo("https://test.com"));
    assertThat(claims.get("tid"), equalTo(tenant3));

    wireMockServer.removeStub(stub);
  }

  @Test()
  @DisplayName("Should handle auth methods in token generation")
  public void testAuthMethodsInToken() {
    // Arrange
    String userId = "1234";
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            1800L,
            "[\"openid\", \"profile\"]",
            "device1",
            "1.2.3.4",
            "app",
            "location",
            "[\"PASSWORDLESS\", \"OTP\"]");

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get("src/test/resources/test-data/tenant1-public-key.pem");

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(userId));
    assertThat(
        claims.get(JWT_CLAIM_RFT_ID), equalTo(DigestUtils.md5Hex(refreshToken).toUpperCase()));
  }

  @Test()
  @DisplayName("Should return error and clear cookies for invalid refresh token")
  public void testInvalidRefreshTokenErrorHandling() {
    // Arrange
    String refreshToken = "completely-invalid-token-12345";

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate error response
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);

    // Validate cookies are cleared
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookies().containsKey(REFRESH_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(""));
    assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME), equalTo(""));
  }

  @Test()
  @DisplayName("Should handle malformed request gracefully")
  public void testMalformedRequest() {
    // Arrange - empty refresh token
    String refreshToken = "";

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test()
  @DisplayName("Should handle null client_id gracefully")
  public void testNullClientIdHandling() {
    // Arrange
    String userId = "1234";
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            1800L,
            "[\"openid\", \"profile\"]",
            "device1",
            "1.2.3.4",
            "app",
            "location",
            "[\"PASSWORD\"]");

    // Act - pass null client_id
    Response response = v2RefreshToken(tenant1, refreshToken, null);

    // Validate - should work without client_id validation
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));
  }

  @Test()
  @DisplayName("Should handle expired refresh token with proper error response")
  public void testExpiredRefreshTokenErrorHandling() {
    // Arrange - create expired token
    String userId = "1234";
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            -3600L,
            "[\"openid\", \"profile\"]",
            "device1",
            "1.2.3.4",
            "app",
            "location",
            "[\"PASSWORD\"]");

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate error handling
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);

    // Validate error cookies are set
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookies().containsKey(REFRESH_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(""));
    assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME), equalTo(""));
  }

  private StubMapping getStubForUserInfoWithAdditionalClaims(String userId) {
    JsonNode jsonNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_USERID, userId)
            .put(CLAIM_EMAIL, randomAlphanumeric(8) + "@example.com")
            .put(CLAIM_ADDRESS, "sampleAddress")
            .put("email-verified", true)
            .put("phoneNumber", randomNumeric(10))
            .put(CLAIM_PHONE_NUMBER_VERIFIED, true)
            .put("item1", "a")
            .put("item2", "b");

    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withJsonBody(jsonNode)));
  }

  private StubMapping getStubForUserInfoWithoutAdditionalClaims(String userId) {
    JsonNode jsonNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_USERID, userId)
            .put(CLAIM_EMAIL, randomAlphanumeric(8) + "@example.com")
            .put(CLAIM_ADDRESS, "sampleAddress")
            .put("email-verified", true)
            .put("phoneNumber", randomNumeric(10))
            .put(CLAIM_PHONE_NUMBER_VERIFIED, true);

    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withJsonBody(jsonNode)));
  }
}
