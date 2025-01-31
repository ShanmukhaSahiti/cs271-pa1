package com.cs271.pa1.service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.LinkedList;
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
				Thread.sleep(1000);
			}
			mutexService.enterCriticalSection();
			log.info("Initiating transaction: {}", transaction);

			if (!validateTransaction(transaction)) {
				log.warn("Transaction validation failed: {}", transaction);
				return false;
			}

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
		BigDecimal senderBalance = balanceTable.getOrDefault(transaction.getSender(), BigDecimal.ZERO);

		return senderBalance.compareTo(transaction.getAmount()) >= 0
				&& transaction.getAmount().compareTo(BigDecimal.ZERO) > 0;
	}

	private void updateBalances(TransactionDto transaction) {
		String sender = transaction.getSender();
		String receiver = transaction.getReceiver();
		BigDecimal amount = transaction.getAmount();

		balanceTable.put(sender, balanceTable.get(sender).subtract(amount));

		balanceTable.merge(receiver, amount, BigDecimal::add);
	}

	private BlockDto createBlock(TransactionDto transaction) {
		BlockDto block = BlockDto.builder().operation(transaction).timestamp(Instant.now().toEpochMilli()).build();

		if (!blockchain.isEmpty()) {
			BlockDto previousBlock = blockchain.getFirst();
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

	public void receiveBlock(BlockDto block) {
		blockchain.add(0, block);
		updateBalances(block.getOperation());
	}

	public BigDecimal checkBalance(String clientName) {
		return balanceTable.getOrDefault(clientName, BigDecimal.ZERO);
	}

	public void printBlockchain() {
		System.out.println("Current Blockchain State:");
		System.out.println("Blockchain Size: " + blockchain.size());
		blockchain.forEach(block -> log.info("Block: {}", block));
	}

	public void printBalanceTable() {
		System.out.println("Current Balance Table:");
		balanceTable.forEach((k, v) -> System.out.println((k + ":" + v)));
	}

}