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
import com.softsec.tase.node.detector.ProcessDetector;
import com.softsec.tase.node.queue.ContextAccessController;
import com.softsec.tase.node.queue.ContextQueue;

/**
 * ContextExecutorCustomer.java
 * @author yanwei
 * @date 2013-2-7 下午8:04:42
 * @description
 */
public class ContextHandlerCustomer {
	

	private static final Logger LOGGER = LoggerFactory.getLogger(ContextHandlerCustomer.class);
	
	private int executorCount;
	
	private ExecutorService threadPool;
	
	public ContextHandlerCustomer() {
		executorCount = Configuration.getInt(Constants.CONTEXT_EXECUTOR_COUNT, 3);
	}
	
	public void start() {
		LOGGER.info("Create context executor queue customers, customer count [ " + executorCount + " ]." );
		// init process flag list
		ContextAccessController.getInstance().initProcessFlagList(executorCount);

		threadPool = Executors.newFixedThreadPool(executorCount);
		for (int index = 0; index < executorCount; index++) {
			Runnable executorCustomer = new ContextHandlerThread(ContextQueue.getInstance().getEquippedContextQueue(), 
																new ContextHandler(), 
																new ProcessDetector(), 
																index);
			threadPool.submit(new Thread(executorCustomer, "ContextExecutor-" + index));
		}
		
		threadPool.shutdown();
	}
}
