package me.kennyyu.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import me.kennyyu.rpc.proto.Rpc.ServiceRequest;
import me.kennyyu.rpc.proto.Rpc.ServiceResponse;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;

public final class RpcChannels {
  private RpcChannels() {};

  private static final class RpcChannelImpl implements RpcChannel {
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final ListeningExecutorService executorService;

    public RpcChannelImpl(
        InputStream inputStream,
        OutputStream outputStream,
        ListeningExecutorService executorService) {
      this.inputStream = inputStream;
      this.outputStream = outputStream;
      this.executorService = executorService;
    }

    @Override
    public void callMethod(
        MethodDescriptor methodDescriptor,
        RpcController controller,
        Message request,
        final Message responsePrototype,
        final RpcCallback<Message> done) {
      ServiceRequest serviceRequest =
          MarshallerUtil.marshalRequest(methodDescriptor, request);
      try {
        // send RPC request
        serviceRequest.writeDelimitedTo(outputStream);

        // create future to wrap response
        ListenableFuture<Message> future = executorService.submit(
            new Callable<Message>() {
              @Override
              public Message call() throws Exception {
                ServiceResponse serviceResponse =
                    ServiceResponse.parseDelimitedFrom(inputStream);
                return MarshallerUtil.unmarshalResponse(
                    responsePrototype, serviceResponse);
              }
            });

        // add a callback when response is received
        Futures.addCallback(future, new FutureCallback<Message>() {
          @Override
          public void onFailure(Throwable throwable) {
            throwable.printStackTrace();
          }

          @Override
          public void onSuccess(Message response) {
            done.run(response);
          }
        });
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    private String host;
    private int port;
    private ListeningExecutorService executorService;

    public Builder() {
      this.host = "";
      this.port = 0;
      this.executorService = MoreExecutors.sameThreadExecutor();
    }

    public Builder withHost(String host) {
      this.host = host;
      return this;
    }

    public Builder withPort(int port) {
      this.port = port;
      return this;
    }

    public Builder withExecutor(ListeningExecutorService executorService) {
      this.executorService = executorService;
      return this;
    }

    public RpcChannel build() {
      try {
        Socket socket = new Socket(host, port);
        return new RpcChannelImpl(
            socket.getInputStream(),
            socket.getOutputStream(),
            executorService);
      } catch (IOException e) {
        Throwables.propagate(e);
        return null;
      }
    }
  }
}
