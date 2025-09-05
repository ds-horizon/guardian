package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.ACCESS_TOKEN_EXPIRY_SECONDS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_GUEST_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPES;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.INVALID_GUEST_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.INVALID_SCOPE;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_EXP;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_IAT;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SUB_TYPE;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_EXPIRES_IN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_TOKEN_TYPE;
import static com.dreamsportslabs.guardian.Constants.SUB_TYPE_GUEST;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TENANT_3;
import static com.dreamsportslabs.guardian.Constants.TOKEN_TYPE_BEARER;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.guestLogin;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.Setup;
import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import io.fusionauth.jwt.JWTDecoder;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.restassured.response.Response;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@ExtendWith(Setup.class)
public class GuestLoginIT {

  private String validGuestIdentifier;
  private List<String> validScopes;
  private final JWTDecoder decoder = JWT.getDecoder();

  @BeforeEach
  void setUp() {
    validGuestIdentifier =
        "lQk/p8rauzIz44v0hvla3A=="; // decrypted guestIdentifier of "abcd12345" String.
    validScopes = List.of("profile", "email", "phone");
  }

  public String getPublicKey(String tenantId) {
    final String PublicKey1 =
        "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAinYeXJY9uI5j9gnRYJxX/A0LCgVQ8NEfnghPzo75Vt6J+ijGOUkpyJ65p8VxztAwXO9200Ro3YSgP1sxluWS4Xj/aMbPbYcbFpFlDvZ9c1zjsFKycUmOouz3fBump92qJoOcuKCoKmS5GZncC6hUKsTyp+0aUXMlKE7ViJaFtRgGmEfyHMlqr3o01cOH8lesyfAnrKcOtyNjAxlNR9E2S4HEBpT8fumYIVii5my55k8TWBaO+iEy4oNjSsRj4gxJvLnVRenXWE/l5gKVDipl98SVHWaCDRr5qFtDO0dwcXb9+Ep42OTsBi8q2XcHTfsyfX/sClAMQtBmAemmdS5kEQIDAQAB-----END PUBLIC KEY-----";

    final String PublicKey2 =
        "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAinYeXJY9uI5j9gnRYJxX/A0LCgVQ8NEfnghPzo75Vt6J+ijGOUkpyJ65p8VxztAwXO9200Ro3YSgP1sxluWS4Xj/aMbPbYcbFpFlDvZ9c1zjsFKycUmOouz3fBump92qJoOcuKCoKmS5GZncC6hUKsTyp+0aUXMlKE7ViJaFtRgGmEfyHMlqr3o01cOH8lesyfAnrKcOtyNjAxlNR9E2S4HEBpT8fumYIVii5my55k8TWBaO+iEy4oNjSsRj4gxJvLnVRenXWE/l5gKVDipl98SVHWaCDRr5qFtDO0dwcXb9+Ep42OTsBi8q2XcHTfsyfX/sClAMQtBmAemmdS5kEQIDAQAB-----END PUBLIC KEY-----";

    final String PublicKey3 =
        "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAinYeXJY9uI5j9gnRYJxX/A0LCgVQ8NEfnghPzo75Vt6J+ijGOUkpyJ65p8VxztAwXO9200Ro3YSgP1sxluWS4Xj/aMbPbYcbFpFlDvZ9c1zjsFKycUmOouz3fBump92qJoOcuKCoKmS5GZncC6hUKsTyp+0aUXMlKE7ViJaFtRgGmEfyHMlqr3o01cOH8lesyfAnrKcOtyNjAxlNR9E2S4HEBpT8fumYIVii5my55k8TWBaO+iEy4oNjSsRj4gxJvLnVRenXWE/l5gKVDipl98SVHWaCDRr5qFtDO0dwcXb9+Ep42OTsBi8q2XcHTfsyfX/sClAMQtBmAemmdS5kEQIDAQAB-----END PUBLIC KEY-----";

    return switch (tenantId) {
      case TENANT_1 -> PublicKey1;
      case TENANT_2 -> PublicKey2;
      case TENANT_3 -> PublicKey3;
      default -> null;
    };
  }

  private void validateAccessTokenClaims(
      String accessToken, String userId, String scope, String tenantId) {
    JWT jwt = decoder.decode(accessToken, RSAVerifier.newVerifier(getPublicKey(tenantId)));
    Map<String, Object> claims = jwt.getAllClaims();
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(userId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));
    assertThat(claims.get(JWT_CLAIM_SCOPE), equalTo(scope));
    assertThat(claims.get(JWT_CLAIM_TENANT_ID), equalTo(tenantId));
    assertThat(claims.get(JWT_CLAIM_SUB_TYPE), equalTo(SUB_TYPE_GUEST));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo("test-client"));
  }

  @Test
  @DisplayName("Should login successfully with valid parameters for encrypted tenant")
  public void testGuestLoginSuccessWithEncryptedTenant() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, "test-client");
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, validGuestIdentifier);
    requestBody.put(BODY_PARAM_SCOPES, validScopes);

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, equalTo(900));
    String accessToken = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
    validateAccessTokenClaims(accessToken, "abcd12345", "profile email phone", TENANT_1);
  }

  @Test
  @DisplayName("Should login successfully with valid parameters for non-encrypted tenant")
  public void testGuestLoginSuccessWithNonEncryptedTenant() {
    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_GUEST_IDENTIFIER, "test123");
    body.put(BODY_PARAM_CLIENT_ID, "test-client");
    body.put(BODY_PARAM_SCOPES, List.of("profile"));

    Response response =
        ApplicationIoUtils.guestLogin(TENANT_2, body); // isEncrypted value is false for tenant2

    response
        .then()
        .statusCode(200)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, equalTo(900));
    String accessToken = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
    validateAccessTokenClaims(accessToken, "test123", "profile", TENANT_2);
  }

  @Test
  @DisplayName("Should fail when guest identifier is missing")
  public void testGuestLoginFailureWhenGuestIdentifierMissing() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CLIENT_ID, "test-client");
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
    requestBody.put(BODY_PARAM_CLIENT_ID, "test-client");
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
    requestBody.put(BODY_PARAM_CLIENT_ID, "test-client");

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
    requestBody.put(BODY_PARAM_CLIENT_ID, "test-client");
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
    requestBody.put(BODY_PARAM_CLIENT_ID, "test-client");
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
    requestBody.put(BODY_PARAM_CLIENT_ID, "test-client");
    requestBody.put(BODY_PARAM_SCOPES, validScopes);

    Response response = guestLogin(TENANT_1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(INVALID_GUEST_IDENTIFIER))
        .body(MESSAGE, equalTo("Decryption failed. guest identifier sent is invalid"));
  }

  @Test
  @DisplayName("Should work with partial scopes")
  public void testGuestLoginWithPartialScopes() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, validGuestIdentifier);
    requestBody.put(BODY_PARAM_CLIENT_ID, "test-client");
    requestBody.put(BODY_PARAM_SCOPES, List.of("profile", "email"));

    Response response = guestLogin(TENANT_2, requestBody);

    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, equalTo(900));
    String accessToken = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
    validateAccessTokenClaims(accessToken, validGuestIdentifier, "profile email", TENANT_2);
  }

  @Test
  @DisplayName("Should work with tenant3 configuration")
  public void testGuestLoginWithTenant3() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_GUEST_IDENTIFIER, "/gvKzp9Sa8I2RqOy6QO1YQ==");
    requestBody.put(BODY_PARAM_CLIENT_ID, "test-client");
    requestBody.put(BODY_PARAM_SCOPES, List.of("profile", "phone"));

    Response response = guestLogin(TENANT_3, requestBody);

    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, equalTo(900));
    String accessToken = response.getBody().jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
    validateAccessTokenClaims(accessToken, "abcd12345", "profile phone", TENANT_3);
  }
}
