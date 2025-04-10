package com.sqlengine.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sqlengine.adapter.BlockingConnectionAdapter;
import com.sqlengine.model.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TableMetadataManager {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Cache<String, Map<String, Integer>> metadataCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    /**
     * Fetch column types (SQL types) for a table using JDBC-based DataSource.
     */
    public Map<String, Integer> getColumnTypes(DatabaseConfig config, DataSource dataSource, String tableName) {
        String cacheKey = generateCacheKey(config, tableName);
        return metadataCache.get(cacheKey, key -> loadFromDatabase(dataSource, tableName));
    }

    /**
     * Extract column metadata via JDBC.
     */
    private Map<String, Integer> loadFromDatabase(DataSource dataSource, String tableName) {
        log.info("📥 Loading column metadata for table: {}", tableName);
        Map<String, Integer> columnTypes = new HashMap<>();

        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {

            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME").toLowerCase();
                int sqlType = rs.getInt("DATA_TYPE");
                columnTypes.put(columnName, sqlType);
            }

        } catch (SQLException e) {
            log.error("❌ SQL Exception while loading metadata for table: {}", tableName, e);
            throw new RuntimeException("Failed to load metadata for table: " + tableName, e);
        } catch (Exception e) {
            log.error("❌ Unexpected error while loading metadata for table: {}", tableName, e);
            throw new RuntimeException("Unexpected error during metadata load", e);
        }

        return columnTypes;
    }

    public Mono<Map<String, Integer>> getColumnTypesReactive(DatabaseConfig config, DatabaseClient dbClient, String tableName) {
        return dbClient.inConnection(connection -> {
            if (!(connection instanceof BlockingConnectionAdapter)) {
                return Mono.error(new UnsupportedOperationException("⚠️ Only BlockingConnectionAdapter supported for metadata lookup."));
            }

            return Mono.fromCallable(() -> {
                java.sql.Connection jdbcConn = ((BlockingConnectionAdapter) connection).getJdbcConnection();
                ResultSet rs = jdbcConn.getMetaData().getColumns(null, null, tableName, null);
                Map<String, Integer> columnTypes = new HashMap<>();
                while (rs.next()) {
                    columnTypes.put(rs.getString("COLUMN_NAME").toLowerCase(), rs.getInt("DATA_TYPE"));
                }
                return columnTypes;
            }).subscribeOn(Schedulers.boundedElastic());
        });
    }


    /**
     * Remove table metadata from cache (e.g., after schema change).
     */
    public void invalidate(DatabaseConfig config, String tableName) {
        String cacheKey = generateCacheKey(config, tableName);
        metadataCache.invalidate(cacheKey);
        log.info("♻️ Invalidated metadata cache for table '{}' (key: {})", tableName, cacheKey);
    }

    /**
     * Generates a unique cache key for each table & database.
     */
    private String generateCacheKey(DatabaseConfig config, String tableName) {
        String hash = hashConfig(config.getConfig());
        return hash + "::" + tableName.toLowerCase();
    }

    /**
     * Creates SHA-256 hash of config map.
     */
    private String hashConfig(Map<String, Object> configMap) {
        try {
            String json = objectMapper.writeValueAsString(configMap);
            return DigestUtils.sha256Hex(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash DB config", e);
        }
    }
}
