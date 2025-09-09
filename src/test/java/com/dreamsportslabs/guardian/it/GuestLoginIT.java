package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.ACCESS_TOKEN_EXPIRY_SECONDS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_GUEST_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPES;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.INVALID_GUEST_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.INVALID_SCOPE;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIMS_AMR;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_EXP;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_IAT;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_EXPIRES_IN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_TOKEN_TYPE;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_HEADER_PARAM_SET_COOKIE;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PROFILE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TENANT_3;
import static com.dreamsportslabs.guardian.Constants.TOKEN_TYPE_BEARER;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_EMAIL_VERIFIED;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.guestLogin;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanUpScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupClients;
import static com.dreamsportslabs.guardian.utils.ScopeUtils.getValidScopeRequestBody;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.ClientUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import io.fusionauth.jwt.JWTDecoder;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.restassured.response.Response;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class GuestLoginIT {

  private static String validGuestIdentifier;
  private static List<String> validScopes;
  private final JWTDecoder decoder = JWT.getDecoder();
  static String testClientId1;
  static String testClientId2;
  static String testClientId3;

  @BeforeAll
  static void setUp() {
    validGuestIdentifier =
        "QwnZ/2k+mpCm27nFOqb95g=="; // decrypted guestIdentifier of "abcd12345" String.
    validScopes = List.of("profile", "email", "phone");

    DbUtils.cleanupClientScopes(TENANT_1);
    DbUtils.cleanupClientScopes(TENANT_2);
    DbUtils.cleanupClientScopes(TENANT_3);
    cleanupClients(TENANT_1);
    cleanupClients(TENANT_2);
    cleanupClients(TENANT_3);
    cleanUpScopes(TENANT_1);
    cleanUpScopes(TENANT_2);
    cleanUpScopes(TENANT_3);
    createRequiredScope(TENANT_1);
    createRequiredScope(TENANT_2);
    createRequiredScope(TENANT_3);
    Response clientResponse1 = createTestClient(TENANT_1);
    testClientId1 = clientResponse1.jsonPath().getString(CLIENT_ID);
    Response clientResponse2 = createTestClient(TENANT_2);
    testClientId2 = clientResponse2.jsonPath().getString(CLIENT_ID);
    Response clientResponse3 = createTestClient(TENANT_3);
    testClientId3 = clientResponse3.jsonPath().getString(CLIENT_ID);
    createClientScope(
        TENANT_1,
        testClientId1,
        ClientUtils.createClientScopeRequest(SCOPE_PROFILE, SCOPE_EMAIL, SCOPE_PHONE));
    createClientScope(
        TENANT_2,
        testClientId2,
        ClientUtils.createClientScopeRequest(SCOPE_PROFILE, SCOPE_EMAIL, SCOPE_PHONE));
    createClientScope(
        TENANT_3, testClientId3, ClientUtils.createClientScopeRequest(SCOPE_PROFILE, SCOPE_PHONE));
  }

  private static Response createTestClient(String tenantId) {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    return createClient(tenantId, requestBody);
  }

  private static void createRequiredScope(String tenantId) {
    createScope(
        tenantId,
        getValidScopeRequestBody(
            SCOPE_EMAIL,
            "Email",
            "Email scope",
            Arrays.asList(CLAIM_EMAIL, CLAIM_EMAIL_VERIFIED),
            "",
            true));

    createScope(
        tenantId,
        getValidScopeRequestBody(
            SCOPE_PHONE,
            "Phone",
            "Phone scope",
            Arrays.asList(CLAIM_PHONE_NUMBER, CLAIM_PHONE_NUMBER_VERIFIED),
            "",
            true));

    createScope(
        tenantId,
        getValidScopeRequestBody(
            SCOPE_PROFILE,
            "Profile",
            "Profile scope",
            Arrays.asList("name", "family_name", "given_name"),
            "",
            true));
  }

  public Path getPublicKeyPath(String tenantId) {
    Path path1 = Paths.get("src/test/resources/test-data/tenant1-public-key.pem");
    Path path2 = Paths.get("src/test/resources/test-data/tenant2-public-key.pem");
    Path path3 = Paths.get("src/test/resources/test-data/tenant3-public-key.pem");

    return switch (tenantId) {
      case TENANT_1 -> path1;
      case TENANT_2 -> path2;
      case TENANT_3 -> path3;
      default -> null;
    };
  }

  private void validateAccessTokenClaims(
      String accessToken, String userId, String scope, String tenantId, String clientId) {
    JWT jwt = decoder.decode(accessToken, RSAVerifier.newVerifier(getPublicKeyPath(tenantId)));
    Map<String, Object> claims = jwt.getAllClaims();
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(userId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));
    assertThat(claims.get(JWT_CLAIM_SCOPE), equalTo(scope));
    assertThat(claims.get(JWT_CLAIM_TENANT_ID), equalTo(tenantId));
    Object amr = claims.get(JWT_CLAIMS_AMR);
    assertThat((java.util.List<?>) amr, empty());
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(clientId));
  }

  @Test
  @DisplayName("Should login successfully with valid parameters for encrypted tenant")
  public void testGuestLoginSuccessWithEncryptedTenant() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, testClientId1);
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, validGuestIdentifier);
    requestBody.put(BODY_PARAM_SCOPES, validScopes);

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, equalTo(900));
    String setCookieHeader = response.getHeader(RESPONSE_HEADER_PARAM_SET_COOKIE);
    String atFromCookie = setCookieHeader.split(";")[0].trim().substring(3);

    String accessToken = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
    assertThat(atFromCookie, equalTo(accessToken));

    validateAccessTokenClaims(
        accessToken, "abcd12345", "profile email phone", TENANT_1, testClientId1);
  }

  @Test
  @DisplayName("Should login successfully with valid parameters for non-encrypted tenant")
  public void testGuestLoginSuccessWithNonEncryptedTenant() {
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_GUEST_IDENTIFIER, "test123");
    body.put(BODY_PARAM_CLIENT_ID, testClientId2);
    body.put(BODY_PARAM_SCOPES, List.of("profile"));

    Response response =
        ApplicationIoUtils.guestLogin(TENANT_2, body); // isEncrypted value is false for tenant2

    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, equalTo(900));
    String setCookieHeader = response.getHeader(RESPONSE_HEADER_PARAM_SET_COOKIE);
    String atFromCookie = setCookieHeader.split(";")[0].trim().substring(3);

    String accessToken = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
    assertThat(atFromCookie, equalTo(accessToken));

    validateAccessTokenClaims(accessToken, "test123", "profile", TENANT_2, testClientId2);
  }

  @Test
  @DisplayName("Should fail when guest identifier is missing")
  public void testGuestLoginFailureWhenGuestIdentifierMissing() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, testClientId1);
    requestBody.put(BODY_PARAM_SCOPES, validScopes);

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("guestIdentifier cannot be null or empty"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should fail when guest identifier is null or empty")
  public void testGuestLoginFailureWhenGuestIdentifierNullOrEmpty(String guestIdentifier) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, guestIdentifier);
    requestBody.put(BODY_PARAM_CLIENT_ID, testClientId1);
    requestBody.put(BODY_PARAM_SCOPES, validScopes);

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("guestIdentifier cannot be null or empty"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should fail when clientId is null or empty")
  public void testGuestLoginFailureWhenClientIdNullOrEmpty(String clientId) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, clientId);
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, validGuestIdentifier);
    requestBody.put(BODY_PARAM_SCOPES, validScopes);

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("clientId cannot be null or empty"));
  }

  @Test
  @DisplayName("Should fail when clientId does not exist")
  public void testGuestLoginFailureWhenClientIdDoesNotExist() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, "non-existent-client");
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, validGuestIdentifier);
    requestBody.put(BODY_PARAM_SCOPES, validScopes);

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo(CLIENT_NOT_FOUND))
        .body(MESSAGE, equalTo("Client not found"));
  }

  @Test
  @DisplayName("Should fail when scopes are missing")
  public void testGuestLoginFailureWhenScopesMissing() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, validGuestIdentifier);
    requestBody.put(BODY_PARAM_CLIENT_ID, testClientId1);

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("scopes cannot be null or empty"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should fail when scopes are null or empty")
  public void testGuestLoginFailureWhenScopesEmpty(List<String> scopes) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, validGuestIdentifier);
    requestBody.put(BODY_PARAM_CLIENT_ID, testClientId1);
    requestBody.put(BODY_PARAM_SCOPES, scopes);

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("scopes cannot be null or empty"));
  }

  @Test
  @DisplayName("Should fail when invalid scope is requested")
  public void testGuestLoginFailureWhenInvalidScopeRequested() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, validGuestIdentifier);
    requestBody.put(BODY_PARAM_CLIENT_ID, testClientId1);
    requestBody.put(BODY_PARAM_SCOPES, List.of("invalid_scope"));

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_SCOPE))
        .body(MESSAGE, equalTo("Invalid scope 'invalid_scope'"));
  }

  @Test
  @DisplayName("Should fail when encrypted identifier is invalid")
  public void testGuestLoginWithEncryptedIdentifier() {
    String encryptedIdentifier = RandomStringUtils.randomAlphanumeric(16);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, encryptedIdentifier);
    requestBody.put(BODY_PARAM_CLIENT_ID, testClientId1);
    requestBody.put(BODY_PARAM_SCOPES, validScopes);

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_GUEST_IDENTIFIER))
        .body(MESSAGE, equalTo("Invalid guest identifier"));
  }

  @Test
  @DisplayName("Should work with partial scopes")
  public void testGuestLoginWithPartialScopes() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, validGuestIdentifier);
    requestBody.put(BODY_PARAM_CLIENT_ID, testClientId2);
    requestBody.put(BODY_PARAM_SCOPES, List.of("profile", "email"));

    Response response = guestLogin(TENANT_2, requestBody);

    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, equalTo(900));
    String setCookieHeader = response.getHeader(RESPONSE_HEADER_PARAM_SET_COOKIE);
    String atFromCookie = setCookieHeader.split(";")[0].trim().substring(3);

    String accessToken = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
    assertThat(atFromCookie, equalTo(accessToken));

    validateAccessTokenClaims(
        accessToken, validGuestIdentifier, "profile email", TENANT_2, testClientId2);
  }

  @Test
  @DisplayName("Should work with tenant3 configuration")
  public void testGuestLoginWithTenant3() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, "/gvKzp9Sa8I2RqOy6QO1YQ==");
    requestBody.put(BODY_PARAM_CLIENT_ID, testClientId3);
    requestBody.put(BODY_PARAM_SCOPES, List.of("profile", "phone"));

    Response response = guestLogin(TENANT_3, requestBody);

    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, equalTo(900));
    String setCookieHeader = response.getHeader(RESPONSE_HEADER_PARAM_SET_COOKIE);
    String atFromCookie = setCookieHeader.split(";")[0].trim().substring(3);

    String accessToken = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
    assertThat(atFromCookie, equalTo(accessToken));

    validateAccessTokenClaims(accessToken, "abcd12345", "profile phone", TENANT_3, testClientId3);
  }
}
