package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_GRANT;

import com.dreamsportslabs.guardian.config.tenant.OidcConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.OidcCodeDao;
import com.dreamsportslabs.guardian.dao.model.OidcCodeModel;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcCodeService {

  private final OidcCodeDao oidcCodeDao;
  private final Registry registry;

  public Completable saveOidcCode(String code, OidcCodeModel oidcCodeModel, String tenantId) {
    OidcConfig oidcConfig = registry.get(tenantId, TenantConfig.class).getOidcConfig();
    return oidcCodeDao.saveOidcCode(code, oidcCodeModel, tenantId, oidcConfig.getAuthorizeTtl());
  }

  public Single<OidcCodeModel> getOidcCode(String code, String tenantId) {
    return oidcCodeDao
        .getOidcCode(code, tenantId)
        .onErrorResumeNext(
            err -> Single.error(INVALID_GRANT.getJsonCustomException(500, "code is invalid")));
  }

  public Completable deleteOidcCode(String code, String tenantId) {
    return oidcCodeDao.deleteOidcCode(code, tenantId);
  }
}
