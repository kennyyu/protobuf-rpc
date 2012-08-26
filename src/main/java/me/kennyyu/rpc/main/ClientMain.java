package me.kennyyu.rpc.main;

import me.kennyyu.flags.Flag;
import me.kennyyu.flags.FlagInfo;
import me.kennyyu.flags.Flags;
import me.kennyyu.rpc.RpcChannels;
import me.kennyyu.rpc.main.Math.AddRequest;
import me.kennyyu.rpc.main.Math.AddResponse;
import me.kennyyu.rpc.main.Math.DivideRequest;
import me.kennyyu.rpc.main.Math.DivideResponse;
import me.kennyyu.rpc.main.Math.MathService;
import me.kennyyu.rpc.main.Math.MultiplyRequest;
import me.kennyyu.rpc.main.Math.MultiplyResponse;
import me.kennyyu.rpc.main.Math.SubtractRequest;
import me.kennyyu.rpc.main.Math.SubtractResponse;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;

public final class ClientMain {

  private static enum MathOperation {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE
  }

  @FlagInfo(help = "math operation", altName = "m", environment = "testing")
  private static final Flag<MathOperation> mathOperation =
      Flags.valueOf(MathOperation.ADD);

  @FlagInfo(help = "first operand", altName = "f", environment = "testing")
  private static final Flag<Integer> firstOperand = Flags.valueOf(0);

  @FlagInfo(help = "second operand", altName = "s", environment = "testing")
  private static final Flag<Integer> secondOperand = Flags.valueOf(0);

  public static void main(String[] args) {
    Flags.parse(args, "", "testing");

    // create an rpc channel
    RpcChannel channel = RpcChannels.newBuilder()
        .withHost("localhost")
        .withPort(8080)
        .withExecutor(MoreExecutors.sameThreadExecutor())
        .build();

    // create a new stub
    MathService.Interface service = MathService.newStub(channel);

    // create a dummy controller
    RpcController controller = new RpcController() {
      @Override public String errorText() { return null; }
      @Override public boolean failed() { return false; }
      @Override public boolean isCanceled() { return false; }
      @Override public void notifyOnCancel(RpcCallback<Object> arg0) {}
      @Override public void reset() {}
      @Override public void setFailed(String arg0) {}
      @Override public void startCancel() {}
    };

    // create and send request
    switch (mathOperation.get()) {
      case ADD:
        service.add(
            controller,
            AddRequest.newBuilder()
                .setFirst(firstOperand.get())
                .setSecond(secondOperand.get())
                .build(),
            new RpcCallback<AddResponse>() {
              @Override
              public void run(AddResponse response) {
                System.out.println(response);
              }
            });
            break;

      case SUBTRACT:
        service.subtract(
            controller,
            SubtractRequest.newBuilder()
                .setFirst(firstOperand.get())
                .setSecond(secondOperand.get())
                .build(),
            new RpcCallback<SubtractResponse>() {
              @Override
              public void run(SubtractResponse response) {
                System.out.println(response);
              }
            });
            break;

      case MULTIPLY:
        service.multiply(
            controller,
            MultiplyRequest.newBuilder()
                .setFirst(firstOperand.get())
                .setSecond(secondOperand.get())
                .build(),
            new RpcCallback<MultiplyResponse>() {
              @Override
              public void run(MultiplyResponse response) {
                System.out.println(response);
              }
            });
            break;

      case DIVIDE:
        service.divide(
            controller,
            DivideRequest.newBuilder()
                .setDividend(firstOperand.get())
                .setDivisor(secondOperand.get())
                .build(),
            new RpcCallback<DivideResponse>() {
              @Override
              public void run(DivideResponse response) {
                System.out.println(response);
              }
            });
            break;
    }
  }
}
