package com.sqlengine.dto;

import com.sqlengine.model.QueryTemplate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CachedQueryTemplate {
    private final QueryTemplate template;
    private final String hash;
}
