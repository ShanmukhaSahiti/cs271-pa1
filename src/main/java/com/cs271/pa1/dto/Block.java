package com.cs271.pa1.dto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class Block {
	private Transaction operation;
	private String previousHash;
	private String currentHash;

	public Block(Transaction operation, String previousHash) {
		this.operation = operation;
		this.previousHash = previousHash;
		this.currentHash = calculateHash();
	}

	private String calculateHash() {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String content = operation.toString() + previousHash;
			byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));

			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not found", e);
		}
	}
}
