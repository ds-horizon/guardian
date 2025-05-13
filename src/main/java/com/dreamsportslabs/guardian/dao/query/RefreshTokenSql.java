package com.dreamsportslabs.guardian.dao.query;

public class RefreshTokenSql {
  public static final String SAVE_REFRESH_TOKEN =
      "INSERT INTO refresh_tokens (tenant_id, user_id, refresh_token, refresh_token_exp, source, device_name, location, ip) VALUES (?, ?, ?, ?, ?, ?, ?, INET6_ATON(?))";

  public static final String VALIDATE_REFRESH_TOKEN =
      "SELECT user_id FROM refresh_tokens WHERE tenant_id = ? AND refresh_token = ? AND is_active = 1 AND refresh_token_exp > UNIX_TIMESTAMP()";

  public static final String GET_ALL_REFRESH_TOKENS_FOR_USER =
      "SELECT refresh_token AS refreshToken FROM refresh_tokens WHERE tenant_id = ? AND user_id = ? AND is_active = 1";

  public static final String INVALIDATE_REFRESH_TOKEN =
      "UPDATE refresh_tokens SET is_active = 0 WHERE tenant_id = ? AND refresh_token = ?";

  public static final String INVALIDATE_ALL_REFRESH_TOKENS_FOR_USER =
      "UPDATE refresh_tokens SET is_active = 0 WHERE tenant_id = ? AND user_id = ?";
}
