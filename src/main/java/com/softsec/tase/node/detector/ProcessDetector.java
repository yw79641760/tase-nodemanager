/**
 * 
 */
package com.softsec.tase.node.detector;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobStatus;
import com.softsec.tase.common.rpc.exception.InvalidRequestException;
import com.softsec.tase.common.rpc.exception.TimeoutException;
import com.softsec.tase.common.rpc.exception.UnavailableException;
import com.softsec.tase.common.rpc.service.task.NodeTrackerService;
import com.softsec.tase.node.Constants;
import com.softsec.tase.node.domain.ProcessResult;
import com.softsec.tase.node.exception.ExecutionException;
import com.softsec.tase.node.util.net.RpcUtils;
import com.softsec.tase.store.Configuration;

/**
 * ProcessDetector
 * <p> monitoring task execution and detect process result for reporting</p>
 * @author yanwei
 * @since 2013-9-8 下午2:54:12
 * @version
 */
public class ProcessDetector {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDetector.class);

	public JobStatus getTaskStatus(ProcessResult processResult) {
		JobStatus status = null;
		if (processResult.getProcessFlagCode(0)) {
			status = JobStatus.FAILURE;
		} else if (processResult.getProcessFlagCode(1)){
			status = JobStatus.INTERRUPTED;
		} else if (processResult.getProcessFlagCode(2)) {
			status = JobStatus.TIMEOUT;
		}
		return status;
	}
	
	public int reportTaskStatus(Long taskId, JobPhase jobPhase, JobStatus jobStatus) throws ExecutionException {
		
		int retCode = 0;
		
		String NODE_ID = Configuration.get(Constants.LOCAL_LISTENER_DOMAIN, "127.0.0.1") + ":" + Configuration.getInt(Constants.TASK_SERVICE_PORT, 7000);
		
		String NODE_TRACKER_SERVICE_IP = Configuration.get(Constants.MASTER_SERVICE_DOMAIN, "127.0.0.1");
		int NODE_TRACKER_SERVICE_PORT = Configuration.getInt(Constants.MASTER_NODE_PORT, 6020);
		int timeout = Configuration.getInt(Constants.NETWORK_CONNECTION_TIMEOUT, 5000);
		int retryTimes = Configuration.getInt(Constants.NETWORK_CONNECTION_RETRY_TIMES, 10);
		
		NodeTrackerService.Client receiver = null;
		try {
			receiver = RpcUtils.getReceiver(NODE_TRACKER_SERVICE_IP, NODE_TRACKER_SERVICE_PORT, timeout, retryTimes);
		} catch (TTransportException tte) {
			LOGGER.error("Failed to establish RPC connection : " + tte.getMessage(), tte);
			throw new ExecutionException("Failed to establish RPC connection : " + tte.getMessage(), tte);
		}
		
		try {
			retCode = receiver.reportTaskExecutionStatus(NODE_ID, taskId, jobPhase, jobStatus);
		} catch (InvalidRequestException ire) {
			LOGGER.error("Failed to update task [ " + taskId + " : " + jobPhase + " ] to [ " + jobStatus + " ] : " + ire.getMessage(), ire);
			throw new ExecutionException("Failed to update task [ " + taskId + " : " + jobPhase + " ] to [ " + jobStatus + " ] : " + ire.getMessage(), ire);
		} catch (UnavailableException ue) {
			LOGGER.error("Failed to update task [ " + taskId + " : " + jobPhase + " ] to [ " + jobStatus + " ] : " + ue.getMessage(), ue);
			throw new ExecutionException("Failed to update task [ " + taskId + " : " + jobPhase + " ] to [ " + jobStatus + " ] : " + ue.getMessage(), ue);
		} catch (TimeoutException te) {
			LOGGER.error("Failed to update task [ " + taskId + " : " + jobPhase + " ] to [ " + jobStatus + " ] : " + te.getMessage(), te);
			throw new ExecutionException("Failed to update task [ " + taskId + " : " + jobPhase + " ] to [ " + jobStatus + " ] : " + te.getMessage(), te);
		} catch (TException te) {
			LOGGER.error("Failed to update task [ " + taskId + " : " + jobPhase + " ] to [ " + jobStatus + " ] : " + te.getMessage(), te);
			throw new ExecutionException("Failed to update task [ " + taskId + " : " + jobPhase + " ] to [ " + jobStatus + " ] : " + te.getMessage(), te);
		}
		
		RpcUtils.close(receiver);
		return retCode;
	}
}
