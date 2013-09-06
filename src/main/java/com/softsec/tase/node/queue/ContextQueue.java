/**
 * 
 */
package com.softsec.tase.node.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import javax.print.attribute.standard.JobPriority;

import com.softsec.tase.common.rpc.domain.container.Context;
import com.softsec.tase.node.Configuration;
import com.softsec.tase.node.Constants;

/**
 * ContextQueue.java
 * @author yanwei
 * @date 2013-2-5 下午12:01:52
 * @description
 */
public class ContextQueue {
	
	private PriorityBlockingQueue<Context> receivedContextQueue = new PriorityBlockingQueue<Context>();
	
	private PriorityBlockingQueue<Context> equippedContextQueue = new PriorityBlockingQueue<Context>();
	
	private static final ContextQueue contextQueueSingleton = new ContextQueue();
	
	private static final int DEFAULT_QUEUE_LIMIT = 5000;
	
	private static int MAX_QUEUE_SIZE;
	
	public ContextQueue() {
		MAX_QUEUE_SIZE = Configuration.getInt(Constants.MAX_QUEUE_SIZE, DEFAULT_QUEUE_LIMIT);
	}
	
	public static ContextQueue getInstance() {
		return contextQueueSingleton;
	}
	
	public int getMaxQueueSize() {
		return MAX_QUEUE_SIZE;
	}
	
	public synchronized PriorityBlockingQueue<Context> getReceivedContextQueue() {
		return receivedContextQueue;
	}
	
	public synchronized PriorityBlockingQueue<Context> getEquippedContextQueue() {
		return equippedContextQueue;
	}
	
	public synchronized boolean addToReceivedContextQueue(Context context) {
		return receivedContextQueue.add(context);
	}
	
	public synchronized boolean addToReceivedContextQueue(Set<Context> contextSet) {
		return receivedContextQueue.addAll(contextSet);
	}
	
	public synchronized boolean addToEquippedContextQueue(Context context) {
		return equippedContextQueue.add(context);
	}
	
	public synchronized boolean addToEquippedContextQueue(Set<Context> contextSet) {
		return equippedContextQueue.addAll(contextSet);
	}
	
	public synchronized List<Long> getReceivedTaskIdList() {
		List<Long> receivedTaskIdList = new ArrayList<Long>();
		for (Context context : receivedContextQueue) {
			receivedTaskIdList.add(context.getTaskId());
		}
		return receivedTaskIdList;
	}
	
	public synchronized List<Long> getEquippedTaskIdList() {
		List<Long> equippedTaskIdList = new ArrayList<Long>();
		for (Context context : equippedContextQueue) {
			equippedTaskIdList.add(context.getTaskId());
		}
		return equippedTaskIdList;
	}
	
	public synchronized int getReceivedContextQueueSize() {
		return receivedContextQueue.size();
	}
	
	public synchronized int getEquippedContextSize() {
		return equippedContextQueue.size();
	}
	
	public synchronized long getAllReceivedContextExpectedDelay() {
		long expectedDelay = 0L;
		for (Context context : receivedContextQueue) {
			expectedDelay += context.getTimeout();
		}
		return expectedDelay;
	}

	public synchronized long getReceivedContextExpectedDelay(JobPriority priority) {
		long expectedDelay = 0L;
		for (Context context : receivedContextQueue) {
			if (priority.getValue() > context.getPriority().getValue()) {
				expectedDelay += context.getTimeout();
			}
		}
		return expectedDelay;
	}
	
	public synchronized long getAllEquippedContextExpectedDelay() {
		long expectedDelay = 0L;
		for (Context context : equippedContextQueue) {
			expectedDelay += context.getTimeout();
		}
		return expectedDelay;
	}
	
	public synchronized long getEquippedContextExpectedDelay(JobPriority priority) {
		long expectedDelay = 0L;
		for (Context context : equippedContextQueue) {
			if (priority.getValue() > context.getPriority().getValue()) {
				expectedDelay += context.getTimeout();
			}
		}
		return expectedDelay;
	}
}
