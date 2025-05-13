package com.dreamsportslabs.guardian.injection;

import com.dream11.rest.ClassInjector;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.List;
import java.util.Objects;

public class GuiceInjector implements ClassInjector {
  private static GuiceInjector guiceInjector;
  private final Injector injector;

  private GuiceInjector(List<Module> modules) {
    injector = Guice.createInjector(modules);
  }

  public static synchronized void initialize(List<Module> modules) {
    if (guiceInjector != null) {
      throw new IllegalStateException("GuiceInjector is already initialised");
    } else {
      guiceInjector = new GuiceInjector(modules);
    }
  }

  public static GuiceInjector getGuiceInjector() {
    if (guiceInjector == null) {
      throw new IllegalStateException("GuiceInjector not initialised");
    }
    return guiceInjector;
  }

  @Override
  public <T> T getInstance(Class<T> clazz) {
    Objects.requireNonNull(injector, "injector is null, initialize first");
    return injector.getInstance(clazz);
  }
}
