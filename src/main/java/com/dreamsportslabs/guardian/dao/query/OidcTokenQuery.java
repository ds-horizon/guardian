package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OidcTokenQuery {
  public static final String SAVE_OIDC_REFRESH_TOKEN =
      """
      INSERT INTO oidc_refresh_token (
          tenant_id, client_id, user_id, refresh_token,
          refresh_token_exp, scope, device_name, ip
      ) VALUES (?, ?, ?, ?, ?, ?, ?, INET6_ATON(?))
      """;

  public static final String GET_OIDC_REFRESH_TOKEN =
      """
      SELECT tenant_id, client_id, user_id, is_active, refresh_token, refresh_token_exp, scope
      FROM oidc_refresh_token
      WHERE tenant_id = ? AND client_id = ? AND refresh_token = ? AND is_active = true
      """;

  public static final String REVOKE_OIDC_REFRESH_TOKEN =
      """
        UPDATE oidc_refresh_token
        SET is_active = false
        WHERE tenant_id = ? AND client_id = ? AND refresh_token = ?;
      """;

  public static final String VALIDATE_OIDC_REFRESH_TOKEN =
      "SELECT user_id FROM oidc_refresh_token WHERE tenant_id = ? AND refresh_token = ? AND is_active = 1 AND refresh_token_exp > UNIX_TIMESTAMP()";
}
