package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.AUTH_RESPONSE_TYPE_CODE;
import static com.dreamsportslabs.guardian.Constants.AUTH_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_CLIENT;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIMS_AMR;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_EXP;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_IAT;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_CREDENTIAL_TYPE_PIN;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_EMAIL_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_EMAIL_2;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_INVALID_SCOPE_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_PASSWORD_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_PHONE_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_PHONE_2;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_PIN_2;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_USERNAME_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_USERNAME_2;
import static com.dreamsportslabs.guardian.utils.DbUtils.addDefaultClientScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.addFirstPartyClient;
import static com.dreamsportslabs.guardian.utils.DbUtils.addScope;
import static com.dreamsportslabs.guardian.utils.DbUtils.addThirdPartyClient;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.fusionauth.jwt.JWTDecoder;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class V2SignUpIT {

  private static final String TENANT_ID = "tenant1";
  private static final String TEST_SCOPE = "profile";
  private static final String V2_SIGNUP_ERROR_USER_EXISTS = "User already exists";
  private static String firstPartyClientId;
  private static String thirdPartyClientId;
  private WireMockServer wireMockServer;
  private final JWTDecoder decoder = JWT.getDecoder();

  @BeforeAll
  static void setup() {
    addScope(TENANT_ID, TEST_SCOPE);
    firstPartyClientId = addFirstPartyClient(TENANT_ID);
    thirdPartyClientId = addThirdPartyClient(TENANT_ID);
    addDefaultClientScopes(TENANT_ID, firstPartyClientId, TEST_SCOPE);
  }

  // Token Response Type Tests
  @Test
  @DisplayName("V2SignUp - Return valid token when username password combination is provided")
  public void returnValidTokenWhenUsernamePasswordCombinationIsProvided() {
    // Arrange
    String username = V2_SIGNIN_TEST_USERNAME_1;
    String password = V2_SIGNIN_TEST_PASSWORD_1;
    StubMapping stub =
        stubRegisterUserSuccess(
            username,
            V2_SIGNIN_TEST_EMAIL_1,
            V2_SIGNIN_TEST_PHONE_1,
            V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            username,
            null,
            null,
            password,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("id_token", notNullValue())
        .body("sso_token", notNullValue())
        .body("expires_in", notNullValue())
        .header("Set-Cookie", notNullValue());

    validateSetCookieTokens(response);
    String accessToken = response.jsonPath().getString("access_token");
    int expiresIn = response.jsonPath().getInt("expires_in");
    validateAccessTokenClaims(
        accessToken,
        TEST_SCOPE,
        TENANT_ID,
        firstPartyClientId,
        expiresIn,
        V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return valid token when username pin combination is provided")
  public void returnValidTokenWhenUsernamePinCombinationIsProvided() {
    // Arrange
    String username = V2_SIGNIN_TEST_USERNAME_2;
    String pin = V2_SIGNIN_TEST_PIN_2;
    StubMapping stub =
        stubRegisterUserSuccess(
            username,
            V2_SIGNIN_TEST_EMAIL_2,
            V2_SIGNIN_TEST_PHONE_2,
            V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            username,
            null,
            null,
            null,
            pin,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("id_token", notNullValue())
        .body("sso_token", notNullValue())
        .body("expires_in", notNullValue())
        .header("Set-Cookie", notNullValue());

    validateSetCookieTokens(response);
    String accessToken = response.jsonPath().getString("access_token");
    int expiresIn = response.jsonPath().getInt("expires_in");
    validateAccessTokenClaims(
        accessToken,
        TEST_SCOPE,
        TENANT_ID,
        firstPartyClientId,
        expiresIn,
        V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return valid token when email password combination is provided")
  public void returnValidTokenWhenEmailPasswordCombinationIsProvided() {
    // Arrange
    String email = V2_SIGNIN_TEST_EMAIL_1;
    String password = V2_SIGNIN_TEST_PASSWORD_1;
    StubMapping stub =
        stubRegisterUserSuccess(
            V2_SIGNIN_TEST_USERNAME_1,
            email,
            V2_SIGNIN_TEST_PHONE_1,
            V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            null,
            email,
            password,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("id_token", notNullValue())
        .body("sso_token", notNullValue())
        .body("expires_in", notNullValue())
        .header("Set-Cookie", notNullValue());

    validateSetCookieTokens(response);
    String accessToken = response.jsonPath().getString("access_token");
    int expiresIn = response.jsonPath().getInt("expires_in");
    validateAccessTokenClaims(
        accessToken,
        TEST_SCOPE,
        TENANT_ID,
        firstPartyClientId,
        expiresIn,
        V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return valid token when email pin combination is provided")
  public void returnValidTokenWhenEmailPinCombinationIsProvided() {
    // Arrange
    String email = V2_SIGNIN_TEST_EMAIL_2;
    String pin = V2_SIGNIN_TEST_PIN_2;
    StubMapping stub =
        stubRegisterUserSuccess(
            V2_SIGNIN_TEST_USERNAME_2,
            email,
            V2_SIGNIN_TEST_PHONE_2,
            V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            null,
            email,
            null,
            pin,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("id_token", notNullValue())
        .body("sso_token", notNullValue())
        .body("expires_in", notNullValue())
        .header("Set-Cookie", notNullValue());

    validateSetCookieTokens(response);
    String accessToken = response.jsonPath().getString("access_token");
    int expiresIn = response.jsonPath().getInt("expires_in");
    validateAccessTokenClaims(
        accessToken,
        TEST_SCOPE,
        TENANT_ID,
        firstPartyClientId,
        expiresIn,
        V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return valid token when phone number password combination is provided")
  public void returnValidTokenWhenPhoneNumberPasswordCombinationIsProvided() {
    // Arrange
    String phoneNumber = V2_SIGNIN_TEST_PHONE_1;
    String password = V2_SIGNIN_TEST_PASSWORD_1;
    StubMapping stub =
        stubRegisterUserSuccess(
            V2_SIGNIN_TEST_USERNAME_1,
            V2_SIGNIN_TEST_EMAIL_1,
            phoneNumber,
            V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            phoneNumber,
            null,
            password,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("id_token", notNullValue())
        .body("sso_token", notNullValue())
        .body("expires_in", notNullValue())
        .header("Set-Cookie", notNullValue());

    validateSetCookieTokens(response);
    String accessToken = response.jsonPath().getString("access_token");
    int expiresIn = response.jsonPath().getInt("expires_in");
    validateAccessTokenClaims(
        accessToken,
        TEST_SCOPE,
        TENANT_ID,
        firstPartyClientId,
        expiresIn,
        V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return valid token when phone number pin combination is provided")
  public void returnValidTokenWhenPhoneNumberPinCombinationIsProvided() {
    // Arrange
    String phoneNumber = V2_SIGNIN_TEST_PHONE_2;
    String pin = V2_SIGNIN_TEST_PIN_2;
    StubMapping stub =
        stubRegisterUserSuccess(
            V2_SIGNIN_TEST_USERNAME_2,
            V2_SIGNIN_TEST_EMAIL_2,
            phoneNumber,
            V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            phoneNumber,
            null,
            null,
            pin,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body("access_token", notNullValue())
        .body("refresh_token", notNullValue())
        .body("id_token", notNullValue())
        .body("sso_token", notNullValue())
        .body("expires_in", notNullValue())
        .header("Set-Cookie", notNullValue());

    validateSetCookieTokens(response);
    String accessToken = response.jsonPath().getString("access_token");
    int expiresIn = response.jsonPath().getInt("expires_in");
    validateAccessTokenClaims(
        accessToken,
        TEST_SCOPE,
        TENANT_ID,
        firstPartyClientId,
        expiresIn,
        V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    wireMockServer.removeStub(stub);
  }

  // Code Response Type Tests
  @Test
  @DisplayName("V2SignUp - Return valid code when username password combination is provided")
  public void returnValidCodeWhenUsernamePasswordCombinationIsProvided() {
    // Arrange
    String username = V2_SIGNIN_TEST_USERNAME_1;
    String password = V2_SIGNIN_TEST_PASSWORD_1;
    StubMapping stub =
        stubRegisterUserSuccess(
            username,
            V2_SIGNIN_TEST_EMAIL_1,
            V2_SIGNIN_TEST_PHONE_1,
            V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            username,
            null,
            null,
            password,
            null,
            AUTH_RESPONSE_TYPE_CODE,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body(AUTH_RESPONSE_TYPE_CODE, notNullValue())
        .body("expires_in", notNullValue())
        .body("is_new_user", equalTo(true));

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return valid code when username pin combination is provided")
  public void returnValidCodeWhenUsernamePinCombinationIsProvided() {
    // Arrange
    String username = V2_SIGNIN_TEST_USERNAME_2;
    String pin = V2_SIGNIN_TEST_PIN_2;
    StubMapping stub =
        stubRegisterUserSuccess(
            username,
            V2_SIGNIN_TEST_EMAIL_2,
            V2_SIGNIN_TEST_PHONE_2,
            V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            username,
            null,
            null,
            null,
            pin,
            AUTH_RESPONSE_TYPE_CODE,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body(AUTH_RESPONSE_TYPE_CODE, notNullValue())
        .body("expires_in", notNullValue())
        .body("is_new_user", equalTo(true));

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return valid code when email password combination is provided")
  public void returnValidCodeWhenEmailPasswordCombinationIsProvided() {
    // Arrange
    String email = V2_SIGNIN_TEST_EMAIL_1;
    String password = V2_SIGNIN_TEST_PASSWORD_1;
    StubMapping stub =
        stubRegisterUserSuccess(
            V2_SIGNIN_TEST_USERNAME_1,
            email,
            V2_SIGNIN_TEST_PHONE_1,
            V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            null,
            email,
            password,
            null,
            AUTH_RESPONSE_TYPE_CODE,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body(AUTH_RESPONSE_TYPE_CODE, notNullValue())
        .body("expires_in", notNullValue())
        .body("is_new_user", equalTo(true));

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return valid code when email pin combination is provided")
  public void returnValidCodeWhenEmailPinCombinationIsProvided() {
    // Arrange
    String email = V2_SIGNIN_TEST_EMAIL_2;
    String pin = V2_SIGNIN_TEST_PIN_2;
    StubMapping stub =
        stubRegisterUserSuccess(
            V2_SIGNIN_TEST_USERNAME_2,
            email,
            V2_SIGNIN_TEST_PHONE_2,
            V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            null,
            email,
            null,
            pin,
            AUTH_RESPONSE_TYPE_CODE,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body(AUTH_RESPONSE_TYPE_CODE, notNullValue())
        .body("expires_in", notNullValue())
        .body("is_new_user", equalTo(true));

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return valid code when phone number password combination is provided")
  public void returnValidCodeWhenPhoneNumberPasswordCombinationIsProvided() {
    // Arrange
    String phoneNumber = V2_SIGNIN_TEST_PHONE_1;
    String password = V2_SIGNIN_TEST_PASSWORD_1;
    StubMapping stub =
        stubRegisterUserSuccess(
            V2_SIGNIN_TEST_USERNAME_1,
            V2_SIGNIN_TEST_EMAIL_1,
            phoneNumber,
            V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            phoneNumber,
            null,
            password,
            null,
            AUTH_RESPONSE_TYPE_CODE,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body(AUTH_RESPONSE_TYPE_CODE, notNullValue())
        .body("expires_in", notNullValue())
        .body("is_new_user", equalTo(true));

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return valid code when phone number pin combination is provided")
  public void returnValidCodeWhenPhoneNumberPinCombinationIsProvided() {
    // Arrange
    String phoneNumber = V2_SIGNIN_TEST_PHONE_2;
    String pin = V2_SIGNIN_TEST_PIN_2;
    StubMapping stub =
        stubRegisterUserSuccess(
            V2_SIGNIN_TEST_USERNAME_2,
            V2_SIGNIN_TEST_EMAIL_2,
            phoneNumber,
            V2_SIGNIN_CREDENTIAL_TYPE_PIN);

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            phoneNumber,
            null,
            null,
            pin,
            AUTH_RESPONSE_TYPE_CODE,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body(AUTH_RESPONSE_TYPE_CODE, notNullValue())
        .body("expires_in", notNullValue())
        .body("is_new_user", equalTo(true));

    wireMockServer.removeStub(stub);
  }

  // Negative Test Cases - User Already Exists
  @Test
  @DisplayName("V2SignUp - Return error when username already exists")
  public void returnErrorWhenUsernameAlreadyExists() {
    // Arrange
    StubMapping stub = stubRegisterUserAlreadyExists();

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            "password123",
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_CONFLICT)
        .rootPath("error")
        .body("message", equalTo(V2_SIGNUP_ERROR_USER_EXISTS));

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return error when email already exists")
  public void returnErrorWhenEmailAlreadyExists() {
    // Arrange
    StubMapping stub = stubRegisterUserAlreadyExists();

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            null,
            V2_SIGNIN_TEST_EMAIL_1,
            "password123",
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_CONFLICT)
        .rootPath("error")
        .body("message", equalTo(V2_SIGNUP_ERROR_USER_EXISTS));

    wireMockServer.removeStub(stub);
  }

  @Test
  @DisplayName("V2SignUp - Return error when phone number already exists")
  public void returnErrorWhenPhoneNumberAlreadyExists() {
    // Arrange
    StubMapping stub = stubRegisterUserAlreadyExists();

    // Act
    Response response =
        ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            "9999999999",
            null,
            "password123",
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_CONFLICT)
        .rootPath("error")
        .body("message", equalTo(V2_SIGNUP_ERROR_USER_EXISTS));

    wireMockServer.removeStub(stub);
  }

  // Validation Test Cases - Multiple Identifiers
  @Test
  @DisplayName("V2SignUp - Return error when both username and email are provided")
  public void returnErrorWhenBothUsernameAndEmailAreProvided() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            "testuser",
            null,
            "test@example.com",
            "password123",
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when both username and phone number are provided")
  public void returnErrorWhenBothUsernameAndPhoneNumberAreProvided() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            "testuser",
            "1234567890",
            null,
            "password123",
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when both email and phone number are provided")
  public void returnErrorWhenBothEmailAndPhoneNumberAreProvided() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            "1234567890",
            "test@example.com",
            "password123",
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when all identifiers are provided")
  public void returnErrorWhenAllIdentifiersAreProvided() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            "testuser",
            "1234567890",
            "test@example.com",
            "password123",
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when no identifier is provided")
  public void returnErrorWhenNoIdentifierIsProvided() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            null,
            null,
            null,
            "password123",
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when both password and pin are provided")
  public void returnErrorWhenBothPasswordAndPinAreProvided() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            "testuser",
            null,
            null,
            "password123",
            "1234",
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when no credential is provided")
  public void returnErrorWhenNoCredentialIsProvided() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            "testuser",
            null,
            null,
            null,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when requesting non-existent scope")
  public void returnErrorWhenRequestingNonExistentScope() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(V2_SIGNIN_TEST_INVALID_SCOPE_1),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when requesting multiple scopes with one non-existent")
  public void returnErrorWhenRequestingMultipleScopesWithOneNonExistent() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE, V2_SIGNIN_TEST_INVALID_SCOPE_1),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when requesting all non-existent scopes")
  public void returnErrorWhenRequestingAllNonExistentScopes() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of("invalid_scope1", "invalid_scope2"),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when client_id is missing")
  public void returnErrorWhenClientIdIsMissing() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            null)
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when client_id is blank")
  public void returnErrorWhenClientIdIsBlank() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            "")
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("V2SignUp - Return error when client_id does not exist")
  public void returnErrorWhenClientIdDoesNotExist() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            "nonexistent_client")
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_CLIENT));
  }

  @Test
  @DisplayName("V2SignUp - Return error when client_id does not correspond to tenant")
  public void returnErrorWhenClientIdDoesNotCorrespondToTenant() {
    ApplicationIoUtils.v2SignUp(
            "tenant2",
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            firstPartyClientId)
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_CLIENT));
  }

  @Test
  @DisplayName("V2SignUp - Return error when using third party client_id")
  public void returnErrorWhenUsingThirdPartyClientId() {
    ApplicationIoUtils.v2SignUp(
            TENANT_ID,
            V2_SIGNIN_TEST_USERNAME_1,
            null,
            null,
            V2_SIGNIN_TEST_PASSWORD_1,
            null,
            AUTH_RESPONSE_TYPE_TOKEN,
            List.of(TEST_SCOPE),
            null,
            thirdPartyClientId)
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(AUTH_RESPONSE_TYPE_CODE, equalTo(ERROR_INVALID_CLIENT));
  }

  // Stub Methods for POST /user
  private StubMapping stubRegisterUserSuccess(
      String username, String email, String phoneNumber, String credentialType) {
    JsonObject responseBody =
        new JsonObject()
            .put("email", email)
            .put("phoneNumber", phoneNumber)
            .put("username", username)
            .put("passwordSet", V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD.equals(credentialType))
            .put("pinSet", V2_SIGNIN_CREDENTIAL_TYPE_PIN.equals(credentialType))
            .put("userId", RandomStringUtils.randomAlphanumeric(10));

    return wireMockServer.stubFor(
        post(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody.encode())));
  }

  private StubMapping stubRegisterUserAlreadyExists() {
    JsonObject error =
        new JsonObject()
            .put(AUTH_RESPONSE_TYPE_CODE, "User-Error")
            .put("message", V2_SIGNUP_ERROR_USER_EXISTS)
            .put("cause", V2_SIGNUP_ERROR_USER_EXISTS)
            .put("details", new JsonObject());

    JsonObject responseBody = new JsonObject().put("error", error);

    return wireMockServer.stubFor(
        post(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(SC_CONFLICT)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody.encode())));
  }

  // Helper Methods
  private void validateSetCookieTokens(Response response) {
    String accessToken = response.jsonPath().getString("access_token");
    String refreshToken = response.jsonPath().getString("refresh_token");
    String ssoToken = response.jsonPath().getString("sso_token");

    assertThat(
        "AT cookie should match access_token", response.getCookie("AT"), equalTo(accessToken));
    assertThat(
        "RT cookie should match refresh_token", response.getCookie("RT"), equalTo(refreshToken));
    assertThat("SSOT cookie should match sso_token", response.getCookie("SSOT"), equalTo(ssoToken));
  }

  private void validateAccessTokenClaims(
      String accessToken,
      String scope,
      String tenantId,
      String clientId,
      int expiresIn,
      String credentialType) {
    Path publicKeyPath = Paths.get("src/test/resources/test-data/tenant1-public-key.pem");
    JWT jwt = decoder.decode(accessToken, RSAVerifier.newVerifier(publicKeyPath));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(claims.get(JWT_CLAIM_SUB), notNullValue());
    assertThat(claims.get(JWT_CLAIM_SCOPE), equalTo(scope));
    assertThat(claims.get(JWT_CLAIM_TENANT_ID), equalTo(tenantId));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(clientId));

    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo((long) expiresIn));

    @SuppressWarnings("unchecked")
    List<String> amr = (List<String>) claims.get(JWT_CLAIMS_AMR);
    String expectedAmr =
        V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD.equals(credentialType)
            ? "pwd"
            : V2_SIGNIN_CREDENTIAL_TYPE_PIN;
    assertThat(amr.size(), equalTo(1));
    assertThat(amr.get(0), equalTo(expectedAmr));
  }
}
