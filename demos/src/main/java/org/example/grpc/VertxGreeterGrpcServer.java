package org.example.grpc;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;

public class VertxGreeterGrpcServer  {
  public interface GreeterApi {

    default Future<HelloReply> sayHello(HelloRequest request) {
      throw new UnsupportedOperationException("Not implemented");
    }

    default void sayHello(HelloRequest request, Promise<HelloReply> response) {
      sayHello(request)
        .onSuccess(msg -> response.complete(msg))
        .onFailure(error -> response.fail(error));
    }

    default GreeterApi bind_sayHello(GrpcServer server) {
      // Bind
//      server.callHandler(GreeterGrpc.getSayHelloMethod(), request -> {
//        invoke_sayHello(request);
//      });
      return this;
    }

    default void invoke_sayHello(GrpcServerRequest<HelloRequest, HelloReply> request) {
      Promise<HelloReply> promise = Promise.promise();
      request.handler(req -> {
        try {
          sayHello(req, promise);
        } catch (RuntimeException err) {
          promise.tryFail(err);
        }
      });
      promise.future()
        .onFailure(err -> request.response().status(GrpcStatus.INTERNAL).end())
        .onSuccess(resp -> request.response().end(resp));
    }

    default GreeterApi bindAll(GrpcServer server) {
      bind_sayHello(server);
      return this;
    }
  }
}
