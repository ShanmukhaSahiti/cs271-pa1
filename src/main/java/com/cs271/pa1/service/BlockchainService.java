package com.cs271.pa1.service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cs271.pa1.dto.BlockDto;
import com.cs271.pa1.dto.TransactionDto;
import com.cs271.pa1.proxy.ClientProxy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BlockchainService {
	private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(10);
	private final LinkedList<BlockDto> blockchain = new LinkedList<>();
	private final Map<String, BigDecimal> balanceTable = new ConcurrentHashMap<>();

	@Autowired
	private LamportMutexService mutexService;

	@Autowired
	private ClientProxy clientProxy;

	public BlockchainService() {
		initializeBalanceTable();
	}

	private void initializeBalanceTable() {
		balanceTable.put("A", INITIAL_BALANCE);
		balanceTable.put("B", INITIAL_BALANCE);
		balanceTable.put("C", INITIAL_BALANCE);
	}

	public boolean initiateTransaction(TransactionDto transaction) {
		mutexService.requestMutex();
		try {
			while (!mutexService.canEnterCriticalSection()) {
				Thread.sleep(1000); // Small delay to prevent busy waiting
			}
			mutexService.enterCriticalSection();
			log.info("Initiating transaction: {}", transaction);

			// Validate transaction
			if (!validateTransaction(transaction)) {
				log.warn("Transaction validation failed: {}", transaction);
				return false;
			}

			// Create and add block
			BlockDto newBlock = createBlock(transaction);
			blockchain.addFirst(newBlock);

			clientProxy.broadcastBlock(newBlock);

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

		// Generate hash based on PREVIOUS block's contents
		if (!blockchain.isEmpty()) {
			BlockDto previousBlock = blockchain.getFirst();
			// Calculate hash using previous block's operation and hash
			block.setCurrentBlockHash(generateBlockHash(previousBlock));
		} else {
			block.setCurrentBlockHash(null);
		}

		return block;
	}

	private String generateBlockHash(BlockDto block) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			String operationString = Optional.ofNullable(block.getOperation())
					.map(op -> op.getSender() + op.getReceiver() + op.getAmount().toString()).orElse("");

			String hashInput = operationString;
			if (block.getCurrentBlockHash() != null) {
				hashInput += block.getCurrentBlockHash();
			}

			byte[] hashBytes = digest.digest(hashInput.getBytes());
			StringBuilder hexString = new StringBuilder();
			for (byte b : hashBytes) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not available", e);
		}
	}

	// Add method to receive block from other clients
	public void receiveBlock(BlockDto block) {
		blockchain.add(0, block);
		// Update balance table based on the received block's transaction
		updateBalances(block.getOperation());
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