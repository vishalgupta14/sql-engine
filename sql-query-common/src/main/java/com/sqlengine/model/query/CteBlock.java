package com.sqlengine.model.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CteBlock {
    private String name; // e.g., active_users
    private String query; // Raw SQL
}
