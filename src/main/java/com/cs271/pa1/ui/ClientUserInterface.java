package com.cs271.pa1.ui;

import java.math.BigDecimal;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cs271.pa1.dto.TransactionDto;
import com.cs271.pa1.service.BlockchainService;

@Component
public class ClientUserInterface {
	@Autowired
	private BlockchainService blockchainService;

	private String clientName;

	public void start(String clientName) {

		this.clientName = clientName;
		System.out.println("Client: " + clientName);
		System.out.println("Initial Balance: $10");

		displayMenu();
	}

	private void displayMenu() {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("\n--- Blockchain Transaction System ---");
			System.out.println("1. Transfer Money");
			System.out.println("2. Check Balance");
			System.out.println("3. View Blockchain");
			System.out.println("4. View Balance Table");
			System.out.println("5. Exit");
			System.out.print("Choose an option: ");

			int choice = scanner.nextInt();
			switch (choice) {
			case 1:
				performTransfer(scanner);
				break;
			case 2:
				checkBalance(scanner);
				break;
			case 3:
				viewBlockchain();
				break;
			case 4:
				viewBalanceTable();
				break;
			case 5:
				System.exit(0);
			default:
				System.out.println("Invalid option. Try again.");
			}
		}
	}

	private void performTransfer(Scanner scanner) {
		System.out.print("Enter receiver client name (A/B/C): ");
		String receiver = scanner.next();

		if (receiver.equals(clientName)) {
			System.out.println("Sender and Receiver cannot be the same client");
		}

		System.out.print("Enter transfer amount: $");
		BigDecimal amount = scanner.nextBigDecimal();

		TransactionDto transaction = TransactionDto.createTransaction(this.clientName, receiver, amount);

		BigDecimal beforeBalance = blockchainService.checkBalance(this.clientName);
		boolean result = blockchainService.initiateTransaction(transaction);
		BigDecimal afterBalance = blockchainService.checkBalance(this.clientName);

		System.out.println(result ? "SUCCESS" : "FAILED");
		System.out.println("Before Balance: $" + beforeBalance);
		System.out.println("After Balance: $" + afterBalance);
	}

	private void checkBalance(Scanner scanner) {
		BigDecimal balance = blockchainService.checkBalance(this.clientName);
		System.out.println("Current Balance: $" + balance);
	}

	private void viewBlockchain() {
		blockchainService.printBlockchain();
	}

	private void viewBalanceTable() {
		blockchainService.printBalanceTable();
	}
}