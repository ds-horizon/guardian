package com.dreamsportslabs.guardian.dao;

import com.dreamsportslabs.guardian.config.tenant.OIDCConfig;
import io.reactivex.rxjava3.core.Single;

public interface OIDCConfigDao {
  Single<OIDCConfig> getOIDCConfig(String tenantId);
}
