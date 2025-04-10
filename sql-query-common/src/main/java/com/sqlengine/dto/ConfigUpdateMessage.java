package com.sqlengine.dto;

import lombok.Data;

@Data
public class ConfigUpdateMessage {
    private String configId;
    private String eventType; // e.g., "SAVE", "UPDATE", "DELETE"
}
