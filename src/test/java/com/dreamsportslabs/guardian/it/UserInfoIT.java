package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.*;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.*;
import static com.dreamsportslabs.guardian.utils.ScopeUtils.getValidScopeRequestBody;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import com.dreamsportslabs.guardian.utils.DbUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class UserInfoIT {

  private static final String VALID_USER_ID = "1";
  private static final String VALID_EMAIL = "john.doe@test.com";
  private static final String VALID_PHONE = randomNumeric(10);
  private static final String VALID_ACCESS_TOKEN =
      "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3Qta2lkIiwidHlwIjoiYXQrand0In0.eyJzdWIiOiIxIiwiZXhwIjozMzMxMDE4MjQ1NiwiaWF0IjoxNzUzMjU4OTg5LCJpc3MiOiJodHRwczovL3Rlc3QuY29tIiwiYXVkIjoiaHR0cHM6Ly9hcGkuZXhhbXBsZS5jb20iLCJjbGllbnRfaWQiOiJhYmMxMjMtY2xpZW50IiwianRpIjoiNmZhNWVhYjAtMmEwMS00YTYxLWJmMGItYjVkOGFhZGM3YzQ0Iiwic2NvcGUiOiJlbWFpbCBwaG9uZSBvcGVuaWQifQ.WkIe8Sbhhe56AlJ-MyoLGDGNSX5LabekrbCTUbB0W2YjSfx0tsOlrHPqrqcGmifPc8AKjuDKwTUwoy4CVPROhEtVspu1zeuiWnkABHJTxMNzw8IqtQESuyAkO61XyUMowFncxB81_wYxBc9He32r2H31tUOA-LCo64IxIjRIFYVuEUhYe5ME3OSmC6jgLC7WZbsWCF6uZKvzDIhFG1ekT69r6NuzNDCnR5mY-2b8pJWUnoRzqZ64S-9st5A0_4xE7-A-zV6l1G_P4YsmjeAYxM3brAMC9TpbgExblo2Dn-FJ0bZLj8STxvWt7Pul6o1_Jc6ovsGkkp_E6lNzl6oA_A";
  private static final String ACCESS_TOKEN_WITHOUT_KID_HEADER =
      "eyJhbGciOiJSUzI1NiIsInR5cCI6ImF0K2p3dCJ9.eyJzdWIiOiIxIiwiZXhwIjozMzMxMDE4MjQ1NiwiaWF0IjoxNzUzMjU4OTg5LCJpc3MiOiJodHRwczovL3Rlc3QuY29tIiwiYXVkIjoiaHR0cHM6Ly9hcGkuZXhhbXBsZS5jb20iLCJjbGllbnRfaWQiOiJhYmMxMjMtY2xpZW50IiwianRpIjoiNmZhNWVhYjAtMmEwMS00YTYxLWJmMGItYjVkOGFhZGM3YzQ0Iiwic2NvcGUiOiJlbWFpbCBwaG9uZSBvcGVuaWQifQ.g3043oU1IZsCNCWwbT4HZoxPzBNlIvYLtcJDk99UVKvNByvwZxp_XZZL-7_OlO9jRAU2D05ONfxElncuWeTFM7IdGsNoHIgmaAfsBtic_PeasOluAJcMfrkSF3lMl2eS35pL34qyW3iz07VkTmbhTcngcnszmJr4bqvwO-epdfvQd05YQYaMlAGHDL9HD8d7hILz9ZapIRSJPScShKP5fY0QtS85CCvr1CGHBxkATzXE6JkU2xYC36mxgNMa6nSwSuL_gqqDXTOv9-o68mHTgBOj3w8YwTCk9M0_RiCHcdiXgVuUFkqP4ZFDC4-JbBeiRQCwzjPs6-s2Ka9M9q_Wfg";
  private static final String ACCESS_TOKEN_WITH_INVALID_TYP_HEADER =
      "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3Qta2lkIiwidHlwIjoiand0In0.eyJzdWIiOiIxIiwiZXhwIjozMzMxMDE4MjQ1NiwiaWF0IjoxNzUzMjU4OTg5LCJpc3MiOiJodHRwczovL3Rlc3QuY29tIiwiYXVkIjoiaHR0cHM6Ly9hcGkuZXhhbXBsZS5jb20iLCJjbGllbnRfaWQiOiJhYmMxMjMtY2xpZW50IiwianRpIjoiNmZhNWVhYjAtMmEwMS00YTYxLWJmMGItYjVkOGFhZGM3YzQ0Iiwic2NvcGUiOiJlbWFpbCBwaG9uZSBvcGVuaWQifQ.MPL0TYXffLfMFMP1S2K0nRygfM3U1vcP-_V5p8gUxRQnKR21uIYzCXqsxs-jRlAfXQKVg-1IC7ztsO4y5Lx55ZL5LGiIC7qNJsWd87IjYbHXNQ1vVLlfMa1ATbQwhE3v6tL0E-VPBrdVKQiRGRo-hDgYDBr6EGVHhoIx4pgs11EM6XxsWfZimvsx1lu_9YZCatab-7xJitFx-u1DFuT8OpL4FsOzzU165IJ4iQ0-Euz2aamO_DbWRaktejy-3-wdedAj7PrDdcC9AkOdURTtDGXHA6lc7t9-WRC9VSdYY43jJPs73XGvURiyffBGqioWmOHQcd_kqzIhmrBZ6WqI-g";
  private static final String ACCESS_TOKEN_WITHOUT_TYP_HEADER =
      "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3Qta2lkIn0.eyJzdWIiOiIxIiwiZXhwIjozMzMxMDE4MjQ1NiwiaWF0IjoxNzUzMjU4OTg5LCJpc3MiOiJodHRwczovL3Rlc3QuY29tIiwiYXVkIjoiaHR0cHM6Ly9hcGkuZXhhbXBsZS5jb20iLCJjbGllbnRfaWQiOiJhYmMxMjMtY2xpZW50IiwianRpIjoiNmZhNWVhYjAtMmEwMS00YTYxLWJmMGItYjVkOGFhZGM3YzQ0Iiwic2NvcGUiOiJlbWFpbCBwaG9uZSBvcGVuaWQifQ.G-kqG3QnpWkccjqSrsrzb_KLcpmCB1t15o1WOZ5zMzvEQrp7wvtAT0Pqsi6_GRt8glvlENfCnSLj6TzrzIEpbUKiaPuCUm44cCq77p_BNzmHt2GfvHBqCwlGJSJsiMviJO9_UaWEC6_itMRrWEviAV6vl3b4phoCTAD1rMS8DapUy_I97Yb347wathc-errQn8xY0gM1gYi1ZXeX9x6zGzrvkDmhcD2Abhc3YGGkiP3jtNuWGATztiqRWkisDkJr5XihpkF7l4033FbIpzVRSSyyJGozpfSlvfIDo-BV3hil2YpBZTlJdRJeLnyr8XYT1zvJFY6w_NdIhKrzDZJuBA";
  private static final String EXPIRED_ACCESS_TOKEN =
      "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3Qta2lkIiwidHlwIjoiYXQrand0In0.eyJzdWIiOiIxIiwiZXhwIjoxNzUzMjU4OTkwLCJpYXQiOjE3NTMyNTg5ODksImlzcyI6Imh0dHBzOi8vdGVzdC5jb20iLCJhdWQiOiJodHRwczovL2FwaS5leGFtcGxlLmNvbSIsImNsaWVudF9pZCI6ImFiYzEyMy1jbGllbnQiLCJqdGkiOiI2ZmE1ZWFiMC0yYTAxLTRhNjEtYmYwYi1iNWQ4YWFkYzdjNDQiLCJzY29wZSI6ImVtYWlsIHBob25lIG9wZW5pZCJ9.JN6OhV8jyEW121GyOzioNm3OiHV9u8krTfOjtEMLjudFL01NFuMuDOM8NrQ-DJyzZEdwBEqxq_0oUr49yeKjVt1qY32HBykkM0Ks6G99JDLZRAQfOsz1Btx1j3EcdxnEPdyTMTWMULZmWSJrrbcttj73I5WBV8xOmt9iHOQNQEsHjfpStkMO9_y8_kx6hYcF9lKWsOb72GyQu_AoopcNmf9-JvuRE3fP7mDt1QsZegnsRlpp2WgljTMXkUBE53ccFG9ps6Hh2R0hu9V_smYwIX0_xGj7z21JdrNheH1pd1sxP0nVLwnbh_6C8JMWMxlNilgZlk5BYPTx-tuToCeD9w";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private WireMockServer wireMockServer;

  private StubMapping getStubForUserInfoWithMultipleScopes() {

    JsonNode jsonNode =
        objectMapper
            .createObjectNode()
            .put(CLAIM_SUB, VALID_USER_ID)
            .put(CLAIM_EMAIL, VALID_EMAIL)
            .put(CLAIM_ADDRESS, "sampleAddress")
            .put(CLAIM_EMAIL_VERIFIED, true)
            .put(CLAIM_PHONE_NUMBER, VALID_PHONE)
            .put(CLAIM_PHONE_NUMBER_VERIFIED, true);

    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withJsonBody(jsonNode)));
  }

  private static void createMultipleScopes() {
    createScope(
        TENANT_1,
        getValidScopeRequestBody(
            SCOPE_EMAIL,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(CLAIM_EMAIL, CLAIM_EMAIL_VERIFIED),
            TEST_ICON_URL,
            true));
    createScope(
        TENANT_1,
        getValidScopeRequestBody(
            SCOPE_PHONE,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(CLAIM_PHONE_NUMBER, CLAIM_PHONE_NUMBER_VERIFIED),
            TEST_ICON_URL,
            true));
    createScope(
        TENANT_1,
        getValidScopeRequestBody(
            SCOPE_OPENID,
            TEST_DISPLAY_NAME,
            TEST_DESCRIPTION,
            List.of(CLAIM_SUB),
            TEST_ICON_URL,
            true));
  }

  @BeforeAll
  public static void setup() {
    DbUtils.cleanupScopes(TENANT_1);
    createMultipleScopes();
  }

  @Test
  @DisplayName("Should return user info with multiple scopes")
  public void testUserInfoWithMultipleScopes() {
    // Act
    StubMapping stub = getStubForUserInfoWithMultipleScopes();

    Response response = getUserInfo(TENANT_1, VALID_ACCESS_TOKEN);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    // Validate
    assertThat(response.getBody().jsonPath().getString(CLAIM_SUB), equalTo(VALID_USER_ID));
    assertThat(response.getBody().jsonPath().getString(CLAIM_EMAIL), equalTo(VALID_EMAIL));
    assertThat(response.getBody().jsonPath().getBoolean(CLAIM_EMAIL_VERIFIED), equalTo(true));
    assertThat(response.getBody().jsonPath().getString(CLAIM_PHONE_NUMBER), equalTo(VALID_PHONE));
    assertThat(
        response.getBody().jsonPath().getBoolean(CLAIM_PHONE_NUMBER_VERIFIED), equalTo(true));
    assertThat(response.getBody().jsonPath().getString(CLAIM_ADDRESS), equalTo(null));

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should support both GET and POST methods")
  public void testGetAndPostMethods() {
    // Act
    StubMapping stub = getStubForUserInfoWithMultipleScopes();

    Response getResponse = getUserInfo(TENANT_1, VALID_ACCESS_TOKEN);
    Response postResponse = postUserInfo(TENANT_1, VALID_ACCESS_TOKEN);

    // Assert
    getResponse.then().statusCode(HttpStatus.SC_OK);
    postResponse.then().statusCode(HttpStatus.SC_OK);

    // Both should return same data
    assertThat(getResponse.getBody().jsonPath().getString(CLAIM_SUB), equalTo(VALID_USER_ID));
    assertThat(getResponse.getBody().jsonPath().getString(CLAIM_EMAIL), equalTo(VALID_EMAIL));
    assertThat(getResponse.getBody().jsonPath().getBoolean(CLAIM_EMAIL_VERIFIED), equalTo(true));
    assertThat(
        getResponse.getBody().jsonPath().getString(CLAIM_PHONE_NUMBER), equalTo(VALID_PHONE));
    assertThat(
        getResponse.getBody().jsonPath().getBoolean(CLAIM_PHONE_NUMBER_VERIFIED), equalTo(true));
    assertThat(getResponse.getBody().jsonPath().getString(CLAIM_ADDRESS), equalTo(null));

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return 401 for invalid access token")
  public void testInvalidAccessToken() {
    // Act
    StubMapping stub = getStubForUserInfoWithMultipleScopes();

    Response response = getUserInfo(TENANT_1, "invalid.token.here");

    // Assert

    response
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED)
        .header("WWW-Authenticate", containsString("error=\"invalid_token\""))
        .header("WWW-Authenticate", containsString("error_description=\"Invalid token\""));
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return 401 for missing kid header")
  public void testMissingKidHeader() {
    // Act
    StubMapping stub = getStubForUserInfoWithMultipleScopes();

    Response response = getUserInfo(TENANT_1, ACCESS_TOKEN_WITHOUT_KID_HEADER);
    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED)
        .header("WWW-Authenticate", containsString("error=\"invalid_token\""))
        .header(
            "WWW-Authenticate",
            containsString("error_description=\"Invalid token: missing kid in headers\""));
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return 401 for missing typ header")
  public void testMissingTypHeader() {
    // Act
    StubMapping stub = getStubForUserInfoWithMultipleScopes();

    Response response = getUserInfo(TENANT_1, ACCESS_TOKEN_WITHOUT_TYP_HEADER);

    // Assert

    response
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED)
        .header("WWW-Authenticate", containsString("error=\"invalid_token\""))
        .header("WWW-Authenticate", containsString("error_description=\"Invalid token type\""));
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return 401 for invalid typ header")
  public void testInvalidTypHeader() {
    // Act
    StubMapping stub = getStubForUserInfoWithMultipleScopes();

    Response response = getUserInfo(TENANT_1, ACCESS_TOKEN_WITH_INVALID_TYP_HEADER);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED)
        .header("WWW-Authenticate", containsString("error=\"invalid_token\""))
        .header("WWW-Authenticate", containsString("error_description=\"Invalid token type\""));
    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("Should return 401 for expired access token")
  public void testExpiredAccessToken() {
    // Act
    StubMapping stub = getStubForUserInfoWithMultipleScopes();

    Response response = getUserInfo(TENANT_1, EXPIRED_ACCESS_TOKEN);

    // Assert
    response
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED)
        .header("WWW-Authenticate", containsString("error=\"invalid_token\""))
        .header("WWW-Authenticate", containsString("error_description=\"Token has expired\""));
    wireMockServer.removeStub(stub);
  }
}
