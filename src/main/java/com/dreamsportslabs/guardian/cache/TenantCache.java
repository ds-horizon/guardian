package com.dreamsportslabs.guardian.cache;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.ConfigDao;
import com.dreamsportslabs.guardian.injection.GuiceInjector;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.registry.RegistryInit;
import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.reactivex.rxjava3.core.Single;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantCache {
  private final AsyncLoadingCache<String, TenantConfig> cache;
  private static TenantCache tenantCache;
  private final Registry registry;

  private TenantCache() {
    this.cache =
        Caffeine.newBuilder()
            // Todo: maybe make the duration configurable someplace (CONFIG)
            .refreshAfterWrite(Duration.ofSeconds(60))
            .buildAsync(getLoader(GuiceInjector.getGuiceInjector().getInstance(ConfigDao.class)));
    this.registry = GuiceInjector.getGuiceInjector().getInstance(Registry.class);
  }

  public static synchronized TenantCache getInstance() {
    if (tenantCache == null) {
      tenantCache = new TenantCache();
    }

    return tenantCache;
  }

  public Single<TenantConfig> getTenantConfig(String tenantId) {
    return Single.fromCompletionStage(cache.get(tenantId));
  }

  private AsyncCacheLoader<String, TenantConfig> getLoader(ConfigDao configDao) {
    return (tenantId, executor) ->
        configDao
            .getTenantConfig(tenantId)
            .map(config -> RegistryInit.initializeRegistry(registry, config))
            .toCompletionStage()
            .toCompletableFuture();
  }
}
