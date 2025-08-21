package com.dreamsportslabs.guardian.it.scope;

import static com.dreamsportslabs.guardian.Constants.HEADER_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.QUERY_PARAM_NAME;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TEST_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_ICON_URL;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.listScopes;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
import com.dreamsportslabs.guardian.utils.ScopeUtils;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DeleteScopeIT {

  @Test
  @DisplayName("Should delete scope successfully")
  public void testDeleteScopeSuccess() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);

    // Act
    createScope(TENANT_1, getValidScopeRequestBody(scope)).then().statusCode(SC_CREATED);
    deleteScope(TENANT_1, scope).then().statusCode(SC_NO_CONTENT);

    // Validate
    Response response = listScopes(TENANT_1, Map.of(QUERY_PARAM_NAME, scope));
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(0));

    JsonObject obj = DbUtils.getScope(TENANT_1, scope);
    assertThat(obj == null || obj.isEmpty(), equalTo(true));
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent scope")
  public void testDeleteNonExistentScope() {
    // Act
    Response response = deleteScope(TENANT_1, RandomStringUtils.randomAlphabetic(10));

    // Validate
    response.then().statusCode(SC_NOT_FOUND);
  }

  @Test
  @DisplayName("Should return error when path parameter is null")
  public void testDeleteScopeWithNullPathParam() {
    // Act
    Response response = deleteScope(TENANT_1, null);

    // Validate
    response.then().statusCode(SC_METHOD_NOT_ALLOWED);
  }

  @Test
  @DisplayName("Should return error when path parameter is empty string")
  public void testDeleteScopeWithEmptyPathParam() {
    // Act
    Response response = deleteScope(TENANT_1, "");

    // Validate
    response.then().statusCode(SC_METHOD_NOT_ALLOWED);
  }

  @Test
  @DisplayName("Should return error when path parameter is blank string")
  public void testDeleteScopeWithBlankPathParam() {
    // Act
    Response response = deleteScope(TENANT_1, "   ");

    // Validate
    response.then().statusCode(SC_METHOD_NOT_ALLOWED);
  }

  @Test
  @DisplayName("Should return error when trying to delete without path parameter")
  public void testDeleteScopeWithoutPathParam() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, TENANT_1);

    // Act
    Response response = given().headers(headers).when().delete("/scopes");

    // Validate
    response.then().statusCode(SC_METHOD_NOT_ALLOWED);
  }

  @Test
  @DisplayName("Delete scope where the scope is mapped to a client")
  public void testDeleteScopeWithClientMapping() {
    // Arrange
    String scope = RandomStringUtils.randomAlphabetic(10);
    Map<String, Object> scopeData = getValidScopeRequestBody(scope);
    createScope(TENANT_1, scopeData).then().statusCode(SC_CREATED);
    String clientId = createTestClient(TENANT_1);
    addScopeToClient(TENANT_1, clientId, scope);

    // Verify the client-scope relationship exists before deletion
    assertThat(DbUtils.clientScopeExists(TENANT_1, clientId, scope), equalTo(true));

    // Act
    deleteScope(TENANT_1, scope).then().statusCode(SC_NO_CONTENT);

    // Validate - scope should be deleted from scope table
    JsonObject scopeObj = DbUtils.getScope(TENANT_1, scope);
    assertThat(scopeObj == null || scopeObj.isEmpty(), equalTo(true));

    // Validate - client-scope relationship should also be deleted (cascade delete)
    assertThat(DbUtils.clientScopeExists(TENANT_1, clientId, scope), equalTo(false));

    // Validate - client should still exist
    assertThat(DbUtils.clientExists(TENANT_1, clientId), equalTo(true));
  }

  private Map<String, Object> getValidScopeRequestBody(String scope) {
    return ScopeUtils.getValidScopeRequestBody(
        scope, TEST_DISPLAY_NAME, TEST_DESCRIPTION, List.of(TEST_EMAIL_CLAIM), TEST_ICON_URL, true);
  }

  private static String createTestClient(String tenantId) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("client_name", "Test Client " + RandomStringUtils.randomAlphanumeric(8));
    requestBody.put("client_uri", "https://example.com");
    requestBody.put("contacts", Arrays.asList("admin@example.com"));
    requestBody.put("grant_types", Arrays.asList("authorization_code", "refresh_token"));
    requestBody.put("response_types", Arrays.asList("code"));
    requestBody.put("redirect_uris", Arrays.asList("https://example.com/callback"));
    requestBody.put("logo_uri", "https://example.com/logo.png");
    requestBody.put("policy_uri", "https://example.com/policy");
    requestBody.put("skip_consent", false);

    Response response = createClient(tenantId, requestBody);
    response.then().statusCode(SC_CREATED);
    return response.jsonPath().getString("client_id");
  }

  private void addScopeToClient(String tenantId, String clientId, String... scopes) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("scopes", List.of(scopes));

    Response response = createClientScope(tenantId, clientId, requestBody);
    response.then().statusCode(SC_NO_CONTENT);
  }
}
