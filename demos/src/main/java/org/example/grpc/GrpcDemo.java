package org.example.grpc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GrpcDemo {

  public static <Req, Resp> GrpcServerRequest<Req, Resp> wrap(GrpcServerRequest<Buffer, Buffer> req, Context workerContext) {
    throw new UnsupportedOperationException();
  }

  private static Map<String, Future<String>> lookupMap = new ConcurrentHashMap<>();

  private static GrpcServerRequest<Buffer, Buffer> unwrapOriginalGrpcRequest(Message<?> msg) {
    return (GrpcServerRequest<Buffer, Buffer>) msg.body();
  }

  private static Future<String> resolveServiceEventBusAddress(String fullMethodName) {
    throw new UnsupportedOperationException();
  }

  public static class MyFunction extends AbstractVerticle {
    @Override
    public void start() throws Exception {

      VertxGreeterGrpcServer.GreeterApi service = new VertxGreeterGrpcServer.GreeterApi() {
        @Override
        public Future<HelloReply> sayHello(HelloRequest request) {
          // Implementation
          return VertxGreeterGrpcServer.GreeterApi.super.sayHello(request);
        }
      };

      // Register in lookupMap
      vertx.eventBus().consumer("the-function-address", msg -> {
        GrpcServerRequest<Buffer, Buffer> req = unwrapOriginalGrpcRequest(msg);
        req.resume();
        GrpcServerRequest<HelloRequest, HelloReply> wrapper = wrap(req, context);
        service.invoke_sayHello(wrapper);
      });
    }
  }

  public static void main(String[] args) throws Exception {

    Vertx vertx = Vertx.vertx();

    GrpcServer server = GrpcServer.server(vertx);

    server.callHandler(request -> {

      String fullMethodName = request.fullMethodName();

      Future<String> fut = resolveServiceEventBusAddress(fullMethodName);

      request.pause();

      vertx.eventBus().registerDefaultCodec(GrpcServerRequest.class, new MessageCodec<>() {
        @Override
        public void encodeToWire(Buffer buffer, GrpcServerRequest o) {
          throw new UnsupportedOperationException();
        }

        @Override
        public Object decodeFromWire(int pos, Buffer buffer) {
          return null;
        }

        @Override
        public Object transform(GrpcServerRequest o) {
          return o;
        }

        @Override
        public String name() {
          return "";
        }

        @Override
        public byte systemCodecID() {
          return -1;
        }
      });

      fut.onComplete(ar -> {
        if (ar.succeeded()) {
          String address = ar.result();
          //
          MessageProducer<Object> producer = vertx.eventBus().sender(address);
          producer.write(request); // In practice it is a wrapper of the request
        } else {
          request.resume();
          request.response().status(GrpcStatus.NOT_FOUND).end();
        }
      });
    });

    vertx.createHttpServer()
      .requestHandler(server)
      .listen(8080, "localhost")
      .toCompletionStage()
      .toCompletableFuture()
      .get();

  }
}
