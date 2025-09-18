package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class SsoTokenQuery {
  public static final String SAVE_SSO_TOKEN =
      "INSERT INTO sso_token (tenant_id, client_id_issues_to, user_id, refresh_token, sso_token, expiry) VALUES (?, ?, ?, ?, ?, ?)";
}
