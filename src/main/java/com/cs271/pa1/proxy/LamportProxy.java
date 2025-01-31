package com.cs271.pa1.proxy;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.cs271.pa1.dto.ReplyResponse;
import com.cs271.pa1.dto.Request;

@Component
public class LamportProxy {
	private RestTemplate restTemplate = new RestTemplateBuilder().build();

	@Async
	public void sendRequest(String targetUrl, Request request) {
		restTemplate.postForObject(targetUrl + "/api/lamport/request", request, ReplyResponse.class);
	}

	@Async
	public void sendReply(String targetUrl, String processId, long timestamp) {
		restTemplate.postForObject(targetUrl + "/api/lamport/reply/" + processId + "?timestamp=" + timestamp, null,
				Void.class);
	}

	@Async
	public void sendRelease(String targetUrl, String processId, long timestamp) {
		restTemplate.postForObject(
				targetUrl + "/api/lamport/release?processId=" + processId + "&timestamp=" + timestamp, null,
				Void.class);
	}
}
