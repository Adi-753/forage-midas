package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IncentiveService {
    private static final Logger logger = LoggerFactory.getLogger(IncentiveService.class);
    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";
    
    private final RestTemplate restTemplate;
    
    public IncentiveService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    
    public Incentive getIncentive(Transaction transaction) {
        try {
            logger.info("Requesting incentive for transaction: sender={}, recipient={}, amount={}", 
                    transaction.getSenderId(), transaction.getRecipientId(), transaction.getAmount());
            
            Incentive incentive = restTemplate.postForObject(INCENTIVE_API_URL, transaction, Incentive.class);
            
            if (incentive != null) {
                logger.info("Received incentive amount: {}", incentive.getAmount());
                return incentive;
            } else {
                logger.warn("Received null incentive from API");
                return new Incentive(0);
            }
        } catch (Exception e) {
            logger.error("Error getting incentive from API: {}", e.getMessage());
            return new Incentive(0);
        }
    }
}