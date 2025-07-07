package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.userInfo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class UserInfoIT {
  public static String tenant1 = "tenant1";

  @Test
  @DisplayName("Should return user info for valid access token")
  public void testValidAccessToken() {
    // Arrange
    String userId = "1234";
    String accessToken = DbUtils.generateAccessToken(tenant1, userId);

    // Act
    Response response = userInfo(tenant1, accessToken);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK);
    JsonObject userInfo = new JsonObject(response.getBody().asString());
    assertThat(userInfo.getString("sub"), equalTo(userId));
    assertThat(userInfo.getString("email"), is(notNullValue()));
    assertThat(userInfo.getString("name"), is(notNullValue()));
  }

  @Test
  @DisplayName("Should return 401 for invalid access token")
  public void testInvalidAccessToken() {
    // Act
    Response response = userInfo(tenant1, "invalid_token");

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 401 for expired access token")
  public void testExpiredAccessToken() {
    // Arrange
    String userId = "1234";
    String accessToken = DbUtils.generateExpiredAccessToken(tenant1, userId);

    // Act
    Response response = userInfo(tenant1, accessToken);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }
}
