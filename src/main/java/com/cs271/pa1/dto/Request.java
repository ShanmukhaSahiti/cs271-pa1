package com.cs271.pa1.dto;

import lombok.Data;

@Data
public class Request implements Comparable<Request> {
    private long timestamp;
    private String processId;
    
    @Override
    public int compareTo(Request other) {
        int timestampComparison = Long.compare(this.timestamp, other.timestamp);
        return timestampComparison != 0 ? 
            timestampComparison : 
            this.processId.compareTo(other.processId);
    }
}