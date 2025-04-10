package com.sqlengine.mapper;

import com.sqlengine.enums.DatabaseProvider;
import com.sqlengine.enums.QueryOperator;
import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.model.query.QueryCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GrpcModelMapper {

    public static QueryTemplate toInternal(com.sqlengine.grpc.QueryTemplate proto) {
        QueryTemplate template = new QueryTemplate();
        template.setId(proto.getId());
        template.setTemplateName(proto.getTemplateName());
        template.setCreatedBy(proto.getCreatedBy());
        template.setQueryType(proto.getQueryType());
        template.setTableName(proto.getTableName());
        template.setConditions(proto.getConditionsList().stream()
                .map(GrpcModelMapper::toInternal).collect(Collectors.toList()));
        template.setHavingConditions(proto.getHavingConditionsList().stream()
                .map(GrpcModelMapper::toInternal).collect(Collectors.toList()));
        template.setDistinct(proto.getDistinct());
        template.setLimit(proto.getLimit());
        template.setOffset(proto.getOffset());
        return template;
    }

    public static DatabaseConfig toInternal(com.sqlengine.grpc.DatabaseConfig proto) {
        DatabaseConfig config = new DatabaseConfig();
        config.setId(proto.getId());
        config.setDatabaseConnectionName(proto.getDatabaseConnectionName());
        config.setProvider(DatabaseProvider.valueOf(proto.getProvider()));
        config.setConfig(new HashMap<>(proto.getConfigMap()));
        config.setActive(proto.getIsActive());
        config.setFallbackConfigId(proto.getFallbackConfigId());
        config.setPrivacyFallbackConfig(new HashMap<>(proto.getPrivacyFallbackConfigMap()));
        return config;
    }

    public static QueryCondition toInternal(com.sqlengine.grpc.QueryCondition c) {
        QueryCondition qc = new QueryCondition();
        qc.setFieldName(c.getFieldName());
        qc.setValue(c.getValue());
        qc.setOperator(QueryOperator.valueOf(c.getOperator()));
        qc.setFilterOperator(c.getFilterOperator());
        return qc;
    }

    public static com.sqlengine.grpc.QueryTemplate toProto(QueryTemplate template) {
        return com.sqlengine.grpc.QueryTemplate.newBuilder()
                .setId(template.getId())
                .setTemplateName(template.getTemplateName())
                .setCreatedBy(template.getCreatedBy())
                .setQueryType(template.getQueryType())
                .setTableName(template.getTableName())
                .addAllConditions(template.getConditions() != null
                        ? template.getConditions().stream().map(GrpcModelMapper::toProto).collect(Collectors.toList())
                        : List.of())
                .addAllHavingConditions(template.getHavingConditions() != null
                        ? template.getHavingConditions().stream().map(GrpcModelMapper::toProto).collect(Collectors.toList())
                        : List.of())
                .setDistinct(template.isDistinct())
                .setLimit(template.getLimit() != null ? template.getLimit() : 0)
                .setOffset(template.getOffset() != null ? template.getOffset() : 0)
                .build();
    }

    public static com.sqlengine.grpc.DatabaseConfig toProto(DatabaseConfig config) {
        return com.sqlengine.grpc.DatabaseConfig.newBuilder()
                .setId(config.getId())
                .setDatabaseConnectionName(config.getDatabaseConnectionName())
                .setProvider(config.getProvider().name())
                .putAllConfig(config.getConfig().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())))
                .setIsActive(config.isActive())
                .setFallbackConfigId(config.getFallbackConfigId() != null ? config.getFallbackConfigId() : "")
                .putAllPrivacyFallbackConfig(config.getPrivacyFallbackConfig().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())))
                .build();
    }

    public static com.sqlengine.grpc.QueryCondition toProto(QueryCondition c) {
        return com.sqlengine.grpc.QueryCondition.newBuilder()
                .setFieldName(c.getFieldName())
                .setValue(c.getValue())
                .setOperator(c.getOperator().name())
                .setFilterOperator(c.getFilterOperator())
                .build();
    }
}
