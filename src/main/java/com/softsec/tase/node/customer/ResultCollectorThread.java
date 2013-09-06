/**
 * 
 */
package com.softsec.tase.node.customer;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.node.exception.ResultException;

/**
 * ResultCollectorThread.java
 * @author yanwei
 * @date 2013-3-28 下午5:28:34
 * @description
 */
public class ResultCollectorThread implements Runnable{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultCollectorThread.class);
	
	private LinkedBlockingQueue<RawResult> queue;
	
	private ResultCollector resultCollector;
	
	public ResultCollectorThread(LinkedBlockingQueue<RawResult> queue, ResultCollector resultCollector) {
		this.queue = queue;
		this.resultCollector = resultCollector;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			RawResult rawResult = null;
			try {
				rawResult = queue.take();
				if (rawResult != null) {
					try {
						resultCollector.collect(rawResult);
					} catch (ResultException re) {
					}
				} else {
					LOGGER.info("Raw result queue is EMPTY.");
				}
			} catch (InterruptedException ie) {
				LOGGER.error("Failed to collect result : " + ie.getMessage(), ie);
			}
		}
	}

}
