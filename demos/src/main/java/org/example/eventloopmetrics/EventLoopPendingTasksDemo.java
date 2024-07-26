package org.example.eventloopmetrics;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventLoopPendingTasksDemo {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    // A single event-loop used for accepting network sockets on server
    // should not execute user tasks
    // EventLoopGroup group = ((VertxInternal) vertx).getAcceptorEventLoopGroup();

    EventLoopGroup group = ((VertxInternal) vertx).getEventLoopGroup();
    List<EventLoop> eventLoops = new ArrayList<>();
    while (true) {
      EventLoop eventLoop = group.next();
      if (eventLoops.contains(eventLoop)) {
        break;
      }
      eventLoops.add(eventLoop);
    }

    eventLoops.forEach(el -> {
      el.scheduleAtFixedRate(() -> {
        SingleThreadEventLoop nioEventLoop = (SingleThreadEventLoop) el;
        System.out.println("EventLoop " + Thread.currentThread());
        System.out.println("Number of registered channels " + nioEventLoop.registeredChannels());
        System.out.println("Pending tasks " + nioEventLoop.pendingTasks());
      }, 0, 1, TimeUnit.SECONDS);
    });

/*
    vertx.runOnContext(v1 -> {
      vertx.setPeriodic(1000, id -> {
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
    });
*/
  }

}
