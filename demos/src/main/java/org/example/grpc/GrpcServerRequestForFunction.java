package org.example.grpc;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpConnection;
import io.vertx.grpc.common.GrpcError;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.common.ServiceName;
import io.vertx.grpc.server.GrpcServerRequest;
import io.vertx.grpc.server.GrpcServerResponse;

import java.util.function.Function;

public class GrpcServerRequestForFunction<Req, Resp> implements GrpcServerRequest<Req, Resp> {

  private final GrpcServerRequest<Buffer, Buffer> delegate;
  private final Function<Buffer, Req> decoder;
  private final Function<Resp, Buffer> encoder;
  private final Context context;

  public GrpcServerRequestForFunction(GrpcServerRequest<Buffer, Buffer> delegate, Function<Buffer, Req> decoder, Function<Resp, Buffer> encoder, Context context) {
    this.delegate = delegate;
    this.decoder = decoder;
    this.encoder = encoder;
    this.context = context;
  }

  @Override
  public ServiceName serviceName() {
    return delegate.serviceName();
  }

  @Override
  public String methodName() {
    return delegate.methodName();
  }

  @Override
  public String fullMethodName() {
    return delegate.fullMethodName();
  }

  @Override
  public GrpcServerResponse<Req, Resp> response() {
    // Need to implement GrpcServerResponse
    return null;
  }

  @Override
  public GrpcServerRequest<Req, Resp> messageHandler(Handler<GrpcMessage> handler) {
    if (handler == null) {
      delegate.messageHandler(null);
    } else {
      delegate.messageHandler(msg -> {
        context.runOnContext(v -> handler.handle(msg));
      });
    }
    return this;
  }

  @Override
  public GrpcServerRequest<Req, Resp> errorHandler(Handler<GrpcError> handler) {
    return null;
  }

  @Override
  public GrpcServerRequest<Req, Resp> exceptionHandler(Handler<Throwable> handler) {
    return null;
  }

  @Override
  public GrpcServerRequest<Req, Resp> handler(Handler<Req> handler) {
    if (handler == null) {
      delegate.handler(null);
    } else {
      delegate.handler(msg -> {
        context.runOnContext(v -> {
          handler.handle(decoder.apply(msg));
        });
      });
    }
    return this;
  }

  @Override
  public GrpcServerRequest<Req, Resp> pause() {
    return null;
  }

  @Override
  public GrpcServerRequest<Req, Resp> resume() {
    return null;
  }

  @Override
  public GrpcServerRequest<Req, Resp> fetch(long amount) {
    return null;
  }

  @Override
  public GrpcServerRequest<Req, Resp> endHandler(Handler<Void> endHandler) {
    if (endHandler == null) {
      delegate.endHandler(null);
    } else {
      delegate.endHandler(msg -> {
        context.runOnContext(v -> {
          endHandler.handle(null);
        });
      });
    }
    return this;
  }

  @Override
  public HttpConnection connection() {
    return null;
  }

  @Override
  public MultiMap headers() {
    return null;
  }

  @Override
  public String encoding() {
    return "";
  }

  @Override
  public Future<Req> last() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Future<Void> end() {
    throw new UnsupportedOperationException();
  }
}
