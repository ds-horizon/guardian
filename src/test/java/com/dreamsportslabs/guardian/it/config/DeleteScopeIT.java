package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_ICON_URL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_OIDC;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.QUERY_PARAM_NAME;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TEST_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_ICON_URL;
import static com.dreamsportslabs.guardian.Constants.TEST_SCOPE_NAME;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.listScopes;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;

import io.restassured.response.Response;
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
    createScope(TENANT_1, valid(scope)).then().statusCode(SC_CREATED);
    deleteScope(TENANT_1, scope).then().statusCode(SC_NO_CONTENT);

    // Validate
    Response response = listScopes(TENANT_1, Map.of(QUERY_PARAM_NAME, scope));
    response.then().statusCode(SC_OK).body("scopes.size()", equalTo(0));
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent scope")
  public void testDeleteNonExistentScope() {
    // Act
    Response response = deleteScope(TENANT_1, RandomStringUtils.randomAlphabetic(10));

    // Validate
    response.then().statusCode(SC_NOT_FOUND);
  }

  private Map<String, Object> valid(String scope) {
    Map<String, Object> m = new HashMap<>();
    m.put(BODY_PARAM_SCOPE, scope);
    m.put(BODY_PARAM_DISPLAY_NAME, TEST_SCOPE_NAME);
    m.put(BODY_PARAM_DESCRIPTION, TEST_DESCRIPTION);
    m.put(BODY_PARAM_CLAIMS, List.of(TEST_EMAIL_CLAIM));
    m.put(BODY_PARAM_ICON_URL, TEST_ICON_URL);
    m.put(BODY_PARAM_IS_OIDC, true);
    return m;
  }
}
