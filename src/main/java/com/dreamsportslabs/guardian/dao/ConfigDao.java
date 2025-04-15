package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.ConfigQuery.AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.ConfigQuery.EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.ConfigQuery.FB_AUTH_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.ConfigQuery.GOOGLE_AUTH_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.ConfigQuery.OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.ConfigQuery.SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.ConfigQuery.TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.ConfigQuery.USER_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.config.tenant.AuthCodeConfig;
import com.dreamsportslabs.guardian.config.tenant.EmailConfig;
import com.dreamsportslabs.guardian.config.tenant.FbConfig;
import com.dreamsportslabs.guardian.config.tenant.GoogleConfig;
import com.dreamsportslabs.guardian.config.tenant.OtpConfig;
import com.dreamsportslabs.guardian.config.tenant.SmsConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.config.tenant.TokenConfig;
import com.dreamsportslabs.guardian.config.tenant.UserConfig;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ConfigDao {
  private final MysqlClient mysqlClient;

  public Single<TenantConfig> getTenantConfig(String tenantId) {
    TenantConfig.TenantConfigBuilder builder = TenantConfig.builder().tenantId(tenantId);
    List<Completable> configSources =
        List.of(
            appendAuthCodeConfig(tenantId, builder),
            appendEmailConfig(tenantId, builder),
            appendUserConfig(tenantId, builder),
            appendTokenConfig(tenantId, builder),
            appendFbConfig(tenantId, builder),
            appendGoogleConfig(tenantId, builder),
            appendSmsConfig(tenantId, builder),
            appendOtpConfig(tenantId, builder));
    return Completable.merge(configSources)
        .andThen(Single.defer(() -> Single.just(builder.build())));
  }

  private Completable appendAuthCodeConfig(
      String tenantId, TenantConfig.TenantConfigBuilder builder) {
    return getConfigFromDb(tenantId, AuthCodeConfig.class, AUTH_CODE_CONFIG)
        .map(builder::authCodeConfig)
        .ignoreElement();
  }

  private Completable appendEmailConfig(String tenantId, TenantConfig.TenantConfigBuilder builder) {
    return getConfigFromDb(tenantId, EmailConfig.class, EMAIL_CONFIG)
        .map(builder::emailConfig)
        .ignoreElement();
  }

  private Completable appendOtpConfig(String tenantId, TenantConfig.TenantConfigBuilder builder) {
    return getConfigFromDb(tenantId, OtpConfig.class, OTP_CONFIG)
        .map(builder::otpConfig)
        .ignoreElement();
  }

  private Completable appendUserConfig(String tenantId, TenantConfig.TenantConfigBuilder builder) {
    return getConfigFromDb(tenantId, UserConfig.class, USER_CONFIG)
        .map(builder::userConfig)
        .ignoreElement();
  }

  private Completable appendTokenConfig(String tenantId, TenantConfig.TenantConfigBuilder builder) {
    return getConfigFromDb(tenantId, TokenConfig.class, TOKEN_CONFIG)
        .map(builder::tokenConfig)
        .ignoreElement();
  }

  private Completable appendFbConfig(String tenantId, TenantConfig.TenantConfigBuilder builder) {
    return getConfigFromDb(tenantId, FbConfig.class, FB_AUTH_CONFIG)
        .map(builder::fbConfig)
        .ignoreElement();
  }

  private Completable appendGoogleConfig(
      String tenantId, TenantConfig.TenantConfigBuilder builder) {
    return getConfigFromDb(tenantId, GoogleConfig.class, GOOGLE_AUTH_CONFIG)
        .map(builder::googleConfig)
        .ignoreElement();
  }

  private Completable appendSmsConfig(String tenantId, TenantConfig.TenantConfigBuilder builder) {
    return getConfigFromDb(tenantId, SmsConfig.class, SMS_CONFIG)
        .map(builder::smsConfig)
        .ignoreElement();
  }

  private <T> Single<T> getConfigFromDb(String tenantId, Class<T> configType, String query) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(query)
        .execute(Tuple.of(tenantId))
        .filter(rowSet -> rowSet.size() > 0)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("No config found")))
        .map(rows -> JsonUtils.rowSetToList(rows, configType).get(0));
  }
}
