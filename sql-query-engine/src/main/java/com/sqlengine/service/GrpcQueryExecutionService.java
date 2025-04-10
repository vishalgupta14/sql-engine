package com.sqlengine.service;

import com.sqlengine.enums.DatabaseProvider;
import com.sqlengine.grpc.QueryRunRequest;
import com.sqlengine.grpc.QueryRunResponse;
import com.sqlengine.grpc.QueryRunnerServiceGrpc;
import com.sqlengine.mapper.GrpcModelMapper;
import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.model.query.QueryCondition;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class GrpcQueryExecutionService {

    private final Map<DatabaseProvider, Integer> providerPortMap;
    private final String host;
    private final Map<DatabaseProvider, ManagedChannel> channelPool = new ConcurrentHashMap<>();

    public GrpcQueryExecutionService(
            @Value("${grpc.execution.mysql.port}") int mysqlPort,
            @Value("${grpc.execution.mariadb.port}") int mariadbPort,
            @Value("${grpc.execution.postgresql.port}") int postgresPort,
            @Value("${grpc.execution.oracle.port}") int oraclePort,
            @Value("${grpc.execution.mssql.port}") int mssqlPort,
            @Value("${grpc.execution.host}") String host
    ) {
        this.host = host;

        this.providerPortMap = Map.of(
                DatabaseProvider.MYSQL, mysqlPort,
                DatabaseProvider.MARIADB, mariadbPort,
                DatabaseProvider.POSTGRESQL, postgresPort,
                DatabaseProvider.ORACLE, oraclePort,
                DatabaseProvider.MSSQL, mssqlPort
        );
    }

    public Mono<String> runQuery(QueryTemplate template, DatabaseConfig config, List<QueryCondition> override) {
        DatabaseProvider provider = config.getProvider();

        // Resolve gRPC channel by provider
        ManagedChannel channel = channelPool.computeIfAbsent(provider, p ->
                ManagedChannelBuilder.forAddress(host, providerPortMap.get(p))
                        .usePlaintext()
                        .build());

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
                    public void onCompleted() {}
                })
        );
    }
}

