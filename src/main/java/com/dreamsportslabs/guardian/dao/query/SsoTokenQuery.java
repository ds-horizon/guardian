package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class SsoTokenQuery {
  public static final String SAVE_SSO_TOKEN =
      "INSERT INTO sso_token (tenant_id, client_id_issues_to, user_id, refresh_token, sso_token, expiry, auth_methods) VALUES (?, ?, ?, ?, ?, ?, ?)";

  public static final String GET_ACTIVE_SSO_TOKEN =
      "SELECT tenant_id, client_id_issues_to, user_id, refresh_token, sso_token, expiry, auth_methods FROM sso_token WHERE tenant_id = ? AND sso_token = ? AND is_active = 1";

  public static final String GET_SSO_TOKEN_FROM_REFRESH_TOKEN =
      "SELECT tenant_id, client_id_issues_to, user_id, refresh_token, sso_token, expiry, auth_methods FROM sso_token WHERE tenant_id = ? AND refresh_token = ? AND is_active = 1";

  public static final String INVALIDATE_SSO_TOKEN_BY_REFRESH_TOKEN =
      """
              UPDATE sso_token
              SET is_active = false
              WHERE tenant_id = ? AND client_id_issues_to = ? AND refresh_token = ?;
            """;

  public static final String INVALIDATE_ALL_SSO_TOKENS_FOR_USER =
      """
               UPDATE sso_token
               SET is_active = false
               WHERE tenant_id = ? AND user_id = ?
            """;

  public static final String INVALIDATE_SSO_TOKENS_OF_CLIENT_FOR_USER =
      """
               UPDATE sso_token
               SET is_active = false
               WHERE tenant_id = ? AND client_id_issues_to = ? AND user_id = ?
            """;
}
