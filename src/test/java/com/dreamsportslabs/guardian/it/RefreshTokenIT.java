package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERID;
import static com.dreamsportslabs.guardian.Constants.CLAIM_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.CLAIM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_EXP;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_IAT;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_ISS;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_RFT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_HEADER_KID;
import static com.dreamsportslabs.guardian.constant.Constants.ACCESS_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.refreshToken;
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
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class RefreshTokenIT {
  public static String tenant1 = "tenant1"; // OTP is mocked for this tenant
  public static String tenant3 = "tenant3"; // Additional claims are enabled for this tenant

  private final ObjectMapper objectMapper = new ObjectMapper();
  private WireMockServer wireMockServer;

  @Test()
  @DisplayName("Should generate access token for a valid refresh token")
  public void testValidRefreshToken() {
    // Arrange
    String userId = "1234";
    String refreshToken =
        DbUtils.insertRefreshToken(
            tenant1, userId, 1800L, "source", "device1", "location", "1.2.3.4");

    // Act
    Response response = refreshToken(tenant1, refreshToken);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("accessToken", isA(String.class));
    String accessToken = response.getBody().jsonPath().getString("accessToken");
    Path path = Paths.get("src/test/resources/test-data/tenant1-public-key.pem");

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));

    Map<String, Object> claims = jwt.getAllClaims();
    assertThat(jwt.getHeaderClaim(JWT_HEADER_KID), equalTo("test-kid"));
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(userId));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo("https://test.com"));
    assertThat(claims.get(JWT_CLAIM_TENANT_ID), equalTo(tenant1));

    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(900L));

    response.then().body("expiresIn", equalTo((int) (exp - iat)));

    assertThat(
        claims.get(JWT_CLAIM_RFT_ID), equalTo(DigestUtils.md5Hex(refreshToken).toUpperCase()));
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    // Validate that the access token is set in the cookie
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(accessToken));
  }

  @Test()
  @DisplayName("Should return error in case of invalid refresh token")
  public void testInvalidRefreshToken() {
    // Arrange
    String refreshToken = "a-random-invalid-refresh-token";

    // Act
    Response response = refreshToken(tenant1, refreshToken);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookies().containsKey(REFRESH_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(""));
    assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME), equalTo(""));
  }

  @Test()
  @DisplayName("Should return error in case of invalid refresh token another tenant")
  public void testInvalidRefreshToken2() {
    // Arrange
    String userId = "1234";
    String refreshToken =
        DbUtils.insertRefreshToken(
            "tenant2", userId, -1800L, "source", "device1", "location", "1.2.3.4");

    // Act
    Response response = refreshToken(tenant1, refreshToken);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookies().containsKey(REFRESH_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(""));
    assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME), equalTo(""));
  }

  @Test()
  @DisplayName("Should return error in case of expired refresh token")
  public void testExpiredRefreshToken() {
    // Arrange
    String userId = "1234";
    String refreshToken =
        DbUtils.insertRefreshToken(
            tenant1, userId, -1800L, "source", "device1", "location", "1.2.3.4");

    // Act
    Response response = refreshToken(tenant1, refreshToken);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookies().containsKey(REFRESH_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(""));
    assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME), equalTo(""));
  }

  @Test()
  @DisplayName("Should add additional claims in Access Token if setting is enabled")
  public void testAdditionalClaimsEnabledRefreshToken() {
    // Arrange
    String userId = "1234";
    StubMapping stub = getStubForUserInfoWithAdditionalClaims(userId);
    String refreshToken =
        DbUtils.insertRefreshToken(
            tenant3, userId, 1800L, "source", "device1", "location", "1.2.3.4");

    // Act
    Response response = refreshToken(tenant3, refreshToken);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("accessToken", isA(String.class));
    String accessToken = response.getBody().jsonPath().getString("accessToken");
    Path path = Paths.get("src/test/resources/test-data/tenant3-public-key.pem");

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();
    assertThat(claims.get("item1"), equalTo("a"));
    assertThat(claims.get("item2"), equalTo("b"));
    wireMockServer.removeStub(stub);
  }

  @Test()
  @DisplayName("Should handle missing additional claims field gracefully")
  public void testMissingAdditionalClaimsFieldRefreshToken() {
    // Arrange
    String userId = "1234";
    StubMapping stub = getStubForUserInfoWithoutAdditionalClaims(userId);
    String refreshToken =
        DbUtils.insertRefreshToken(
            tenant3, userId, 1800L, "source", "device1", "location", "1.2.3.4");

    // Act
    Response response = refreshToken(tenant3, refreshToken);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("accessToken", isA(String.class));
    String accessToken = response.getBody().jsonPath().getString("accessToken");
    Path path = Paths.get("src/test/resources/test-data/tenant3-public-key.pem");

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Should not have additional claims, only standard claims
    assertThat(claims.containsKey("item1"), equalTo(false));
    assertThat(claims.containsKey("item2"), equalTo(false));

    // Standard claims should still be present
    assertThat(claims.get("sub"), equalTo(userId));
    assertThat(claims.get("iss"), equalTo("https://test.com"));
    assertThat(claims.get("tid"), equalTo(tenant3));

    wireMockServer.removeStub(stub);
  }

  @Test()
  @DisplayName("Should handle partially added additional claims gracefully")
  public void testPartialAdditionalClaimsRefreshToken() {
    // Arrange
    String userId = "1234";
    StubMapping stub = getStubForUserInfoWithEmptyAdditionalClaims(userId);
    String refreshToken =
        DbUtils.insertRefreshToken(
            tenant3, userId, 1800L, "source", "device1", "location", "1.2.3.4");

    // Act
    Response response = refreshToken(tenant3, refreshToken);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("accessToken", isA(String.class));
    String accessToken = response.getBody().jsonPath().getString("accessToken");
    Path path = Paths.get("src/test/resources/test-data/tenant3-public-key.pem");

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(claims.containsKey("item1"), equalTo(true));
    assertThat(claims.get("item1"), equalTo("a"));
    assertThat(claims.containsKey("item2"), equalTo(false));

    // Standard claims should still be present
    assertThat(claims.get("sub"), equalTo(userId));
    assertThat(claims.get("iss"), equalTo("https://test.com"));
    assertThat(claims.get("tid"), equalTo(tenant3));

    wireMockServer.removeStub(stub);
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
    // Note: no additionalClaims field at all

    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withJsonBody(jsonNode)));
  }

  private StubMapping getStubForUserInfoWithEmptyAdditionalClaims(String userId) {

    JsonNode jsonNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_USERID, userId)
            .put(CLAIM_EMAIL, randomAlphanumeric(8) + "@example.com")
            .put(CLAIM_ADDRESS, "sampleAddress")
            .put("email-verified", true)
            .put("phoneNumber", randomNumeric(10))
            .put(CLAIM_PHONE_NUMBER_VERIFIED, true)
            .put("item1", "a");

    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withJsonBody(jsonNode)));
  }

  @Test()
  @DisplayName("Should handle completely malformed refresh token gracefully")
  public void testMalformedRefreshTokenHandling() {
    // Arrange - malformed token that doesn't exist in DB
    String refreshToken = "malformed-token-xyz-123";

    // Act
    Response response = refreshToken(tenant1, refreshToken);

    // Validate error response and cookie clearing
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookies().containsKey(REFRESH_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(""));
    assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME), equalTo(""));
  }
}
