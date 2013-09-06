/**
 * 
 */
package com.softsec.tase.node.scheduler;

import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.container.Context;
import com.softsec.tase.node.queue.ContextQueue;
import com.softsec.tase.node.queue.EquippedContextProducer;

/**
 * PriorityScheduler.java
 * @author yanwei
 * @date 2013-3-28 上午9:42:33
 * @description
 */
public class PriorityScheduler extends ContainerScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityScheduler.class);
	
	public void start() {
		LOGGER.info("Starting priority scheduler ...");
		
		PriorityBlockingQueue<Context> globalContextQueue = ContextQueue.getInstance().getReceivedContextQueue();
		EquippedContextProducer equippedContextProducer = new EquippedContextProducer(globalContextQueue);
		Thread equippedContextProducerRunner = new Thread(equippedContextProducer);
		equippedContextProducerRunner.start();
	}
}
