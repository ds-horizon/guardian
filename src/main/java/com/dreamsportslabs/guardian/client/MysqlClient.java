package com.dreamsportslabs.guardian.client;

import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.mysqlclient.MySQLPool;

public interface MysqlClient {

  MySQLPool getMasterClient();

  MySQLPool getSlaveClient();

  JsonObject getConfig();

  Completable rxConnect(JsonObject config);

  Completable rxClose();
}
