package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_EXP;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_IAT;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_ISS;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_RFT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.JWT_HEADER_KID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.refreshToken;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
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

    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(900L));

    response.then().body("expiresIn", equalTo((int) (exp - iat)));

    assertThat(
        claims.get(JWT_CLAIM_RFT_ID), equalTo(DigestUtils.md5Hex(refreshToken).toUpperCase()));
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
  }
}
