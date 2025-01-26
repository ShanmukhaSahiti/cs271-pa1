package com.cs271.pa1.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionDto {
    private String sender;
    private String receiver;
    private BigDecimal amount;
    private Long timestamp;
}
