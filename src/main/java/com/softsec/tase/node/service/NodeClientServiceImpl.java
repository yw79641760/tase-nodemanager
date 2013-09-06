/**
 * 
 */
package com.softsec.tase.node.service;

import org.apache.thrift.TException;

import com.softsec.tase.common.rpc.exception.TimeoutException;
import com.softsec.tase.common.rpc.exception.UnavailableException;
import com.softsec.tase.common.rpc.service.node.NodeClientService;

/**
 * NodeClientServiceImpl.java
 * @author yanwei
 * @date 2013-3-27 下午7:08:33
 * @description
 */
public class NodeClientServiceImpl implements NodeClientService.Iface{

	/* (non-Javadoc)
	 * @see com.softsec.tase.rpc.service.node.NodeClientService.Iface#obtainQueueInfo()
	 */
	@Override
	public String obtainQueueInfo() throws UnavailableException,
			TimeoutException, TException {
		// TODO Auto-generated method stub
		return null;
	}

}
