package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.Constants.AUTH_RESPONSE_TYPE_CODE;
import static com.dreamsportslabs.guardian.Constants.EQUALS_SIGN;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_CALLBACK;
import static com.dreamsportslabs.guardian.Constants.LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.PARAM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.PARAM_REDIRECT_URI;
import static com.dreamsportslabs.guardian.Constants.PARAM_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.PARAM_SEPARATOR;
import static com.dreamsportslabs.guardian.Constants.PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.QUERY_SEPARATOR;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.TEST_STATE;

import java.util.HashMap;
import java.util.Map;

public class OidcUtils {

  /**
   * Creates a valid authorize request with all required parameters.
   *
   * @param clientId The client ID to use in the request
   * @return Map containing the query parameters for a valid authorize request
   */
  public static Map<String, String> createValidAuthorizeRequest(String clientId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(PARAM_RESPONSE_TYPE, AUTH_RESPONSE_TYPE_CODE);
    queryParams.put(PARAM_CLIENT_ID, clientId);
    queryParams.put(PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL + " " + SCOPE_ADDRESS);
    queryParams.put(PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);
    queryParams.put(PARAM_STATE, TEST_STATE);
    return queryParams;
  }

  /**
   * Extracts the login challenge from a redirect location URL.
   *
   * @param location The redirect location URL
   * @return The login challenge value, or null if not found
   */
  public static String extractLoginChallenge(String location) {
    if (location == null) return null;
    String[] params = location.split(QUERY_SEPARATOR)[1].split(PARAM_SEPARATOR);
    for (String param : params) {
      if (param.startsWith(LOGIN_CHALLENGE + EQUALS_SIGN)) {
        return param.split(EQUALS_SIGN)[1];
      }
    }
    return null;
  }
}
