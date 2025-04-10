package com.sqlengine.adapter;

import io.r2dbc.spi.Batch;
import io.r2dbc.spi.ConnectionMetadata;
import io.r2dbc.spi.IsolationLevel;
import io.r2dbc.spi.Statement;
import io.r2dbc.spi.TransactionDefinition;
import io.r2dbc.spi.ValidationDepth;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.time.Duration;

public class BlockingConnectionAdapter implements io.r2dbc.spi.Connection {

    private final Connection jdbcConnection;

    public BlockingConnectionAdapter(Connection jdbcConnection) {
        this.jdbcConnection = jdbcConnection;
    }

    public Connection getJdbcConnection() {
        return this.jdbcConnection;
    }

    @Override
    public Publisher<Void> beginTransaction() {
        return Mono.error(new UnsupportedOperationException("Reactive transaction not supported in JDBC adapter"));
    }

    @Override
    public Publisher<Void> close() {
        return Mono.fromRunnable(() -> {
            try {
                jdbcConnection.close();
            } catch (Exception e) {
                throw new RuntimeException("Failed to close JDBC connection", e);
            }
        });
    }

    @Override
    public Publisher<Void> commitTransaction() {
        return Mono.error(new UnsupportedOperationException("Not supported"));
    }

    @Override
    public Batch createBatch() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Publisher<Void> createSavepoint(String name) {
        return Mono.error(new UnsupportedOperationException("Not supported"));
    }

    @Override
    public Statement createStatement(String sql) {
        throw new UnsupportedOperationException("Reactive statement execution not supported with JDBC fallback.");
    }

    @Override
    public boolean isAutoCommit() {
        return true;
    }

    @Override
    public ConnectionMetadata getMetadata() {
        return new ConnectionMetadata() {
            @Override
            public String getDatabaseProductName() {
                try {
                    return jdbcConnection.getMetaData().getDatabaseProductName();
                } catch (Exception e) {
                    return "Unknown-Product";
                }
            }

            @Override
            public String getDatabaseVersion() {
                try {
                    return jdbcConnection.getMetaData().getDatabaseProductVersion();
                } catch (Exception e) {
                    return "Unknown-Version";
                }
            }
        };
    }



    @Override
    public Publisher<Void> releaseSavepoint(String name) {
        return Mono.error(new UnsupportedOperationException("Not supported"));
    }

    @Override
    public Publisher<Void> rollbackTransaction() {
        return Mono.error(new UnsupportedOperationException("Not supported"));
    }

    @Override
    public Publisher<Void> rollbackTransactionToSavepoint(String name) {
        return Mono.error(new UnsupportedOperationException("Not supported"));
    }

    @Override
    public Publisher<Void> setAutoCommit(boolean autoCommit) {
        return Mono.error(new UnsupportedOperationException("Not supported"));
    }

    @Override
    public Publisher<Boolean> validate(ValidationDepth depth) {
        return Mono.just(true); // best effort
    }


    @Override
    public Publisher<Void> beginTransaction(TransactionDefinition transactionDefinition) {
        return Mono.error(new UnsupportedOperationException("Reactive transactions not supported in JDBC adapter"));
    }

    @Override
    public IsolationLevel getTransactionIsolationLevel() {
        throw new UnsupportedOperationException("getTransactionIsolationLevel not supported in JDBC adapter");
    }

    @Override
    public Publisher<Void> setLockWaitTimeout(Duration duration) {
        return Mono.error(new UnsupportedOperationException("Lock wait timeout not supported in JDBC adapter"));
    }

    @Override
    public Publisher<Void> setStatementTimeout(Duration duration) {
        return Mono.error(new UnsupportedOperationException("Statement timeout not supported in JDBC adapter"));
    }

    @Override
    public Publisher<Void> setTransactionIsolationLevel(IsolationLevel isolationLevel) {
        return Mono.error(new UnsupportedOperationException("Setting isolation level not supported in JDBC adapter"));
    }

}
