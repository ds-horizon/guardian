package com.dreamsportslabs.guardian.dao.impl;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.config.tenant.OIDCConfig;
import com.dreamsportslabs.guardian.dao.OIDCConfigDao;
import com.dreamsportslabs.guardian.dao.query.OIDCConfigQuery;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowSet;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OIDCConfigDaoImpl implements OIDCConfigDao {

  @Inject private MysqlClient mysqlClient;

  @Override
  public Single<OIDCConfig> getOIDCConfig(String tenantId) {
    return Single.zip(
        getOIDCConfigData(tenantId),
        getScopes(tenantId),
        getClaims(tenantId),
        (configData, scopes, claims) -> {
          OIDCConfig config = new OIDCConfig();
          config.setTenantId(tenantId);
          config.setIssuer(configData.getString("issuer"));
          config.setAuthorizationEndpoint(configData.getString("authorization_endpoint"));
          config.setTokenEndpoint(configData.getString("token_endpoint"));
          config.setUserinfoEndpoint(configData.getString("userinfo_endpoint"));
          config.setRevocationEndpoint(configData.getString("revocation_endpoint"));
          config.setJwksUri(configData.getString("jwks_uri"));

          // Parse JSON arrays from database
          config.setGrantTypesSupported(
              parseJsonArray(configData.getJsonArray("grant_types_supported")));
          config.setResponseTypesSupported(
              parseJsonArray(configData.getJsonArray("response_types_supported")));
          config.setSubjectTypesSupported(
              parseJsonArray(configData.getJsonArray("subject_types_supported")));
          config.setIdTokenSigningAlgValuesSupported(
              parseJsonArray(configData.getJsonArray("id_token_signing_alg_values_supported")));
          config.setUserinfoSigningAlgValuesSupported(
              parseJsonArray(configData.getJsonArray("userinfo_signing_alg_values_supported")));
          config.setTokenEndpointAuthMethodsSupported(
              parseJsonArray(configData.getJsonArray("token_endpoint_auth_methods_supported")));
          // Set scopes and claims from separate tables
          config.setScopesSupported(extractScopeNames(scopes));
          config.setClaimsSupported(extractClaimNames(claims));

          return config;
        });
  }

  private Single<JsonObject> getOIDCConfigData(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(OIDCConfigQuery.OIDC_CONFIG)
        .execute(Tuple.of(tenantId))
        .filter(rowSet -> rowSet.size() > 0)
        .switchIfEmpty(
            Single.error(new RuntimeException("OIDC config not found for tenant: " + tenantId)))
        .map(rows -> rows.iterator().next().toJson());
  }

  private Single<List<JsonObject>> getScopes(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(OIDCConfigQuery.SCOPES)
        .execute(Tuple.of(tenantId))
        .map(this::extractJsonObjects);
  }

  private Single<List<JsonObject>> getClaims(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(OIDCConfigQuery.CLAIMS)
        .execute(Tuple.of(tenantId))
        .map(this::extractJsonObjects);
  }

  private List<String> extractScopeNames(List<JsonObject> scopes) {
    List<String> result = new ArrayList<>();
    for (JsonObject scope : scopes) {
      result.add(scope.getString("scope_name"));
    }
    return result;
  }

  private List<String> extractClaimNames(List<JsonObject> claims) {
    List<String> result = new ArrayList<>();
    for (JsonObject claim : claims) {
      result.add(claim.getString("claim_name"));
    }
    return result;
  }

  private List<JsonObject> extractJsonObjects(RowSet<Row> rows) {
    List<JsonObject> result = new ArrayList<>();
    for (Row row : rows) {
      JsonObject obj = row.toJson();
      if (obj != null) result.add(obj);
    }
    return result;
  }

  private List<String> parseJsonArray(JsonArray jsonArray) {
    List<String> result = new ArrayList<>();
    if (jsonArray != null) {
      for (int i = 0; i < jsonArray.size(); i++) {
        result.add(jsonArray.getString(i));
      }
    }
    return result;
  }
}
