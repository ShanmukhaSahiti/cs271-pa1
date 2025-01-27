package com.cs271.pa1.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cs271.pa1.dto.BlockDto;
import com.cs271.pa1.service.BlockchainService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NetworkManager {
	private List<ClientConnection> activeConnections = new CopyOnWriteArrayList<>();
	private static final int BASE_PORT = 8000;

	@Autowired
    private BlockchainService blockchainService;

	@Autowired
	private ObjectMapper objectMapper;

	@Data
	private class ClientConnection {
		private String clientId;
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;
	}

	public void connectToClients(List<String> clientAddresses) {
		for (String address : clientAddresses) {
			try {
				Socket socket = new Socket(address, BASE_PORT);
				ClientConnection connection = new ClientConnection();
				connection.setSocket(socket);
				connection.setOut(new PrintWriter(socket.getOutputStream(), true));
				connection.setIn(new BufferedReader(new InputStreamReader(socket.getInputStream())));
				activeConnections.add(connection);
			} catch (IOException e) {
				// Log connection failure
			}
		}
	}

	public void broadcastTransaction(String transactionJson) {
		activeConnections.forEach(conn -> conn.getOut().println(transactionJson));
	}

	public void listenForIncomingTransactions() {
		activeConnections.forEach(conn -> {
			new Thread(() -> {
				try {
					String incomingMessage;
					while ((incomingMessage = conn.getIn().readLine()) != null) {
						processIncomingTransaction(incomingMessage);
					}
				} catch (IOException e) {
					// Handle connection interruption
				}
			}).start();
		});
	}

	private void processIncomingTransaction(String blockJson) {
		try {
			BlockDto block = objectMapper.readValue(blockJson, BlockDto.class);
			blockchainService.receiveBlock(block);
		} catch (Exception e) {
			log.error("Error processing incoming block", e);
		}
	}

//    private void processIncomingTransaction(String transactionJson) {
//        try {
//            // Parse JSON to TransactionDto
//            TransactionDto transaction = objectMapper.readValue(transactionJson, TransactionDto.class);
//            
//            // Validate transaction
//            if (transaction == null || 
//                transaction.getSender() == null || 
//                transaction.getReceiver() == null || 
//                transaction.getAmount() == null) {
//                log.warn("Invalid transaction received: {}", transactionJson);
//                return;
//            }
//            
//            // Process transaction through blockchain service
//            boolean transactionResult = blockchainService.initiateTransaction(transaction);
//            
//            // Log transaction result
//            if (transactionResult) {
//                log.info("Successfully processed incoming transaction from {}", transaction.getSender());
//            } else {
//                log.warn("Failed to process incoming transaction: {}", transaction);
//            }
//        } catch (Exception e) {
//            log.error("Error processing incoming transaction", e);
//        }
//    }
}