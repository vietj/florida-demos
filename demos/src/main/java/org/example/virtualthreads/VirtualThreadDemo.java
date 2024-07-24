package org.example.virtualthreads;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;

public class VirtualThreadDemo {

  public static void main(String[] args) throws Exception {

    Vertx vertx = Vertx.vertx();

    HttpServer server = vertx.createHttpServer().requestHandler(req -> {
      vertx.setTimer(1000, id -> {
        req.response().end("Hello");
      });
    });
    server.listen(8080, "localhost").toCompletionStage().toCompletableFuture().get();
    HttpClient client = vertx.createHttpClient(new HttpClientOptions().setMaxPoolSize(100));

    int numTasks = 5;

    vertx.deployVerticle(() -> new AbstractVerticle() {
      @Override
      public void start() throws Exception {
        for (int i = 0;i < numTasks;i++) {
          int idx = i;
          context.runOnContext(v -> {

//            Future<Buffer> responseBody = client.request(HttpMethod.GET, 8080, "localhost", "/")
//              .compose(request -> request
//                .send()
//                .compose(response -> response.body()));
//            responseBody.onComplete(ar -> {
//
//            });


            try {
              System.out.println("Doing request");
              HttpClientRequest request = Future.await(client.request(HttpMethod.GET, 8080, "localhost", "/"));
              HttpClientResponse response = Future.await(request.send());
              System.out.println("Got response " + response.statusCode());
              Buffer body = Future.await(response.body());
              System.out.println("Got response body " + body);
            } catch (Exception e) {
              e.printStackTrace();
            }


          });
        }
      }
    }, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD));

    System.in.read();
  }
}
