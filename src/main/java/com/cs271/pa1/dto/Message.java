package com.cs271.pa1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Message {
	private Type type;
	private String clientId;
	private int lamportClock;
	private BlockDto block;
	
}
