package org.example.eventbusinterceptor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import java.util.concurrent.atomic.AtomicInteger;

public class EventBusInterceptorDemo {

  public static void main(String[] args) throws Exception {

    Vertx vertx = Vertx.vertx();

    AtomicInteger inflight = new AtomicInteger();

    vertx.eventBus().addOutboundInterceptor(event -> {
      // On event-loop thread
      inflight.incrementAndGet();
      System.out.println(inflight);
      event.next();
    });

    vertx.eventBus().addInboundInterceptor(event -> {
      // On worker thread
      inflight.decrementAndGet();
      System.out.println(inflight);
      event.next();
    });

    vertx.deployVerticle(new AbstractVerticle() {
      @Override
      public void start() throws Exception {
        vertx.eventBus().consumer("foo", msg -> {
          System.out.println("got msg");
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        });
      }
    }, new DeploymentOptions().setWorker(true)).toCompletionStage().toCompletableFuture().get();

    for (int i = 0;i < 10;i++) {
      System.out.println("sent");
      vertx.eventBus().send("foo", "bar");
    }

  }
}
