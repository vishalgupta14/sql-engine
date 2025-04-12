package com.sqlengine.model.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SubqueryBlock {
    private String name;   // e.g., active_users
    private String query;  // e.g., SELECT id FROM users WHERE active = 1
}
