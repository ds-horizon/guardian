package com.dreamsportslabs.guardian.dao.query;

public final class OidcConfigQuery {
  private OidcConfigQuery() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String GET_ALL_SCOPES =
      """
            SELECT id, scope, display_name, description, claims
            FROM scope
            WHERE tenant_id = ? limit ? offset ?
        """;

  public static final String GET_SCOPES_BY_NAME =
      """
            SELECT id, scope, display_name, description, claims
            FROM scope
            WHERE tenant_id = ? and scope = ?
        """;

  public static final String CREATE_SCOPE =
      """
            INSERT INTO scope (tenant_id, scope, display_name, description, claims)
            VALUES (?, ?, ?, ?, ?)
        """;

  public static final String DELETE_SCOPE =
      """
        DELETE FROM scope WHERE tenant_id = ? AND scope = ?
      """;
}
