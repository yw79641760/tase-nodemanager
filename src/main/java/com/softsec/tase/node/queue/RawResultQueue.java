/**
 * 
 */
package com.softsec.tase.node.queue;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import com.softsec.tase.node.domain.RawResult;

/**
 * RawResultQueue.java
 * @author yanwei
 * @date 2013-3-28 下午4:59:22
 * @description
 */
public class RawResultQueue {

	private LinkedBlockingQueue<RawResult> rawResultQueue = new LinkedBlockingQueue<RawResult>();
	
	private static final RawResultQueue rawResultQueueSingleton = new RawResultQueue();
	
	public RawResultQueue() {
	}
	
	public static RawResultQueue getInstance() {
		return rawResultQueueSingleton;
	}
	
	public synchronized LinkedBlockingQueue<RawResult> getRawResultQueue() {
		return rawResultQueue;
	}
	
	public synchronized boolean addToRawResultQueue(RawResult rawResult) {
		return rawResultQueue.add(rawResult);
	}
	
	public synchronized boolean addToRawResultQueue(Set<RawResult> rawResultSet) {
		return rawResultQueue.addAll(rawResultSet);
	}
}
