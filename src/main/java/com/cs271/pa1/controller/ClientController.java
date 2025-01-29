package com.cs271.pa1.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs271.pa1.dto.BlockDto;
import com.cs271.pa1.service.BlockchainService;
import com.cs271.pa1.service.ClientPortService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/client")
public class ClientController {
	
	@Autowired
	private BlockchainService blockchainService;

	@Autowired
	private ClientPortService clientPortService;
	
	@PostMapping("/message")
	public ResponseEntity<String> processMessage(@RequestBody BlockDto blockDto)  throws InterruptedException{
		log.info("Incoming broadcast message {}", blockDto);
		TimeUnit.SECONDS.sleep(3);
		try {
			blockchainService.receiveBlock(blockDto);
			return ResponseEntity.ok("SUCCESS");
		} catch (Exception error) {
			return ResponseEntity.status(500).body(error.getMessage());
		}

	}

}