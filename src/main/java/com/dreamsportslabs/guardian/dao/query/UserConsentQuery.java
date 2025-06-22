package com.dreamsportslabs.guardian.dao.query;

public final class UserConsentQuery {

  public static final String INSERT_CONSENT =
      """
      INSERT INTO consent (tenant_id, client_id, user_id, scope)
      VALUES (?, ?, ?, ?)
      """;

  public static final String GET_USER_CONSENTS =
      """
      SELECT id, tenant_id, client_id, user_id, scope
      FROM consent
      WHERE tenant_id = ? AND client_id = ? AND user_id = ?
      """;

  public static final String DELETE_USER_CONSENTS =
      """
      DELETE FROM consent
      WHERE tenant_id = ? AND client_id = ? AND user_id = ?
      """;

  private UserConsentQuery() {}
}
