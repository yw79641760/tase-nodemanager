/**
 * 
 */
package com.softsec.tase.node.service;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.container.Context;
import com.softsec.tase.common.rpc.domain.node.NodeInfo;
import com.softsec.tase.common.rpc.exception.InvalidRequestException;
import com.softsec.tase.common.rpc.exception.NotFoundException;
import com.softsec.tase.common.rpc.exception.TimeoutException;
import com.softsec.tase.common.rpc.exception.UnavailableException;
import com.softsec.tase.common.rpc.service.node.TaskService;
import com.softsec.tase.node.queue.ContextQueue;

/**
 * TaskServiceImpl.java
 * @author yanwei
 * @date 2013-2-7 下午1:59:43
 * @description
 */
public class TaskServiceImpl implements TaskService.Iface {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.node.TaskService.Iface#submitContext(com.softsec.tase.rpc.domain.container.Context)
	 */
	@Override
	public int submitContext(Context context) throws InvalidRequestException,
			UnavailableException, TimeoutException, TException {
		
		int retValue = 0;
		if (context == null) {
			retValue = -1;
			LOGGER.error("Context must not be null.");
			throw new InvalidRequestException("Context must not be null : " + context);
		} else {
			ContextQueue.getInstance().addToReceivedContextQueue(context);
			LOGGER.info("Received context : " + context.getTaskId() + ", adding to context queue ...");
		}
		
		return retValue;
	}

	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.node.TaskService.Iface#updateNodeInfo()
	 */
	@Override
	public NodeInfo updateNodeInfo() throws UnavailableException,
			TimeoutException, TException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.node.TaskService.Iface#updateProgram(java.lang.String)
	 */
	@Override
	public int updateProgram(String programName)
			throws InvalidRequestException, UnavailableException,
			NotFoundException, TimeoutException, TException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.node.TaskService.Iface#terminateContext(java.lang.String)
	 */
	@Override
	public int terminateContext(String taskId) throws InvalidRequestException,
			UnavailableException, NotFoundException, TimeoutException,
			TException {
		
		return 0;
	}

}
