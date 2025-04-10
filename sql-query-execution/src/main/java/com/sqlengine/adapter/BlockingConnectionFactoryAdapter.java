package com.sqlengine.adapter;

import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;

public class BlockingConnectionFactoryAdapter implements ConnectionFactory {

    private final HikariDataSource dataSource;

    public BlockingConnectionFactoryAdapter(HikariDataSource dataSource) {
            this.dataSource = dataSource;
        }

        public DataSource getDataSource() {
            return this.dataSource;
        }

    @Override
    public Publisher<? extends Connection> create() {
        return Mono.fromCallable(() -> {
            java.sql.Connection jdbcConn = dataSource.getConnection();
            return new BlockingConnectionAdapter(jdbcConn);
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    @Override
    public ConnectionFactoryMetadata getMetadata() {
        return () -> "JDBC-Fallback-Adapter";
    }
}
