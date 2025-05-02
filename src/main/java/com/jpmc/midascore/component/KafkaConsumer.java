package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    
    private final DatabaseConduit databaseConduit;
    
    public KafkaConsumer(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-group")
    public void listen(Transaction transaction) {
        logger.info("TRANSACTION RECEIVED: amount = {}", transaction.getAmount());
        logger.info("TRANSACTION DETAILS: senderId = {}, recipientId = {}", 
                    transaction.getSenderId(), transaction.getRecipientId());
        
        // Process the transaction
        databaseConduit.processTransaction(transaction);
    }
}