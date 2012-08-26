package me.kennyyu.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import me.kennyyu.rpc.proto.Rpc.ServiceRequest;
import me.kennyyu.rpc.proto.Rpc.ServiceResponse;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;

public final class Servers {
  private Servers() {};

  private static final class ServerImpl implements Server {
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final Map<String, Service> nameToServiceMap;

    public ServerImpl(
        ServerSocket serverSocket,
        ExecutorService executorService,
        Map<String, Service> nameToServiceMap) {
      this.serverSocket = serverSocket;
      this.executorService = executorService;
      this.nameToServiceMap = nameToServiceMap;
    }

    @Override
    public void start() {
      try {
        while (true) {
          Socket clientSocket = serverSocket.accept();
          InputStream inputStream = clientSocket.getInputStream();
          OutputStream outputStream = clientSocket.getOutputStream();
          executorService.execute(new ServerRunnable(
              inputStream, outputStream, nameToServiceMap));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static class ServerRunnable implements Runnable {
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Map<String, Service> nameToServiceMap;

    public ServerRunnable(
        InputStream inputStream,
        OutputStream outputStream,
        Map<String, Service> nameToServiceMap) {
      this.inputStream = inputStream;
      this.outputStream = outputStream;
      this.nameToServiceMap = nameToServiceMap;
    }

    @Override
    public void run() {
      try {
        final ServiceRequest serviceRequest =
            ServiceRequest.parseDelimitedFrom(inputStream);
        System.out.println(serviceRequest);

        // TODO(kennyyu): handle null case
        @Nullable Service service =
            nameToServiceMap.get(serviceRequest.getServiceName());
        MethodDescriptor methodDescriptor = service
            .getDescriptorForType()
            .findMethodByName(serviceRequest.getMethodName());
        Message request = MarshallerUtil.unmarshalRequest(
            service.getRequestPrototype(methodDescriptor),
            serviceRequest);
  
        // callback writes response back to output stream
        RpcCallback<Message> callback = new RpcCallback<Message>() {
          @Override
          public void run(Message responseMessage) {
            try {
              ServiceResponse response = ServiceResponse.newBuilder()
                  .setServiceName(serviceRequest.getServiceName())
                  .setMethodName(serviceRequest.getMethodName())
                  .setResponseProto(responseMessage.toByteString())
                  .build();
              response.writeDelimitedTo(outputStream);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        };
  
        // dummy controller
        RpcController controller = new RpcController() {
          @Override public String errorText() { return null; }
          @Override public boolean failed() { return false; }
          @Override public boolean isCanceled() { return false; }
          @Override public void notifyOnCancel(RpcCallback<Object> arg0) {}
          @Override public void reset() {}
          @Override public void setFailed(String arg0) {}
          @Override public void startCancel() {}
        };
  
        // execute the service call
        service.callMethod(methodDescriptor, controller, request, callback);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    private int port;
    private ExecutorService executorService;
    private List<Service> services;

    public Builder() {
      this.port = 0;
      this.executorService = Executors.newSingleThreadExecutor();
      this.services = Lists.newArrayList();
    }

    public Builder withPort(int port) {
      this.port = port;
      return this;
    }

    public Builder withExecutor(ExecutorService executorService) {
      this.executorService = executorService;
      return this;
    }

    public Builder addService(Service service) {
      services.add(service);
      return this;
    }

    public Server build() {
      try {
        ServerSocket serverSocket = new ServerSocket(port);
        Map<String, Service> nameToServiceMap = Maps.newHashMap();
        for (Service service : services) {
          nameToServiceMap.put(
              service.getDescriptorForType().getFullName(), service);
        }
        return new ServerImpl(serverSocket, executorService, nameToServiceMap);
      } catch (IOException e) {
        Throwables.propagate(e);
        return null;
      }
    }
  }
}
