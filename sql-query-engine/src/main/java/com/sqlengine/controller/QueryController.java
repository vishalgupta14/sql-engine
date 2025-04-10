package com.sqlengine.controller;

import com.sqlengine.dto.QueryExecutionRequest;
import com.sqlengine.manager.QueryTemplateCacheManager;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.service.DatabaseConfigService;
import com.sqlengine.service.GrpcMetadataClientService;
import com.sqlengine.service.QueryExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    private final QueryExecutionService executionService;

    @PostMapping("/run")
    public Mono<String> runQuery(@RequestBody QueryExecutionRequest request) {
        QueryTemplate template = queryTemplateCacheManager.getById(request.getTemplateId());
        return databaseConfigService.findById(request.getDatabaseConfigId())
                .flatMap(config ->
                        executionService.runQuery(template, config, request.getOverrideConditions())
                );
    }

    @GetMapping("/table-schema")
    public Mono<ResponseEntity<Object>> getTableSchema(
            @RequestParam String configId,
            @RequestParam(required = false) String templateId,
            @RequestParam(required = false) String tableName
    ) {
        return Mono.defer(() -> {
            if (templateId == null && tableName == null) {
                return Mono.just(ResponseEntity.badRequest()
                        .body((Object) Map.of("error", "Either 'templateId' or 'tableName' must be provided")));
            }

            return Mono.justOrEmpty(templateId)
                    .flatMap(id -> Mono.fromCallable(() -> queryTemplateCacheManager.getById(id)))
                    .map(QueryTemplate::getTableName)
                    .switchIfEmpty(Mono.justOrEmpty(tableName))
                    .flatMap(finalTable ->
                            grpcMetadataClientService.getTableSchema(configId, finalTable)
                                    .map(schema -> ResponseEntity.ok((Object) Map.of(
                                            "table", finalTable,
                                            "columns", schema
                                    )))
                    )
                    .onErrorResume(ex -> {
                        log.error("❌ Error fetching table schema", ex);
                        return Mono.just(ResponseEntity.internalServerError()
                                .body((Object) Map.of("error", ex.getMessage())));
                    });
        });
    }


}
