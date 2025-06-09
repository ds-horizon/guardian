package com.dreamsportslabs.guardian.dao.query;

public class OIDCConfigQuery {

  public static final String OIDC_CONFIG =
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

  public static final String SCOPES =
      """
        SELECT scope_name,
               description,
               is_default,
               claims,
               created_at,
               updated_at
        FROM oidc_tenant_scopes
        WHERE tenant_id = ?
        ORDER BY scope_name
        """;

  public static final String CLAIMS =
      """
        SELECT claim_name,
               description,
               data_type,
               created_at,
               updated_at
        FROM oidc_tenant_claims
        WHERE tenant_id = ?
        ORDER BY claim_name
        """;
}
