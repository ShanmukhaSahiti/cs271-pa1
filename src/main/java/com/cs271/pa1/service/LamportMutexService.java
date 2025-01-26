package com.cs271.pa1.service;

import com.cs271.pa1.dto.Request;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class LamportMutexService {
    // Lamport's Distributed Mutual Exclusion Implementation
    private long lamportClock = 0;
    private final String processId = UUID.randomUUID().toString();
    
    private PriorityBlockingQueue<Request> requestQueue = new PriorityBlockingQueue<>();
    
    public synchronized long incrementClock() {
        return ++lamportClock;
    }
    
    public synchronized void requestMutex() {
        // Implement Lamport's mutex request logic
        long currentTimestamp = incrementClock();
        Request request = new Request();
        request.setTimestamp(currentTimestamp);
        request.setProcessId(processId);
        requestQueue.add(request);
        
        // Send request to all other processes
        // Broadcast request with timestamp
    }
    
    public synchronized boolean canEnterCriticalSection() {
        // Check if this process can enter critical section
        Request myRequest = requestQueue.stream()
            .filter(r -> r.getProcessId().equals(processId))
            .findFirst()
            .orElseThrow();
        
        return requestQueue.stream()
            .filter(r -> r.compareTo(myRequest) < 0)
            .count() == 0;
    }
    
    public synchronized void releaseMutex() {
        // Remove this process's request from queue
        requestQueue.removeIf(r -> r.getProcessId().equals(processId));
    }
}