package com.cs271.pa1.proxy;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.cs271.pa1.dto.ReplyResponse;
import com.cs271.pa1.dto.Request;

@Component
public class LamportProxy {
	private RestTemplate restTemplate = new RestTemplateBuilder().build();

	public ReplyResponse sendRequest(String targetUrl, Request request) {
		return restTemplate.postForObject(targetUrl + "/api/lamport/request", request, ReplyResponse.class);
	}

	public void sendReply(String targetUrl, String processId, long timestamp) {
		restTemplate.postForObject(targetUrl + "/api/lamport/reply/" + processId + "?timestamp=" + timestamp, null,
				Void.class);
	}

	public void sendRelease(String targetUrl, String processId, long timestamp) {
		restTemplate.postForObject(
				targetUrl + "/api/lamport/release?processId=" + processId + "&timestamp=" + timestamp, null,
				Void.class);
	}
}
