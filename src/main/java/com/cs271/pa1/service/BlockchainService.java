package com.cs271.pa1.service;

import com.cs271.pa1.dto.BlockDto;
import com.cs271.pa1.dto.TransactionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class BlockchainService {
    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(10);
    private final List<BlockDto> blockchain = new CopyOnWriteArrayList<>();
    private final Map<String, BigDecimal> balanceTable = new ConcurrentHashMap<>();
    private final ReentrantLock transactionLock = new ReentrantLock();

    public BlockchainService() {
        initializeBalanceTable();
    }

    private void initializeBalanceTable() {
        balanceTable.put("ClientA", INITIAL_BALANCE);
        balanceTable.put("ClientB", INITIAL_BALANCE);
        balanceTable.put("ClientC", INITIAL_BALANCE);
    }

    public boolean initiateTransaction(TransactionDto transaction) {
        transactionLock.lock();
        try {
            log.info("Initiating transaction: {}", transaction);
            
            // Validate transaction
            if (!validateTransaction(transaction)) {
                log.warn("Transaction validation failed: {}", transaction);
                return false;
            }

            // Update balances
            updateBalances(transaction);

            // Create and add block
            BlockDto newBlock = createBlock(transaction);
            blockchain.add(0, newBlock);

            log.info("Transaction successful: {}", transaction);
            return true;
        } catch (Exception e) {
            log.error("Transaction processing error", e);
            return false;
        } finally {
            transactionLock.unlock();
        }
    }

    private boolean validateTransaction(TransactionDto transaction) {
        // Validate sender has sufficient balance
        BigDecimal senderBalance = balanceTable.getOrDefault(transaction.getSender(), BigDecimal.ZERO);
        
        // Check if sender has enough balance and transaction amount is positive
        return senderBalance.compareTo(transaction.getAmount()) >= 0 && 
               transaction.getAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private void updateBalances(TransactionDto transaction) {
        String sender = transaction.getSender();
        String receiver = transaction.getReceiver();
        BigDecimal amount = transaction.getAmount();

        // Subtract from sender
        balanceTable.put(sender, balanceTable.get(sender).subtract(amount));
        
        // Add to receiver (create account if not exists)
        balanceTable.merge(receiver, amount, BigDecimal::add);
    }

    private BlockDto createBlock(TransactionDto transaction) {
        BlockDto block = BlockDto.builder()
            .operation(transaction)
            .timestamp(Instant.now().toEpochMilli())
            .build();

        // Set previous block hash if blockchain is not empty
        if (!blockchain.isEmpty()) {
            BlockDto previousBlock = blockchain.get(0);
            block.setPreviousBlockHash(generateBlockHash(previousBlock));
        }

        // Generate current block hash
        block.setCurrentBlockHash(generateBlockHash(block));
        return block;
    }

    private String generateBlockHash(BlockDto block) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Create hash input from transaction and previous block hash
            String hashInput = Optional.ofNullable(block.getOperation())
                .map(op -> op.getSender() + op.getReceiver() + op.getAmount())
                .orElse("") + 
                Optional.ofNullable(block.getPreviousBlockHash())
                .orElse("");
            
            byte[] hashBytes = digest.digest(hashInput.getBytes());
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public BigDecimal checkBalance(String clientName) {
        return balanceTable.getOrDefault(clientName, BigDecimal.ZERO);
    }

    public List<BlockDto> getBlockchain() {
        return new ArrayList<>(blockchain);
    }

    public Map<String, BigDecimal> getBalanceTable() {
        return new HashMap<>(balanceTable);
    }

    // Debugging method to print blockchain state
    public void printBlockchainState() {
        log.info("Current Blockchain State:");
        log.info("Balance Table: {}", balanceTable);
        log.info("Blockchain Size: {}", blockchain.size());
        blockchain.forEach(block -> log.info("Block: {}", block));
    }
}