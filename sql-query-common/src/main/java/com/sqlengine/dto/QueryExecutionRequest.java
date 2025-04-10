package com.sqlengine.dto;

import com.sqlengine.model.query.QueryCondition;
import lombok.Data;

import java.util.List;

@Data
public class QueryExecutionRequest {
    private String templateId;
    private String databaseConfigId;
    private List<QueryCondition> overrideConditions;
}
