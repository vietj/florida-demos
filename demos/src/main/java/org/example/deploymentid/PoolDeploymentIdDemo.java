package org.example.deploymentid;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PoolDeploymentIdDemo {

  private static class Pool {
    private int maxSize;
  }

  private static final Map<String, Pool> pools = new ConcurrentHashMap<>();

  private static synchronized Pool getPool() {
    String id = Vertx.currentContext().deploymentID();
    Pool pool = PoolDeploymentIdDemo.pools.get(id);
    if (pool == null) {
      pool = new Pool();
      pool.maxSize = Vertx.currentContext().getInstanceCount();
      pools.put(id, pool);
    }
    return pool;
  }

  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(() -> new AbstractVerticle() {
      @Override
      public void start() {
        Pool pool = getPool();
        System.out.println("Got pool " + pool);
      }
    }, new DeploymentOptions().setInstances(20));

  }
}
