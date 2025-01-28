package com.cs271.pa1.ui;

import com.cs271.pa1.dto.TransactionDto;
import com.cs271.pa1.service.BlockchainService;
import com.cs271.pa1.service.ClientPortService;
import com.cs271.pa1.network.NetworkManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Component
public class ClientUserInterface {
	@Autowired
	private ClientPortService clientPortService;
	
	@Autowired
	private BlockchainService blockchainService;

	@Autowired
	private NetworkManager networkManager;

	private String clientName;

	public void start(String clientName) {
		List<Integer> ports = clientPortService.getClientPorts(clientName);
		this.clientName = clientName;

		// Connect to clients
		networkManager.connectToClients(ports);

		// Add 3-second startup delay
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Display initial balance
		System.out.println("Client: " + clientName);
		System.out.println("Initial Balance: $10");

		// Start interactive menu
		displayMenu(ports);
	}

	private void displayMenu(List<Integer> ports) {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("\n--- Blockchain Transaction System ---");
			System.out.println("1. Transfer Money");
			System.out.println("2. Check Balance");
			System.out.println("3. View Blockchain");
			System.out.println("4. Exit");
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
				System.exit(0);
			default:
				System.out.println("Invalid option. Try again.");
			}
		}
	}
	
	private void performTransfer(Scanner scanner) {
		System.out.print("Enter receiver client name (A/B/C): ");
		String receiver = scanner.next();

		System.out.print("Enter transfer amount: $");
		BigDecimal amount = scanner.nextBigDecimal();

		TransactionDto transaction = TransactionDto.createTransaction(this.clientName, receiver, amount);

		// Simulate network delay
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Check transaction result
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
		blockchainService.getBlockchain().forEach(System.out::println);
	}
}