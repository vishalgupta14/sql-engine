package com.sqlengine.mapper;

import com.sqlengine.enums.DatabaseProvider;
import com.sqlengine.enums.QueryOperator;
import com.sqlengine.enums.SortDirection;
import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.model.query.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GrpcModelMapper {

    public static QueryTemplate toInternal(com.sqlengine.grpc.QueryTemplate proto) {
        QueryTemplate template = new QueryTemplate();
        template.setId(proto.getId());
        template.setTemplateName(proto.getTemplateName());
        template.setCreatedBy(proto.getCreatedBy());
        template.setQueryType(proto.getQueryType());
        template.setTableName(proto.getTableName());
        template.setConditions(proto.getConditionsList().stream().map(GrpcModelMapper::toInternal).collect(Collectors.toList()));
        template.setHavingConditions(proto.getHavingConditionsList().stream().map(GrpcModelMapper::toInternal).collect(Collectors.toList()));
        template.setDistinct(proto.getDistinct());
        template.setLimit(proto.getLimit());
        template.setOffset(proto.getOffset());
        template.setGroupBy(proto.getGroupByList());

        template.setCtes(proto.getCtesList().stream().map(c -> new CteBlock(c.getName(), c.getQuery())).collect(Collectors.toList()));
        template.setJoins(proto.getJoinsList().stream().map(j ->
                new JoinConfig(com.sqlengine.enums.JoinType.valueOf(j.getJoinType().name()), j.getTable(), j.getAlias(), j.getOnCondition())
        ).collect(Collectors.toList()));
        template.setSubqueries(proto.getSubqueriesList().stream().map(s -> new SubqueryBlock(s.getName(), s.getQuery())).collect(Collectors.toList()));
        template.setSelectedColumns(proto.getSelectedColumnsList().stream().map(col -> {
            SelectedColumn sc = new SelectedColumn();
            sc.setAlias(col.getAlias());
            sc.setExpression(col.getExpression());
            return sc;
        }).collect(Collectors.toList()));
        template.setUnions(proto.getUnionsList().stream().map(u -> {
            UnionQuery uq = new UnionQuery();
            uq.setUnionAll(u.getUnionAll());
            uq.setTemplate(toInternal(u.getTemplate()));
            return uq;
        }).collect(Collectors.toList()));

        template.setUpdatedValues(new HashMap<>(proto.getUpdatedValuesMap()));

        Map<String, SortDirection> orderMap = new LinkedHashMap<>();
        proto.getOrderByMap().forEach((k, v) -> orderMap.put(k, SortDirection.valueOf(v.name())));
        template.setOrderBy(orderMap);

        template.setCreatedAt(LocalDateTime.parse(proto.getCreatedAt()));
        template.setUpdatedAt(LocalDateTime.parse(proto.getUpdatedAt()));
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
        com.sqlengine.grpc.QueryTemplate.Builder builder = com.sqlengine.grpc.QueryTemplate.newBuilder()
                .setId(template.getId())
                .setTemplateName(template.getTemplateName())
                .setCreatedBy(template.getCreatedBy())
                .setQueryType(template.getQueryType())
                .setTableName(template.getTableName())
                .addAllConditions(template.getConditions() != null ?
                        template.getConditions().stream().map(GrpcModelMapper::toProto).collect(Collectors.toList()) : List.of())
                .addAllHavingConditions(template.getHavingConditions() != null ?
                        template.getHavingConditions().stream().map(GrpcModelMapper::toProto).collect(Collectors.toList()) : List.of())
                .setDistinct(template.isDistinct())
                .setLimit(Optional.ofNullable(template.getLimit()).orElse(0))
                .setOffset(Optional.ofNullable(template.getOffset()).orElse(0))
                .addAllGroupBy(template.getGroupBy() != null ? template.getGroupBy() : List.of())
                .putAllUpdatedValues(template.getUpdatedValues().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())))
                .putAllOrderBy(template.getOrderBy().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> com.sqlengine.grpc.SortDirection.valueOf(e.getValue().name()))))
                .setCreatedAt(Optional.ofNullable(template.getCreatedAt()).map(LocalDateTime::toString).orElse(""))
                .setUpdatedAt(Optional.ofNullable(template.getUpdatedAt()).map(LocalDateTime::toString).orElse(""));

        if (template.getCtes() != null)
            template.getCtes().forEach(cte -> builder.addCtes(
                    com.sqlengine.grpc.CteBlock.newBuilder().setName(cte.getName()).setQuery(cte.getQuery()).build()));

        if (template.getJoins() != null)
            template.getJoins().forEach(join -> builder.addJoins(
                    com.sqlengine.grpc.JoinConfig.newBuilder()
                            .setJoinType(com.sqlengine.grpc.JoinType.valueOf(join.getJoinType().name()))
                            .setTable(join.getTable())
                            .setAlias(join.getAlias())
                            .setOnCondition(join.getOnCondition())
                            .build()));

        if (template.getSelectedColumns() != null)
            template.getSelectedColumns().forEach(col -> builder.addSelectedColumns(
                    com.sqlengine.grpc.SelectedColumn.newBuilder()
                            .setAlias(Optional.ofNullable(col.getAlias()).orElse(""))
                            .setExpression(col.getExpression())
                            .build()));

        if (template.getSubqueries() != null)
            template.getSubqueries().forEach(sub -> builder.addSubqueries(
                    com.sqlengine.grpc.SubqueryBlock.newBuilder().setName(sub.getName()).setQuery(sub.getQuery()).build()));

        if (template.getUnions() != null)
            template.getUnions().forEach(union -> builder.addUnions(
                    com.sqlengine.grpc.UnionQuery.newBuilder()
                            .setUnionAll(union.isUnionAll())
                            .setTemplate(toProto(union.getTemplate()))
                            .build()));

        return builder.build();
    }

    public static com.sqlengine.grpc.DatabaseConfig toProto(DatabaseConfig config) {
        return com.sqlengine.grpc.DatabaseConfig.newBuilder()
                .setId(config.getId())
                .setDatabaseConnectionName(config.getDatabaseConnectionName())
                .setProvider(config.getProvider().name())
                .putAllConfig(config.getConfig().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())))
                .setIsActive(config.isActive())
                .setFallbackConfigId(Optional.ofNullable(config.getFallbackConfigId()).orElse(""))
                .putAllPrivacyFallbackConfig(config.getPrivacyFallbackConfig().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())))
                .build();
    }

    public static com.sqlengine.grpc.QueryCondition toProto(QueryCondition c) {
        return com.sqlengine.grpc.QueryCondition.newBuilder()
                .setFieldName(c.getFieldName())
                .setValue(c.getValue())
                .setOperator(c.getOperator().name())
                .setFilterOperator(Optional.ofNullable(c.getFilterOperator()).orElse(""))
                .build();
    }
}
