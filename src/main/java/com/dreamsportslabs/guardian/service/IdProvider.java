package com.dreamsportslabs.guardian.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public interface IdProvider {
  Single<JsonObject> getUserIdentity(String authorizationToken);
}
