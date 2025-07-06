package com.dreamsportslabs.guardian.dao.query;

public final class ClientQuery {

  public static final String CREATE_CLIENT =
      """
      INSERT INTO client (
          tenant_id, client_id, client_name, client_secret, client_uri,
          contacts, grant_types, logo_uri, policy_uri, redirect_uris,
          response_types, skip_consent
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  public static final String SELECT_CLIENT_BY_ID =
      """
      SELECT tenant_id, client_id, client_name, client_secret, client_uri,
             contacts, grant_types, logo_uri, policy_uri, redirect_uris,
             response_types, skip_consent
      FROM client
      WHERE client_id = ? AND tenant_id = ?
      """;

  public static final String SELECT_CLIENTS_BY_TENANT =
      """
      SELECT tenant_id, client_id, client_name, client_secret, client_uri,
             contacts, grant_types, logo_uri, policy_uri, redirect_uris,
             response_types, skip_consent
      FROM client
      WHERE tenant_id = ?
      ORDER BY created_at DESC
      LIMIT ? OFFSET ?
      """;

  public static final String UPDATE_CLIENT =
      """
      UPDATE client SET
          client_name = ?, client_secret = ?, client_uri = ?,
          contacts = ?, grant_types = ?, logo_uri = ?, policy_uri = ?,
          redirect_uris = ?, response_types = ?, skip_consent = ?
      WHERE client_id = ? AND tenant_id = ?
      """;

  public static final String DELETE_CLIENT =
      """
      DELETE FROM client
      WHERE client_id = ? AND tenant_id = ?
      """;

  private ClientQuery() {
    // Utility class
  }
}
