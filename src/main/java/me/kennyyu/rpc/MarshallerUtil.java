package me.kennyyu.rpc;

import me.kennyyu.rpc.proto.Rpc.ServiceRequest;
import me.kennyyu.rpc.proto.Rpc.ServiceResponse;

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

public final class MarshallerUtil {
  private MarshallerUtil() {}

  public static ServiceRequest marshalRequest(
      MethodDescriptor methodDescriptor,
      Message request) {
    return ServiceRequest.newBuilder()
        .setServiceName(methodDescriptor.getService().getFullName())
        .setMethodName(methodDescriptor.getName())
        .setRequestProto(request.toByteString())
        .build();
  }

  public static Message unmarshalRequest(
      Message requestPrototype,
      ServiceRequest serviceRequest) {
    try {
      return requestPrototype
          .newBuilderForType()
          .mergeFrom(serviceRequest.getRequestProto().toByteArray())
          .build();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
      return requestPrototype;
    }
  }

  public static ServiceResponse marshalResponse(
      MethodDescriptor methodDescriptor,
      Message response) {
    return ServiceResponse.newBuilder()
        .setServiceName(methodDescriptor.getService().getFullName())
        .setMethodName(methodDescriptor.getName())
        .setResponseProto(response.toByteString())
        .build();
  }

  // TODO(kennyyu): throw exception? 
  // TODO(kennyyu): how to handle ServiceError?
  public static Message unmarshalResponse(
      Message responsePrototype,
      ServiceResponse serviceResponse) {
    try {
      return responsePrototype
          .newBuilderForType()
          .mergeFrom(serviceResponse.getResponseProto())
          .build();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
      return responsePrototype;
    }
  }
}
