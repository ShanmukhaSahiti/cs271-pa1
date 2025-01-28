package com.cs271.pa1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ClientPorts {
	A(8080),
	B(8081),
	C(8082);
	
	private final int port;
}
