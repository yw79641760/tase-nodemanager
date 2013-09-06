/**
 * 
 */
package com.softsec.tase.node.scheduler;

import com.softsec.tase.node.queue.EquippedContextProducer;

/**
 * ContainerScheduler.java
 * @author yanwei
 * @date 2013-3-28 上午8:39:37
 * @description
 */
public abstract class ContainerScheduler {

	protected Thread equippedContextProducerThread = null;
	
	protected EquippedContextProducer equippedContextProducerRunner = null;
	
	public void start() {
	}
	
	public void shutdown() {
		if (equippedContextProducerThread != null && equippedContextProducerThread.isAlive()) {
			equippedContextProducerRunner.shutdown();
		}
	}
}
