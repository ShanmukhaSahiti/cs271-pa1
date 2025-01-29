package com.cs271.pa1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class ReplyResponse {
    private String fromProcessId;
    private long timestamp;
}