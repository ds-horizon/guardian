package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ClientQuery {

  public static final String CREATE_CLIENT =
      """
      INSERT INTO client (
          tenant_id, client_id, client_name, client_secret, client_uri,
          contacts, grant_types, logo_uri, policy_uri, redirect_uris,
          response_types, skip_consent
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  public static final String GET_CLIENT =
      """
      SELECT tenant_id, client_id, client_name, client_secret, client_uri,
             contacts, grant_types, logo_uri, policy_uri, redirect_uris,
             response_types, skip_consent
      FROM client
      WHERE tenant_id = ? AND client_id = ?
      """;

  public static final String GET_CLIENTS =
      """
      SELECT tenant_id, client_id, client_name, client_secret, client_uri,
             contacts, grant_types, logo_uri, policy_uri, redirect_uris,
             response_types, skip_consent
      FROM client
      WHERE tenant_id = ?
      ORDER BY created_at DESC
      LIMIT ? OFFSET ?
      """;

  public static final String UPDATE_CLIENT_SECRET =
      """
      UPDATE client SET client_secret = ?
      WHERE tenant_id = ? AND client_id = ?
      """;

  public static final String UPDATE_CLIENT =
      """
      UPDATE client SET <<insert_attributes>>
      WHERE tenant_id = ? AND client_id = ?
      """;

  public static final String DELETE_CLIENT =
      """
      DELETE FROM client
      WHERE tenant_id = ? AND client_id = ?
      """;
}
