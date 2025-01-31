package com.cs271.pa1.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDto {
	private String sender;
	private String receiver;
	private BigDecimal amount;
	private Long timestamp;
	private String transactionId;

	public static TransactionDto createTransaction(String sender, String receiver, BigDecimal amount) {
		return TransactionDto.builder().sender(sender).receiver(receiver).amount(amount)
				.timestamp(Instant.now().toEpochMilli()).transactionId(java.util.UUID.randomUUID().toString()).build();
	}
}