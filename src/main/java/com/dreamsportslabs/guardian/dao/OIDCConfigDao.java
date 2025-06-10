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
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OIDCConfigDao {

  private final MysqlClient mysqlClient;

  public Single<OIDCConfig> getOIDCConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(OIDCConfigQuery.GET_OIDC_CONFIG_BY_TENANT_ID)
        .rxExecute(Tuple.of(tenantId))
        .map(this::mapToOIDCConfig);
  }

  private OIDCConfig mapToOIDCConfig(RowSet<Row> rowSet) {
    if (rowSet.size() == 0) return null;

    Row row = rowSet.iterator().next();
    OIDCConfig config = new OIDCConfig();
    config.setTenantId(row.getString("tenant_id"));
    config.setIssuer(row.getString("issuer"));
    config.setAuthorizationEndpoint(row.getString("authorization_endpoint"));
    config.setTokenEndpoint(row.getString("token_endpoint"));
    config.setUserinfoEndpoint(row.getString("userinfo_endpoint"));
    config.setRevocationEndpoint(row.getString("revocation_endpoint"));
    config.setJwksUri(row.getString("jwks_uri"));
    config.setGrantTypesSupported(toListFromJson(row, "grant_types_supported"));
    config.setResponseTypesSupported(toListFromJson(row, "response_types_supported"));
    config.setSubjectTypesSupported(toListFromJson(row, "subject_types_supported"));
    config.setIdTokenSigningAlgValuesSupported(
        toListFromJson(row, "id_token_signing_alg_values_supported"));
    config.setUserinfoSigningAlgValuesSupported(
        toListFromJson(row, "userinfo_signing_alg_values_supported"));
    config.setTokenEndpointAuthMethodsSupported(
        toListFromJson(row, "token_endpoint_auth_methods_supported"));

    return config;
  }

  private List<String> toListFromJson(Row row, String columnName) {
    Object value = row.getValue(columnName);
    return value == null ? List.of() : new JsonArray(value.toString()).getList();
  }
}
