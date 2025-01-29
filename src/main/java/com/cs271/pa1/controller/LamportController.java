package com.cs271.pa1.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cs271.pa1.dto.ReplyResponse;
import com.cs271.pa1.dto.Request;
import com.cs271.pa1.service.ClientPortService;
import com.cs271.pa1.service.LamportMutexService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/lamport")
public class LamportController {
	@Autowired
	private LamportMutexService lamportService;
	
	@Autowired
	private ClientPortService clientPortService;

	@PostMapping("/request")
	public ResponseEntity<ReplyResponse> receiveRequest(@RequestBody Request request)  throws InterruptedException{
		//TimeUnit.SECONDS.sleep(3);
		ReplyResponse reply = lamportService.receiveRequest(request);
        return ResponseEntity.ok(reply);
	}

	@PostMapping("/reply/{processId}")
	public void receiveReply(@PathVariable String processId, @RequestParam long timestamp)  throws InterruptedException{
		//TimeUnit.SECONDS.sleep(3);
		log.info("receievd reply in controller from{}", processId);
		lamportService.receiveReply(processId, timestamp);
	}

	@PostMapping("/release")
	public void receiveRelease(@RequestParam String processId, @RequestParam long timestamp)  throws InterruptedException{
		//TimeUnit.SECONDS.sleep(3);
		lamportService.receiveRelease(processId, timestamp);
	}
}