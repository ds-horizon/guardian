package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.ADDITIONAL_CLAIM_ITEM1;
import static com.dreamsportslabs.guardian.Constants.ADDITIONAL_CLAIM_ITEM2;
import static com.dreamsportslabs.guardian.Constants.ADDITIONAL_CLAIM_VALUE1;
import static com.dreamsportslabs.guardian.Constants.ADDITIONAL_CLAIM_VALUE2;
import static com.dreamsportslabs.guardian.Constants.AUTHORIZATION_CODE;
import static com.dreamsportslabs.guardian.Constants.AUTH_BASIC_PREFIX;
import static com.dreamsportslabs.guardian.Constants.AUTH_CODE_CHALLENGE_METHOD_PLAIN;
import static com.dreamsportslabs.guardian.Constants.AUTH_CODE_CHALLENGE_METHOD_S256;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONSENTED_SCOPES;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONSENT_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERNAME;
import static com.dreamsportslabs.guardian.Constants.CACHE_CONTROL_NO_STORE;
import static com.dreamsportslabs.guardian.Constants.CALLBACK_1;
import static com.dreamsportslabs.guardian.Constants.CLAIM_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.CLAIM_EMAIL_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.CLIENT_CREDENTIALS;
import static com.dreamsportslabs.guardian.Constants.CLIENT_CREDENTIALS_SEPARATOR;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NAME;
import static com.dreamsportslabs.guardian.Constants.CLIENT_SECRET;
import static com.dreamsportslabs.guardian.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static com.dreamsportslabs.guardian.Constants.CONTENT_TYPE_FORM_URLENCODED;
import static com.dreamsportslabs.guardian.Constants.DEVICE_VALUE;
import static com.dreamsportslabs.guardian.Constants.EMAIL_DOMAIN_EXAMPLE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_SCOPE;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_GRANT_TYPE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_CALLBACK;
import static com.dreamsportslabs.guardian.Constants.EXPIRED_TOKEN_OFFSET_SECONDS;
import static com.dreamsportslabs.guardian.Constants.GRANT_TYPES;
import static com.dreamsportslabs.guardian.Constants.HEADER_AUTHORIZATION;
import static com.dreamsportslabs.guardian.Constants.HEADER_CACHE_CONTROL;
import static com.dreamsportslabs.guardian.Constants.HEADER_CONTENT_TYPE;
import static com.dreamsportslabs.guardian.Constants.HEADER_LOCATION;
import static com.dreamsportslabs.guardian.Constants.HEADER_PRAGMA;
import static com.dreamsportslabs.guardian.Constants.HEADER_WWW_AUTHENTICATE;
import static com.dreamsportslabs.guardian.Constants.INVALID_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.INVALID_CLIENT_SECRET;
import static com.dreamsportslabs.guardian.Constants.INVALID_CODE;
import static com.dreamsportslabs.guardian.Constants.INVALID_GRANT_TYPE;
import static com.dreamsportslabs.guardian.Constants.INVALID_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.IP_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.JSON_EMAIL_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.JSON_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.JSON_PHONE_NUMBER_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_ISS;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.LOCATION_VALUE;
import static com.dreamsportslabs.guardian.Constants.MOCK_USERNAME;
import static com.dreamsportslabs.guardian.Constants.MOCK_USER_ID;
import static com.dreamsportslabs.guardian.Constants.MOCK_USER_NAME;
import static com.dreamsportslabs.guardian.Constants.OIDC_BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.PARAM_CODE_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.PARAM_CODE_CHALLENGE_METHOD;
import static com.dreamsportslabs.guardian.Constants.PRAGMA_NO_CACHE;
import static com.dreamsportslabs.guardian.Constants.REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.REFRESH_TOKEN_EXPIRY_SECONDS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.SCOPE_SEPARATOR;
import static com.dreamsportslabs.guardian.Constants.SCOPE_SPLIT_REGEX;
import static com.dreamsportslabs.guardian.Constants.SOURCE_VALUE;
import static com.dreamsportslabs.guardian.Constants.TENANT3_PUBLIC_KEY_PATH;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TENANT_3;
import static com.dreamsportslabs.guardian.Constants.TEST_CODE_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.TEST_CODE_CHALLENGE_2;
import static com.dreamsportslabs.guardian.Constants.TEST_CODE_VERIFIER_2;
import static com.dreamsportslabs.guardian.Constants.TEST_DEVICE_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_IP_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.TEST_ISSUER;
import static com.dreamsportslabs.guardian.Constants.TEST_USER_ID;
import static com.dreamsportslabs.guardian.Constants.TEST_USER_ID_2;
import static com.dreamsportslabs.guardian.Constants.TEST_USER_ID_3;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_INVALID_CLIENT;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_INVALID_GRANT;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_INVALID_SCOPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_AUTH;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_AUTHORIZATION_CODE_INVALID;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_CLIENT_AUTH_FAILED;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_CODE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_CODE_VERIFIER_INVALID;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_CODE_VERIFIER_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_INVALID_SCOPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_REDIRECT_URI_INVALID;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_REDIRECT_URI_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_REFRESH_TOKEN_EXPIRED;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_REFRESH_TOKEN_INVALID;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_UNAUTHORIZED_CLIENT;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_UNSUPPORTED_GRANT_TYPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_UNAUTHORIZED_CLIENT;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_UNSUPPORTED_GRANT_TYPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_CODE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_CODE_VERIFIER;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_EXPIRES_IN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_GRANT_TYPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_ID_TOKEN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_REDIRECT_URI;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_TOKEN_TYPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_TYPE_BEARER;
import static com.dreamsportslabs.guardian.Constants.WIREMOCK_USER_ENDPOINT;
import static com.dreamsportslabs.guardian.Constants.WWW_AUTHENTICATE_BASIC_REALM_FORMAT;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_EMAIL;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.authorize;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.consentAccept;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.loginAccept;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupClients;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupOidcRefreshTokens;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupRedis;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertRefreshToken;
import static com.dreamsportslabs.guardian.utils.OidcUtils.createValidAuthorizeRequest;
import static com.dreamsportslabs.guardian.utils.OidcUtils.extractAuthCodeFromLocation;
import static com.dreamsportslabs.guardian.utils.OidcUtils.extractConsentChallenge;
import static com.dreamsportslabs.guardian.utils.OidcUtils.extractLoginChallenge;
import static com.dreamsportslabs.guardian.utils.OidcUtils.validateAccessTokenClaims;
import static com.dreamsportslabs.guardian.utils.OidcUtils.validateIdTokenClaims;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.ClientUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import com.dreamsportslabs.guardian.utils.OidcUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OidcTokenIT {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String tenant1 = TENANT_1;
  private static final String tenant2 = TENANT_2;
  private static final String tenant3 = TENANT_3; // Additional claims enabled for this tenant

  private String validClientId;
  private String validClientSecret;
  private String validAuthCode;
  private String validAuthCodeWithCodeChallengePlain;
  private String validAuthCodeWithCodeChallengeS256;
  private WireMockServer wireMockServer;

  @BeforeEach
  void setUp() {
    // Clean up any existing test data
    cleanupClients(tenant1);
    cleanupClients(tenant2);
    cleanupScopes(tenant1);
    cleanupScopes(tenant2);
    cleanupOidcRefreshTokens(tenant1);
    cleanupOidcRefreshTokens(tenant2);
    cleanupRedis();

    OidcUtils.createRequiredScopes(tenant1);
    OidcUtils.createRequiredScopes(tenant2);

    // Create a test client for authorization tests
    Response clientResponse = createTestClient();
    validClientId = clientResponse.jsonPath().getString(CLIENT_ID);
    validClientSecret = clientResponse.jsonPath().getString(CLIENT_SECRET);

    // Using ApplicationIoUtils directly (alternative approach)
    createClientScope(
        tenant1,
        validClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    createTestData();
  }

  private Response createTestClient() {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    requestBody.put(
        GRANT_TYPES, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
    return createClient(tenant1, requestBody);
  }

  private StubMapping getOidcUserStub(String email, String phoneNumber) {
    JsonNode jsonNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_NAME, MOCK_USER_NAME)
            .put(BODY_PARAM_EMAIL, email)
            .put(BODY_PARAM_USERID, MOCK_USER_ID)
            .put(BODY_PARAM_USERNAME, MOCK_USERNAME)
            .put(JSON_PHONE_NUMBER, phoneNumber)
            .put(JSON_PHONE_NUMBER_VERIFIED, phoneNumber)
            .put(JSON_EMAIL_VERIFIED, email);
    return wireMockServer.stubFor(
        get(urlPathMatching(WIREMOCK_USER_ENDPOINT))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                    .withJsonBody(jsonNode)));
  }

  private String generateRandomEmail() {
    return RandomStringUtils.randomAlphanumeric(10) + EMAIL_DOMAIN_EXAMPLE;
  }

  private String generateRandomPhoneNumber() {
    return RandomStringUtils.randomNumeric(10);
  }

  public Response createTestClientWithLimitedGrantTypes() {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    return createClient(tenant1, requestBody);
  }

  public Response createTestClientWithRandomName() {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    requestBody.put(CLIENT_NAME, RandomStringUtils.randomAlphanumeric(20));
    requestBody.put(
        GRANT_TYPES, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
    return createClient(tenant1, requestBody);
  }

  private void createTestData() {
    String validRefreshToken;
    String validRefreshTokenForUser2;
    String validRefreshTokenForUser3;
    String validConsentChallenge;
    String validConsentChallengeWithCodeChallengeMethodPlain;
    String validConsentChallengeWithCodeChallengeMethodS256;
    validRefreshToken =
        insertRefreshToken(
            tenant1, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);
    validRefreshTokenForUser2 =
        insertRefreshToken(
            tenant1, TEST_USER_ID_2, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);
    validRefreshTokenForUser3 =
        insertRefreshToken(
            tenant1, TEST_USER_ID_3, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);
    List<String> scopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);

    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId, scopes);

    Response authorizeResponse = authorize(tenant1, queryParams);
    queryParams.put(PARAM_CODE_CHALLENGE, TEST_CODE_CHALLENGE);
    queryParams.put(PARAM_CODE_CHALLENGE_METHOD, AUTH_CODE_CHALLENGE_METHOD_PLAIN);

    Response authorizeResponseWithCodeChallengeMethodPlain = authorize(tenant1, queryParams);

    queryParams.put(PARAM_CODE_CHALLENGE, TEST_CODE_CHALLENGE_2);
    queryParams.put(PARAM_CODE_CHALLENGE_METHOD, AUTH_CODE_CHALLENGE_METHOD_S256);

    Response authorizeResponseWithCodeChallengeMethodS256 = authorize(tenant1, queryParams);

    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));
    String loginChallengeWithCodeChallengeMethodPlain =
        extractLoginChallenge(
            authorizeResponseWithCodeChallengeMethodPlain.getHeader(HEADER_LOCATION));
    String loginChallengeWithCodeChallengeMethodS256 =
        extractLoginChallenge(
            authorizeResponseWithCodeChallengeMethodS256.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response loginAcceptResponse = loginAccept(tenant1, loginAcceptBody);

    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallengeWithCodeChallengeMethodPlain);
    loginAcceptBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, validRefreshTokenForUser2);
    Response loginAcceptResponseWithCodeChallengeMethodPlain =
        loginAccept(tenant1, loginAcceptBody);

    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallengeWithCodeChallengeMethodS256);
    loginAcceptBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, validRefreshTokenForUser3);
    Response loginAcceptResponseWithCodeChallengeMethodS256 = loginAccept(tenant1, loginAcceptBody);

    validConsentChallenge = extractConsentChallenge(loginAcceptResponse);
    validConsentChallengeWithCodeChallengeMethodPlain =
        extractConsentChallenge(loginAcceptResponseWithCodeChallengeMethodPlain);
    validConsentChallengeWithCodeChallengeMethodS256 =
        extractConsentChallenge(loginAcceptResponseWithCodeChallengeMethodS256);

    Map<String, Object> consentRequestBody = new HashMap<>();
    consentRequestBody.put(BODY_PARAM_CONSENT_CHALLENGE, validConsentChallenge);
    consentRequestBody.put(
        BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE));
    consentRequestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response consentAcceptResponse = consentAccept(tenant1, consentRequestBody);
    validAuthCode = extractAuthCodeFromLocation(consentAcceptResponse.getHeader(HEADER_LOCATION));

    consentRequestBody.put(
        BODY_PARAM_CONSENT_CHALLENGE, validConsentChallengeWithCodeChallengeMethodPlain);
    consentRequestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, validRefreshTokenForUser2);

    Response consentAcceptResponseWithCodeChallengeMethodPlain =
        consentAccept(tenant1, consentRequestBody);
    validAuthCodeWithCodeChallengePlain =
        extractAuthCodeFromLocation(
            consentAcceptResponseWithCodeChallengeMethodPlain.getHeader(HEADER_LOCATION));

    consentRequestBody.put(
        BODY_PARAM_CONSENT_CHALLENGE, validConsentChallengeWithCodeChallengeMethodS256);
    consentRequestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, validRefreshTokenForUser3);

    Response consentAcceptResponseWithCodeChallengeMethodS256 =
        consentAccept(tenant1, consentRequestBody);
    validAuthCodeWithCodeChallengeS256 =
        extractAuthCodeFromLocation(
            consentAcceptResponseWithCodeChallengeMethodS256.getHeader(HEADER_LOCATION));
  }

  private String getBasicAuthHeader(String clientId, String clientSecret) {
    String clientCredentials = clientId + CLIENT_CREDENTIALS_SEPARATOR + clientSecret;
    String authHeader =
        new String(Base64.getEncoder().encode(clientCredentials.getBytes(StandardCharsets.UTF_8)));
    return AUTH_BASIC_PREFIX + authHeader;
  }

  /** Helper method to validate scope using assertThat with containsInAnyOrder */
  private void validateScope(Response response, String... expectedScopes) {
    String actualScope = response.jsonPath().getString(TOKEN_PARAM_SCOPE);
    String[] actualScopeArray = actualScope.trim().split(SCOPE_SPLIT_REGEX);
    assertThat(Arrays.asList(actualScopeArray), containsInAnyOrder(expectedScopes));
  }

  @Test
  @DisplayName("Should return error in case of missing grant_type")
  public void testMissingGrantType() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    // Test with empty grant_type value instead of missing grant_type
    formParams.put(TOKEN_PARAM_GRANT_TYPE, "");

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR, equalTo(INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_MSG_GRANT_TYPE_REQUIRED));
  }

  @Test
  @DisplayName("Should return error in case of invalid grant_type")
  public void testInvalidGrantType() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, INVALID_GRANT_TYPE);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR, equalTo(TOKEN_ERROR_UNSUPPORTED_GRANT_TYPE))
        .body(
            ERROR_DESCRIPTION,
            equalTo(String.format(TOKEN_ERROR_MSG_UNSUPPORTED_GRANT_TYPE, INVALID_GRANT_TYPE)));
  }

  @Test
  @DisplayName(
      "Client Credentials - Should return token successfully for client credentials grant type with Basic auth")
  public void testClientCredentialsBasicAuth() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    List<String> notExpectedScopes = List.of();
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        validClientId,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        false,
        null);
  }

  @Test
  @DisplayName(
      "Should return token successfully for client credentials grant type with Request Params auth")
  public void testClientCredentialsRequestBodyAuth() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);
    formParams.put(CLIENT_ID, validClientId);
    formParams.put(CLIENT_SECRET, validClientSecret);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    List<String> notExpectedScopes = List.of();
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        validClientId,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        false,
        null);
  }

  @Test
  @DisplayName(
      "Should return token successfully for client credentials grant type with basic auth and custom scopes")
  public void testClientCredentialsBasicAuthAndCustomScopes() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + SCOPE_SEPARATOR + SCOPE_EMAIL);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL);
    List<String> notExpectedScopes = List.of(SCOPE_ADDRESS, SCOPE_PHONE);
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        validClientId,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        false,
        null);
  }

  @Test
  @DisplayName(
      "Should return token successfully for client credentials grant type with request body auth and custom scopes")
  public void testClientCredentialsRequestBodyAuthAndCustomScopes() {

    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);
    formParams.put(CLIENT_ID, validClientId);
    formParams.put(CLIENT_SECRET, validClientSecret);
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + SCOPE_SEPARATOR + SCOPE_EMAIL);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL);
    List<String> notExpectedScopes = List.of(SCOPE_ADDRESS, SCOPE_PHONE);
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        validClientId,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        false,
        null);
  }

  @Test
  @DisplayName(
      "Client Credentials - Should return error in case of invalid client credentials - basic auth")
  public void testInvalidClientCredentialsBasicAuth() {
    // Arrange
    String invalidClientCredentials =
        INVALID_CLIENT_ID + CLIENT_CREDENTIALS_SEPARATOR + INVALID_CLIENT_SECRET;
    String authHeader =
        new String(
            Base64.getEncoder().encode(invalidClientCredentials.getBytes(StandardCharsets.UTF_8)));
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, AUTH_BASIC_PREFIX + authHeader);
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(401)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_CLIENT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_CLIENT_AUTH_FAILED))
        .header(
            HEADER_WWW_AUTHENTICATE,
            equalTo(String.format(WWW_AUTHENTICATE_BASIC_REALM_FORMAT, TEST_ISSUER)));
  }

  @Test
  @DisplayName(
      "Client Credentials - Should return error in case of invalid client credentials - request body auth")
  public void testInvalidClientCredentialsRequestBodyAuth() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);
    formParams.put(CLIENT_ID, INVALID_CLIENT_ID);
    formParams.put(CLIENT_SECRET, INVALID_CLIENT_SECRET);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(401)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_CLIENT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_CLIENT_AUTH_FAILED));
  }

  @Test
  @DisplayName(
      "Client Credentials - Should return error in case of missing client credentials - request body auth")
  public void testMissingClientCredentialsRequestBodyAuth() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_AUTH));
  }

  @Test
  @DisplayName(
      "Client Credentials - Should return error in case client doesn't support client credentials grant type - basic auth")
  public void testClientDoesNotSupportClientCredentialsGrantTypeBasicAuth() {
    // Arrange
    cleanupClients(tenant1);
    Response clientResponse = createTestClientWithLimitedGrantTypes();
    String clientId = clientResponse.jsonPath().getString(CLIENT_ID);
    String clientSecret = clientResponse.jsonPath().getString(CLIENT_SECRET);

    String clientCredentials = clientId + CLIENT_CREDENTIALS_SEPARATOR + clientSecret;
    String authHeader =
        new String(Base64.getEncoder().encode(clientCredentials.getBytes(StandardCharsets.UTF_8)));

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, AUTH_BASIC_PREFIX + authHeader);
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR, equalTo(TOKEN_ERROR_UNAUTHORIZED_CLIENT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_UNAUTHORIZED_CLIENT));
  }

  @Test
  @DisplayName(
      "Client Credentials - Should return error in case client doesn't support client credentials grant type - request body auth")
  public void testClientDoesNotSupportClientCredentialsGrantType() {
    // Arrange
    cleanupClients(tenant1);
    Response clientResponse = createTestClientWithLimitedGrantTypes();
    String clientId = clientResponse.jsonPath().getString(CLIENT_ID);
    String clientSecret = clientResponse.jsonPath().getString(CLIENT_SECRET);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);
    formParams.put(CLIENT_ID, clientId);
    formParams.put(CLIENT_SECRET, clientSecret);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR, equalTo(TOKEN_ERROR_UNAUTHORIZED_CLIENT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_UNAUTHORIZED_CLIENT));
  }

  @Test
  @DisplayName(
      "Client Credentials - Should return error in case requested more scopes than allowed for client - basic auth")
  public void testClientDoesNotSupportRequestedScopesBasicAuth() {
    // Arrange
    String clientCredentials = validClientId + ":" + validClientSecret;
    ApplicationIoUtils.deleteClientScope(tenant1, validClientId, SCOPE_ADDRESS);
    String authHeader =
        new String(Base64.getEncoder().encode(clientCredentials.getBytes(StandardCharsets.UTF_8)));
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, AUTH_BASIC_PREFIX + authHeader);
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);
    formParams.put(
        TOKEN_PARAM_SCOPE,
        SCOPE_OPENID + SCOPE_SEPARATOR + SCOPE_EMAIL + SCOPE_SEPARATOR + SCOPE_ADDRESS);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_SCOPE))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_INVALID_SCOPE));
  }

  @Test
  @DisplayName(
      "Refresh Token - Should return access token successfully for valid refresh token - basic auth")
  public void testRefreshTokenBasicAuth() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            validClientId,
            userId,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);

    // Create WireMock stub with additional claims
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStubWithAdditionalClaims(email, phoneNumber);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    List<String> notExpectedScopes = List.of();
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        TEST_USER_ID,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        true,
        refreshToken);
    wireMockServer.removeStub(stubMapping);
  }

  @Test
  @DisplayName(
      "Refresh Token - Should return access token successfully for valid refresh token - request body auth")
  public void testRefreshTokenRequestBodyAuth() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            validClientId,
            userId,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(CLIENT_ID, validClientId);
    formParams.put(CLIENT_SECRET, validClientSecret);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    List<String> notExpectedScopes = List.of();
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        TEST_USER_ID,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        true,
        refreshToken);
  }

  @Test
  @DisplayName(
      "Refresh Token - Should return access token successfully for valid refresh token - basic auth with partial scopes")
  public void testRefreshTokenBasicAuthWithPartialScope() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            validClientId,
            userId,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + SCOPE_SEPARATOR + SCOPE_EMAIL);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL);
    List<String> notExpectedScopes = List.of(SCOPE_ADDRESS, SCOPE_PHONE);
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        TEST_USER_ID,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        true,
        refreshToken);
  }

  @Test
  @DisplayName(
      "Refresh Token - Should return access token successfully for valid refresh token - request body auth with partial scopes")
  public void testRefreshTokenRequestBodyAuthWithPartialScope() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            validClientId,
            userId,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(CLIENT_ID, validClientId);
    formParams.put(CLIENT_SECRET, validClientSecret);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + SCOPE_SEPARATOR + SCOPE_EMAIL);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL);
    List<String> notExpectedScopes = List.of(SCOPE_ADDRESS, SCOPE_PHONE);
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        TEST_USER_ID,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        true,
        refreshToken);
  }

  @Test
  @DisplayName(
      "Refresh Token - Should return error for valid refresh token with more scopes requested than present in refresh token")
  public void testRefreshTokenBasicAuthWithExcessScopes() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            validClientId,
            userId,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);
    formParams.put(
        TOKEN_PARAM_SCOPE,
        SCOPE_OPENID + SCOPE_SEPARATOR + SCOPE_EMAIL + SCOPE_SEPARATOR + SCOPE_ADDRESS);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(ERROR_INVALID_SCOPE))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_INVALID_SCOPE));
  }

  @Test
  @DisplayName("Refresh Token - Should return error for invalid refresh token")
  public void testRefreshTokenInvalid() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, INVALID_REFRESH_TOKEN);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_GRANT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_REFRESH_TOKEN_INVALID));
  }

  @Test
  @DisplayName("Refresh Token - Should return error for refresh token of different client")
  public void testRefreshTokenDifferentClient() {
    // Arrange
    Response client2Response = createTestClientWithRandomName();
    String clientId2 = client2Response.jsonPath().getString(CLIENT_ID);
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId2,
            userId,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);
    formParams.put(
        TOKEN_PARAM_SCOPE,
        SCOPE_OPENID + SCOPE_SEPARATOR + SCOPE_EMAIL + SCOPE_SEPARATOR + SCOPE_ADDRESS);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_GRANT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_REFRESH_TOKEN_INVALID));
  }

  @Test
  @DisplayName("Refresh Token - Should return error for expired refresh token")
  public void testRefreshTokenExpired() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            validClientId,
            userId,
            EXPIRED_TOKEN_OFFSET_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_GRANT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_REFRESH_TOKEN_EXPIRED));
  }

  @Test
  @DisplayName("Refresh Token - Should return error for inactive refresh token")
  public void testRefreshTokenInactive() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            validClientId,
            userId,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            false,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_GRANT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_REFRESH_TOKEN_INVALID));
  }

  @Test
  @DisplayName(
      "Authorization Code - Should return tokens for valid authorization code without code challenge")
  public void testAuthorizationCodeWithoutCodeChallenge() {
    // Arrange
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStub(email, phoneNumber);
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, validAuthCode);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_ID_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_REFRESH_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    String idToken = response.jsonPath().getString(TOKEN_PARAM_ID_TOKEN);
    String refreshToken = response.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);
    List<String> notExpectedScopes = List.of(SCOPE_ADDRESS);
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        TEST_USER_ID,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        true,
        refreshToken);
    List<String> expectedClaims =
        List.of(
            CLAIM_SUB,
            CLAIM_EMAIL,
            CLAIM_EMAIL_VERIFIED,
            CLAIM_PHONE_NUMBER,
            CLAIM_PHONE_NUMBER_VERIFIED);
    List<String> notExpectedClaims = List.of(CLAIM_ADDRESS);
    validateIdTokenClaims(idToken, TEST_USER_ID, validClientId, expectedClaims, notExpectedClaims);

    wireMockServer.removeStub(stubMapping);
  }

  @Test
  @DisplayName(
      "Authorization Code - Should return tokens for valid authorization code with code challenge plain")
  public void testAuthorizationCodeWithCodeChallengePlain() {
    // Arrange
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStub(email, phoneNumber);
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, validAuthCodeWithCodeChallengePlain);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);
    formParams.put(TOKEN_PARAM_CODE_VERIFIER, TEST_CODE_CHALLENGE);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_ID_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_REFRESH_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    String idToken = response.jsonPath().getString(TOKEN_PARAM_ID_TOKEN);
    String refreshToken = response.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);
    List<String> notExpectedScopes = List.of(SCOPE_ADDRESS);
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        TEST_USER_ID_2,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        true,
        refreshToken);
    List<String> expectedClaims =
        List.of(
            CLAIM_SUB,
            CLAIM_EMAIL,
            CLAIM_EMAIL_VERIFIED,
            CLAIM_PHONE_NUMBER,
            CLAIM_PHONE_NUMBER_VERIFIED);
    List<String> notExpectedClaims = List.of(CLAIM_ADDRESS);
    validateIdTokenClaims(
        idToken, TEST_USER_ID_2, validClientId, expectedClaims, notExpectedClaims);

    wireMockServer.removeStub(stubMapping);
  }

  @Test
  @DisplayName(
      "Authorization Code - Should return tokens for valid authorization code with code challenge S256")
  public void testAuthorizationCodeWithCodeChallengeS256() {
    // Arrange
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStub(email, phoneNumber);
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, validAuthCodeWithCodeChallengeS256);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);
    formParams.put(TOKEN_PARAM_CODE_VERIFIER, TEST_CODE_VERIFIER_2);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_ID_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_REFRESH_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    String idToken = response.jsonPath().getString(TOKEN_PARAM_ID_TOKEN);
    String refreshToken = response.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);
    List<String> notExpectedScopes = List.of(SCOPE_ADDRESS);
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        TEST_USER_ID_3,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        true,
        refreshToken);
    List<String> expectedClaims =
        List.of(
            CLAIM_SUB,
            CLAIM_EMAIL,
            CLAIM_EMAIL_VERIFIED,
            CLAIM_PHONE_NUMBER,
            CLAIM_PHONE_NUMBER_VERIFIED);
    List<String> notExpectedClaims = List.of(CLAIM_ADDRESS);
    validateIdTokenClaims(
        idToken, TEST_USER_ID_3, validClientId, expectedClaims, notExpectedClaims);

    wireMockServer.removeStub(stubMapping);
  }

  @Test
  @DisplayName("Authorization Code - Should return error for invalid authorization code")
  public void testAuthorizationCodeInvalid() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, INVALID_CODE);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_GRANT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_AUTHORIZATION_CODE_INVALID));
  }

  @Test
  @DisplayName("Authorization Code - Should return error for different client")
  public void testAuthorizationCodeDifferentClient() {
    // Arrange
    Response client2Response = createTestClientWithRandomName();
    String clientId2 = client2Response.jsonPath().getString(CLIENT_ID);
    String validClientSecret2 = client2Response.jsonPath().getString(CLIENT_SECRET);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(clientId2, validClientSecret2));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, validAuthCode);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_GRANT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_AUTHORIZATION_CODE_INVALID));
  }

  @Test
  @DisplayName("Authorization Code - Should return error for invalid redirect uri")
  public void testAuthorizationCodeInvalidRedirectUri() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, validAuthCode);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, CALLBACK_1);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_GRANT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_REDIRECT_URI_INVALID));
  }

  @Test
  @DisplayName(
      "Authorization Code - Should return error for invalid code verifier with code challenge plain")
  public void testAuthorizationCodeInvalidCodeVerifier() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, validAuthCodeWithCodeChallengePlain);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);
    formParams.put(TOKEN_PARAM_CODE_VERIFIER, TEST_CODE_VERIFIER_2);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_GRANT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_CODE_VERIFIER_INVALID));
  }

  @Test
  @DisplayName(
      "Authorization Code - Should return error for invalid code verifier with code challenge S256")
  public void testAuthorizationCodeInvalidCodeVerifierS256() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, validAuthCodeWithCodeChallengeS256);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);
    formParams.put(TOKEN_PARAM_CODE_VERIFIER, TEST_CODE_CHALLENGE);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_GRANT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_CODE_VERIFIER_INVALID));
  }

  @Test
  @DisplayName("Authorization Code - Should return error for missing code verifier")
  public void testAuthorizationCodeMissingCodeVerifier() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, validAuthCodeWithCodeChallengePlain);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_CODE_VERIFIER_REQUIRED));
  }

  @Test
  @DisplayName("Authorization Code - Should return error for missing authorization code")
  public void testAuthorizationCodeMissingAuthorizationCode() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_CODE_REQUIRED));
  }

  @Test
  @DisplayName("Authorization Code - Should return error for missing redirect uri")
  public void testAuthorizationCodeMissingRedirectUri() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, validAuthCode);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo(INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_REDIRECT_URI_REQUIRED));
  }

  @Test
  @DisplayName("Authorization Code - Should ignore scope passed in request body")
  public void testAuthorizationCodeIgnoresScopeInRequestBody() {
    // Arrange
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStub(email, phoneNumber);
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, validAuthCode);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);
    formParams.put(
        TOKEN_PARAM_SCOPE,
        SCOPE_OPENID + SCOPE_SEPARATOR + SCOPE_EMAIL); // Different scope than consented

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_ID_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_REFRESH_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Validate scope using assertThat
    validateScope(response, SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);

    // Verify that the tokens contain the originally consented scopes, not the requested ones
    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    String idToken = response.jsonPath().getString(TOKEN_PARAM_ID_TOKEN);
    String refreshToken = response.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);

    // Should contain the originally consented scopes (openid email phone), not the requested ones
    // (openid email)
    List<String> expectedScopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);
    List<String> notExpectedScopes = List.of(SCOPE_ADDRESS);
    validateAccessTokenClaims(
        accessToken,
        response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN),
        TEST_USER_ID,
        validClientId,
        expectedScopes,
        notExpectedScopes,
        true,
        refreshToken);

    List<String> expectedClaims =
        List.of(
            CLAIM_SUB,
            CLAIM_EMAIL,
            CLAIM_EMAIL_VERIFIED,
            CLAIM_PHONE_NUMBER,
            CLAIM_PHONE_NUMBER_VERIFIED);
    List<String> notExpectedClaims = List.of(CLAIM_ADDRESS);
    validateIdTokenClaims(idToken, TEST_USER_ID, validClientId, expectedClaims, notExpectedClaims);

    wireMockServer.removeStub(stubMapping);
  }

  @Test
  @DisplayName(
      "Authorization Code - Should include additional claims in Access Token when feature is enabled")
  public void testAuthorizationCodeWithAdditionalClaims() {
    // Arrange - Clean up tenant3 data and create required setup
    cleanupClients(tenant3);
    cleanupScopes(tenant3);
    cleanupOidcRefreshTokens(tenant3);

    OidcUtils.createRequiredScopes(tenant3);

    // Create client for tenant3
    Response clientResponse = createTestClientForTenant3();
    String tenant3ClientId = clientResponse.jsonPath().getString(CLIENT_ID);
    String tenant3ClientSecret = clientResponse.jsonPath().getString(CLIENT_SECRET);

    createClientScope(
        tenant3,
        tenant3ClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    // Create test data for tenant3
    String tenant3RefreshToken =
        insertRefreshToken(
            tenant3, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    List<String> scopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);
    Map<String, String> queryParams = createValidAuthorizeRequest(tenant3ClientId, scopes);

    Response authorizeResponse = authorize(tenant3, queryParams);
    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, tenant3RefreshToken);

    Response loginAcceptResponse = loginAccept(tenant3, loginAcceptBody);
    String consentChallenge = extractConsentChallenge(loginAcceptResponse);

    Map<String, Object> consentRequestBody = new HashMap<>();
    consentRequestBody.put(BODY_PARAM_CONSENT_CHALLENGE, consentChallenge);
    consentRequestBody.put(
        BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE));
    consentRequestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, tenant3RefreshToken);

    Response consentAcceptResponse = consentAccept(tenant3, consentRequestBody);
    String authCode = extractAuthCodeFromLocation(consentAcceptResponse.getHeader(HEADER_LOCATION));

    // Create WireMock stub with additional claims
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStubWithAdditionalClaims(email, phoneNumber);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(tenant3ClientId, tenant3ClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, authCode);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);

    // Act
    Response response = ApplicationIoUtils.token(tenant3, headers, formParams);

    // Assert
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_ID_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_REFRESH_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Verify additional claims are present in access token
    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Validate additional claims
    assertThat(claims.get(ADDITIONAL_CLAIM_ITEM1), equalTo(ADDITIONAL_CLAIM_VALUE1));
    assertThat(claims.get(ADDITIONAL_CLAIM_ITEM2), equalTo(ADDITIONAL_CLAIM_VALUE2));

    wireMockServer.removeStub(stubMapping);
  }

  @Test
  @DisplayName(
      "Refresh Token - Should include additional claims in Access Token when feature is enabled")
  public void testRefreshTokenWithAdditionalClaims() {
    // Arrange - Clean up tenant3 data and create required setup
    cleanupClients(tenant3);
    cleanupScopes(tenant3);
    cleanupOidcRefreshTokens(tenant3);

    OidcUtils.createRequiredScopes(tenant3);

    // Create client for tenant3
    Response clientResponse = createTestClientForTenant3();
    String tenant3ClientId = clientResponse.jsonPath().getString(CLIENT_ID);
    String tenant3ClientSecret = clientResponse.jsonPath().getString(CLIENT_SECRET);

    createClientScope(
        tenant3,
        tenant3ClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    // Create OIDC refresh token for tenant3
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            tenant3ClientId,
            userId,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    // Create WireMock stub with additional claims
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStubWithAdditionalClaims(email, phoneNumber);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(tenant3ClientId, tenant3ClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);

    // Act
    Response response = ApplicationIoUtils.token(tenant3, headers, formParams);

    // Assert
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Verify additional claims are present in access token
    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Validate additional claims
    assertThat(claims.get(ADDITIONAL_CLAIM_ITEM1), equalTo(ADDITIONAL_CLAIM_VALUE1));
    assertThat(claims.get(ADDITIONAL_CLAIM_ITEM2), equalTo(ADDITIONAL_CLAIM_VALUE2));

    wireMockServer.removeStub(stubMapping);
  }

  @Test
  @DisplayName("Authorization Code - Should handle missing additional claims field gracefully")
  public void testAuthorizationCodeMissingAdditionalClaims() {
    // Arrange - Clean up tenant3 data and create required setup
    cleanupClients(tenant3);
    cleanupScopes(tenant3);
    cleanupOidcRefreshTokens(tenant3);

    OidcUtils.createRequiredScopes(tenant3);

    // Create client for tenant3
    Response clientResponse = createTestClientForTenant3();
    String tenant3ClientId = clientResponse.jsonPath().getString(CLIENT_ID);
    String tenant3ClientSecret = clientResponse.jsonPath().getString(CLIENT_SECRET);

    createClientScope(
        tenant3,
        tenant3ClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    // Create test data for tenant3
    String tenant3RefreshToken =
        insertRefreshToken(
            tenant3, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    List<String> scopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);
    Map<String, String> queryParams = createValidAuthorizeRequest(tenant3ClientId, scopes);

    Response authorizeResponse = authorize(tenant3, queryParams);
    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, tenant3RefreshToken);

    Response loginAcceptResponse = loginAccept(tenant3, loginAcceptBody);
    String consentChallenge = extractConsentChallenge(loginAcceptResponse);

    Map<String, Object> consentRequestBody = new HashMap<>();
    consentRequestBody.put(BODY_PARAM_CONSENT_CHALLENGE, consentChallenge);
    consentRequestBody.put(
        BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE));
    consentRequestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, tenant3RefreshToken);

    Response consentAcceptResponse = consentAccept(tenant3, consentRequestBody);
    String authCode = extractAuthCodeFromLocation(consentAcceptResponse.getHeader(HEADER_LOCATION));

    // Create WireMock stub WITHOUT additional claims
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStubWithoutAdditionalClaims(email, phoneNumber);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(tenant3ClientId, tenant3ClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, authCode);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);

    // Act
    Response response = ApplicationIoUtils.token(tenant3, headers, formParams);

    // Assert
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Verify NO additional claims are present in access token
    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Should not have additional claims
    assertThat(claims.containsKey(ADDITIONAL_CLAIM_ITEM1), equalTo(false));
    assertThat(claims.containsKey(ADDITIONAL_CLAIM_ITEM2), equalTo(false));

    // Standard claims should still be present
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(MOCK_USER_ID));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));

    wireMockServer.removeStub(stubMapping);
  }

  @Test
  @DisplayName("Authorization Code - Should handle empty additional claims gracefully")
  public void testAuthorizationCodeEmptyAdditionalClaims() {
    // Arrange - Clean up tenant3 data and create required setup
    cleanupClients(tenant3);
    cleanupScopes(tenant3);
    cleanupOidcRefreshTokens(tenant3);

    OidcUtils.createRequiredScopes(tenant3);

    // Create client for tenant3
    Response clientResponse = createTestClientForTenant3();
    String tenant3ClientId = clientResponse.jsonPath().getString(CLIENT_ID);
    String tenant3ClientSecret = clientResponse.jsonPath().getString(CLIENT_SECRET);

    createClientScope(
        tenant3,
        tenant3ClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    // Create test data for tenant3
    String tenant3RefreshToken =
        insertRefreshToken(
            tenant3, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    List<String> scopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);
    Map<String, String> queryParams = createValidAuthorizeRequest(tenant3ClientId, scopes);

    Response authorizeResponse = authorize(tenant3, queryParams);
    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, tenant3RefreshToken);

    Response loginAcceptResponse = loginAccept(tenant3, loginAcceptBody);
    String consentChallenge = extractConsentChallenge(loginAcceptResponse);

    Map<String, Object> consentRequestBody = new HashMap<>();
    consentRequestBody.put(BODY_PARAM_CONSENT_CHALLENGE, consentChallenge);
    consentRequestBody.put(
        BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE));
    consentRequestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, tenant3RefreshToken);

    Response consentAcceptResponse = consentAccept(tenant3, consentRequestBody);
    String authCode = extractAuthCodeFromLocation(consentAcceptResponse.getHeader(HEADER_LOCATION));

    // Create WireMock stub with EMPTY additional claims
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStubWithPartialAdditionalClaims(email, phoneNumber);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(tenant3ClientId, tenant3ClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, AUTHORIZATION_CODE);
    formParams.put(TOKEN_PARAM_CODE, authCode);
    formParams.put(TOKEN_PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);

    // Act
    Response response = ApplicationIoUtils.token(tenant3, headers, formParams);

    // Assert
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Verify NO additional claims are present in access token
    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Should not have additional claims since they were empty
    assertThat(claims.containsKey(ADDITIONAL_CLAIM_ITEM1), equalTo(true));
    assertThat(claims.get(ADDITIONAL_CLAIM_ITEM1), equalTo(ADDITIONAL_CLAIM_VALUE1));
    assertThat(claims.containsKey(ADDITIONAL_CLAIM_ITEM2), equalTo(false));

    // Standard claims should still be present
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(MOCK_USER_ID));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));

    wireMockServer.removeStub(stubMapping);
  }

  @Test
  @DisplayName("Refresh Token - Should handle missing additional claims field gracefully")
  public void testRefreshTokenMissingAdditionalClaims() {
    // Arrange - Clean up tenant3 data and create required setup
    cleanupClients(tenant3);
    cleanupScopes(tenant3);
    cleanupOidcRefreshTokens(tenant3);

    OidcUtils.createRequiredScopes(tenant3);

    // Create client for tenant3
    Response clientResponse = createTestClientForTenant3();
    String tenant3ClientId = clientResponse.jsonPath().getString(CLIENT_ID);
    String tenant3ClientSecret = clientResponse.jsonPath().getString(CLIENT_SECRET);

    createClientScope(
        tenant3,
        tenant3ClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    // Create OIDC refresh token for tenant3
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            tenant3ClientId,
            userId,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    // Create WireMock stub WITHOUT additional claims
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStubWithoutAdditionalClaims(email, phoneNumber);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(tenant3ClientId, tenant3ClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);

    // Act
    Response response = ApplicationIoUtils.token(tenant3, headers, formParams);

    // Assert
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Verify NO additional claims are present in access token
    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Should not have additional claims
    assertThat(claims.containsKey(ADDITIONAL_CLAIM_ITEM1), equalTo(false));
    assertThat(claims.containsKey(ADDITIONAL_CLAIM_ITEM2), equalTo(false));

    // Standard claims should still be present
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(MOCK_USER_ID));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));

    wireMockServer.removeStub(stubMapping);
  }

  @Test
  @DisplayName("Refresh Token - Should handle empty additional claims gracefully")
  public void testRefreshTokenEmptyAdditionalClaims() {
    // Arrange - Clean up tenant3 data and create required setup
    cleanupClients(tenant3);
    cleanupScopes(tenant3);
    cleanupOidcRefreshTokens(tenant3);

    OidcUtils.createRequiredScopes(tenant3);

    // Create client for tenant3
    Response clientResponse = createTestClientForTenant3();
    String tenant3ClientId = clientResponse.jsonPath().getString(CLIENT_ID);
    String tenant3ClientSecret = clientResponse.jsonPath().getString(CLIENT_SECRET);

    createClientScope(
        tenant3,
        tenant3ClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    // Create OIDC refresh token for tenant3
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE);
    String userId = TEST_USER_ID;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            tenant3ClientId,
            userId,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    // Create WireMock stub with EMPTY additional claims
    String email = generateRandomEmail();
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping stubMapping = getOidcUserStubWithPartialAdditionalClaims(email, phoneNumber);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(tenant3ClientId, tenant3ClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);

    // Act
    Response response = ApplicationIoUtils.token(tenant3, headers, formParams);

    // Assert
    response
        .then()
        .statusCode(200)
        .header(HEADER_CACHE_CONTROL, equalTo(CACHE_CONTROL_NO_STORE))
        .header(HEADER_PRAGMA, equalTo(PRAGMA_NO_CACHE))
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    // Verify NO additional claims are present in access token
    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Should not have additional claims since they were empty
    assertThat(claims.containsKey(ADDITIONAL_CLAIM_ITEM1), equalTo(true));
    assertThat(claims.get(ADDITIONAL_CLAIM_ITEM1), equalTo(ADDITIONAL_CLAIM_VALUE1));
    assertThat(claims.containsKey(ADDITIONAL_CLAIM_ITEM2), equalTo(false));

    // Standard claims should still be present
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(MOCK_USER_ID));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));

    wireMockServer.removeStub(stubMapping);
  }

  private Response createTestClientForTenant3() {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    requestBody.put(
        GRANT_TYPES, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
    return createClient(tenant3, requestBody);
  }

  private StubMapping getOidcUserStubWithAdditionalClaims(String email, String phoneNumber) {
    JsonNode jsonNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_NAME, MOCK_USER_NAME)
            .put(BODY_PARAM_EMAIL, email)
            .put(BODY_PARAM_USERID, MOCK_USER_ID)
            .put(BODY_PARAM_USERNAME, MOCK_USERNAME)
            .put(JSON_PHONE_NUMBER, phoneNumber)
            .put(JSON_PHONE_NUMBER_VERIFIED, phoneNumber)
            .put(JSON_EMAIL_VERIFIED, email)
            .put(ADDITIONAL_CLAIM_ITEM1, ADDITIONAL_CLAIM_VALUE1)
            .put(
                ADDITIONAL_CLAIM_ITEM2,
                ADDITIONAL_CLAIM_VALUE2); // Key field for OIDC additional claims

    return wireMockServer.stubFor(
        get(urlPathMatching(WIREMOCK_USER_ENDPOINT))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                    .withJsonBody(jsonNode)));
  }

  private StubMapping getOidcUserStubWithoutAdditionalClaims(String email, String phoneNumber) {
    JsonNode jsonNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_NAME, MOCK_USER_NAME)
            .put(BODY_PARAM_EMAIL, email)
            .put(BODY_PARAM_USERID, MOCK_USER_ID)
            .put(BODY_PARAM_USERNAME, MOCK_USERNAME)
            .put(JSON_PHONE_NUMBER, phoneNumber)
            .put(JSON_PHONE_NUMBER_VERIFIED, phoneNumber)
            .put(JSON_EMAIL_VERIFIED, email);
    // Note: no additional_claims field at all

    return wireMockServer.stubFor(
        get(urlPathMatching(WIREMOCK_USER_ENDPOINT))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                    .withJsonBody(jsonNode)));
  }

  private StubMapping getOidcUserStubWithPartialAdditionalClaims(String email, String phoneNumber) {

    JsonNode jsonNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_NAME, MOCK_USER_NAME)
            .put(BODY_PARAM_EMAIL, email)
            .put(BODY_PARAM_USERID, MOCK_USER_ID)
            .put(BODY_PARAM_USERNAME, MOCK_USERNAME)
            .put(JSON_PHONE_NUMBER, phoneNumber)
            .put(JSON_PHONE_NUMBER_VERIFIED, phoneNumber)
            .put(JSON_EMAIL_VERIFIED, email)
            .put(ADDITIONAL_CLAIM_ITEM1, ADDITIONAL_CLAIM_VALUE1);

    return wireMockServer.stubFor(
        get(urlPathMatching(WIREMOCK_USER_ENDPOINT))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                    .withJsonBody(jsonNode)));
  }
}
