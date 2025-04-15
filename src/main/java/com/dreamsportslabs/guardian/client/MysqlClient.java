package com.dreamsportslabs.guardian.client;

import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.mysqlclient.MySQLPool;

public interface MysqlClient {

  MySQLPool getWriterPool();

  MySQLPool getReaderPool();

  Completable rxConnect();

  Completable rxClose();
}
