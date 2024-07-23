package org.example.micrometerworkerpool;

import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.core.*;
import io.vertx.micrometer.Label;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;

import java.util.concurrent.atomic.AtomicInteger;

public class MicrometerWorkerPoolDemo {
  public static void main(String[] args) throws Exception {

    Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(new MicrometerMetricsOptions()
        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
        .addLabels(Label.POOL_NAME)
        .setEnabled(true)));

    int instances =  100;

    AtomicInteger waiting = new AtomicInteger(0);
    AtomicInteger done = new AtomicInteger(0);

    vertx.deployVerticle(() -> new AbstractVerticle() {
      @Override
      public void start() throws Exception {
        waiting.incrementAndGet();
        Thread.sleep(1000);
        done.incrementAndGet();
      }
    }, new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER).setInstances(instances));

    while (waiting.get() < 20) {
      Thread.sleep(1);
    }

    MeterRegistry registry = BackendRegistries.getNow(MicrometerMetricsOptions.DEFAULT_REGISTRY_NAME);
    registry.getMeters().forEach(meter -> {
      System.out.println(meter.getId() + " " + meter.measure());
    });

    while (done.get() < 100) {
      Thread.sleep(1);
    }

    System.out.println("------------------------------");

    registry.getMeters().forEach(meter -> {
      System.out.println(meter.getId() + " " + meter.measure());
    });
  }
}
