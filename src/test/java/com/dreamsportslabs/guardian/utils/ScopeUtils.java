package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_ICON_URL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_OIDC;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;

public class ScopeUtils {
  public static Map<String, Object> getValidScopeRequestBody(
      String scope,
      String displayName,
      String description,
      List<String> claims,
      String iconUrl,
      Boolean isOidc) {

    Map<String, Object> body = new HashMap<>();
    body.put(BODY_PARAM_SCOPE, scope);
    body.put(BODY_PARAM_DISPLAY_NAME, displayName);
    body.put(BODY_PARAM_DESCRIPTION, description);
    body.put(BODY_PARAM_CLAIMS, claims);
    body.put(BODY_PARAM_ICON_URL, iconUrl);
    body.put(BODY_PARAM_IS_OIDC, isOidc);
    return body;
  }

  @SneakyThrows
  public static void validateInDb(String tenant, Map<String, Object> body) {
    JsonObject dbScope = DbUtils.getScope(tenant, body.get(BODY_PARAM_SCOPE).toString());
    Assertions.assertNotNull(dbScope);

    assertThat(dbScope.getString(BODY_PARAM_SCOPE), equalTo(body.get(BODY_PARAM_SCOPE)));

    assertThat(
        dbScope.getString(BODY_PARAM_DISPLAY_NAME), equalTo(body.get(BODY_PARAM_DISPLAY_NAME)));

    assertThat(
        dbScope.getString(BODY_PARAM_DESCRIPTION), equalTo(body.get(BODY_PARAM_DESCRIPTION)));

    assertThat(dbScope.getString(BODY_PARAM_ICON_URL), equalTo(body.get(BODY_PARAM_ICON_URL)));

    assertThat(dbScope.getString("tenantId"), equalTo(tenant));

    assertThat(dbScope.getBoolean(BODY_PARAM_IS_OIDC), equalTo(body.get(BODY_PARAM_IS_OIDC)));

    if (StringUtils.isNotBlank(dbScope.getString(BODY_PARAM_CLAIMS))
        && body.containsKey(BODY_PARAM_CLAIMS)) {
      List<String> claims =
          (new ObjectMapper())
              .readValue(dbScope.getString(BODY_PARAM_CLAIMS), new TypeReference<>() {});
      for (String claim : (List<String>) body.get(BODY_PARAM_CLAIMS)) {
        assertThat(claims, hasItem(claim));
      }
    } else {
      assertThat(dbScope.getString(BODY_PARAM_CLAIMS), equalTo(body.get(BODY_PARAM_CLAIMS)));
    }
  }
}
