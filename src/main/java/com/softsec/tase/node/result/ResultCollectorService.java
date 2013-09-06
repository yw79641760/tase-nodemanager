/**
 * 
 */
package com.softsec.tase.node.result;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.exception.InvalidRequestException;
import com.softsec.tase.common.rpc.exception.TimeoutException;
import com.softsec.tase.common.rpc.exception.UnavailableException;
import com.softsec.tase.common.rpc.service.task.NodeTrackerService;
import com.softsec.tase.node.Constants;
import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.node.exception.ResultException;
import com.softsec.tase.node.util.net.RpcUtils;
import com.softsec.tase.store.Configuration;

/**
 * ResultCollectService
 * <p> </p>
 * @author yanwei
 * @since 2013-8-8 上午8:49:27
 * @version
 */
public abstract class ResultCollectorService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultCollectorService.class);
	
	protected RawResult rawResult = null;
	
	protected String contentChecksum = null;
	
	/**
	 * check RawResult.content 's validation, 
	 * if valid, replace RawResult.content to database item
	 * if valid, assign this.contentChecksum to MD5(content)
	 * @param rawResult
	 * @return 0: valid, -1:invalid
	 * @throws ResultException
	 */
	public abstract int validate(RawResult rawResult) throws ResultException;
	
	/**
	 * submit result or new job request
	 * to master node
	 * @return
	 * @throws ResultException
	 */
	public int commit() throws ResultException {
		
		int retCode = -1;
		
		String NODE_TRACKER_SERVICE_IP = Configuration.get(Constants.MASTER_SERVICE_DOMAIN, "127.0.0.1");
		int NODE_TRACKER_SERVICE_PORT = Configuration.getInt(Constants.MASTER_NODE_PORT, 6020);
		int timeout = Configuration.getInt(Constants.NETWORK_CONNECTION_TIMEOUT, 5000);
		int retryTimes = Configuration.getInt(Constants.NETWORK_CONNECTION_RETRY_TIMES, 10);
		
		NodeTrackerService.Client receiver = null;
		try {
			receiver = RpcUtils.getReceiver(NODE_TRACKER_SERVICE_IP, NODE_TRACKER_SERVICE_PORT, timeout, retryTimes);
		} catch (TTransportException tte) {
			LOGGER.error("Failed to establish RPC connection : " + tte.getMessage(), tte);
			throw new ResultException("Failed to establish RPC connection : " + tte.getMessage(), tte);
		}
		
		try {
			retCode = receiver.submitResult(rawResult.getAppType(), rawResult.getJobLifecycle(), rawResult.getResultType(), 
											rawResult.getContent(), contentChecksum, rawResult.getTaskId(), rawResult.getIdentifier());
		} catch (InvalidRequestException ire) {
			LOGGER.error("Failed to submit result [ " + rawResult.getTaskId() 
					+ " ] to [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ] : " + ire.getMessage(), ire);
			throw new ResultException("Failed to submit result [ " + rawResult.getTaskId() 
					+ " ] to [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ] : " + ire.getMessage(), ire);
		} catch (UnavailableException ue) {
			LOGGER.error("Failed to submit result [ " + rawResult.getTaskId() 
					+ " ] to [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ] : " + ue.getMessage(), ue);
			throw new ResultException("Failed to submit result [ " + rawResult.getTaskId() 
					+ " ] to [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ] : " + ue.getMessage(), ue);
		} catch (TimeoutException te) {
			LOGGER.error("Failed to submit result [ " + rawResult.getTaskId() 
					+ " ] to [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ] : " + te.getMessage(), te);
			throw new ResultException("Failed to submit result [ " + rawResult.getTaskId() 
					+ " ] to [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ] : " + te.getMessage(), te);
		} catch (TException te) {
			LOGGER.error("Failed to submit result [ " + rawResult.getTaskId() 
					+ " ] to [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ] : " + te.getMessage(), te);
			throw new ResultException("Failed to submit result [ " + rawResult.getTaskId() 
					+ " ] to [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ] : " + te.getMessage(), te);
		}
		
		
		RpcUtils.close(receiver);
		
		if (retCode == 0) {
			LOGGER.info("Succeed to distribute task [ " + rawResult.getTaskId() 
					+ " ] to Node [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ].");
		} else {
			LOGGER.error("Failed to distribute task [ " + rawResult.getTaskId() 
					+ " ] to Node [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ] with error code : " + retCode);
			throw new ResultException("Failed to distribute task [ " + rawResult.getTaskId() 
					+ " ] to Node [ " + NODE_TRACKER_SERVICE_IP + ":" + NODE_TRACKER_SERVICE_PORT + " ] with error code : " + retCode);
		}
		rawResult = null;
		contentChecksum = null;
		return retCode;
	}
}
