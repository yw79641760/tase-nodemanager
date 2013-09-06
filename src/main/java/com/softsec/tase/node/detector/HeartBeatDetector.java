/**
 * 
 */
package com.softsec.tase.node.detector;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.node.ClusterType;
import com.softsec.tase.common.rpc.domain.node.NodePayload;
import com.softsec.tase.common.rpc.domain.node.NodeType;
import com.softsec.tase.common.rpc.exception.InvalidRequestException;
import com.softsec.tase.common.rpc.exception.TimeoutException;
import com.softsec.tase.common.rpc.exception.UnavailableException;
import com.softsec.tase.common.rpc.service.task.NodeTrackerService;
import com.softsec.tase.node.Configuration;
import com.softsec.tase.node.Constants;
import com.softsec.tase.node.queue.ContextQueue;
import com.softsec.tase.node.util.net.RpcUtils;
import com.softsec.tase.store.service.NodeHealthCheckerService;

/**
 * HeartBeatDetection.java
 * @author yanwei
 * @date 2013-3-29 下午6:16:21
 * @description
 */
public class HeartBeatDetector implements Runnable, StatefulJob {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatDetector.class);
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		
		LOGGER.info("Start collecting heart beat quartz ...");
		NodePayload nodePayload = generateHeartBeat();
		try {
			reportHeartBeat(nodePayload);
		} catch (InvalidRequestException ire) {
			throw new JobExecutionException("Failed to report heart beat : Invalid heart beat information : " + nodePayload + " : " + ire.getMessage(), ire);
		} catch (UnavailableException ue) {
			throw new JobExecutionException("Failed to report heart beat : Master Error : " + ue.getMessage(), ue);
		} catch (TimeoutException toe) {
			throw new JobExecutionException("Failed to report heart beat : Timeout : "  + toe.getMessage(), toe);
		} catch (TException te) {
			throw new JobExecutionException("Failed to report heart beat : RPC Error : " + te.getMessage(), te);
		} finally {
			nodePayload.setNodeRuntime(null);
			nodePayload = null;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		LOGGER.info("Start collecting heart beat thread ...");
		NodePayload nodePayload = generateHeartBeat();
		try {
			if (nodePayload != null) {
				reportHeartBeat(nodePayload);
			}
		} catch (InvalidRequestException ire) {
		} catch (UnavailableException ue) {
		} catch (TimeoutException toe) {
		} catch (TException te) {
		} finally{
			nodePayload.setNodeRuntime(null);
			nodePayload = null;
		}
	}

	/**
	 * generate heart beat information
	 * @return nodePayload
	 */
	public NodePayload generateHeartBeat() {
		
		final String NODE_ID = Configuration.get(Constants.LISTENER_DOMAIN, null)
				+ ":" + Configuration.getInt(Constants.TASK_SERVICE_PORT, 7000);
		
		final ClusterType CLUSTER_TYPE = ClusterType.valueOf(Configuration.get(Constants.CLUSTER_TYPE, "FIXED"));
		
		final String NODE_TYPE_STRING = Configuration.get(Constants.NODE_TYPE, "BASIC");
		
		final int MAX_QUEUE_SIZE = Configuration.getInt(Constants.MAX_QUEUE_SIZE, 500);
		
		NodePayload nodePayload = new NodePayload();
		
		// init payload id info
		nodePayload.setNodeId(NODE_ID);
		nodePayload.setClusterType(CLUSTER_TYPE);
		
		Set<NodeType> nodeTypeSet = new HashSet<NodeType>();
		String[] nodeTypes = NODE_TYPE_STRING.split(",");
		for (String nodeTypeStr : nodeTypes) {
			if (!StringUtils.isEmpty(nodeTypeStr)) {
				nodeTypeSet.add(NodeType.valueOf(nodeTypeStr.trim()));
			}
		}
		nodePayload.setNodeTypeList(nodeTypeSet);
		nodePayload.setNodeRuntime(NodeHealthCheckerService.getNodeRuntime());
		nodePayload.setQueueLimit(MAX_QUEUE_SIZE);
		nodePayload.setQueueNum(ContextQueue.getInstance().getReceivedContextQueueSize()
				+ ContextQueue.getInstance().getEquippedContextSize());
		nodePayload.setExpectedDelay(ContextQueue.getInstance().getAllReceivedContextExpectedDelay()
				+ ContextQueue.getInstance().getAllEquippedContextExpectedDelay());
		
		nodePayload.setPreferredProgramTypeList(ProgramDetector.getPreferredProgramTypeList());
		nodePayload.setPreferredProgramIdList(ProgramDetector.getLocalProgramIdList());
		
		return nodePayload;
	}
	
	/**
	 * report heart beat information to task manager using thrift rpc
	 * @param nodePayload
	 * @throws InvalidRequestException
	 * @throws UnavailableException
	 * @throws TimeoutException
	 * @throws TException
	 */
	public void reportHeartBeat(NodePayload nodePayload) 
			throws InvalidRequestException, UnavailableException, TimeoutException, TTransportException, TException {
		
		final String MASTER_SERVICE_IP = Configuration.get(Constants.MASTER_SERVICE_DOMAIN, "iscentos1");
		
		final int MASTER_SERVICE_PORT = Configuration.getInt(Constants.MASTER_NODE_PORT, 6020);
		
		final int timeout = Configuration.getInt(Constants.NETWORK_CONNECTION_TIMEOUT, 5000);

		final int retryTimes = Configuration.getInt(Constants.NETWORK_CONNECTION_RETRY_TIMES, 10);
		
		NodeTrackerService.Client nodeTrackerService = RpcUtils.getReceiver(MASTER_SERVICE_IP, MASTER_SERVICE_PORT, timeout, retryTimes);
		
		if (nodeTrackerService.reportHeartbeat(nodePayload) != 0) {
			LOGGER.error("Failed to report node [ " + nodePayload.getNodeId() + " ] to master [ " 
					+ MASTER_SERVICE_IP +  ":" + MASTER_SERVICE_PORT + " ]");
			throw new UnavailableException("Failed to report node [ " + nodePayload.getNodeId() + " ] to master [ " 
					+ MASTER_SERVICE_IP +  ":" + MASTER_SERVICE_PORT + " ]");
		}
	}
}
