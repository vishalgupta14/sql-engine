package com.sqlengine.service;

import com.sqlengine.grpc.QueryRunnerServiceGrpc;
import com.sqlengine.grpc.QueryRunnerServiceGrpc.QueryRunnerServiceStub;
import com.sqlengine.grpc.TableSchemaRequest;
import com.sqlengine.grpc.TableSchemaResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GrpcMetadataClientService {

    private final QueryRunnerServiceStub stub;

    public GrpcMetadataClientService() {
        // Replace host and port with actual gRPC server address
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext()
                .build();

        this.stub = QueryRunnerServiceGrpc.newStub(channel);
    }

    /**
     * Fetches the table schema from a remote gRPC service.
     *
     * @param configId  The ID of the DatabaseConfig
     * @param tableName The table to fetch schema for
     * @return Mono emitting a List of Column Metadata Maps
     */
    public Mono<List<Map<String, Object>>> getTableSchema(String configId, String tableName) {
        TableSchemaRequest request = TableSchemaRequest.newBuilder()
                .setConfigId(configId)
                .setTableName(tableName)
                .build();

        return Mono.create(sink ->
                stub.getTableSchema(request, new StreamObserver<TableSchemaResponse>() {
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
