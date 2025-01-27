package com.cs271.pa1.service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.cs271.pa1.dto.BlockDto;
import com.cs271.pa1.dto.TransactionDto;
import com.cs271.pa1.network.NetworkManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BlockchainService {
	private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(10);
	private final List<BlockDto> blockchain = new CopyOnWriteArrayList<>();
	private final Map<String, BigDecimal> balanceTable = new ConcurrentHashMap<>();

	@Autowired
	private LamportMutexService mutexService;

	@Autowired
	@Lazy
	private NetworkManager networkManager;

	public BlockchainService() {
		initializeBalanceTable();
	}

	private void initializeBalanceTable() {
		balanceTable.put("ClientA", INITIAL_BALANCE);
		balanceTable.put("ClientB", INITIAL_BALANCE);
		balanceTable.put("ClientC", INITIAL_BALANCE);
	}

	public boolean initiateTransaction(TransactionDto transaction) {
		mutexService.requestMutex();
		try {
			while (!mutexService.canEnterCriticalSection()) {
				Thread.sleep(100); // Small delay to prevent busy waiting
			}

			log.info("Initiating transaction: {}", transaction);

			// Validate transaction
			if (!validateTransaction(transaction)) {
				log.warn("Transaction validation failed: {}", transaction);
				return false;
			}

			// Create and add block
			BlockDto newBlock = createBlock(transaction);
			blockchain.add(0, newBlock);

			networkManager.broadcastTransaction(newBlock.toString());
			
			updateBalances(transaction);
			
			log.info("Transaction successful: {}", transaction);
			return true;
		} catch (Exception e) {
			log.error("Transaction processing error", e);
			return false;
		} finally {
			mutexService.releaseMutex();
		}
	}

	private boolean validateTransaction(TransactionDto transaction) {
		// Validate sender has sufficient balance
		BigDecimal senderBalance = balanceTable.getOrDefault(transaction.getSender(), BigDecimal.ZERO);

		// Check if sender has enough balance and transaction amount is positive
		return senderBalance.compareTo(transaction.getAmount()) >= 0
				&& transaction.getAmount().compareTo(BigDecimal.ZERO) > 0;
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
		BlockDto block = BlockDto.builder().operation(transaction).timestamp(Instant.now().toEpochMilli()).build();

		// Set previous block hash if blockchain is not empty
		if (!blockchain.isEmpty()) {
			BlockDto previousBlock = blockchain.get(0);
			block.setPreviousBlockHash(generateBlockHash(previousBlock));
		}

		// Generate current block hash
		block.setCurrentBlockHash(generateBlockHash(block));
		return block;
	}
	
	// Add method to receive block from other clients
    public void receiveBlock(BlockDto block) {
        blockchain.add(0, block);
        // Update balance table based on the received block's transaction
        updateBalances(block.getOperation());
    }

	private String generateBlockHash(BlockDto block) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			// Create hash input from transaction and previous block hash
			String hashInput = Optional.ofNullable(block.getOperation())
					.map(op -> op.getSender() + op.getReceiver() + op.getAmount()).orElse("")
					+ Optional.ofNullable(block.getPreviousBlockHash()).orElse("");

			byte[] hashBytes = digest.digest(hashInput.getBytes());

			// Convert to hex string
			StringBuilder hexString = new StringBuilder();
			for (byte hashByte : hashBytes) {
				String hex = Integer.toHexString(0xff & hashByte);
				if (hex.length() == 1)
					hexString.append('0');
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