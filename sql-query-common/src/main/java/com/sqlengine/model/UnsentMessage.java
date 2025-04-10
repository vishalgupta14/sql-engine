package com.sqlengine.model;

import com.sqlengine.dto.MessagingMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("unsent_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnsentMessage {

    @Id
    private String id;

    private String queueName;
    private String message;
    private MessagingMode messagingType; // KAFKA or ACTIVEMQ or BOTH

    private LocalDateTime timestamp = LocalDateTime.now();

    public UnsentMessage(String queueName, String message, MessagingMode messagingType) {
        this.queueName = queueName;
        this.message = message;
        this.messagingType = messagingType;
        this.timestamp = LocalDateTime.now();
    }
}
