package com.dreamsportslabs.guardian.dao.query;

public class OIDCConfigQuery {

  public static final String GET_OIDC_CONFIG_BY_TENANT_ID =
      """
        SELECT tenant_id,
               issuer,
               authorization_endpoint,
               token_endpoint,
               userinfo_endpoint,
               revocation_endpoint,
               jwks_uri,
               grant_types_supported,
               response_types_supported,
               subject_types_supported,
               id_token_signing_alg_values_supported,
               userinfo_signing_alg_values_supported,
               token_endpoint_auth_methods_supported
        FROM oidc_tenant_config
        WHERE tenant_id = ?
        """;
}
