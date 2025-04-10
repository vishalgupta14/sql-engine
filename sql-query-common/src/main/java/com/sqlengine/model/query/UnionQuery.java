package com.sqlengine.model.query;

import com.sqlengine.model.QueryTemplate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnionQuery {
    private QueryTemplate template;
    private boolean unionAll; // true = UNION ALL, false = UNION
}
