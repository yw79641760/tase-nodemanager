/**
 * 
 */
package com.softsec.tase.node.util.cmd;

import java.util.BitSet;

import com.softsec.tase.node.domain.ProcessResult;

/**
 * ThreadWatcher.java
 * @author yanwei
 * @date 2013-4-1 下午4:25:04
 * @description
 */
public class ThreadTimer implements Runnable {
	
	private Process process;
	
	private BitSet processFlagSet;
	
	private long timeout;
	
	public ThreadTimer(Process process, BitSet processFlagSet, long timeout) {
		this.process = process;
		this.processFlagSet = processFlagSet;
		this.timeout = timeout;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException ie) {
		}
		interrupt();
	}
	
	public void interrupt() {
		processFlagSet.set(ProcessResult.TIMEOUT_CODE_BIT);
		process.destroy();
	}
}