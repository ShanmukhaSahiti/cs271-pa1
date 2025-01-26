package com.cs271.pa1.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BalanceTableDto {
    private String clientName;
    private BigDecimal balance;
}