package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getJwks;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class JwksIT {

  /** Generates a unique tenant ID for testing */
  private static final String TENANT_VALID = "tenant1";

  private static final String TENANT_UNKNOWN = randomAlphanumeric(8);

  private static JsonArray extractKeys(Response response) {
    JsonObject json = new JsonObject(response.getBody().asString());
    Assertions.assertTrue(json.containsKey("keys"));
    return json.getJsonArray("keys");
  }

  @Test
  void jwksStructureIsCorrect() {
    Response response = getJwks(TENANT_VALID);

    Assertions.assertEquals(200, response.getStatusCode());

    JsonArray keys = extractKeys(response);
    Assertions.assertEquals(1, keys.size());

    JsonObject key = keys.getJsonObject(0);
    Assertions.assertEquals("RSA", key.getString("kty"));
    Assertions.assertEquals("sig", key.getString("use"));
    assertNotNull(key.getString("kid"));
    assertNotNull(key.getString("n"));
    Assertions.assertEquals("AQAB", key.getString("e"));
  }

  @Test
  void unknownTenantReturnsError() {
    Response response = getJwks(TENANT_UNKNOWN);
    Assertions.assertEquals(400, response.getStatusCode());
  }
}
