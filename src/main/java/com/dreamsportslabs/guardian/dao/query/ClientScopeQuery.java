package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ClientScopeQuery {

  public static final String INSERT_CLIENT_SCOPE =
      """
      INSERT INTO client_scope (tenant_id, client_id, scope)
      VALUES (?, ?, ?)
      """;

  public static final String GET_CLIENT_SCOPES =
      """
      SELECT tenant_id, scope, client_id, is_default
      FROM client_scope
      WHERE tenant_id = ? AND client_id = ?
      ORDER BY created_at ASC
      """;

  public static final String DELETE_CLIENT_SCOPE =
      """
      DELETE FROM client_scope
      WHERE tenant_id = ? and client_id = ? and scope = ?
      """;
}
