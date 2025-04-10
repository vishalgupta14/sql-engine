package com.sqlengine.producer;

public interface MessageProducer {
    void sendMessage(String queueName, String message, boolean isPubSub);
}
