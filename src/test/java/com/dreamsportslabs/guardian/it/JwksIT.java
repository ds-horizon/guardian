package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getJwks;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class JwksIT {

  private static final String TENANT_VALID = "tenant1";
  private static final String TENANT_UNKNOWN = randomAlphanumeric(8);

  private static JsonArray extractKeys(Response response) {
    JsonObject json = new JsonObject(response.getBody().asString());
    assertThat(json.containsKey("keys"), equalTo(true));
    return json.getJsonArray("keys");
  }

  @Test
  @DisplayName("Should return Multiple JWKS keys successfully")
  void jwksReturnsMultipleKeys() {

    // Act
    Response response = getJwks("tenant3");

    // validate
    assertThat(response.getStatusCode(), equalTo(200));

    JsonArray keys = extractKeys(response);
    assertThat(keys.size(), equalTo(2));

    List<String> kids = keys.stream().map(o -> ((JsonObject) o).getString("kid")).toList();

    assertThat(kids.contains("multi-key-1"), equalTo(true));
    assertThat(kids.contains("multi-key-2"), equalTo(true));
  }

  @Test
  @DisplayName("Validate JWKS structure")
  void jwksStructureIsCorrect() {
    // Act
    Response response = getJwks(TENANT_VALID);

    // Validate
    assertThat(response.getStatusCode(), equalTo(200));

    JsonArray keys = extractKeys(response);
    assertThat(keys.size(), equalTo(1));

    JsonObject key = keys.getJsonObject(0);
    assertThat(key.getString("kty"), equalTo("RSA"));
    assertThat(key.getString("use"), equalTo("sig"));
    assertNotNull(key.getString("kid"));
    assertNotNull(key.getString("n"));
    assertThat(key.getString("e"), equalTo("AQAB"));
    assertThat(key.getString("alg"), equalTo("RS256"));
  }

  @Test
  @DisplayName("Should return error for unknown tenant")
  void unknownTenantReturnsError() {
    // Act
    Response response = getJwks(TENANT_UNKNOWN);
    // Validate
    assertThat(response.getStatusCode(), equalTo(400));
  }
}
