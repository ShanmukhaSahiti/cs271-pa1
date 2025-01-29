package com.cs271.pa1.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs271.pa1.dto.TransactionDto;
import com.cs271.pa1.service.BlockchainService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/blockchain")
public class BlockchainController {
    @Autowired
    private BlockchainService blockchainService;

    @GetMapping("/balance/{clientName}")
    public ResponseEntity<BigDecimal> checkBalance(@PathVariable String clientName) {
        BigDecimal balance = blockchainService.checkBalance(clientName);
        log.info("Balance check for {}: {}", clientName, balance);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/blockchain")
    public ResponseEntity<?> getBlockchain() {
        return ResponseEntity.ok(blockchainService.getBlockchain());
    }
}