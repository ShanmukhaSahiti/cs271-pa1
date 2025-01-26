package com.cs271.pa1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Transaction {
	private final String sender = "";
	private final String receiver = "";
	private final double amount = 10;

	@Override
	public String toString() {
		return String.format("<%s,%s,%.2f>", sender, receiver, amount);
	}
}
