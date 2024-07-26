package org.example.eventloopmetrics;

import io.netty.channel.EventLoop;
import io.netty.channel.SingleThreadEventLoop;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;

public class EventLoopPendingTasksDemo {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.runOnContext(v1 -> {
      Context ctx = vertx.getOrCreateContext();
      EventLoop nettyEventLoop = ((ContextInternal) ctx).nettyEventLoop();
      SingleThreadEventLoop nioEventLoop = (SingleThreadEventLoop) nettyEventLoop;

      System.out.println("Number of registered channels " + nioEventLoop.registeredChannels());

      // Should be 0
      System.out.println("Pending tasks " + nioEventLoop.pendingTasks());
      ctx.runOnContext(v2 -> {

      });
      // Should be 1
      System.out.println("Pending tasks " + nioEventLoop.pendingTasks());
    });
  }

}
