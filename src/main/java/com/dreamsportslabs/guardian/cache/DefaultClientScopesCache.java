package com.dreamsportslabs.guardian.cache;

import com.dreamsportslabs.guardian.dao.model.ClientScopeModel;
import com.dreamsportslabs.guardian.service.ClientScopeService;
import com.dreamsportslabs.guardian.service.ClientService;
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
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Singleton
public class DefaultClientScopesCache {
  private final AsyncLoadingCache<String, Pair<String, List<String>>> cache;
  private static final String CACHE_NAME = "DEFAULT_CLIENT_SCOPE_CACHE";
  private static final long REVOCATIONS_EXPIRY = 120;

  @Inject
  public DefaultClientScopesCache(
      ClientService clientService, ClientScopeService clientScopeService, Vertx vertx) {
    this.cache = getOrCreateCacheInSharedData(clientService, clientScopeService, vertx);
  }

  private AsyncLoadingCache<String, Pair<String, List<String>>> getOrCreateCacheInSharedData(
      ClientService clientService, ClientScopeService clientScopeService, Vertx vertx) {
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
                .buildAsync(getLoader(clientService, clientScopeService)));
  }

  private AsyncCacheLoader<String, Pair<String, List<String>>> getLoader(
      ClientService clientService, ClientScopeService clientScopeService) {
    return (tenantId, executor) ->
        clientService
            .getDefaultClientId(tenantId)
            .flatMap(
                clientId ->
                    clientScopeService
                        .getClientScopes(clientId, tenantId)
                        .map(
                            clientScopes ->
                                clientScopes.stream()
                                    .filter(ClientScopeModel::getIsDefault)
                                    .map(ClientScopeModel::getScope)
                                    .toList())
                        .map(clientScopes -> Pair.of(clientId, clientScopes)))
            .toCompletionStage()
            .toCompletableFuture();
  }

  public Single<Pair<String, List<String>>> getDefaultClientScopes(String key) {
    return Single.fromCompletionStage(this.cache.get(key));
  }
}
