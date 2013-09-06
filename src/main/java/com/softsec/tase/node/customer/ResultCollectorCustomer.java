/**
 * 
 */
package com.softsec.tase.node.customer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.node.Configuration;
import com.softsec.tase.node.Constants;
import com.softsec.tase.node.queue.RawResultQueue;

/**
 * ResultCollectorCustomer.java
 * @author yanwei
 * @date 2013-3-28 下午5:29:17
 * @description
 */
public class ResultCollectorCustomer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultCollectorCustomer.class);
	
	private int threadCount;
	
	private ExecutorService threadPool;
	
	public ResultCollectorCustomer() {
		threadCount = Configuration.getInt(Constants.RESULT_COLLECTOR_COUNT, 3);
	}
	
	public void start() {
		LOGGER.info("Create result queue customers, customer count [ " + threadCount + " ].");
		threadPool = Executors.newFixedThreadPool(threadCount);
		for (int i = 0; i < threadCount; i++) {
			Runnable collectorCustomer = new ResultCollectorThread(RawResultQueue.getInstance().getRawResultQueue(), new ResultCollector());
			threadPool.submit(new Thread(collectorCustomer), "ResultCollector-" + i);
		}
		threadPool.shutdown();
	}
}
