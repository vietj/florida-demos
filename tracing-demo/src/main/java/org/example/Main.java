package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.http.*;

public class Main {
  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();

    HttpClient client = vertx.createHttpClient();

    HttpServer server = vertx
      .createHttpServer()
      .requestHandler(req -> {
        System.out.println("got req");
        switch (req.path()) {
          case "/foo":
            req.response().end();
            break;
          default:
            client.request(new RequestOptions().setURI("/foo").setHost("localhost").setPort(8080))
              .compose(creq -> creq
                .send()
                .compose(HttpClientResponse::body)).onSuccess(body -> {
                  req.response().end("Hello World " + body);
              });
            break;
        }
    });

    server.listen(8080, "localhost");


  }
}
