package org.example.jfrthreadevents;

import jdk.jfr.consumer.RecordingStream;

public class JFRThreadEventsDemo {
  public static void main(String[] args) throws Exception {

    // https://todd.ginsberg.com/post/java/virtual-thread-pinning/
    // https://bestsolution-at.github.io/jfr-doc/openjdk-18.html

    // Todo: change ofPlatform -> ofVirtual
    Thread.ofPlatform().name("vt1").start(() -> {
      while (true) {
        try {
          synchronized (JFRThreadEventsDemo.class) {
            // Pin the thread when it is virtual
            Thread.sleep(2000);
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });

    try (var rs = new RecordingStream()) {
      rs.enable("jdk.ThreadSleep");
      rs.enable("jdk.VirtualThreadPinned");
      rs.onEvent(e -> {
        System.out.println(e);
      });
      rs.start();
    }
  }
}
