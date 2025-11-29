package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_NOT_EXISTS;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.config.tenant.OidcConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.OidcCodeDao;
import com.dreamsportslabs.guardian.dao.model.OidcCodeModel;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcCodeService {

  private final OidcCodeDao oidcCodeDao;
  private final Registry registry;

  public Completable saveOidcCode(String code, OidcCodeModel oidcCodeModel, String tenantId) {
    OidcConfig oidcConfig = registry.get(tenantId, TenantConfig.class).getOidcConfig();
    if (oidcConfig == null) {
      return Completable.error(OIDC_CONFIG_NOT_EXISTS.getException());
    }
    return oidcCodeDao.saveOidcCode(code, oidcCodeModel, tenantId, oidcConfig.getAuthorizeTtl());
  }

  public Maybe<OidcCodeModel> getOidcCode(String code, String tenantId) {
    return oidcCodeDao
        .getOidcCode(code, tenantId)
        .onErrorResumeNext(
            err ->
                Maybe.error(INTERNAL_SERVER_ERROR.getJsonCustomException(500, "code is invalid")));
  }

  public Completable deleteOidcCode(String code, String tenantId) {
    return oidcCodeDao.deleteOidcCode(code, tenantId);
  }
}
