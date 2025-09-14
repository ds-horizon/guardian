package com.dreamsportslabs.guardian;

import static com.dreamsportslabs.guardian.constant.Constants.APPLICATION_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.APPLICATION_SHUTDOWN_GRACE_PERIOD;

import com.dreamsportslabs.guardian.injection.GuiceInjector;
import com.dreamsportslabs.guardian.injection.MainModule;
import com.dreamsportslabs.guardian.utils.ApplicationUtil;
import com.dreamsportslabs.guardian.utils.SharedDataUtils;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main extends Launcher {

  public static void main(String[] args) {
    Main launcher = new Main();
    launcher.dispatch(args);
  }

  @Override
  public void beforeStartingVertx(VertxOptions vertxOptions) {
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

  @Override
  public void beforeStoppingVertx(Vertx vertx) {
    JsonObject config = SharedDataUtils.get(vertx, JsonObject.class, APPLICATION_CONFIG);
    long shutdownDelayInterval =
        Long.parseLong(config.getString(APPLICATION_SHUTDOWN_GRACE_PERIOD));

    Completable.complete()
        .doOnComplete(() -> ApplicationUtil.setShutdownStatus(vertx))
        .delay(shutdownDelayInterval, TimeUnit.SECONDS)
        .doOnComplete(() -> log.info("Successfully stopped application"))
        .blockingSubscribe();
  }
}
