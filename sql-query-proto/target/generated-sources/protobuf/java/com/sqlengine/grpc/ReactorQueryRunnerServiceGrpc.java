package com.sqlengine.grpc;

import static com.sqlengine.grpc.QueryRunnerServiceGrpc.getServiceDescriptor;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;


@javax.annotation.Generated(
value = "by ReactorGrpc generator",
comments = "Source: query_runner.proto")
public final class ReactorQueryRunnerServiceGrpc {
    private ReactorQueryRunnerServiceGrpc() {}

    public static ReactorQueryRunnerServiceStub newReactorStub(io.grpc.Channel channel) {
        return new ReactorQueryRunnerServiceStub(channel);
    }

    public static final class ReactorQueryRunnerServiceStub extends io.grpc.stub.AbstractStub<ReactorQueryRunnerServiceStub> {
        private QueryRunnerServiceGrpc.QueryRunnerServiceStub delegateStub;

        private ReactorQueryRunnerServiceStub(io.grpc.Channel channel) {
            super(channel);
            delegateStub = QueryRunnerServiceGrpc.newStub(channel);
        }

        private ReactorQueryRunnerServiceStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
            delegateStub = QueryRunnerServiceGrpc.newStub(channel).build(channel, callOptions);
        }

        @Override
        protected ReactorQueryRunnerServiceStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new ReactorQueryRunnerServiceStub(channel, callOptions);
        }

        public reactor.core.publisher.Mono<com.sqlengine.grpc.QueryRunResponse> runQuery(reactor.core.publisher.Mono<com.sqlengine.grpc.QueryRunRequest> reactorRequest) {
            return com.salesforce.reactorgrpc.stub.ClientCalls.oneToOne(reactorRequest, delegateStub::runQuery, getCallOptions());
        }

        public reactor.core.publisher.Mono<com.sqlengine.grpc.TableSchemaResponse> getTableSchema(reactor.core.publisher.Mono<com.sqlengine.grpc.TableSchemaRequest> reactorRequest) {
            return com.salesforce.reactorgrpc.stub.ClientCalls.oneToOne(reactorRequest, delegateStub::getTableSchema, getCallOptions());
        }

        public reactor.core.publisher.Mono<com.sqlengine.grpc.QueryRunResponse> runQuery(com.sqlengine.grpc.QueryRunRequest reactorRequest) {
           return com.salesforce.reactorgrpc.stub.ClientCalls.oneToOne(reactor.core.publisher.Mono.just(reactorRequest), delegateStub::runQuery, getCallOptions());
        }

        public reactor.core.publisher.Mono<com.sqlengine.grpc.TableSchemaResponse> getTableSchema(com.sqlengine.grpc.TableSchemaRequest reactorRequest) {
           return com.salesforce.reactorgrpc.stub.ClientCalls.oneToOne(reactor.core.publisher.Mono.just(reactorRequest), delegateStub::getTableSchema, getCallOptions());
        }

    }

    public static abstract class QueryRunnerServiceImplBase implements io.grpc.BindableService {

        public reactor.core.publisher.Mono<com.sqlengine.grpc.QueryRunResponse> runQuery(reactor.core.publisher.Mono<com.sqlengine.grpc.QueryRunRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        public reactor.core.publisher.Mono<com.sqlengine.grpc.TableSchemaResponse> getTableSchema(reactor.core.publisher.Mono<com.sqlengine.grpc.TableSchemaRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            com.sqlengine.grpc.QueryRunnerServiceGrpc.getRunQueryMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            com.sqlengine.grpc.QueryRunRequest,
                                            com.sqlengine.grpc.QueryRunResponse>(
                                            this, METHODID_RUN_QUERY)))
                    .addMethod(
                            com.sqlengine.grpc.QueryRunnerServiceGrpc.getGetTableSchemaMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            com.sqlengine.grpc.TableSchemaRequest,
                                            com.sqlengine.grpc.TableSchemaResponse>(
                                            this, METHODID_GET_TABLE_SCHEMA)))
                    .build();
        }

        protected io.grpc.CallOptions getCallOptions(int methodId) {
            return null;
        }

    }

    public static final int METHODID_RUN_QUERY = 0;
    public static final int METHODID_GET_TABLE_SCHEMA = 1;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final QueryRunnerServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(QueryRunnerServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_RUN_QUERY:
                    com.salesforce.reactorgrpc.stub.ServerCalls.oneToOne((com.sqlengine.grpc.QueryRunRequest) request,
                            (io.grpc.stub.StreamObserver<com.sqlengine.grpc.QueryRunResponse>) responseObserver,
                            serviceImpl::runQuery);
                    break;
                case METHODID_GET_TABLE_SCHEMA:
                    com.salesforce.reactorgrpc.stub.ServerCalls.oneToOne((com.sqlengine.grpc.TableSchemaRequest) request,
                            (io.grpc.stub.StreamObserver<com.sqlengine.grpc.TableSchemaResponse>) responseObserver,
                            serviceImpl::getTableSchema);
                    break;
                default:
                    throw new java.lang.AssertionError();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new java.lang.AssertionError();
            }
        }
    }

}
