package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.*;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class UserInfoIT {

  private static final String VALID_USER_ID = "1";
  private static final String VALID_EMAIL = "john.doe@test.com";
  private static final String VALID_PHONE = randomNumeric(10);
  private static final String VALID_ACCESS_TOKEN =
      "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3Qta2lkIiwidHlwIjoiYXQrand0In0.eyJzdWIiOiJ1c2VyMSIsImV4cCI6NDc2NjY3ODQwMCwiaWF0IjoxNzUzMjU4OTg5LCJpc3MiOiJodHRwczovL3Rlc3QuY29tIiwiYXVkIjoiaHR0cHM6Ly9hcGkuZXhhbXBsZS5jb20iLCJjbGllbnRfaWQiOiJhYmMxMjMtY2xpZW50IiwianRpIjoiNmZhNWVhYjAtMmEwMS00YTYxLWJmMGItYjVkOGFhZGM3YzQ0Iiwic2NvcGUiOiJlbWFpbCBwaG9uZSBwcm9maWxlIn0.C7Kqtq4qFUqJMD675kgJj71cl_Hb1zQzPOHAQEJv1s8cuVPfWLc-mgoBIyILigyo18ZZYtzapF39g5Q33rve6kbIncT5O-VIHOL0JPUQutTgDKNgrxW9XrgL3IDyajmKcldsUhjMeW2HE4Xb2SDqJH0kE3ipl-VzVOqym0CYjQLZF7pj7_k1yyiqa4moB12qsYsdeMfe6CRH7lpaZ4bOr2Wsw52hXV_B8nc5xGfUuAg0-76wdUMAbgfRa5-CzfwAS_IrE5T__PXuqxpL40_ur5J2BOSSjgCyKuhcyC1uGRQVsiEEPcRjLaPJxBTVyZpv6ay9ilGejkczzenrRkDuSA";
  private static final String ACCESS_TOKEN_WITHOUT_KID_HEADER =
      "eyJhbGciOiJSUzI1NiIsInR5cCI6ImF0K2p3dCJ9.eyJzdWIiOiJ1c2VyMSIsImV4cCI6NDc2NjY3ODQwMCwiaWF0IjoxNzUzMjU4OTg5LCJpc3MiOiJodHRwczovL3Rlc3QuY29tIiwiYXVkIjoiaHR0cHM6Ly9hcGkuZXhhbXBsZS5jb20iLCJjbGllbnRfaWQiOiJhYmMxMjMtY2xpZW50IiwianRpIjoiNmZhNWVhYjAtMmEwMS00YTYxLWJmMGItYjVkOGFhZGM3YzQ0Iiwic2NvcGUiOiJlbWFpbCBwaG9uZSBwcm9maWxlIn0.d0bupIpaINY7oiXasPSY6v30BBdiiivsJS1Bt273UOce4jcUrnufllS9ShTHlJ0A1Niahh7OmYSgRi-xxfjFHM1NwqSIdApckI5us007Uo3q-qPMEt50gfSQCcr4E4j-gbe3B6ZP93f2n14CfXUx7YpzcEmSGPuyhf4el5LWrD7hYgCup8kPyBYBrT28_2cMsjfPYNM0ctsBv8prLzypuJ1OLvE1rDNwsIrZFF4r2uP0fD2Ns73yKpqefLHPE_9HHyWUo8_qoAbLxYAHEF4sCPxeGbPKfYejxmwOQFAqDBJeoiHvvTbV6e0TPnhwHbR-NC0tPqdFDI6D2p44aU8DfQ";
  private static final String ACCESS_TOKEN_WITH_INVALID_TYP_HEADER =
      "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3Qta2lkIiwidHlwIjoiand0In0.eyJzdWIiOiJ1c2VyMSIsImV4cCI6NDc2NjY3ODQwMCwiaWF0IjoxNzUzMjU4OTg5LCJpc3MiOiJodHRwczovL3Rlc3QuY29tIiwiYXVkIjoiaHR0cHM6Ly9hcGkuZXhhbXBsZS5jb20iLCJjbGllbnRfaWQiOiJhYmMxMjMtY2xpZW50IiwianRpIjoiNmZhNWVhYjAtMmEwMS00YTYxLWJmMGItYjVkOGFhZGM3YzQ0Iiwic2NvcGUiOiJlbWFpbCBwaG9uZSBwcm9maWxlIn0.RBxo6NISptim7YjNedSu-xJ2L50zkZhuz3lnrR0hMJ-Fws3tZhv9vEhhHEyHanneVpXL_OnHlFUU4alHErPgwH6OAK17JhQD-EM_Rq75CKXnA6sk-8eIevqmW8c_zNvKJorWD61Xw69PC2KROmnaHgc5e_Z-48fFQfbcHztBisToy8WXHCjvxm3GZ_hTGxet_9ECGaUt9-1Zpx9LOF8LNGQb6hO5iYmde1HkOzhUZOg5dWaTdR8pSMl7G7-rJKKMngniyPzQUdMn7XtM8dJ_oCm_xx9ITP5QTNFUcL_qgwjt7dZ__HDKUUafLCYxcAutDAZaGtLaB3s4T5WVdQJ_TA";
  private static final String ACCESS_TOKEN_WITHOUT_TYP_HEADER =
      "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3Qta2lkIn0.eyJzdWIiOiJ1c2VyMSIsImV4cCI6NDc2NjY3ODQwMCwiaWF0IjoxNzUzMjU4OTg5LCJpc3MiOiJodHRwczovL3Rlc3QuY29tIiwiYXVkIjoiaHR0cHM6Ly9hcGkuZXhhbXBsZS5jb20iLCJjbGllbnRfaWQiOiJhYmMxMjMtY2xpZW50IiwianRpIjoiNmZhNWVhYjAtMmEwMS00YTYxLWJmMGItYjVkOGFhZGM3YzQ0Iiwic2NvcGUiOiJlbWFpbCBwaG9uZSBwcm9maWxlIn0.Z7oZsCjPMLgM_r8kSShZDEKDZ5C5BsKXtpkTWRefmZjPFFFzu9UlLspFDW-2zRN1bm1OGREKUUaIuBykjd7paVkiM2mXyGDmd0s8NjFEKM2mD_8Q5v379G9cGV5WciXXLuGik0bH1PWLACDDairyOPHRbOpDk8lY1EY6B2HP3aj82ArVoBMvyrGFlH5Ec_qh9s3RAZdnvQBGTrftJ8gpQdfWUlNdOeZGIKLpFmw_qShSuaFbak3axfL1Qo4alMuq6NmGZMqf0feD_B0sn26A8tXcX6y71STRr-_5cc7BdAadsvm0YytoK7uWwNIwZXlg_62yD4qCQhxy2_dOXIN_tw";
  private static final String EXPIRED_ACCESS_TOKEN =
      "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3Qta2lkIiwidHlwIjoiand0In0.eyJzdWIiOiJ1c2VyMSIsImV4cCI6MTc1MzE3ODY0NiwiaWF0IjoxNzUzMjU4OTg5LCJpc3MiOiJodHRwczovL3Rlc3QuY29tIiwiYXVkIjoiaHR0cHM6Ly9hcGkuZXhhbXBsZS5jb20iLCJjbGllbnRfaWQiOiJhYmMxMjMtY2xpZW50IiwianRpIjoiNmZhNWVhYjAtMmEwMS00YTYxLWJmMGItYjVkOGFhZGM3YzQ0Iiwic2NvcGUiOiJlbWFpbCBwaG9uZSBwcm9maWxlIn0.FTbPdY3WfmJUUa0tu74kgYD5SOqm81igHpcmvm_KGFTJlqd0ZhYgBEkmvhzq_0ZZ_ocKiDVGkiZXUs-a-LQnz12vq6gqQ-o4beYKJLcVp5Lnu3UpiO_gaIhkXPWaXqUnYNIXib_eQ2PDIrzFEO6xHxNtDWf_5c83w28YXtpk8_Ca55Qww-sOlYC8luwyFqa4gmlhcPld-N4llVH8qmwvAUt-lDVqQ6vDcLAsvpPjcJlqweteEw12kLE6i7opgF9brzYhV70rQGoAKneo_F_rQRYqM3VOMOvz47gSD1wSTebpJ2fhsPqLtSQidtSrJ2fqubs5ft9lK4z1srAJC707Uw";

  private WireMockServer wireMockServer;

  @Test
  @DisplayName("Should return user info with multiple scopes")
  public void testUserInfoWithMultipleScopes() {
    // Act
    Response response = getUserInfo(TENANT_1, VALID_ACCESS_TOKEN);

    // Assert
    response.then().statusCode(HttpStatus.SC_OK);

    // Validate
    assertThat(response.getBody().jsonPath().getString("sub"), equalTo(VALID_USER_ID));
    assertThat(response.getBody().jsonPath().getString("iss"), equalTo("https://test.com"));
    assertThat(response.getBody().jsonPath().getString("email"), equalTo(VALID_EMAIL));
    assertThat(response.getBody().jsonPath().getBoolean("email_verified"), equalTo(true));
    assertThat(response.getBody().jsonPath().getString("phone_number"), equalTo(VALID_PHONE));
    assertThat(response.getBody().jsonPath().getBoolean("phone_number_verified"), equalTo(true));
  }

  @Test
  @DisplayName("Should support both GET and POST methods")
  public void testGetAndPostMethods() {
    // Act
    Response getResponse = getUserInfo(TENANT_1, VALID_ACCESS_TOKEN);
    Response postResponse = postUserInfo(TENANT_1, VALID_ACCESS_TOKEN);

    // Assert
    getResponse.then().statusCode(HttpStatus.SC_OK);
    postResponse.then().statusCode(HttpStatus.SC_OK);

    // Both should return same data
    assertThat(getResponse.getBody().asString(), equalTo(postResponse.getBody().asString()));
    assertThat(getResponse.getBody().jsonPath().getString("sub"), equalTo(VALID_USER_ID));
  }

  @Test
  @DisplayName("Should return 401 for invalid access token")
  public void testInvalidAccessToken() {
    // Act
    Response response = getUserInfo(TENANT_1, "invalid.token.here");

    // Assert
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 401 for missing kid header")
  public void testMissingKidHeader() {
    // Act
    Response response = getUserInfo(TENANT_1, ACCESS_TOKEN_WITHOUT_KID_HEADER);
    // Assert
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 401 for missing typ header")
  public void testMissingTypHeader() {

    // Act
    Response response = getUserInfo(TENANT_1, ACCESS_TOKEN_WITHOUT_TYP_HEADER);

    // Assert
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 401 for invalid typ header")
  public void testInvalidTypHeader() {

    // Act
    Response response = getUserInfo(TENANT_1, ACCESS_TOKEN_WITH_INVALID_TYP_HEADER);

    // Assert
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 401 for expired access token")
  public void testExpiredAccessToken() {
    // Act
    Response response = getUserInfo(TENANT_1, EXPIRED_ACCESS_TOKEN);

    // Assert
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }
}
