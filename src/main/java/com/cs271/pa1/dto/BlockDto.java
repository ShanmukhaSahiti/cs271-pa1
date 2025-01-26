package com.cs271.pa1.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlockDto {
    private TransactionDto operation;
    private String previousBlockHash;
    private String currentBlockHash;
    private Long timestamp;
}