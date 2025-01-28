package com.cs271.pa1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cs271.pa1.dto.Request;
import com.cs271.pa1.service.LamportMutexService;

@RestController
@RequestMapping("/api/lamport")
public class LamportController {
	@Autowired
	private LamportMutexService lamportService;

	@PostMapping("/request")
	public void receiveRequest(@RequestBody Request request) {
		lamportService.receiveRequest(request);
	}

	@PostMapping("/reply/{processId}")
	public void receiveReply(@PathVariable String processId, @RequestParam long timestamp) {
		lamportService.receiveReply(processId, timestamp);
	}

	@PostMapping("/release")
	public void receiveRelease(@RequestParam String processId, @RequestParam long timestamp) {
		lamportService.receiveRelease(processId, timestamp);
	}
}