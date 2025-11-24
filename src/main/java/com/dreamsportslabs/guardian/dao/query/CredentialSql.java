package com.dreamsportslabs.guardian.dao.query;

public class CredentialSql {

  public static final String GET_ACTIVE_CREDENTIALS_BY_USER_AND_CLIENT =
      """
      SELECT id, tenant_id, client_id, user_id, credential_id, public_key, binding_type, alg, sign_count, aaguid, is_active, first_use_complete
      FROM credentials
      WHERE tenant_id = ? AND client_id = ? AND user_id = ? AND is_active = true
      """;

  public static final String GET_CREDENTIAL_BY_ID =
      """
      SELECT id, tenant_id, client_id, user_id, credential_id, public_key, binding_type, alg, sign_count, aaguid, is_active, first_use_complete
      FROM credentials
      WHERE tenant_id = ? AND client_id = ? AND user_id = ? AND credential_id = ? AND is_active = true
      """;

  public static final String SAVE_CREDENTIAL =
      """
      INSERT INTO credentials (tenant_id, client_id, user_id, credential_id, public_key, binding_type, alg, sign_count, aaguid, first_use_complete)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  public static final String REVOKE_CREDENTIAL =
      """
      UPDATE credentials
      SET revoked_at = CURRENT_TIMESTAMP
      WHERE tenant_id = ? AND client_id = ? AND user_id = ? AND credential_id = ?
      """;

  public static final String UPDATE_SIGN_COUNT =
      """
      UPDATE credentials
      SET sign_count = ?
      WHERE tenant_id = ? AND client_id = ? AND user_id = ? AND credential_id = ?
      """;

  public static final String MARK_FIRST_USE_COMPLETE =
      """
      UPDATE credentials
      SET first_use_complete = TRUE
      WHERE tenant_id = ? AND client_id = ? AND user_id = ? AND credential_id = ?
      """;
}
