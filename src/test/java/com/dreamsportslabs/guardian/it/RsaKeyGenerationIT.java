package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.generateRsaKey;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.equalTo;
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

  private static final String TENANT_VALID = "tenant1";
  private static final String TENANT_UNKNOWN = randomAlphanumeric(8);

  private JsonObject extractResponseBody(Response response) {
    return new JsonObject(response.getBody().asString());
  }

  @Test
  @DisplayName("Should generate RSA key with defaults successfully")
  void generateRsaKeyWithDefaults() throws Exception {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();

    // Act
    Response response = generateRsaKey(TENANT_VALID, requestBody);

    // Assert
    assertThat(response.getStatusCode(), equalTo(200));

    JsonObject responseBody = extractResponseBody(response);

    // Verify response structure
    assertThat(responseBody.getString("kid"), notNullValue());
    assertThat(responseBody.getValue("publicKey"), notNullValue());
    assertThat(responseBody.getValue("privateKey"), notNullValue());
    assertThat(responseBody.getInteger("keySize"), equalTo(2048));

    // Verify PEM format (default)
    String publicKey = responseBody.getString("publicKey");
    String privateKey = responseBody.getString("privateKey");
    String kid = responseBody.getString("kid");

    assertThat(publicKey, containsString("-----BEGIN PUBLIC KEY-----"));
    assertThat(publicKey, containsString("-----END PUBLIC KEY-----"));
    assertThat(privateKey, containsString("-----BEGIN PRIVATE KEY-----"));
    assertThat(privateKey, containsString("-----END PRIVATE KEY-----"));

    // Verify keys can be parsed as valid RSA keys
    RSAPublicKey publicKeyObj = parsePublicKey(publicKey);
    RSAPrivateKey privateKeyObj = parsePrivateKey(privateKey);

    // Verify key properties
    assertThat(
        "Public key modulus should be 2048 bits",
        publicKeyObj.getModulus().bitLength(),
        equalTo(2048));
    assertThat(
        "Private key modulus should be 2048 bits",
        privateKeyObj.getModulus().bitLength(),
        equalTo(2048));

    // Verify public and private keys are mathematically related
    assertThat(
        "Public and private keys should have same modulus",
        publicKeyObj.getModulus(),
        equalTo(privateKeyObj.getModulus()));

    // Verify public exponent is standard (65537)
    assertThat(
        "Public exponent should be 65537",
        publicKeyObj.getPublicExponent().intValue(),
        equalTo(65537));

    assertThat(kid, notNullValue());
    assertThat(kid.length(), greaterThan(0));

    // Should be a reasonable length for a key identifier
    assertThat(kid.length(), greaterThan(10));
  }

  @Test
  @DisplayName("Should generate RSA key with 4096-bit key size")
  void generateRsaKeyWith4096BitSize() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("keySize", 4096);
    requestBody.put("format", "PEM");

    // Act
    Response response = generateRsaKey(TENANT_VALID, requestBody);

    // Assert
    assertThat(response.getStatusCode(), equalTo(200));

    JsonObject responseBody = extractResponseBody(response);
    assertThat(responseBody.getInteger("keySize"), equalTo(4096));

    // 4096-bit keys should be longer than 2048-bit keys
    String privateKey = responseBody.getString("privateKey");
    assertThat(privateKey.length(), greaterThan(3000));
  }

  @Test
  @DisplayName("Should generate RSA key with 3072-bit key size")
  void generateRsaKeyWith3072BitSize() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("keySize", 3072);
    requestBody.put("format", "PEM");

    // Act
    Response response = generateRsaKey(TENANT_VALID, requestBody);

    // Assert
    assertThat(response.getStatusCode(), equalTo(200));

    JsonObject responseBody = extractResponseBody(response);
    assertThat(responseBody.getInteger("keySize"), equalTo(3072));
  }

  @Test
  @DisplayName("Should generate RSA key in JWKS format")
  void generateRsaKeyInJwksFormat() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("format", "JWKS");

    // Act
    Response response = generateRsaKey(TENANT_VALID, requestBody);

    // Assert
    assertThat(response.getStatusCode(), equalTo(200));

    JsonObject responseBody = extractResponseBody(response);

    // In JWKS format, keys should be JSON objects, not strings
    assertThat(responseBody.getValue("publicKey"), instanceOf(JsonObject.class));
    assertThat(responseBody.getValue("privateKey"), instanceOf(JsonObject.class));

    // Verify JWKS structure for public key
    JsonObject publicKey = responseBody.getJsonObject("publicKey");
    assertThat(publicKey.getString("kty"), equalTo("RSA"));
    assertThat(publicKey.getString("use"), equalTo("sig"));
    assertThat(publicKey.getString("n"), notNullValue());
    assertThat(publicKey.getString("e"), equalTo("AQAB"));
  }

  @Test
  @DisplayName("Should generate different keys on multiple calls")
  void generateMultipleRsaKeysAreDifferent() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();

    // Act
    Response response1 = generateRsaKey(TENANT_VALID, requestBody);
    Response response2 = generateRsaKey(TENANT_VALID, requestBody);

    // Assert
    assertThat(response1.getStatusCode(), equalTo(200));
    assertThat(response2.getStatusCode(), equalTo(200));

    JsonObject responseBody1 = extractResponseBody(response1);
    JsonObject responseBody2 = extractResponseBody(response2);

    // Keys should be different
    assertThat(responseBody1.getString("kid"), is(not(equalTo(responseBody2.getString("kid")))));
    assertThat(
        responseBody1.getString("publicKey"),
        is(not(equalTo(responseBody2.getString("publicKey")))));
    assertThat(
        responseBody1.getString("privateKey"),
        is(not(equalTo(responseBody2.getString("privateKey")))));
  }

  @Test
  @DisplayName("Should return error for invalid key size")
  void generateRsaKeyWithInvalidKeySize() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("keySize", 1024); // Invalid key size

    // Act
    Response response = generateRsaKey(TENANT_VALID, requestBody);

    // Assert
    assertThat(response.getStatusCode(), equalTo(400));
    assertThat(response.getBody().jsonPath().getString("error.code"), equalTo("invalid_request"));
    assertThat(
        response.getBody().jsonPath().getString("error.message"),
        equalTo("Invalid RSA key length. Allowed values are [2048, 3072, 4096]"));
  }

  @Test
  @DisplayName("Should return error for invalid format")
  void generateRsaKeyWithInvalidFormat() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("format", "INVALID"); // Invalid format

    // Act
    Response response = generateRsaKey(TENANT_VALID, requestBody);

    // Assert
    assertThat(response.getStatusCode(), equalTo(400));

    assertThat(response.getBody().jsonPath().getString("error.code"), equalTo("invalid_request"));
    assertThat(
        response.getBody().jsonPath().getString("error.message"),
        equalTo("Invalid key format. Allowed values are PEM or JWKS"));
  }

  @Test
  @DisplayName("Should return error for missing format")
  void generateRsaKeyWithMissingFormat() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("format", ""); // Empty format

    // Act
    Response response = generateRsaKey(TENANT_VALID, requestBody);

    // Assert
    assertThat(response.getStatusCode(), equalTo(400));

    assertThat(response.getBody().jsonPath().getString("error.code"), equalTo("invalid_request"));
    assertThat(
        response.getBody().jsonPath().getString("error.message"),
        equalTo("Invalid key format. Allowed values are PEM or JWKS"));
  }

  @Test
  @DisplayName("Should return error for unknown tenant")
  void generateRsaKeyWithUnknownTenant() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();

    // Act
    Response response = generateRsaKey(TENANT_UNKNOWN, requestBody);

    // Assert
    assertThat(response.getStatusCode(), equalTo(400));
  }

  @Test
  @DisplayName("Should return error when tenant header is missing")
  void generateRsaKeyWithoutTenantHeader() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();

    // Act
    Response response = generateRsaKey(null, requestBody);

    // Assert
    assertThat(response.getStatusCode(), equalTo(401));
  }

  // Helper methods for key parsing
  private RSAPublicKey parsePublicKey(String publicKeyPem) throws Exception {
    String publicKeyContent =
        publicKeyPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");

    byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) keyFactory.generatePublic(spec);
  }

  private RSAPrivateKey parsePrivateKey(String privateKeyPem) throws Exception {
    String privateKeyContent =
        privateKeyPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

    byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey) keyFactory.generatePrivate(spec);
  }
}
