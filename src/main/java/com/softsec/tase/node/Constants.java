/**
 * 
 */
package com.softsec.tase.node;

/**
 * 常量类
 * @author yanwei
 * @date 2012-12-28 上午9:16:42
 * 
 */
public final class Constants {
	
	/** rpc settings */
	public static final String LISTENER_DOMAIN= "nodemanager.listener.domain";
	
	public static final String TASK_SERVICE_PORT = "task.service.port";
	
	public static final String NODE_CLIENT_SERVICE_PORT = "node.client.service.port";
	
	public static final String PROGRAM_TRACKER_SERVICE_PORT = "program.tracker.service.port";
	
	public static final String NETWORK_CONNECTION_TIMEOUT = "network.connection.timeout";

	public static final String NETWORK_CONNECTION_RETRY_TIMES = "network.connection.retry.times";
	
	public static final String MASTER_SERVICE_DOMAIN = "taskmanager.listener.domain";
	
	public static final String MASTER_NODE_PORT = "taskmanager.node.port";
	
	/** node local heart beat setting */
	public static final String CLUSTER_TYPE = "cluster.type";
	
	public static final String NODE_TYPE = "node.type";
	
	public static final String PROGRAM_PREFERRED_TYPE = "program.preferred.type";
	
	/** container scheduling settings */
	public static final String MAX_QUEUE_SIZE = "max.queue.size";
	
	/** customer settings */
	public static final String CONTEXT_EXECUTOR_COUNT = "context.executor.count";
	
	public static final String CONTEXT_CHECKUP_INTERVAL = "context.checkup.interval";

	public static final String RESULT_COLLECTOR_COUNT = "result.collector.count";
	
	/** container execution settings */
	public static final String EXECUTION_TEMP_DIR = "execution.temp.dir";
	
	/** FTP settings */
	public static final String FTP_SERVER_URLS = "ftp.server.urls";

	public static final String FTP_DEFAULT_REPO = "ftp.default.repo";
	
	public static final String FTP_REINFORCE_REPO = "ftp.reinforce.repo";

}
