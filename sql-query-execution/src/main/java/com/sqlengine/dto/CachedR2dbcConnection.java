package com.sqlengine.dto;

import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CachedR2dbcConnection {
    private ConnectionFactory connectionFactory;
    private String configHash;
}
