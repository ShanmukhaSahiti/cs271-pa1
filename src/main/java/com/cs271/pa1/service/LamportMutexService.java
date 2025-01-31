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

import com.cs271.pa1.dto.ReplyResponse;
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

	// Process identification
	@Value("${server.port}")
	private String processId;

	// Lamport clock
	private long lamportClock = 0;

	// Request queue for mutual exclusion
	private final PriorityBlockingQueue<Request> requestQueue = new PriorityBlockingQueue<>();

	// Track replies from other processes
	private final ConcurrentHashMap<String, Set<String>> replyTracker = new ConcurrentHashMap<>();

	// Set of other processes in the system
	private final Set<String> otherProcesses = Collections.synchronizedSet(new HashSet<>());

	// Map to store process URLs for communication
	private final Map<String, String> processUrls = new ConcurrentHashMap<>();

	// Flag to track if we're in critical section
	private volatile boolean inCriticalSection = false;

	@PostConstruct
	public void init() {
		for (Integer port : clientPortService.getClientPorts()) {
			registerProcess(port + "", "http://localhost:" + port);
		}
	}

	/**
	 * Gets the current process ID
	 */
	public String getProcessId() {
		return processId;
	}

	/**
	 * Increments the Lamport clock
	 */
	public synchronized long incrementClock() {
		return ++lamportClock;
	}

	/**
	 * Updates the Lamport clock based on received timestamp
	 */
	public synchronized void updateClock(long receivedTimestamp) {
		lamportClock = Math.max(lamportClock, receivedTimestamp) + 1;
	}

	public void requestMutex() {
		// Create and timestamp the request
		long currentTimestamp = incrementClock();
		Request request = new Request();
		request.setTimestamp(currentTimestamp);
		request.setProcessId(processId);
		synchronized (this) {
			if (inCriticalSection) {
				throw new IllegalStateException("Already in critical section");
			}
			// Add to local queue
			requestQueue.add(request);
			log.info("Request mutex after adding to queue {}", requestQueue);
			log.info("Added request to queue: {}", request);

			// Initialize reply tracker
			replyTracker.put(processId, Collections.synchronizedSet(new HashSet<>()));
		}

		log.info("Broadcasting request to all");

		// Broadcast request to all other processes and collect replies
		// This part is not synchronized to prevent deadlock
		for (String url : processUrls.values()) {
			log.info("sending request to {}", url);
			try {
				lamportProxy.sendRequest(url, request);
			} catch (Exception e) {
				log.error("Failed to send request to {}: {}", url, e.getMessage());
			}
		}
	}

	/**
	 * Handles incoming request from other process and returns reply
	 */
	public void receiveRequest(Request request) {
		synchronized (this) {
			log.info("Received request from process {}: {}", request.getProcessId(), request);

			// Update local clock
			updateClock(request.getTimestamp());

			// Add request to queue
			requestQueue.add(request);
			log.info("receive Request after adding to queue {}", requestQueue);

			// Create reply with updated timestamp
			ReplyResponse reply = new ReplyResponse();
			reply.setFromProcessId(processId);
			reply.setTimestamp(incrementClock());

			lamportProxy.sendReply("http://localhost:" + request.getProcessId(), processId, lamportClock);
		}
	}

	/**
	 * Processes reply from other process
	 */
	public synchronized void receiveReply(String fromProcessId, long timestamp) {
		log.info("Received reply from process: {}", fromProcessId);

//        // Update local clock
		updateClock(timestamp);

		Set<String> replies = replyTracker.get(processId);
		if (replies != null) {
			replies.add(fromProcessId);
			log.info("Added reply from {} to tracker. Total replies: {}", fromProcessId, replies.size());
		}
	}

	/**
	 * Checks if process can enter critical section
	 */
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

	/**
	 * Enters critical section
	 */
	public synchronized void enterCriticalSection() {
		if (!canEnterCriticalSection()) {
			throw new IllegalStateException("Cannot enter critical section");
		}
		inCriticalSection = true;
		log.info("Entered critical section");
	}

	/**
	 * Releases mutual exclusion
	 */
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

		log.info("Released critical section");
	}

	/**
	 * Processes release message from other process
	 */
	public synchronized void receiveRelease(String fromProcessId, long timestamp) {
		log.info("Received release from process: {}", fromProcessId);

		// Update local clock
		updateClock(timestamp);

		// Remove the released request from queue
		requestQueue.removeIf(r -> r.getProcessId().equals(fromProcessId));
	}

	/**
	 * Registers a new process
	 */
	public void registerProcess(String processId, String url) {
		processUrls.put(processId, url);
		otherProcesses.add(processId);
		log.info("Registered process {} at {}", processId, url);
	}

	/**
	 * Unregisters a process
	 */
	public void unregisterProcess(String processId) {
		processUrls.remove(processId);
		otherProcesses.remove(processId);
		log.info("Unregistered process {}", processId);
	}

	/**
	 * Gets the number of registered processes
	 */
	public int getProcessCount() {
		return otherProcesses.size();
	}

	/**
	 * Checks if currently in critical section
	 */
	public boolean isInCriticalSection() {
		return inCriticalSection;
	}
}