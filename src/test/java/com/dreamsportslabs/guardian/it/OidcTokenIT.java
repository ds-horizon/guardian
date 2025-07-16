package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.ACCESS_TOKEN_EXPIRY_SECONDS;
import static com.dreamsportslabs.guardian.Constants.AUTHORIZATION_CODE;
import static com.dreamsportslabs.guardian.Constants.AUTH_BASIC_PREFIX;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONSENTED_SCOPES;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONSENT_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_OIDC;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERNAME;
import static com.dreamsportslabs.guardian.Constants.CLAIM_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.CLAIM_EMAIL_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.CLIENT_CREDENTIALS;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NAME;
import static com.dreamsportslabs.guardian.Constants.CLIENT_SECRET;
import static com.dreamsportslabs.guardian.Constants.CONTENT_TYPE_FORM_URLENCODED;
import static com.dreamsportslabs.guardian.Constants.DEVICE_VALUE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_CALLBACK;
import static com.dreamsportslabs.guardian.Constants.EXPIRED_TOKEN_OFFSET_SECONDS;
import static com.dreamsportslabs.guardian.Constants.GRANT_TYPES;
import static com.dreamsportslabs.guardian.Constants.HEADER_AUTHORIZATION;
import static com.dreamsportslabs.guardian.Constants.HEADER_CONTENT_TYPE;
import static com.dreamsportslabs.guardian.Constants.HEADER_LOCATION;
import static com.dreamsportslabs.guardian.Constants.HEADER_WWW_AUTHENTICATE;
import static com.dreamsportslabs.guardian.Constants.INVALID_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.INVALID_CLIENT_SECRET;
import static com.dreamsportslabs.guardian.Constants.INVALID_GRANT_TYPE;
import static com.dreamsportslabs.guardian.Constants.INVALID_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.IP_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.JWT_ALGORITHM_RS256;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_EXP;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_IAT;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_ISS;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_JTI;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.JWT_TYPE_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.Constants.LOCATION_VALUE;
import static com.dreamsportslabs.guardian.Constants.REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.REFRESH_TOKEN_EXPIRY_SECONDS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.SOURCE_VALUE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TEST_DEVICE_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_IP_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.TEST_ISSUER;
import static com.dreamsportslabs.guardian.Constants.TEST_KID;
import static com.dreamsportslabs.guardian.Constants.TEST_PUBLIC_KEY_PATH;
import static com.dreamsportslabs.guardian.Constants.TEST_USER_ID;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_INVALID_CLIENT;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_INVALID_GRANT;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_INVALID_SCOPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_CLIENT_AUTH_FAILED;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_INVALID_SCOPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_REFRESH_TOKEN_EXPIRED;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_REFRESH_TOKEN_INACTIVE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_REFRESH_TOKEN_INVALID;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_UNAUTHORIZED_CLIENT;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_UNSUPPORTED_GRANT_TYPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_UNAUTHORIZED_CLIENT;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_UNSUPPORTED_GRANT_TYPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_CODE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_EXPIRES_IN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_GRANT_TYPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_ID_TOKEN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_REDIRECT_URI;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_TOKEN_TYPE;
import static com.dreamsportslabs.guardian.Constants.TOKEN_TYPE_BEARER;
import static com.dreamsportslabs.guardian.Constants.WWW_AUTHENTICATE_BASIC_REALM_FORMAT;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_EMAIL;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.authorize;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.consentAccept;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.ClientUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.time.ZonedDateTime;
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

  private static final ObjectMapper objectMapper = new ObjectMapper();
  public static String tenant1 = TENANT_1;
  public static String tenant2 = TENANT_2;

  private String validClientId;
  private String validClientSecret;
  private String validAuthCode;
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

    createRequiredScopes(tenant1);
    createRequiredScopes(tenant2);

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
            .put(BODY_PARAM_NAME, "John Doe")
            .put(BODY_PARAM_EMAIL, email)
            .put(BODY_PARAM_USERID, "testuser")
            .put(BODY_PARAM_USERNAME, "testuser")
            .put("phone_number", phoneNumber)
            .put("phone_number_verified", phoneNumber)
            .put("email_verified", email);
    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .withJsonBody(jsonNode)));
  }

  private String generateRandomEmail() {
    return RandomStringUtils.randomAlphanumeric(10) + "@example.com";
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

  private void createRequiredScopes(String tenantId) {
    Map<String, Object> openidScope = new HashMap<>();
    openidScope.put(BODY_PARAM_SCOPE, SCOPE_OPENID);
    openidScope.put(BODY_PARAM_DISPLAY_NAME, "OpenID Connect");
    openidScope.put(BODY_PARAM_DESCRIPTION, "OpenID Connect scope");
    openidScope.put(BODY_PARAM_CLAIMS, Arrays.asList(CLAIM_SUB));
    openidScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, openidScope);

    Map<String, Object> emailScope = new HashMap<>();
    emailScope.put(BODY_PARAM_SCOPE, SCOPE_EMAIL);
    emailScope.put(BODY_PARAM_DISPLAY_NAME, "Email");
    emailScope.put(BODY_PARAM_DESCRIPTION, "Email scope");
    emailScope.put(BODY_PARAM_CLAIMS, Arrays.asList(CLAIM_EMAIL, CLAIM_EMAIL_VERIFIED));
    emailScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, emailScope);

    Map<String, Object> addressScope = new HashMap<>();
    addressScope.put(BODY_PARAM_SCOPE, SCOPE_ADDRESS);
    addressScope.put(BODY_PARAM_DISPLAY_NAME, "Address");
    addressScope.put(BODY_PARAM_DESCRIPTION, "Address scope");
    addressScope.put(BODY_PARAM_CLAIMS, Arrays.asList(CLAIM_ADDRESS));
    addressScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, addressScope);

    Map<String, Object> phoneScope = new HashMap<>();
    phoneScope.put(BODY_PARAM_SCOPE, SCOPE_PHONE);
    phoneScope.put(BODY_PARAM_DISPLAY_NAME, "Phone");
    phoneScope.put(BODY_PARAM_DESCRIPTION, "Phone scope");
    phoneScope.put(
        BODY_PARAM_CLAIMS, Arrays.asList(CLAIM_PHONE_NUMBER, CLAIM_PHONE_NUMBER_VERIFIED));
    phoneScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, phoneScope);
  }

  private void createTestData() {
    String validRefreshToken;
    String validConsentChallenge;
    validRefreshToken =
        insertRefreshToken(
            tenant1, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    List<String> scopes = List.of(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);

    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId, scopes);

    Response authorizeResponse = authorize(tenant1, queryParams);

    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response loginAcceptResponse = loginAccept(tenant1, loginAcceptBody);

    validConsentChallenge = extractConsentChallenge(loginAcceptResponse);

    Map<String, Object> consentRequestBody = new HashMap<>();
    consentRequestBody.put(BODY_PARAM_CONSENT_CHALLENGE, validConsentChallenge);
    consentRequestBody.put(
        BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE));
    consentRequestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response consentAcceptResponse = consentAccept(tenant1, consentRequestBody);
    validAuthCode = extractAuthCodeFromLocation(consentAcceptResponse.getHeader(HEADER_LOCATION));
  }

  private String getBasicAuthHeader(String clientId, String clientSecret) {
    String clientCredentials = clientId + ":" + clientSecret;
    String authHeader =
        new String(Base64.getEncoder().encode(clientCredentials.getBytes(StandardCharsets.UTF_8)));
    return AUTH_BASIC_PREFIX + authHeader;
  }

  @Test
  @DisplayName("Should return error in case of missing grant_type")
  public void testMissingGrantType() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response.then().statusCode(SC_BAD_REQUEST);
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
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);

    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    String encodeHeaderClaims = accessToken.split("\\.")[0];
    String decodedHeader =
        new String(Base64.getDecoder().decode(encodeHeaderClaims), StandardCharsets.UTF_8);
    Map<String, Object> headerClaims = new HashMap<>();
    try {
      headerClaims = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
    } catch (JsonProcessingException ignored) {
    }
    assertThat(headerClaims.get("alg"), equalTo(JWT_ALGORITHM_RS256));
    assertThat(headerClaims.get("typ"), equalTo(JWT_TYPE_ACCESS_TOKEN));
    assertThat(headerClaims.get("kid"), equalTo(TEST_KID));

    assertThat(claims.get("aud"), equalTo(validClientId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));
    assertThat(response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN), equalTo(exp - iat));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(CLAIM_SUB), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_OPENID));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_EMAIL));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_ADDRESS));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_PHONE));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_JTI), isA(String.class));
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
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);

    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    String encodeHeaderClaims = accessToken.split("\\.")[0];
    String decodedHeader =
        new String(Base64.getDecoder().decode(encodeHeaderClaims), StandardCharsets.UTF_8);
    Map<String, Object> headerClaims = new HashMap<>();
    try {
      headerClaims = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
    } catch (JsonProcessingException ignored) {
    }

    assertThat(headerClaims.get("alg"), equalTo(JWT_ALGORITHM_RS256));
    assertThat(headerClaims.get("typ"), equalTo(JWT_TYPE_ACCESS_TOKEN));
    assertThat(headerClaims.get("kid"), equalTo(TEST_KID));

    assertThat(claims.get("aud"), equalTo(validClientId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));
    assertThat(response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN), equalTo(exp - iat));

    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(CLAIM_SUB), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_OPENID));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_EMAIL));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_ADDRESS));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_PHONE));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_JTI), isA(String.class));
  }

  @Test
  @DisplayName(
      "Should return token successfully for client credentials grant type with basic auth and custom scopes")
  public void testClientCredentialsBasicAuthAndCustomScopes() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, CLIENT_CREDENTIALS);
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);

    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    String encodeHeaderClaims = accessToken.split("\\.")[0];
    String decodedHeader =
        new String(Base64.getDecoder().decode(encodeHeaderClaims), StandardCharsets.UTF_8);
    Map<String, Object> headerClaims = new HashMap<>();
    try {
      headerClaims = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
    } catch (JsonProcessingException ignored) {
    }

    assertThat(headerClaims.get("alg"), equalTo(JWT_ALGORITHM_RS256));
    assertThat(headerClaims.get("typ"), equalTo(JWT_TYPE_ACCESS_TOKEN));
    assertThat(headerClaims.get("kid"), equalTo(TEST_KID));

    assertThat(claims.get("aud"), equalTo(validClientId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));

    assertThat(response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN), equalTo(exp - iat));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(CLAIM_SUB), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_OPENID));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_EMAIL));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), not(containsString(SCOPE_ADDRESS)));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), not(containsString(SCOPE_PHONE)));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_JTI), isA(String.class));
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
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);

    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    String encodeHeaderClaims = accessToken.split("\\.")[0];
    String decodedHeader =
        new String(Base64.getDecoder().decode(encodeHeaderClaims), StandardCharsets.UTF_8);
    Map<String, Object> headerClaims = new HashMap<>();
    try {
      headerClaims = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
    } catch (JsonProcessingException ignored) {
    }

    assertThat(headerClaims.get("alg"), equalTo(JWT_ALGORITHM_RS256));
    assertThat(headerClaims.get("typ"), equalTo(JWT_TYPE_ACCESS_TOKEN));
    assertThat(headerClaims.get("kid"), equalTo(TEST_KID));

    assertThat(claims.get("aud"), equalTo(validClientId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));
    assertThat(response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN), equalTo(exp - iat));

    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(CLAIM_SUB), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_OPENID));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_EMAIL));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), not(containsString(SCOPE_ADDRESS)));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), not(containsString(SCOPE_PHONE)));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_JTI), isA(String.class));
  }

  @Test
  @DisplayName(
      "Client Credentials - Should return error in case of invalid client credentials - basic auth")
  public void testInvalidClientCredentialsBasicAuth() {
    // Arrange
    String invalidClientCredentials = INVALID_CLIENT_ID + ":" + INVALID_CLIENT_SECRET;
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
        .statusCode(401)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_CLIENT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_CLIENT_AUTH_FAILED));
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

    String clientCredentials = clientId + ":" + clientSecret;
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
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL + " " + SCOPE_ADDRESS);

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

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();
    String encodeHeaderClaims = accessToken.split("\\.")[0];
    String decodedHeader =
        new String(Base64.getDecoder().decode(encodeHeaderClaims), StandardCharsets.UTF_8);
    Map<String, Object> headerClaims = new HashMap<>();
    try {
      headerClaims = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
    } catch (JsonProcessingException ignored) {
    }
    assertThat(headerClaims.get("alg"), equalTo(JWT_ALGORITHM_RS256));
    assertThat(headerClaims.get("typ"), equalTo(JWT_TYPE_ACCESS_TOKEN));
    assertThat(headerClaims.get("kid"), equalTo(TEST_KID));

    assertThat(claims.get("aud"), equalTo(validClientId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));
    assertThat(response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN), equalTo(exp - iat));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(CLAIM_SUB), equalTo(userId));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_OPENID));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_EMAIL));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_ADDRESS));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_PHONE));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_JTI), isA(String.class));
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
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();
    String encodeHeaderClaims = accessToken.split("\\.")[0];
    String decodedHeader =
        new String(Base64.getDecoder().decode(encodeHeaderClaims), StandardCharsets.UTF_8);
    Map<String, Object> headerClaims = new HashMap<>();
    try {
      headerClaims = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
    } catch (JsonProcessingException ignored) {
    }
    assertThat(headerClaims.get("alg"), equalTo(JWT_ALGORITHM_RS256));
    assertThat(headerClaims.get("typ"), equalTo(JWT_TYPE_ACCESS_TOKEN));
    assertThat(headerClaims.get("kid"), equalTo(TEST_KID));

    assertThat(claims.get("aud"), equalTo(validClientId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));
    assertThat(response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN), equalTo(exp - iat));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(CLAIM_SUB), equalTo(userId));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_OPENID));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_EMAIL));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_ADDRESS));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_PHONE));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_JTI), isA(String.class));
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
    headers.put("Authorization", getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();
    String encodeHeaderClaims = accessToken.split("\\.")[0];
    String decodedHeader =
        new String(Base64.getDecoder().decode(encodeHeaderClaims), StandardCharsets.UTF_8);
    Map<String, Object> headerClaims = new HashMap<>();
    try {
      headerClaims = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
    } catch (JsonProcessingException ignored) {
    }
    assertThat(headerClaims.get("alg"), equalTo(JWT_ALGORITHM_RS256));
    assertThat(headerClaims.get("typ"), equalTo(JWT_TYPE_ACCESS_TOKEN));
    assertThat(headerClaims.get("kid"), equalTo(TEST_KID));

    assertThat(claims.get("aud"), equalTo(validClientId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));
    assertThat(response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN), equalTo(exp - iat));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(CLAIM_SUB), equalTo(userId));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_OPENID));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_EMAIL));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), not(containsString(SCOPE_ADDRESS)));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), not(containsString(SCOPE_PHONE)));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_JTI), isA(String.class));
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
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(200)
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

    String accessToken = response.jsonPath().getString(TOKEN_PARAM_ACCESS_TOKEN);
    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();
    String encodeHeaderClaims = accessToken.split("\\.")[0];
    String decodedHeader =
        new String(Base64.getDecoder().decode(encodeHeaderClaims), StandardCharsets.UTF_8);
    Map<String, Object> headerClaims = new HashMap<>();
    try {
      headerClaims = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
    } catch (JsonProcessingException ignored) {
    }
    assertThat(headerClaims.get("alg"), equalTo(JWT_ALGORITHM_RS256));
    assertThat(headerClaims.get("typ"), equalTo(JWT_TYPE_ACCESS_TOKEN));
    assertThat(headerClaims.get("kid"), equalTo(TEST_KID));

    assertThat(claims.get("aud"), equalTo(validClientId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));
    assertThat(response.jsonPath().getLong(TOKEN_PARAM_EXPIRES_IN), equalTo(exp - iat));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(CLAIM_SUB), equalTo(userId));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_OPENID));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(SCOPE_EMAIL));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), not(containsString(SCOPE_ADDRESS)));
    assertThat((String) claims.get(JWT_CLAIM_SCOPE), not(containsString(SCOPE_PHONE)));
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(validClientId));
    assertThat((String) claims.get(JWT_CLAIM_JTI), isA(String.class));
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
    headers.put("Authorization", getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL + " " + SCOPE_ADDRESS);

    // Act
    Response response = ApplicationIoUtils.token(tenant1, headers, formParams);

    // Validate
    response
        .then()
        .statusCode(400)
        .body(ERROR, equalTo("invalid_scope"))
        .body(
            ERROR_DESCRIPTION,
            equalTo(
                "The requested scope is invalid, unknown, malformed, or exceeds the scope granted by the resource owner"));
  }

  @Test
  @DisplayName("Refresh Token - Should return error for invalid refresh token")
  public void testRefreshTokenInvalid() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", getBasicAuthHeader(validClientId, validClientSecret));
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
    headers.put("Authorization", getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN_PARAM_GRANT_TYPE, REFRESH_TOKEN);
    formParams.put(TOKEN_PARAM_REFRESH_TOKEN, refreshToken);
    formParams.put(TOKEN_PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL + " " + SCOPE_ADDRESS);

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
    headers.put("Authorization", getBasicAuthHeader(validClientId, validClientSecret));
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
    headers.put("Authorization", getBasicAuthHeader(validClientId, validClientSecret));
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
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_REFRESH_TOKEN_INACTIVE));
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
    headers.put("Authorization", getBasicAuthHeader(validClientId, validClientSecret));
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
        .body(TOKEN_PARAM_ACCESS_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_ID_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_REFRESH_TOKEN, isA(String.class))
        .body(TOKEN_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER));

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
}
