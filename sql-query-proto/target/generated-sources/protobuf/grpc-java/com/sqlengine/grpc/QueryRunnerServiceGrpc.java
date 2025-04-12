package com.sqlengine.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.1)",
    comments = "Source: query_runner.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class QueryRunnerServiceGrpc {

  private QueryRunnerServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "queryrunner.QueryRunnerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.sqlengine.grpc.QueryRunRequest,
      com.sqlengine.grpc.QueryRunResponse> getRunQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RunQuery",
      requestType = com.sqlengine.grpc.QueryRunRequest.class,
      responseType = com.sqlengine.grpc.QueryRunResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.sqlengine.grpc.QueryRunRequest,
      com.sqlengine.grpc.QueryRunResponse> getRunQueryMethod() {
    io.grpc.MethodDescriptor<com.sqlengine.grpc.QueryRunRequest, com.sqlengine.grpc.QueryRunResponse> getRunQueryMethod;
    if ((getRunQueryMethod = QueryRunnerServiceGrpc.getRunQueryMethod) == null) {
      synchronized (QueryRunnerServiceGrpc.class) {
        if ((getRunQueryMethod = QueryRunnerServiceGrpc.getRunQueryMethod) == null) {
          QueryRunnerServiceGrpc.getRunQueryMethod = getRunQueryMethod =
              io.grpc.MethodDescriptor.<com.sqlengine.grpc.QueryRunRequest, com.sqlengine.grpc.QueryRunResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RunQuery"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sqlengine.grpc.QueryRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sqlengine.grpc.QueryRunResponse.getDefaultInstance()))
              .setSchemaDescriptor(new QueryRunnerServiceMethodDescriptorSupplier("RunQuery"))
              .build();
        }
      }
    }
    return getRunQueryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.sqlengine.grpc.TableSchemaRequest,
      com.sqlengine.grpc.TableSchemaResponse> getGetTableSchemaMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTableSchema",
      requestType = com.sqlengine.grpc.TableSchemaRequest.class,
      responseType = com.sqlengine.grpc.TableSchemaResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.sqlengine.grpc.TableSchemaRequest,
      com.sqlengine.grpc.TableSchemaResponse> getGetTableSchemaMethod() {
    io.grpc.MethodDescriptor<com.sqlengine.grpc.TableSchemaRequest, com.sqlengine.grpc.TableSchemaResponse> getGetTableSchemaMethod;
    if ((getGetTableSchemaMethod = QueryRunnerServiceGrpc.getGetTableSchemaMethod) == null) {
      synchronized (QueryRunnerServiceGrpc.class) {
        if ((getGetTableSchemaMethod = QueryRunnerServiceGrpc.getGetTableSchemaMethod) == null) {
          QueryRunnerServiceGrpc.getGetTableSchemaMethod = getGetTableSchemaMethod =
              io.grpc.MethodDescriptor.<com.sqlengine.grpc.TableSchemaRequest, com.sqlengine.grpc.TableSchemaResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTableSchema"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sqlengine.grpc.TableSchemaRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sqlengine.grpc.TableSchemaResponse.getDefaultInstance()))
              .setSchemaDescriptor(new QueryRunnerServiceMethodDescriptorSupplier("GetTableSchema"))
              .build();
        }
      }
    }
    return getGetTableSchemaMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static QueryRunnerServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<QueryRunnerServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<QueryRunnerServiceStub>() {
        @java.lang.Override
        public QueryRunnerServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new QueryRunnerServiceStub(channel, callOptions);
        }
      };
    return QueryRunnerServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static QueryRunnerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<QueryRunnerServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<QueryRunnerServiceBlockingStub>() {
        @java.lang.Override
        public QueryRunnerServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new QueryRunnerServiceBlockingStub(channel, callOptions);
        }
      };
    return QueryRunnerServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static QueryRunnerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<QueryRunnerServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<QueryRunnerServiceFutureStub>() {
        @java.lang.Override
        public QueryRunnerServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new QueryRunnerServiceFutureStub(channel, callOptions);
        }
      };
    return QueryRunnerServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void runQuery(com.sqlengine.grpc.QueryRunRequest request,
        io.grpc.stub.StreamObserver<com.sqlengine.grpc.QueryRunResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRunQueryMethod(), responseObserver);
    }

    /**
     */
    default void getTableSchema(com.sqlengine.grpc.TableSchemaRequest request,
        io.grpc.stub.StreamObserver<com.sqlengine.grpc.TableSchemaResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTableSchemaMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service QueryRunnerService.
   */
  public static abstract class QueryRunnerServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return QueryRunnerServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service QueryRunnerService.
   */
  public static final class QueryRunnerServiceStub
      extends io.grpc.stub.AbstractAsyncStub<QueryRunnerServiceStub> {
    private QueryRunnerServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryRunnerServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new QueryRunnerServiceStub(channel, callOptions);
    }

    /**
     */
    public void runQuery(com.sqlengine.grpc.QueryRunRequest request,
        io.grpc.stub.StreamObserver<com.sqlengine.grpc.QueryRunResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRunQueryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTableSchema(com.sqlengine.grpc.TableSchemaRequest request,
        io.grpc.stub.StreamObserver<com.sqlengine.grpc.TableSchemaResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTableSchemaMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service QueryRunnerService.
   */
  public static final class QueryRunnerServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<QueryRunnerServiceBlockingStub> {
    private QueryRunnerServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryRunnerServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new QueryRunnerServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.sqlengine.grpc.QueryRunResponse runQuery(com.sqlengine.grpc.QueryRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRunQueryMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.sqlengine.grpc.TableSchemaResponse getTableSchema(com.sqlengine.grpc.TableSchemaRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTableSchemaMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service QueryRunnerService.
   */
  public static final class QueryRunnerServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<QueryRunnerServiceFutureStub> {
    private QueryRunnerServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryRunnerServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new QueryRunnerServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.sqlengine.grpc.QueryRunResponse> runQuery(
        com.sqlengine.grpc.QueryRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRunQueryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.sqlengine.grpc.TableSchemaResponse> getTableSchema(
        com.sqlengine.grpc.TableSchemaRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTableSchemaMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_RUN_QUERY = 0;
  private static final int METHODID_GET_TABLE_SCHEMA = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RUN_QUERY:
          serviceImpl.runQuery((com.sqlengine.grpc.QueryRunRequest) request,
              (io.grpc.stub.StreamObserver<com.sqlengine.grpc.QueryRunResponse>) responseObserver);
          break;
        case METHODID_GET_TABLE_SCHEMA:
          serviceImpl.getTableSchema((com.sqlengine.grpc.TableSchemaRequest) request,
              (io.grpc.stub.StreamObserver<com.sqlengine.grpc.TableSchemaResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getRunQueryMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.sqlengine.grpc.QueryRunRequest,
              com.sqlengine.grpc.QueryRunResponse>(
                service, METHODID_RUN_QUERY)))
        .addMethod(
          getGetTableSchemaMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.sqlengine.grpc.TableSchemaRequest,
              com.sqlengine.grpc.TableSchemaResponse>(
                service, METHODID_GET_TABLE_SCHEMA)))
        .build();
  }

  private static abstract class QueryRunnerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    QueryRunnerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.sqlengine.grpc.QueryRunner.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("QueryRunnerService");
    }
  }

  private static final class QueryRunnerServiceFileDescriptorSupplier
      extends QueryRunnerServiceBaseDescriptorSupplier {
    QueryRunnerServiceFileDescriptorSupplier() {}
  }

  private static final class QueryRunnerServiceMethodDescriptorSupplier
      extends QueryRunnerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    QueryRunnerServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (QueryRunnerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new QueryRunnerServiceFileDescriptorSupplier())
              .addMethod(getRunQueryMethod())
              .addMethod(getGetTableSchemaMethod())
              .build();
        }
      }
    }
    return result;
  }
}
