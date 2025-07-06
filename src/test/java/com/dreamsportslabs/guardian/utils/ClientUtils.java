package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.Constants.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientUtils {

  public static Map<String, Object> createValidClientRequest() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(CLIENT_NAME, AUTH_TEST_CLIENT_NAME);
    requestBody.put(CLIENT_URI, EXAMPLE_COM);
    requestBody.put(REDIRECT_URIS, Arrays.asList(EXAMPLE_CALLBACK));
    requestBody.put(CONTACTS, Arrays.asList(ADMIN_EMAIL));
    requestBody.put(GRANT_TYPES, Arrays.asList(AUTHORIZATION_CODE, REFRESH_TOKEN));
    requestBody.put(RESPONSE_TYPES, Arrays.asList(AUTH_RESPONSE_TYPE_CODE));
    requestBody.put(LOGO_URI, EXAMPLE_LOGO);
    requestBody.put(POLICY_URI, EXAMPLE_POLICY);
    requestBody.put(SKIP_CONSENT, AUTH_SKIP_CONSENT_FALSE);
    return requestBody;
  }

  public static Map<String, Object> createClientScopeRequest(List<String> scopes) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(SCOPES, scopes);
    return requestBody;
  }

  public static Map<String, Object> createClientScopeRequest(String... scopes) {
    return createClientScopeRequest(Arrays.asList(scopes));
  }
}
