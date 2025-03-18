package com.dreamsportslabs.guardian.cache;

import com.dreamsportslabs.guardian.dao.RevocationDao;
import com.dreamsportslabs.guardian.utils.VertxUtil;
import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class RevocationsCache {
  private final AsyncLoadingCache<String, List<String>> cache;
  private static final String CACHE_NAME = "REVOCATION_CACHE";
  private static final long REVOCATIONS_EXPIRY = 120;

  @Inject
  public RevocationsCache(RevocationDao revocationDao, Vertx vertx) {
    this.cache = getOrCreateCacheInSharedData(revocationDao, vertx);
  }

  private AsyncLoadingCache<String, List<String>> getOrCreateCacheInSharedData(
      RevocationDao revocationDao, Vertx vertx) {
    return VertxUtil.getOrCreateSharedData(
        vertx.getDelegate(),
        CACHE_NAME,
        () ->
            Caffeine.newBuilder()
                .executor(
                    cmd -> {
                      Objects.requireNonNull(Vertx.currentContext());
                      Vertx.currentContext().runOnContext(v -> cmd.run());
                    })
                .expireAfterWrite(REVOCATIONS_EXPIRY, TimeUnit.SECONDS)
                .buildAsync(getLoader(revocationDao)));
  }

  private AsyncCacheLoader<String, List<String>> getLoader(RevocationDao revocationDao) {
    return (tenantId, executor) ->
        revocationDao.getRevocations(tenantId).toCompletionStage().toCompletableFuture();
  }

  public Single<List<String>> getRevocationList(String key) {
    return Single.fromCompletionStage(this.cache.get(key));
  }
}
