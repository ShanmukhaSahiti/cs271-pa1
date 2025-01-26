package com.cs271.pa1.dto;

import lombok.Data;

@Data
public class BlockDto {
    private TransactionDto operation;
    private String previousBlockHash;
    private String currentBlockHash;
}
