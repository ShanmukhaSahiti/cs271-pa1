package com.cs271.pa1.proxy;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.cs271.pa1.dto.BlockDto;
import com.cs271.pa1.service.ClientPortService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ClientProxy {

	private RestTemplate restTemplate = new RestTemplateBuilder().build();

	@Autowired
	private ClientPortService clientPortService;

	public void broadcastBlock(BlockDto block) {

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<BlockDto> entity = new HttpEntity<BlockDto>(block, headers);

		for (int port : clientPortService.getClientPorts()) {
			log.info("Transfer to client on port "+port);
			restTemplate.exchange("http://localhost:" + port + "/client/message", HttpMethod.POST, entity,
					String.class);
		}
		
	}

}
