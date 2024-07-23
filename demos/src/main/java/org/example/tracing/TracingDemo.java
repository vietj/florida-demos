package org.example.tracing;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.spi.tracing.SpanKind;
import io.vertx.core.spi.tracing.TagExtractor;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingPolicy;

import java.util.function.BiConsumer;

public class TracingDemo {
  public static void main(String[] args) {

    Vertx vertx = Vertx.builder().withTracer(
      options -> new VertxTracer() {
        @Override
        public Object receiveRequest(Context context, SpanKind kind, TracingPolicy policy, Object request, String operation, Iterable headers, TagExtractor tagExtractor) {
          String path = ((io.vertx.core.spi.observability.HttpRequest) request).uri();
          System.out.println("receiveRequest " + path);
          // Span
          return path;
        }
        @Override
        public void sendResponse(Context context, Object response, Object payload, Throwable failure, TagExtractor tagExtractor) {
          System.out.println("sendResponse " + payload);
        }
        @Override
        public Object sendRequest(Context context, SpanKind kind, TracingPolicy policy, Object request, String operation, BiConsumer headers, TagExtractor tagExtractor) {
          String path = ((io.vertx.core.spi.observability.HttpRequest) request).uri();
          System.out.println("sendRequest " + path);
          return path;
        }
        @Override
        public void receiveResponse(Context context, Object response, Object payload, Throwable failure, TagExtractor tagExtractor) {
          System.out.println("receiveResponse " + payload);
        }
      }
    ).build();

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
