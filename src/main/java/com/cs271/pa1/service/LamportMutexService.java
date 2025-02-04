package com.cs271.pa1.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cs271.pa1.dto.Request;
import com.cs271.pa1.proxy.LamportProxy;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LamportMutexService {
	@Autowired
	private LamportProxy lamportProxy;

	@Autowired
	private ClientPortService clientPortService;

	@Value("${server.port}")
	private String processId;

	private long lamportClock = 0;

	private final PriorityBlockingQueue<Request> requestQueue = new PriorityBlockingQueue<>();

	private final ConcurrentHashMap<String, Set<String>> replyTracker = new ConcurrentHashMap<>();

	private final Set<String> otherProcesses = Collections.synchronizedSet(new HashSet<>());
	private final Map<String, String> processUrls = new ConcurrentHashMap<>();

	private volatile boolean inCriticalSection = false;

	@PostConstruct
	public void init() {
		for (Integer port : clientPortService.getClientPorts()) {
			registerProcess(port + "", "http://localhost:" + port);
		}
	}

	public String getProcessId() {
		return processId;
	}

	public synchronized long incrementClock() {
		return ++lamportClock;
	}

	public synchronized void updateClock(long receivedTimestamp) {
		lamportClock = Math.max(lamportClock, receivedTimestamp) + 1;
	}

	public void requestMutex() {
		long currentTimestamp = incrementClock();
		Request request = new Request();
		request.setTimestamp(currentTimestamp);
		request.setProcessId(processId);
		synchronized (this) {
			if (inCriticalSection) {
				throw new IllegalStateException("Already in critical section");
			}
			requestQueue.add(request);
			log.info("Request mutex after adding to queue {}", requestQueue);
			log.info("Added request to queue: {}", request);

			replyTracker.put(processId, Collections.synchronizedSet(new HashSet<>()));
		}

		log.info("Broadcasting request to all");

		for (String url : processUrls.values()) {
			log.info("sending request to {}", url);
			try {
				lamportProxy.sendRequest(url, request);
			} catch (Exception e) {
				log.error("Failed to send request to {}: {}", url, e.getMessage());
			}
		}
	}

	public void receiveRequest(Request request) {
		synchronized (this) {
			log.info("Received request from process {}: {}", request.getProcessId(), request);

			updateClock(request.getTimestamp());

			requestQueue.add(request);
			log.info("receive Request after adding to queue {}", requestQueue);

			lamportProxy.sendReply("http://localhost:" + request.getProcessId(), processId, lamportClock);
		}
	}

	public synchronized void receiveReply(String fromProcessId, long timestamp) {
		log.info("Received reply from process: {}", fromProcessId);

		updateClock(timestamp);

		Set<String> replies = replyTracker.get(processId);
		if (replies != null) {
			replies.add(fromProcessId);
			log.info("Added reply from {} to tracker. Total replies: {}", fromProcessId, replies.size());
		}
	}

	public synchronized boolean canEnterCriticalSection() {
		if (inCriticalSection) {
			return false;
		}

		log.info("Can enter critical section: {}", requestQueue);

		// Get the earliest request in queue
		Request myRequest = requestQueue.stream().filter(r -> r.getProcessId().equals(processId)).findFirst()
				.orElse(null);

		if (myRequest == null) {
			return false;
		}

		// Ensure our request is at the front of the queue
		boolean earliestRequest = requestQueue.peek().equals(myRequest);

		// Ensure we received replies from all processes
		Set<String> replies = replyTracker.get(processId);
		boolean allRepliesReceived = replies != null && replies.size() >= otherProcesses.size();

		boolean canEnter = earliestRequest && allRepliesReceived;
		log.info("Can enter critical section: {} (earliest: {}, allReplies: {})", canEnter, earliestRequest,
				allRepliesReceived);

		return canEnter;
	}

	public synchronized void enterCriticalSection() {
		if (!canEnterCriticalSection()) {
			throw new IllegalStateException("Cannot enter critical section");
		}
		inCriticalSection = true;
		log.info("Entered critical section");
	}

	public synchronized void releaseMutex() {
		if (!inCriticalSection) {
			throw new IllegalStateException("Not in critical section");
		}

		// Remove request from queue
		requestQueue.removeIf(r -> r.getProcessId().equals(processId));

		// Clear reply tracker
		replyTracker.remove(processId);

		// Reset critical section flag
		inCriticalSection = false;

		// Broadcast release message
		long timestamp = incrementClock();
		for (String url : processUrls.values()) {
			try {
				lamportProxy.sendRelease(url, processId, timestamp);
				log.info("Sent release to: {}", url);
			} catch (Exception e) {
				log.error("Failed to send release to {}: {}", url, e.getMessage());
			}
		}
		log.info("Released critical section lamport clock: {}", lamportClock);
	}

	public synchronized void receiveRelease(String fromProcessId, long timestamp) {
		log.info("Received release from process: {}", fromProcessId);
		updateClock(timestamp);
		log.info("lamport clock: {}", lamportClock);
		requestQueue.removeIf(r -> r.getProcessId().equals(fromProcessId));
	}

	public void registerProcess(String processId, String url) {
		processUrls.put(processId, url);
		otherProcesses.add(processId);
		log.info("Registered process {} at {}", processId, url);
	}

	public void unregisterProcess(String processId) {
		processUrls.remove(processId);
		otherProcesses.remove(processId);
		log.info("Unregistered process {}", processId);
	}

	public boolean isInCriticalSection() {
		return inCriticalSection;
	}
}