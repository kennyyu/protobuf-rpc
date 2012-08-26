package me.kennyyu.rpc.main;

import java.util.concurrent.Executors;

import me.kennyyu.rpc.Server;
import me.kennyyu.rpc.Servers;
import me.kennyyu.rpc.proto.Math.AddRequest;
import me.kennyyu.rpc.proto.Math.AddResponse;
import me.kennyyu.rpc.proto.Math.DivideRequest;
import me.kennyyu.rpc.proto.Math.DivideResponse;
import me.kennyyu.rpc.proto.Math.MathService;
import me.kennyyu.rpc.proto.Math.MultiplyRequest;
import me.kennyyu.rpc.proto.Math.MultiplyResponse;
import me.kennyyu.rpc.proto.Math.SubtractRequest;
import me.kennyyu.rpc.proto.Math.SubtractResponse;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;

public final class ServerMain {

  private static class MathServiceImpl implements MathService.Interface {
    public MathServiceImpl() {}

    @Override
    public void add(RpcController controller, AddRequest request,
        RpcCallback<AddResponse> done) {
      AddResponse response = AddResponse.newBuilder()
          .setSum(request.getFirst() + request.getSecond())
          .build();
      done.run(response);
    }

    @Override
    public void subtract(RpcController controller, SubtractRequest request,
        RpcCallback<SubtractResponse> done) {
      SubtractResponse response = SubtractResponse.newBuilder()
          .setDifference(request.getFirst() - request.getSecond())
          .build();
      done.run(response);
    }

    @Override
    public void multiply(RpcController controller, MultiplyRequest request,
        RpcCallback<MultiplyResponse> done) {
      MultiplyResponse response = MultiplyResponse.newBuilder()
          .setProduct(request.getFirst() * request.getSecond())
          .build();
      done.run(response);
    }

    @Override
    public void divide(RpcController controller, DivideRequest request,
        RpcCallback<DivideResponse> done) {
      int divisor = request.getDivisor();
      int dividend = request.getDividend();
      DivideResponse response = divisor == 0 ?
          DivideResponse.newBuilder().setDivideByZero(true).build() :
          DivideResponse.newBuilder()
              .setQuotient(dividend / divisor)
              .setRemainder(dividend % divisor)
              .build();
      done.run(response);
    }
  }

  public static void main(String[] args) {
    Service service = MathService.newReflectiveService(new MathServiceImpl());
    Server server = Servers.newBuilder()
        .withPort(8080)
        .withExecutor(Executors.newCachedThreadPool())
        .addService(service)
        .build();
    server.start();
  }
}
