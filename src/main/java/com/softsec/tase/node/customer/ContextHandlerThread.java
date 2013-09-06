/**
 * 
 */
package com.softsec.tase.node.customer;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.container.Context;
import com.softsec.tase.common.rpc.domain.job.JobExecutionMode;
import com.softsec.tase.node.Configuration;
import com.softsec.tase.node.Constants;
import com.softsec.tase.node.exception.ExecutionException;
import com.softsec.tase.node.queue.ContextAccessController;

/**
 * ContextExecutorThread.java
 * @author yanwei
 * @date 2013-2-7 下午8:04:55
 * @description
 */
public class ContextHandlerThread implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextHandlerThread.class);
	
	private static final int CONTEXT_CHECKUP_INTERVAL = Configuration.getInt(Constants.CONTEXT_CHECKUP_INTERVAL, 5000);
	
	private BlockingQueue<Context> queue;
	
	private ContextHandler contextExecutor;
	
	private int index;
	
	public ContextHandlerThread(BlockingQueue<Context> queue, ContextHandler executor, int index) {
		this.queue = queue;
		this.contextExecutor = executor;
		this.index = index;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			Context context = null;
			try {
				context = queue.take();
				if (context != null) {
					if (context.getJobExecutionMode().equals(JobExecutionMode.CONCURRENT)) {
						accessConcurrentContext(context, index);
					} else if (context.getJobExecutionMode().equals(JobExecutionMode.EXCLUSIVE)) {
						accessExclusiveContext(context);
					}
				} else {
					LOGGER.info("Equipped context queue is EMPTY.");
				}
			} catch (InterruptedException ie) {
				LOGGER.error("Thread [ " + Thread.currentThread().getName() + " ] is interrupted while executing context [ " 
						+ context.getTaskId() + " ] : " + ie.getMessage(), ie);
			} catch (ExecutionException ee) {
			}
		}
	}

	/**
	 * access concurrent context <br />
	 * get the lock and release it immediately
	 * @param context
	 * @param index
	 */
	private void accessConcurrentContext(Context context, int index) throws ExecutionException {
		try {
			ContextAccessController.getInstance().getLock().lock();
			ContextAccessController.getInstance().setProcessFlag(index, Thread.State.RUNNABLE);
		} finally {
			ContextAccessController.getInstance().getLock().unlock();
		}
		contextExecutor.launch(context);
		ContextAccessController.getInstance().setProcessFlag(index, Thread.State.NEW);
	}
	
	/**
	 * access exclusive context <br />
	 * get the lock and release it until finishing execution
	 * and the execution will be launched after all previous running container exits.
	 * @param context
	 */
	private void accessExclusiveContext(Context context) throws InterruptedException, ExecutionException {
		try {
			ContextAccessController.getInstance().getLock().lock();
			while(ContextAccessController.getInstance().existRunningProcess()) {
				Thread.sleep(CONTEXT_CHECKUP_INTERVAL);
			}
			contextExecutor.launch(context);
		} finally {
			ContextAccessController.getInstance().getLock().unlock();
		}
	}
}
