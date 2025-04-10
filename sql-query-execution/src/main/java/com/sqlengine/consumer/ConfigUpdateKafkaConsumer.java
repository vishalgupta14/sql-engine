package com.sqlengine.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlengine.dto.ConfigUpdateMessage;
import com.sqlengine.manager.DatabaseConnectionPoolManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "messaging.mode", havingValue = "kafka")
public class ConfigUpdateKafkaConsumer {

    private final DatabaseConnectionPoolManager poolManager;
    private final ObjectMapper objectMapper;

    public ConfigUpdateKafkaConsumer(DatabaseConnectionPoolManager poolManager, ObjectMapper objectMapper) {
        this.poolManager = poolManager;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${messaging.db-config-update-topic}",
        groupId = "#{T(java.util.UUID).randomUUID().toString()}"
    )
    public void handleUpdate(String message) {
        try {
            ConfigUpdateMessage update = objectMapper.readValue(message, ConfigUpdateMessage.class);

            switch (update.getEventType()) {
                case "SAVE":
                case "UPDATE":
                    poolManager.evict(update.getConfigId());
                    poolManager.getDatabaseClientReactive(update.getConfigId()).subscribe();
                    break;
                case "DELETE":
                    poolManager.evict(update.getConfigId());
                    break;
            }
        } catch (Exception e) {
            log.error("❌ Failed to handle config update message", e);
        }
    }
}
