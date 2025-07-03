package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.*;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.generateRsaKey;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class RsaKeyGenerationIT {

  private JsonObject extractResponseBody(Response response) {
    return new JsonObject(response.getBody().asString());
  }

  @Test
  @DisplayName("Should generate RSA key with defaults successfully")
  void generateRsaKeyWithDefaults() throws Exception {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();

    // Act
    Response response = generateRsaKey(requestBody);

    // Assert
    response
        .then()
        .statusCode(200)
        .body(RSA_KEY_KID, isA(String.class))
        .body(RSA_KEY_PUBLIC_KEY, isA(String.class))
        .body(RSA_KEY_PRIVATE_KEY, isA(String.class))
        .body(RSA_KEY_SIZE, equalTo(RSA_KEY_SIZE_2048));
    assertThat(response.getStatusCode(), equalTo(200));

    JsonObject responseBody = extractResponseBody(response);

    // Verify PEM format (default)
    String publicKey = responseBody.getString(RSA_KEY_PUBLIC_KEY);
    String privateKey = responseBody.getString(RSA_KEY_PRIVATE_KEY);
    String kid = responseBody.getString(RSA_KEY_KID);

    assertThat(publicKey, containsString(PEM_PUBLIC_KEY_HEADER));
    assertThat(publicKey, containsString(PEM_PUBLIC_KEY_FOOTER));
    assertThat(privateKey, containsString(PEM_PRIVATE_KEY_HEADER));
    assertThat(privateKey, containsString(PEM_PRIVATE_KEY_FOOTER));

    // Verify keys can be parsed as valid RSA keys
    RSAPublicKey publicKeyObj = parsePublicKey(publicKey);
    RSAPrivateKey privateKeyObj = parsePrivateKey(privateKey);

    // Verify key properties
    assertThat(
        ASSERT_PUBLIC_KEY_MODULUS_2048,
        publicKeyObj.getModulus().bitLength(),
        equalTo(RSA_KEY_SIZE_2048));
    assertThat(
        ASSERT_PRIVATE_KEY_MODULUS_2048,
        privateKeyObj.getModulus().bitLength(),
        equalTo(RSA_KEY_SIZE_2048));

    // Verify public and private keys are mathematically related
    assertThat(
        ASSERT_KEYS_SAME_MODULUS, publicKeyObj.getModulus(), equalTo(privateKeyObj.getModulus()));

    // Verify public exponent is standard (65537)
    assertThat(
        ASSERT_PUBLIC_EXPONENT_65537,
        publicKeyObj.getPublicExponent().intValue(),
        equalTo(RSA_PUBLIC_EXPONENT));

    assertThat(kid, notNullValue());
    assertThat(kid.length(), greaterThan(0));

    // Should be a reasonable length for a key identifier
    assertThat(kid.length(), greaterThan(RSA_KID_MIN_LENGTH));
  }

  @Test
  @DisplayName("Should generate RSA key with 4096-bit key size")
  void generateRsaKeyWith4096BitSize() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(RSA_KEY_SIZE, RSA_KEY_SIZE_4096);
    requestBody.put(RSA_KEY_FORMAT, RSA_FORMAT_PEM);

    // Act
    Response response = generateRsaKey(requestBody);

    // Assert
    response.then().statusCode(200).body(RSA_KEY_SIZE, equalTo(RSA_KEY_SIZE_4096));

    JsonObject responseBody = extractResponseBody(response);

    // 4096-bit keys should be longer than 2048-bit keys
    String privateKey = responseBody.getString(RSA_KEY_PRIVATE_KEY);
    assertThat(privateKey.length(), greaterThan(RSA_4096_MIN_LENGTH));
  }

  @Test
  @DisplayName("Should generate RSA key with 3072-bit key size")
  void generateRsaKeyWith3072BitSize() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(RSA_KEY_SIZE, RSA_KEY_SIZE_3072);
    requestBody.put(RSA_KEY_FORMAT, RSA_FORMAT_PEM);

    // Act
    Response response = generateRsaKey(requestBody);

    // Assert
    assertThat(response.getStatusCode(), equalTo(200));

    JsonObject responseBody = extractResponseBody(response);
    assertThat(responseBody.getInteger(RSA_KEY_SIZE), equalTo(RSA_KEY_SIZE_3072));
  }

  @Test
  @DisplayName("Should generate RSA key in JWKS format")
  void generateRsaKeyInJwksFormat() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(RSA_KEY_FORMAT, RSA_FORMAT_JWKS);

    // Act
    Response response = generateRsaKey(requestBody);

    // Assert
    response.then().statusCode(200);

    JsonObject responseBody = extractResponseBody(response);

    // In JWKS format, keys should be JSON objects, not strings
    assertThat(responseBody.getValue(RSA_KEY_PUBLIC_KEY), instanceOf(JsonObject.class));
    assertThat(responseBody.getValue(RSA_KEY_PRIVATE_KEY), instanceOf(JsonObject.class));

    // Verify JWKS structure for public key
    JsonObject publicKey = responseBody.getJsonObject(RSA_KEY_PUBLIC_KEY);
    assertThat(publicKey.getString(RSA_KEY_TYPE), equalTo(RSA_KEY_TYPE_RSA));
    assertThat(publicKey.getString(RSA_KEY_USE), equalTo(RSA_KEY_USE_SIG));
    assertThat(publicKey.getString(RSA_KEY_MODULUS), notNullValue());
    assertThat(publicKey.getString(RSA_KEY_EXPONENT), equalTo(RSA_KEY_EXPONENT_AQAB));
  }

  @Test
  @DisplayName("Should generate different keys on multiple calls")
  void generateMultipleRsaKeysAreDifferent() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();

    // Act
    Response response1 = generateRsaKey(requestBody);
    Response response2 = generateRsaKey(requestBody);

    // Assert
    assertThat(response1.getStatusCode(), equalTo(200));
    assertThat(response2.getStatusCode(), equalTo(200));

    JsonObject responseBody1 = extractResponseBody(response1);
    JsonObject responseBody2 = extractResponseBody(response2);

    // Keys should be different
    assertThat(
        responseBody1.getString(RSA_KEY_KID),
        is(not(equalTo(responseBody2.getString(RSA_KEY_KID)))));
    assertThat(
        responseBody1.getString(RSA_KEY_PUBLIC_KEY),
        is(not(equalTo(responseBody2.getString(RSA_KEY_PUBLIC_KEY)))));
    assertThat(
        responseBody1.getString(RSA_KEY_PRIVATE_KEY),
        is(not(equalTo(responseBody2.getString(RSA_KEY_PRIVATE_KEY)))));
  }

  @Test
  @DisplayName("Should return error for invalid key size")
  void generateRsaKeyWithInvalidKeySize() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(RSA_KEY_SIZE, RSA_KEY_SIZE_INVALID); // Invalid key size

    // Act
    Response response = generateRsaKey(requestBody);

    // Assert
    response
        .then()
        .statusCode(400)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_INVALID_RSA_KEY_LENGTH));
  }

  @Test
  @DisplayName("Should return error for invalid format")
  void generateRsaKeyWithInvalidFormat() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(RSA_KEY_FORMAT, RSA_FORMAT_INVALID); // Invalid format

    // Act
    Response response = generateRsaKey(requestBody);

    // Assert
    response
        .then()
        .statusCode(400)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_INVALID_KEY_FORMAT));
  }

  @Test
  @DisplayName("Should return error for missing format")
  void generateRsaKeyWithMissingFormat() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(RSA_KEY_FORMAT, RSA_FORMAT_EMPTY); // Empty format

    // Act
    Response response = generateRsaKey(requestBody);

    // Assert
    response
        .then()
        .statusCode(400)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_INVALID_KEY_FORMAT));
  }

  // Helper methods for key parsing
  private RSAPublicKey parsePublicKey(String publicKeyPem) throws Exception {
    String publicKeyContent =
        publicKeyPem
            .replace(PEM_PUBLIC_KEY_HEADER, "")
            .replace(PEM_PUBLIC_KEY_FOOTER, "")
            .replaceAll("\\s", "");

    byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
    return (RSAPublicKey) keyFactory.generatePublic(spec);
  }

  private RSAPrivateKey parsePrivateKey(String privateKeyPem) throws Exception {
    String privateKeyContent =
        privateKeyPem
            .replace(PEM_PRIVATE_KEY_HEADER, "")
            .replace(PEM_PRIVATE_KEY_FOOTER, "")
            .replaceAll("\\s", "");

    byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
    return (RSAPrivateKey) keyFactory.generatePrivate(spec);
  }
}
