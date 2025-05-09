package com.sqlengine.service;

import com.sqlengine.grpc.QueryRunRequest;
import com.sqlengine.grpc.QueryRunResponse;
import com.sqlengine.grpc.QueryRunnerServiceGrpc;
import com.sqlengine.manager.GrpcChannelHashRingManager;
import com.sqlengine.mapper.GrpcModelMapper;
import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.model.query.QueryCondition;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
@RequiredArgsConstructor
public class GrpcQueryExecutionService {

    private final GrpcChannelHashRingManager channelManager;

    public Mono<String> runQuery(QueryTemplate template, DatabaseConfig config, List<QueryCondition> override) {
        String key = config.getId();

        ManagedChannel channel = channelManager.getChannelForKey(key);
        QueryRunnerServiceGrpc.QueryRunnerServiceStub stub = QueryRunnerServiceGrpc.newStub(channel);

        QueryRunRequest request = QueryRunRequest.newBuilder()
                .setTemplate(GrpcModelMapper.toProto(template))
                .setConfig(GrpcModelMapper.toProto(config))
                .addAllOverrideConditions(override.stream().map(GrpcModelMapper::toProto).toList())
                .build();

        return Mono.create(sink ->
                stub.runQuery(request, new StreamObserver<>() {
                    @Override
                    public void onNext(QueryRunResponse value) {
                        sink.success(value.getJsonResult());
                    }

                    @Override
                    public void onError(Throwable t) {
                        sink.error(t);
                    }

                    @Override
                    public void onCompleted() {
                    }
                })
        );
    }
}


