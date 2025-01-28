package com.cs271.pa1.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cs271.pa1.dto.ClientPorts;

@Service
public class ClientPortService {
	public List<Integer> getClientPorts(String clientName) {
        return switch(clientName) {
            case "A" -> List.of(ClientPorts.B.getPort(), ClientPorts.C.getPort());
            case "B" -> List.of(ClientPorts.A.getPort(), ClientPorts.C.getPort());
            case "C" -> List.of(ClientPorts.A.getPort(), ClientPorts.B.getPort());
            default -> throw new IllegalArgumentException("Invalid client name");
        };
    }
}
