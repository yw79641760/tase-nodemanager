/**
 * 
 */
package com.softsec.tase.node.queue;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ContextQueueAccessController.java
 * @author yanwei
 * @date 2013-3-28 上午11:29:53
 * @description
 */
public class ContextAccessController {

	private ReentrantLock lock = new ReentrantLock();
	
	private ArrayList<Thread.State> processFlagList = new ArrayList<Thread.State>();
	
	private static final ContextAccessController contextAccessController = new ContextAccessController();
	
	public ContextAccessController() {
	}
	
	public static ContextAccessController getInstance() {
		return contextAccessController;
	}
	
	public void initProcessFlagList(int scale) {
		for(int index = 0; index < scale; index++) {
			processFlagList.add(Thread.State.NEW);
		}
	}
	
	public synchronized ReentrantLock getLock() {
		return lock;
	}
	
	public synchronized ArrayList<Thread.State> getProcessFlagList() {
		return processFlagList;
	}
	
	public synchronized void setProcessFlag(int index, Thread.State status) {
		processFlagList.set(index, status);
	}
	
	public synchronized Thread.State getProcessFlag(int index) {
		return processFlagList.get(index);
	}
	
	public synchronized boolean existRunningProcess() {
		return processFlagList.contains(Thread.State.RUNNABLE);
	}
}	
