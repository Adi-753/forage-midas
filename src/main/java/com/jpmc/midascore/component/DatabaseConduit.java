package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.service.IncentiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseConduit {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConduit.class);
    
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final IncentiveService incentiveService;

    public DatabaseConduit(UserRepository userRepository, 
                          TransactionRepository transactionRepository,
                          IncentiveService incentiveService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.incentiveService = incentiveService;
    }

    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }
    
    public UserRecord findUserById(long id) {
        return userRepository.findById(id);
    }
    
    @Transactional
    public boolean processTransaction(Transaction transaction) {
        // Find sender and recipient users
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());
        
        // Validate transaction
        if (isValidTransaction(sender, recipient, transaction.getAmount())) {
            // Get incentive from API
            Incentive incentive = incentiveService.getIncentive(transaction);
            float incentiveAmount = incentive != null ? incentive.getAmount() : 0;
            
            // Update balances
            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);
            
            // Save updated users
            userRepository.save(sender);
            userRepository.save(recipient);
            
            // Record transaction with incentive
            TransactionRecord transactionRecord = new TransactionRecord(
                sender, recipient, transaction.getAmount(), incentiveAmount);
            transactionRepository.save(transactionRecord);
            
            logger.info("Transaction processed successfully: {} -> {}, amount: {}, incentive: {}", 
                    sender.getName(), recipient.getName(), transaction.getAmount(), incentiveAmount);
            return true;
        } else {
            logger.warn("Invalid transaction rejected: senderId={}, recipientId={}, amount={}", 
                    transaction.getSenderId(), transaction.getRecipientId(), transaction.getAmount());
            return false;
        }
    }
    
    private boolean isValidTransaction(UserRecord sender, UserRecord recipient, float amount) {
        // Check if sender and recipient exist
        if (sender == null || recipient == null) {
            logger.warn("Invalid transaction: sender or recipient not found");
            return false;
        }
        
        // Check if sender has sufficient balance
        if (sender.getBalance() < amount) {
            logger.warn("Invalid transaction: insufficient balance. User {} has {} but needs {}", 
                    sender.getName(), sender.getBalance(), amount);
            return false;
        }
        
        return true;
    }
}