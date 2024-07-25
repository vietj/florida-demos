package org.example.webrouting;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;

public class WebRoutingDemo {

  public static void main(String[] args) throws Exception {

    Vertx vertx = Vertx.vertx();

    Router router = Router.router(vertx);

    HttpServer server = vertx
      .createHttpServer()
      .requestHandler(router);

    Router sub = Router.router(vertx);
    sub.route("/a").handler(rc -> {
      System.out.println("got request");
      System.out.println(rc.pathParams());
    });

    router
      .route("/path/to/*")
      .subRouter(sub);

    server.listen(8080, "localhost")
      .toCompletionStage()
      .toCompletableFuture()
      .get();

    HttpClient client = vertx.createHttpClient();

    Future<Buffer> f = client.request(new RequestOptions()
        .setHost("localhost")
        .setPort(8080)
        .setURI("/path/to/a")
      )
      .compose(req -> req
        .send()
        .compose(HttpClientResponse::body));

  }
}
