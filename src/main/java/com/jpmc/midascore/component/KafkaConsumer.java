package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-group")
    public void listen(Transaction transaction) {
        // Log very clearly the important information you need to collect
        logger.info("TRANSACTION RECEIVED: amount = {}", transaction.getAmount());
        logger.info("TRANSACTION DETAILS: senderId = {}, recipientId = {}", 
                    transaction.getSenderId(), transaction.getRecipientId());
    }
}