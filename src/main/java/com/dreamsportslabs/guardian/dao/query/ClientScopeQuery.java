package com.dreamsportslabs.guardian.dao.query;

public final class ClientScopeQuery {

  public static final String INSERT_CLIENT_SCOPE =
      """
      INSERT INTO client_scope (tenant_id, scope, client_id)
      VALUES (?, ?, ?)
      """;

  public static final String SELECT_CLIENT_SCOPES =
      """
      SELECT tenant_id, scope, client_id
      FROM client_scope
      WHERE client_id = ? AND tenant_id = ?
      ORDER BY created_at ASC
      """;

  public static final String DELETE_CLIENT_SCOPE =
      """
      DELETE FROM client_scope
      WHERE tenant_id = ? and client_id = ? and scope = ?
      """;

  public static final String DELETE_CLIENT_SCOPES_BY_CLIENT =
      """
      DELETE FROM client_scope
      WHERE client_id = ? AND tenant_id = ?
      """;

  private ClientScopeQuery() {
    // Utility class
  }
}
