package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.dao.OidcCodeDao;
import com.dreamsportslabs.guardian.dao.model.OidcCodeModel;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcCodeService {

  private final OidcCodeDao oidcCodeDao;

  public Single<OidcCodeModel> getOidcCode(String code, String tenantId) {
    return oidcCodeDao.getOidcCode(code, tenantId);
  }

  public Completable deleteOidcCode(String code, String tenantId) {
    return oidcCodeDao.deleteOidcCode(code, tenantId);
  }
}
