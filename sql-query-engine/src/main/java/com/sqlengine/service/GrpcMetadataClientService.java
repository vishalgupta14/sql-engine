package com.sqlengine.service;

import com.sqlengine.enums.DatabaseProvider;
import com.sqlengine.grpc.QueryRunnerServiceGrpc;
import com.sqlengine.grpc.QueryRunnerServiceGrpc.QueryRunnerServiceStub;
import com.sqlengine.grpc.TableSchemaRequest;
import com.sqlengine.grpc.TableSchemaResponse;
import com.sqlengine.manager.GrpcChannelHashRingManager;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcMetadataClientService {

    private final GrpcChannelHashRingManager channelManager;

    /**
     * Fetches the table schema from a remote gRPC metadata service using sticky routing based on configId.
     *
     * @param configId  The ID of the DatabaseConfig
     * @param tableName The table to fetch schema for
     * @return Mono emitting a List of Column Metadata Maps
     */
    public Mono<List<Map<String, Object>>> getTableSchema(String configId, String tableName) {
        ManagedChannel channel = channelManager.getChannelForKey(configId);
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
                        log.error("❌ gRPC error while fetching table schema");
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
