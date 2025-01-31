package com.cs271.pa1.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cs271.pa1.dto.Request;
import com.cs271.pa1.service.LamportMutexService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/lamport")
public class LamportController {
	@Autowired
	private LamportMutexService lamportService;

	@PostMapping("/request")
	public void receiveRequest(@RequestBody Request request) throws InterruptedException {
		TimeUnit.SECONDS.sleep(3);
		lamportService.receiveRequest(request);
	}

	@PostMapping("/reply/{processId}")
	public void receiveReply(@PathVariable String processId, @RequestParam long timestamp) throws InterruptedException {
		TimeUnit.SECONDS.sleep(3);
		lamportService.receiveReply(processId, timestamp);
	}

	@PostMapping("/release")
	public void receiveRelease(@RequestParam String processId, @RequestParam long timestamp)
			throws InterruptedException {
		TimeUnit.SECONDS.sleep(3);
		lamportService.receiveRelease(processId, timestamp);
	}
}