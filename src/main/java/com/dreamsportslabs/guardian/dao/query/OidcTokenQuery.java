package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OidcTokenQuery {
  public static final String SAVE_OIDC_REFRESH_TOKEN =
      """
      INSERT INTO refresh_tokens (
          tenant_id, client_id, user_id, refresh_token,
          refresh_token_exp, scope, device_name, ip, source, location, auth_method
      ) VALUES (?, ?, ?, ?, ?, ?, ?, INET6_ATON(?), ?, ?, ?)
      """;

  public static final String GET_OIDC_REFRESH_TOKEN =
      """
      SELECT tenant_id, client_id, user_id, is_active, refresh_token, refresh_token_exp, scope
      FROM refresh_tokens
      WHERE tenant_id = ? AND client_id = ? AND refresh_token = ? AND is_active = true
      """;

  public static final String GET_ACTIVE_REFRESH_TOKEN =
      """
          SELECT tenant_id, client_id, user_id, is_active, refresh_token, refresh_token_exp, scope
          FROM refresh_tokens
          WHERE tenant_id = ? AND refresh_token = ? AND is_active = true
          """;

  public static final String REVOKE_OIDC_REFRESH_TOKEN =
      """
        UPDATE refresh_tokens
        SET is_active = false
        WHERE tenant_id = ? AND client_id = ? AND refresh_token = ?;
      """;
}
