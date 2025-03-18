package com.dreamsportslabs.guardian;

import com.dreamsportslabs.guardian.injection.GuiceInjector;
import com.dreamsportslabs.guardian.injection.MainModule;
import com.dreamsportslabs.guardian.utils.SharedDataUtils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import java.util.List;

public class Main extends Launcher {

  public static void main(String[] args) {
    Main launcher = new Main();
    launcher.dispatch(args);
  }

  @Override
  public void beforeStartingVertx(VertxOptions vertxOptions) {
    // Todo: customize options later on (CONFIG)
    vertxOptions
        .setEventLoopPoolSize(this.getNumOfCores())
        .setPreferNativeTransport(true)
        .setWorkerPoolSize(10);
  }

  @Override
  public void afterStartingVertx(Vertx vertx) {
    this.initializeGuiceInjector(vertx);
  }

  @Override
  public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
    deploymentOptions.setInstances(1);
  }

  private Integer getNumOfCores() {
    return CpuCoreSensor.availableProcessors();
  }

  private void initializeGuiceInjector(Vertx vertx) {
    GuiceInjector.initialize(List.of(new MainModule(vertx)));
    SharedDataUtils.put(vertx, GuiceInjector.class);
  }
}
