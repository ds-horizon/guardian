package com.dreamsportslabs.guardian.utils;

import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class VertxUtil {

  private static final String SHARED_DATA_MAP_NAME = "__vertx.sharedDataUtils";
  private static final String CLASS_PREFIX = "__class.";
  private static final String SHARED_DATA_DEFAULT_KEY = "__default.";

  private static final String CONTEXT_INSTANCE_PREFIX = "__vertx.contextUtils.";

  private VertxUtil() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Returns a singleton shared object across vert.x instance Note: It is your responsibility to
   * ensure T returned by supplier is thread-safe
   */
  public static <T> T getOrCreateSharedData(Vertx vertx, String name, Supplier<T> supplier) {
    LocalMap<String, ThreadSafe<T>> singletons =
        vertx.sharedData().getLocalMap(SHARED_DATA_MAP_NAME);
    // LocalMap is internally backed by a ConcurrentMap
    return singletons.computeIfAbsent(name, k -> new ThreadSafe<>(supplier.get())).object();
  }

  /**
   * Helper wrapper on getOrCreate to setInstance. Note: Doesn't reset the instance if already
   * exists
   */
  public static <T> void setInstanceInSharedData(Vertx vertx, T instance) {
    setInstanceInSharedData(vertx, instance, SHARED_DATA_DEFAULT_KEY);
  }

  /**
   * Helper wrapper on getOrCreate to setInstance. Note: Doesn't reset the instance if already
   * exists
   */
  public static <T> void setInstanceInSharedData(Vertx vertx, T instance, String key) {
    log.debug(
        "setInstanceInSharedData: vertx instance {} is setting type : {} for instance: {}  in key {}",
        System.identityHashCode(vertx),
        instance.getClass().getName(),
        System.identityHashCode(instance),
        key);
    getOrCreateSharedData(
        vertx, CLASS_PREFIX + instance.getClass().getName() + key, () -> instance);
  }

  /** Helper wrapper on getOrCreate to getInstance. */
  public static <T> T getInstanceFromSharedData(Vertx vertx, Class<T> clazz) {
    return getInstanceFromSharedData(vertx, clazz, SHARED_DATA_DEFAULT_KEY);
  }

  /** Helper wrapper on getOrCreate to getInstance. */
  public static <T> T getInstanceFromSharedData(Vertx vertx, Class<T> clazz, String key) {
    log.debug(
        "getInstanceFromSharedData: vertx instance {} is getting type : {}  in key {}",
        System.identityHashCode(vertx),
        clazz.getName(),
        key);
    return getOrCreateSharedData(
        vertx,
        CLASS_PREFIX + clazz.getName() + key,
        () -> {
          throw new NoSuchElementException("Cannot find default instance of " + clazz.getName());
        });
  }

  /**
   * Accessible from anywhere in this verticle instance. Note: This has to be set from one of the
   * VertxThreads (may cause NullPointerException otherwise) We are intentionally avoiding
   * vertx.getOrCreateContext() to ensure better coding practices
   */
  public static <T> void setInstanceInContext(String key, T object) {
    Vertx.currentContext().put(CONTEXT_INSTANCE_PREFIX + CLASS_PREFIX + key, object);
  }

  public static <T> T getInstanceFromContext(String key) {
    return Vertx.currentContext().get(CONTEXT_INSTANCE_PREFIX + CLASS_PREFIX + key);
  }

  record ThreadSafe<T>(@Getter T object) implements Shareable {}
}
