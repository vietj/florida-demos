package org.example.fileupload;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.streams.Pipe;

public class FileUploadDemo {

  public static void main(String[] args) {
    Vertx vertx = null;
    HttpServerRequest request = null;

    request.uploadHandler(fileUpload -> {

      // Process fileupload

      // Option 1
      fileUpload
        .streamToFileSystem("myupload.tmp")
        .onSuccess(v -> {
          // Then we can open myupload.tmp
        });

      // Option 3
      Pipe<Buffer> pipe = fileUpload.pipe();
      vertx.setTimer(10, id -> {
//        pipe.to(new );
      });

      // Option 2
      fileUpload.pause();
      vertx.setTimer(10, id -> {
        fileUpload.resume();
        Buffer buffer = Buffer.buffer();
        fileUpload.handler(buff -> {
          // Do something with buffers
          buffer.appendBuffer(buff);
        });
        fileUpload.endHandler(v -> {
          // End
          // Do something the buffer
        });
      });
    });
  }
}
