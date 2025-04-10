package com.sqlengine.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sqlengine.adapter.BlockingConnectionFactoryAdapter;
import com.sqlengine.dto.CachedR2dbcConnection;
import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.repository.DatabaseConfigRepository;
import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseConnectionPoolManager {

    private final DatabaseConfigRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Cache<String, CachedR2dbcConnection> cache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .refreshAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(300)
            .build(this::loadFromMongoBlocking);

    private CachedR2dbcConnection loadFromMongoBlocking(String configId) {
        DatabaseConfig config = repository.findById(configId).block();
        if (config == null) {
            throw new IllegalArgumentException("DatabaseConfig not found: " + configId);
        }
        return createConnectionFactory(config);
    }

    public DatabaseClient getDatabaseClient(DatabaseConfig config) {
        return DatabaseClient.create(getConnectionFactory(config));
    }

    public Mono<DatabaseClient> getDatabaseClientReactive(String configId) {
        CachedR2dbcConnection cached = cache.getIfPresent(configId);
        if (cached != null) {
            return Mono.just(DatabaseClient.create(cached.getConnectionFactory()));
        }

        return repository.findById(configId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("DatabaseConfig not found: " + configId)))
                .map(config -> {
                    CachedR2dbcConnection conn = createConnectionFactory(config);
                    cache.put(configId, conn);
                    return DatabaseClient.create(conn.getConnectionFactory());
                });
    }

    public DataSource getDataSource(DatabaseConfig config) {
        CachedR2dbcConnection cached = cache.getIfPresent(config.getId());
        String currentHash = hashConfig(config.getConfig());

        if (cached != null && cached.getConfigHash().equals(currentHash)) {
            if (cached.getConnectionFactory() instanceof BlockingConnectionFactoryAdapter) {
                return ((BlockingConnectionFactoryAdapter) cached.getConnectionFactory()).getDataSource();
            } else {
                throw new UnsupportedOperationException("R2DBC connection does not expose JDBC DataSource.");
            }
        } else {
            CachedR2dbcConnection newConn = createConnectionFactory(config);
            cache.put(config.getId(), newConn);
            if (newConn.getConnectionFactory() instanceof BlockingConnectionFactoryAdapter) {
                return ((BlockingConnectionFactoryAdapter) newConn.getConnectionFactory()).getDataSource();
            } else {
                throw new UnsupportedOperationException("R2DBC connection does not expose JDBC DataSource.");
            }
        }
    }

    private ConnectionFactory getConnectionFactory(DatabaseConfig config) {
        CachedR2dbcConnection cached = cache.getIfPresent(config.getId());
        String currentHash = hashConfig(config.getConfig());

        if (cached != null && cached.getConfigHash().equals(currentHash)) {
            return cached.getConnectionFactory();
        }

        CachedR2dbcConnection newConn = createConnectionFactory(config);
        cache.put(config.getId(), newConn);
        return newConn.getConnectionFactory();
    }

    private CachedR2dbcConnection createConnectionFactory(DatabaseConfig config) {
        Map<String, Object> cfg = config.getConfig();
        String jdbcUrl = (String) cfg.get("url");

        String driverType = resolveDriver(jdbcUrl);

        if ("jdbc-fallback".equals(driverType)) {
            log.warn("⚠️ Falling back to JDBC connection for: {}", jdbcUrl);
            return createJDBCConnectionFallback(cfg);
        }

        return createR2DBCConnection(cfg, driverType);
    }

    private CachedR2dbcConnection createR2DBCConnection(Map<String, Object> cfg, String driver) {
        String host = (String) cfg.get("host");
        int port = (int) cfg.getOrDefault("port", 3306);
        String database = (String) cfg.get("database");
        String username = (String) cfg.get("username");
        String password = (String) cfg.get("password");

        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .option(DRIVER, driver)
                .option(HOST, host)
                .option(PORT, port)
                .option(USER, username)
                .option(PASSWORD, password)
                .option(DATABASE, database)
                .build();

        ConnectionFactory factory = ConnectionFactories.get(options);

        ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(factory)
                .maxIdleTime(Duration.ofMinutes(15))
                .maxSize((Integer) cfg.getOrDefault("maxPoolSize", 10))
                .initialSize((Integer) cfg.getOrDefault("minIdle", 1))
                .build();

        ConnectionPool pool = new ConnectionPool(poolConfig);
        return new CachedR2dbcConnection(pool, hashConfig(cfg));
    }

    private CachedR2dbcConnection createJDBCConnectionFallback(Map<String, Object> cfg) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl((String) cfg.get("url"));
        ds.setUsername((String) cfg.get("username"));
        ds.setPassword((String) cfg.get("password"));
        ds.setDriverClassName((String) cfg.get("driverClassName"));
        ds.setMaximumPoolSize((Integer) cfg.getOrDefault("maxPoolSize", 5));

        return new CachedR2dbcConnection(new BlockingConnectionFactoryAdapter(ds), hashConfig(cfg));
    }

    private String hashConfig(Map<String, Object> configMap) {
        try {
            String json = objectMapper.writeValueAsString(configMap);
            return DigestUtils.sha256Hex(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash DB config", e);
        }
    }

    private String resolveDriver(String jdbcUrl) {
        if (jdbcUrl.contains("mysql")) return "mysql";
        if (jdbcUrl.contains("postgresql")) return "postgresql";
        if (jdbcUrl.contains("mariadb")) return "mariadb";
        return "jdbc-fallback";
    }

    public void evict(String configId) {
        cache.invalidate(configId);
    }

    public void preloadConnections(int limit) {
        log.info("🚀 Preloading up to {} active database connections...", limit);

        repository.findAll()
                .filter(DatabaseConfig::isActive)
                .take(limit)
                .collectList()
                .doOnNext(configs -> {
                    for (DatabaseConfig config : configs) {
                        try {
                            CachedR2dbcConnection cached = createConnectionFactory(config);
                            cache.put(config.getId(), cached);
                            log.info("✅ Preloaded DB client for: {}", config.getDatabaseConnectionName());
                        } catch (Exception e) {
                            log.error("❌ Failed to preload DB client for: {}", config.getDatabaseConnectionName(), e);
                        }
                    }
                })
                .subscribe();

        log.info("✅ Preloading initiated.");
    }

}
