package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.CookieSql.INVALIDATE_ALL_COOKIES_FOR_USER;
import static com.dreamsportslabs.guardian.dao.query.CookieSql.INVALIDATE_COOKIE;
import static com.dreamsportslabs.guardian.dao.query.CookieSql.SAVE_COOKIE;
import static com.dreamsportslabs.guardian.dao.query.CookieSql.VALIDATE_COOKIE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.CookieModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import jakarta.ws.rs.core.Cookie;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CookieDao {
  private final MysqlClient mysqlClient;

  public Single<CookieModel> saveCookie(CookieModel cookieModel) {
    return mysqlClient
        .getMasterClient()
        .preparedQuery(SAVE_COOKIE)
        .rxExecute(
            Tuple.wrap(
                Arrays.asList(
                    cookieModel.getTenantId(),
                    cookieModel.getUserId(),
                    cookieModel.getCookieName(),
                    cookieModel.getDomain(),
                    cookieModel.getPath(),
                    cookieModel.getSameSite(),
                    cookieModel.getCookieValue(),
                    cookieModel.getCookieExp(),
                    cookieModel.isActive(),
                    cookieModel.getSource(),
                    cookieModel.getDeviceName(),
                    cookieModel.getLocation(),
                    cookieModel.getIp())))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(res -> cookieModel);
  }

  public Maybe<String> getCookie(Cookie cookie, String tenantId) {
    return mysqlClient
        .getSlaveClient()
        .preparedQuery(VALIDATE_COOKIE)
        .rxExecute(Tuple.of(tenantId, cookie.getValue()))
        .filter(rowset -> rowset.size() > 0)
        .map(rows -> JsonUtils.rowSetToList(rows, String.class).get(0));
  }

  public Completable invalidateCookie(Cookie cookie, String tenantId) {
    return mysqlClient
        .getMasterClient()
        .preparedQuery(INVALIDATE_COOKIE)
        .rxExecute(Tuple.of(tenantId, cookie.getValue()))
        .ignoreElement();
  }

  public Completable invalidateAllCookiesForUser(String userId, String tenantId) {
    return mysqlClient
        .getMasterClient()
        .preparedQuery(INVALIDATE_ALL_COOKIES_FOR_USER)
        .rxExecute(Tuple.of(tenantId, userId))
        .ignoreElement();
  }
}
