package com.sqlengine.model.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CteBlock {
    private String name; // e.g., active_users
    private String query; // Raw SQL
}
