package com.sqlengine.controller;

import com.sqlengine.dto.QueryExecutionRequest;
import com.sqlengine.enums.DatabaseProvider;
import com.sqlengine.manager.QueryTemplateCacheManager;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.service.DatabaseConfigService;
import com.sqlengine.service.GrpcMetadataClientService;
import com.sqlengine.service.GrpcQueryExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/query")
@RequiredArgsConstructor
public class QueryController {

    private final GrpcMetadataClientService grpcMetadataClientService;
    private final QueryTemplateCacheManager queryTemplateCacheManager;
    private final DatabaseConfigService databaseConfigService;
    private final GrpcQueryExecutionService executionService;

    /**
     * Run query using templateId and databaseConfigId with optional override conditions
     */
    @PostMapping("/run")
    public Mono<String> runQuery(@RequestBody QueryExecutionRequest request) {
        return queryTemplateCacheManager.getById(request.getTemplateId())
                .flatMap(template ->
                        databaseConfigService.findById(request.getDatabaseConfigId())
                                .flatMap(config ->
                                        executionService.runQuery(template, config, request.getOverrideConditions())
                                )
                )
                .onErrorResume(ex -> {
                    log.error("❌ Error during query execution", ex);
                    return Mono.just("❌ Error: " + ex.getMessage());
                });
    }

    /**
     * Fetch table schema either using templateId or direct tableName
     */
    @GetMapping("/table-schema")
    public Mono<ResponseEntity<Object>> getTableSchema(
            @RequestParam String configId,
            @RequestParam(required = false) String templateId,
            @RequestParam(required = false) String tableName
    ) {
        if (templateId == null && tableName == null) {
            return Mono.just(ResponseEntity.badRequest()
                    .body((Object) Map.of("error", "Either 'templateId' or 'tableName' must be provided")));
        }

        return databaseConfigService.findById(configId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No DatabaseConfig found for ID: " + configId)))
                .flatMap(config -> {
                    Mono<String> finalTableNameMono = (templateId != null)
                            ? queryTemplateCacheManager.getById(templateId).map(QueryTemplate::getTableName)
                            : Mono.justOrEmpty(tableName);

                    return finalTableNameMono
                            .flatMap(finalTableName ->
                                    grpcMetadataClientService.getTableSchema(configId, finalTableName)
                                            .map(columns -> ResponseEntity.ok((Object) Map.of(
                                                    "table", finalTableName,
                                                    "columns", columns
                                            )))
                            );
                })
                .onErrorResume(ex -> {
                    log.error("❌ Error fetching table schema", ex);
                    return Mono.just(ResponseEntity.internalServerError()
                            .body((Object) Map.of("error", ex.getMessage())));
                });
    }
}
