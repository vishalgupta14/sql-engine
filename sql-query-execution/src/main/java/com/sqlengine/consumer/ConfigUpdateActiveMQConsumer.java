package com.sqlengine.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlengine.dto.ConfigUpdateMessage;
import com.sqlengine.manager.DatabaseConnectionPoolManager;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "messaging.mode", havingValue = "activemq")
public class ConfigUpdateActiveMQConsumer {

    private final DatabaseConnectionPoolManager poolManager;
    private final ObjectMapper objectMapper;
    private final String clientId = UUID.randomUUID().toString();

    public ConfigUpdateActiveMQConsumer(DatabaseConnectionPoolManager poolManager, ObjectMapper objectMapper) {
        this.poolManager = poolManager;
        this.objectMapper = objectMapper;
    }

    @JmsListener(
        destination = "${messaging.db-config-update-topic}",
        containerFactory = "topicListenerFactory",
        subscription = "#{T(java.util.UUID).randomUUID().toString()}"
    )
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String json = textMessage.getText();
                ConfigUpdateMessage update = objectMapper.readValue(json, ConfigUpdateMessage.class);

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
            }
        } catch (JMSException | RuntimeException e) {
            log.error("❌ Failed to process ActiveMQ topic message", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
