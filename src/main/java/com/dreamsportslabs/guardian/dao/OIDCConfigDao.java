package com.dreamsportslabs.guardian.dao;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.config.tenant.OIDCConfig;
import com.dreamsportslabs.guardian.dao.query.OIDCConfigQuery;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowSet;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OIDCConfigDao {

  private final MysqlClient mysqlClient;

  public Single<OIDCConfig> getOIDCConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(OIDCConfigQuery.GET_OIDC_CONFIG_BY_TENANT_ID)
        .rxExecute(Tuple.of(tenantId))
        .map(this::mapToOIDCConfig)
        .doOnSuccess(config -> log.debug("Retrieved OIDC config for tenant: {}", tenantId))
        .doOnError(
            error -> log.error("Error retrieving OIDC config for tenant: {}", tenantId, error));
  }

  private OIDCConfig mapToOIDCConfig(RowSet<Row> rowSet) {
    if (rowSet.size() == 0) {
      return null;
    }

    Row row = rowSet.iterator().next();
    OIDCConfig config = new OIDCConfig();
    config.setTenantId(row.getString("tenant_id"));
    config.setIssuer(row.getString("issuer"));
    config.setAuthorizationEndpoint(row.getString("authorization_endpoint"));
    config.setTokenEndpoint(row.getString("token_endpoint"));
    config.setUserinfoEndpoint(row.getString("userinfo_endpoint"));
    config.setRevocationEndpoint(row.getString("revocation_endpoint"));
    config.setJwksUri(row.getString("jwks_uri"));

    // Parse JSON arrays
    JsonArray grantTypes = new JsonArray(row.getString("grant_types_supported"));
    JsonArray responseTypes = new JsonArray(row.getString("response_types_supported"));
    JsonArray subjectTypes = new JsonArray(row.getString("subject_types_supported"));
    JsonArray idTokenAlgs = new JsonArray(row.getString("id_token_signing_alg_values_supported"));
    JsonArray userinfoAlgs = new JsonArray(row.getString("userinfo_signing_alg_values_supported"));
    JsonArray authMethods = new JsonArray(row.getString("token_endpoint_auth_methods_supported"));

    config.setGrantTypesSupported(grantTypes.getList());
    config.setResponseTypesSupported(responseTypes.getList());
    config.setSubjectTypesSupported(subjectTypes.getList());
    config.setIdTokenSigningAlgValuesSupported(idTokenAlgs.getList());
    config.setUserinfoSigningAlgValuesSupported(userinfoAlgs.getList());
    config.setTokenEndpointAuthMethodsSupported(authMethods.getList());

    return config;
  }
}
