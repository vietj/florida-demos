package com.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class TestVerticle extends AbstractVerticle {

  public static final String TEST_ADDRESS = "test-address";

  static int value;

  @Override
  public void start(Promise<Void> startPromise) {
    int value = TestVerticle.value++;
    String addr = config().getString(TEST_ADDRESS);
    if (addr != null) {
      vertx.eventBus().request(addr, value).onComplete(ar -> {
        if (ar.succeeded()) {
          startPromise.complete();
        } else {
          startPromise.fail(ar.cause());
        }
      });
    }
  }
}
