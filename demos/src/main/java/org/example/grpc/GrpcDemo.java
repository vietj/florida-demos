package org.example.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
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
import java.util.function.Function;

public class GrpcDemo {

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
        GrpcServerRequest<HelloRequest, HelloReply> wrapper = new GrpcServerRequestForFunction<>(
          req,
          new Function<Buffer, HelloRequest>() {
            @Override
            public HelloRequest apply(Buffer buffer) {
              try {
                return HelloRequest.parseFrom(buffer.getBytes());
              } catch (InvalidProtocolBufferException e) {
                req.response().cancel();
                throw new RuntimeException(e);
              }
            }
          },
          new Function<HelloReply, Buffer>() {
            @Override
            public Buffer apply(HelloReply helloReply) {
              return Buffer.buffer(helloReply.toByteArray());
            }
          },
          context // Context of the function that is a worker context (same as upload :-) )
        );
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
