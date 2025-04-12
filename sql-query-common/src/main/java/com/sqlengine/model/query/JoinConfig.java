package com.sqlengine.model.query;

import com.sqlengine.enums.JoinType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JoinConfig {
    private JoinType joinType;     // INNER, LEFT, RIGHT
    private String table;        // e.g., "customers"
    private String alias;        // optional, e.g., "c"
    private String onCondition;  // e.g., "orders.customer_id = c.id"
}
