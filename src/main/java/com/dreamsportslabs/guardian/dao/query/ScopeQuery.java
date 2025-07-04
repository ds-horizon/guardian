package com.dreamsportslabs.guardian.dao.query;

public final class ScopeQuery {
  private ScopeQuery() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String GET_SCOPES_PAGINATED =
      "SELECT name, display_name, description, claims, is_oidc, icon_url FROM scope WHERE tenant_id = ? LIMIT ? OFFSET ?";

  public static final String GET_SCOPES_BY_NAMES_TEMPLATE =
      "SELECT name, display_name, description, claims, is_oidc, icon_url FROM scope WHERE tenant_id = ? AND name IN (%s)";

  public static final String SAVE_SCOPE =
      "INSERT INTO scope (tenant_id, name, display_name, description, claims, is_oidc, icon_url) VALUES (?, ?, ?, ?, ?, ?, ?)";

  public static final String DELETE_SCOPE = "DELETE FROM scope WHERE tenant_id = ? AND name = ?";

  public static final String GET_OIDC_SCOPES =
      "SELECT name, display_name, description, claims, is_oidc, icon_url FROM scope WHERE tenant_id = ? AND is_oidc = true";
}
