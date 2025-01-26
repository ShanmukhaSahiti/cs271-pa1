package com.cs271.pa1.controller;

import com.cs271.pa1.dto.TransactionDto;
import com.cs271.pa1.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/client")
public class TransactionController {
    @Autowired
    private BlockchainService blockchainService;

    @PostMapping("/transfer")
    public String processTransfer(@RequestBody TransactionDto transaction) {
        boolean result = blockchainService.initiateTransaction(transaction);
        return result ? "SUCCESS" : "FAILED";
    }

    @GetMapping("/balance/{clientName}")
    public BigDecimal checkBalance(@PathVariable String clientName) {
        return blockchainService.checkBalance(clientName);
    }
}