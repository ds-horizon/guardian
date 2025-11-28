package com.dreamsportslabs.guardian.dao.query;

public class WebAuthnConfigSql {

  public static final String GET_WEBAUTHN_CONFIG =
      """
      SELECT tenant_id, client_id, rp_id, allowed_web_origins, allowed_algorithms, aaguid_policy_mode,
             allowed_aaguids, blocked_aaguids, require_uv_enrollment, require_uv_auth, allowed_transports, require_device_bound
      FROM webauthn_config
      WHERE tenant_id = ? AND client_id = ?
      """;
}
