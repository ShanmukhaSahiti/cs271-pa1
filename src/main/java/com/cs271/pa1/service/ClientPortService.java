package com.cs271.pa1.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClientPortService {

	@Value("${server.port}")
	private Integer serverPort;

	private final List<Integer> clientPorts = List.of(8080, 8081, 8082);

	public List<Integer> getClientPorts() {
		List<Integer> ports = new ArrayList<Integer>(clientPorts);
		ports.remove(serverPort);
		return ports;
	}

//	public void sleep() {
//		try {
//			TimeUnit.SECONDS.sleep(3);
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//		}
//	}
}
