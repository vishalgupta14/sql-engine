package com.sqlengine.service;

import com.sqlengine.grpc.ColumnInfo;
import com.sqlengine.grpc.QueryRunRequest;
import com.sqlengine.grpc.QueryRunResponse;
import com.sqlengine.grpc.QueryRunnerServiceGrpc;
import com.sqlengine.grpc.TableSchemaRequest;
import com.sqlengine.grpc.TableSchemaResponse;
import com.sqlengine.manager.DatabaseConnectionPoolManager;
import com.sqlengine.mapper.GrpcModelMapper;
import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.repository.DatabaseConfigRepository;
import com.sqlengine.strategy.QueryExecutionStrategy;
import com.sqlengine.strategy.QueryExecutionStrategyFactory;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QueryRunnerServiceImpl extends QueryRunnerServiceGrpc.QueryRunnerServiceImplBase {

    private final QueryExecutionStrategyFactory strategyFactory;
    private final DatabaseConnectionPoolManager poolManager;
    private final DatabaseConfigRepository repository;

    public QueryRunnerServiceImpl(QueryExecutionStrategyFactory strategyFactory,
                                  DatabaseConnectionPoolManager poolManager, DatabaseConfigRepository repository) {
        this.strategyFactory = strategyFactory;
        this.poolManager = poolManager;
        this.repository = repository;
    }

    @Override
    public void runQuery(QueryRunRequest request, StreamObserver<QueryRunResponse> responseObserver) {
        try {
            // Convert to internal model
            QueryTemplate template = GrpcModelMapper.toInternal(request.getTemplate());
            DatabaseConfig config = GrpcModelMapper.toInternal(request.getConfig());

            if (!request.getOverrideConditionsList().isEmpty()) {
                template.setConditions(request.getOverrideConditionsList().stream()
                        .map(GrpcModelMapper::toInternal).collect(Collectors.toList()));
            }

            DatabaseClient dbClient = poolManager.getDatabaseClient(config);

            if (template.getSqlQuery() != null && !template.getSqlQuery().isBlank()) {
                dbClient.sql(template.getSqlQuery())
                        .fetch()
                        .all()
                        .collectList()
                        .map(result -> QueryRunResponse.newBuilder().setJsonResult(result.toString()).build())
                        .subscribe(responseObserver::onNext,
                                error -> {
                                    responseObserver.onError(error);
                                    log.error("Native SQL execution failed", error);
                                },
                                responseObserver::onCompleted);
            } else {
                QueryExecutionStrategy strategy = strategyFactory.getStrategy(template.getQueryType());

                strategy.execute(template, config, dbClient)
                        .map(Object::toString)
                        .map(result -> QueryRunResponse.newBuilder().setJsonResult(result).build())
                        .subscribe(responseObserver::onNext,
                                error -> {
                                    responseObserver.onError(error);
                                    log.error("Query execution failed", error);
                                },
                                responseObserver::onCompleted);
            }

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }


    @Override
    public void getTableSchema(TableSchemaRequest request, StreamObserver<TableSchemaResponse> responseObserver) {
        String configId = request.getConfigId();
        String tableName = request.getTableName();

        repository.findById(configId)
                .flatMap(config -> Mono.fromCallable(() -> {
                    DataSource ds = poolManager.getDataSource(config);
                    List<ColumnInfo> columns = new ArrayList<>();

                    try (Connection conn = ds.getConnection()) {
                        DatabaseMetaData meta = conn.getMetaData();
                        try (ResultSet rs = meta.getColumns(null, null, tableName, null)) {
                            while (rs.next()) {
                                columns.add(ColumnInfo.newBuilder()
                                        .setName(rs.getString("COLUMN_NAME"))
                                        .setType(rs.getString("TYPE_NAME"))
                                        .setSize(rs.getInt("COLUMN_SIZE"))
                                        .setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable)
                                        .setRemarks(rs.getString("REMARKS"))
                                        .build());
                            }
                        }
                    }

                    return TableSchemaResponse.newBuilder()
                            .addAllColumns(columns)
                            .build();
                }))
                .subscribe(
                        responseObserver::onNext,
                        responseObserver::onError,
                        responseObserver::onCompleted
                );
    }

}
