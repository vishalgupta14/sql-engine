package com.sqlengine.service;

import com.sqlengine.enums.DatabaseProvider;
import com.sqlengine.grpc.QueryRunnerServiceGrpc;
import com.sqlengine.grpc.QueryRunnerServiceGrpc.QueryRunnerServiceStub;
import com.sqlengine.grpc.TableSchemaRequest;
import com.sqlengine.grpc.TableSchemaResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GrpcMetadataClientService {

    private final Map<DatabaseProvider, Integer> providerPortMap;
    private final String host;
    private final Map<DatabaseProvider, ManagedChannel> channelPool = new ConcurrentHashMap<>();

    public GrpcMetadataClientService(
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

    /**
     * Fetches the table schema from a remote gRPC metadata service based on DB provider.
     *
     * @param configId  The ID of the DatabaseConfig
     * @param tableName The table to fetch schema for
     * @param provider  The database provider (MYSQL, POSTGRESQL, etc.)
     * @return Mono emitting a List of Column Metadata Maps
     */
    public Mono<List<Map<String, Object>>> getTableSchema(String configId, String tableName, DatabaseProvider provider) {
        ManagedChannel channel = channelPool.computeIfAbsent(provider, p ->
                ManagedChannelBuilder.forAddress(host, providerPortMap.get(p))
                        .usePlaintext()
                        .build());

        QueryRunnerServiceStub stub = QueryRunnerServiceGrpc.newStub(channel);

        TableSchemaRequest request = TableSchemaRequest.newBuilder()
                .setConfigId(configId)
                .setTableName(tableName)
                .build();

        return Mono.create(sink ->
                stub.getTableSchema(request, new StreamObserver<>() {
                    @Override
                    public void onNext(TableSchemaResponse response) {
                        List<Map<String, Object>> mapped = response.getColumnsList().stream().map(col -> {
                            Map<String, Object> map = new LinkedHashMap<>();
                            map.put("name", col.getName());
                            map.put("type", col.getType());
                            map.put("size", col.getSize());
                            map.put("nullable", col.getNullable());
                            map.put("remarks", col.getRemarks());
                            return map;
                        }).collect(Collectors.toList());

                        sink.success(mapped);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("❌ gRPC error while fetching table schema", t);
                        sink.error(t);
                    }

                    @Override
                    public void onCompleted() {
                        log.debug("✅ gRPC getTableSchema completed");
                    }
                })
        );
    }
}
