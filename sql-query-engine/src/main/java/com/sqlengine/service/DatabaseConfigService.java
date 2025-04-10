package com.sqlengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlengine.dto.ConfigUpdateMessage;
import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.producer.MessageProducer;
import com.sqlengine.repository.DatabaseConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DatabaseConfigService {

    private final DatabaseConfigRepository repository;
    private final MessageProducer messageProducer;

    public Mono<DatabaseConfig> save(DatabaseConfig config) {
        validateDatabaseConfig(config.getConfig());

        return repository.findByDatabaseConnectionName(config.getDatabaseConnectionName())
                .flatMap(existing -> Mono.<DatabaseConfig>error(new IllegalArgumentException(
                        "DatabaseConfig already exists for: " + config.getDatabaseConnectionName()
                )))
                .switchIfEmpty(Mono.defer(() -> {
                    config.setCreatedAt(LocalDateTime.now());
                    config.setUpdatedAt(LocalDateTime.now());
                    return repository.save(config)
                            .doOnNext(cfg -> {
                                notifyConfigChange(cfg.getId(), "SAVE");
                            });
                }));
    }

    public Mono<DatabaseConfig> findById(String id) {
        return repository.findById(id);
    }

    public Mono<DatabaseConfig> findByClientName(String clientName) {
        return repository.findByDatabaseConnectionName(clientName);
    }

    public Flux<DatabaseConfig> findAll() {
        return repository.findAll();
    }

    public Mono<Void> deleteById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No DatabaseConfig exists with ID: " + id)))
                .flatMap(existing -> repository.deleteById(id)
                        .doOnSuccess(unused -> {
                            notifyConfigChange(id, "DELETE");
                        }));
    }

    public Mono<DatabaseConfig> update(String id, DatabaseConfig updatedConfig) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("DatabaseConfig not found with ID: " + id)))
                .flatMap(existing -> {
                    if (!existing.getDatabaseConnectionName().equals(updatedConfig.getDatabaseConnectionName())) {
                        return Mono.error(new IllegalArgumentException("Client name cannot be changed."));
                    }
                    validateDatabaseConfig(updatedConfig.getConfig());
                    updatedConfig.setId(id);
                    updatedConfig.setCreatedAt(existing.getCreatedAt());
                    updatedConfig.setUpdatedAt(LocalDateTime.now());

                    return repository.save(updatedConfig)
                            .doOnSuccess(cfg -> {
                                notifyConfigChange(cfg.getId(), "UPDATE");
                            });
                });
    }

    private void notifyConfigChange(String configId, String eventType) {
        ConfigUpdateMessage msg = new ConfigUpdateMessage();
        msg.setConfigId(configId);
        msg.setEventType(eventType);

        try {
            String json = new ObjectMapper().writeValueAsString(msg);
            messageProducer.sendMessage("db-config-update-topic", json, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send DB config update message", e);
        }
    }

    private void validateDatabaseConfig(Map<String, Object> config) {
        if (config == null) throw new IllegalArgumentException("Database config cannot be null");
        if (!StringUtils.hasText((String) config.get("url"))) throw new IllegalArgumentException("Database 'url' must be provided");
        if (!StringUtils.hasText((String) config.get("username"))) throw new IllegalArgumentException("Database 'username' must be provided");
        if (!StringUtils.hasText((String) config.get("password"))) throw new IllegalArgumentException("Database 'password' must be provided");
    }
}
