/**
 * 
 */
package com.softsec.tase.node;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.service.node.NodeClientService;
import com.softsec.tase.common.rpc.service.node.ProgramTrackerService;
import com.softsec.tase.common.rpc.service.node.TaskService;
import com.softsec.tase.node.customer.ContextHandlerCustomer;
import com.softsec.tase.node.customer.ResultCollectorCustomer;
import com.softsec.tase.node.scheduler.ContainerScheduler;
import com.softsec.tase.node.scheduler.PriorityScheduler;
import com.softsec.tase.node.service.NodeClientServiceImpl;
import com.softsec.tase.node.service.ProgramTrackerServiceImpl;
import com.softsec.tase.node.service.TaskServiceImpl;

/**
 * 
 * @author yanwei
 * @date 2013-1-11 上午10:09:19
 * 
 */
public class Startup {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Startup.class);
	
	private static final int TASK_SERVICE_PORT = Configuration.getInt(Constants.TASK_SERVICE_PORT, 7000);
	
	private static final int NODE_CLIENT_SERVICE_PORT = Configuration.getInt(Constants.NODE_CLIENT_SERVICE_PORT, 7010);
	
	private static final int PROGRAM_TRACKER_SERVICE_PORT = Configuration.getInt(Constants.PROGRAM_TRACKER_SERVICE_PORT, 7020);
	
	/**
	 * startup task service listener
	 */
	private void startupTaskServiceListener() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				startupTaskServer();
			}

			private void startupTaskServer() {
				try {
					final TServerTransport serverTransport 
						= new TServerSocket(TASK_SERVICE_PORT);
					final TaskService.Processor<TaskService.Iface> processor 
						= new TaskService.Processor<TaskService.Iface>(new TaskServiceImpl());
					final TServer server 
						= new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
					LOGGER.info("Task Service start listening on port : [ " + TASK_SERVICE_PORT + " ]...");
					server.serve();
				} catch (TTransportException tte) {
					LOGGER.error("Failed to startup task service listener at port : " + TASK_SERVICE_PORT + " : " + tte.getMessage(), tte);
					throw new RuntimeException("Failed to startup task service listener at port : " + TASK_SERVICE_PORT + " : " + tte.getMessage(), tte);
				}
			}}) {
			
		}.start();
	}
	
	/**
	 * startup node client service listener
	 */
	private void startupNodeClientServiceListener() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				startupNodeClientServer();
			}
			
			private void startupNodeClientServer() {
				try {
					final TServerTransport serverTransport 
					= new TServerSocket(NODE_CLIENT_SERVICE_PORT);
					final NodeClientService.Processor<NodeClientService.Iface> processor 
					= new NodeClientService.Processor<NodeClientService.Iface>(new NodeClientServiceImpl());
					final TServer server 
					= new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
					LOGGER.info("Node Client Service start listening on port : [ " + NODE_CLIENT_SERVICE_PORT + " ]...");
					server.serve();
				} catch (TTransportException tte) {
					LOGGER.error("Failed to startup node client service listener at port : " + NODE_CLIENT_SERVICE_PORT + " : " + tte.getMessage(), tte);
					throw new RuntimeException("Failed to startup node client service listener at port : " + NODE_CLIENT_SERVICE_PORT + " : " + tte.getMessage(), tte);
				}
			}}) {
			
		}.start();
	}
	
	/**
	 * startup program tracker service listener
	 */
	private void startupProgramTrackerServiceListener() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				startupProgramTrackerServer();
			}

			private void startupProgramTrackerServer() {
				try {
					final TServerTransport serverTransport 
						= new TServerSocket(PROGRAM_TRACKER_SERVICE_PORT);
					final ProgramTrackerService.Processor<ProgramTrackerService.Iface> processor 
						= new ProgramTrackerService.Processor<ProgramTrackerService.Iface>(new ProgramTrackerServiceImpl());
					final TServer server 
						= new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
					LOGGER.info("Program Tracker Service start listening on port : [ " + PROGRAM_TRACKER_SERVICE_PORT + " ]...");
					server.serve();
				} catch (TTransportException tte) {
					LOGGER.error("Failed to startup program tracker service at port : " + PROGRAM_TRACKER_SERVICE_PORT + " : " + tte.getMessage(), tte);
					throw new RuntimeException("Failed to startup program tracker service at port : " + PROGRAM_TRACKER_SERVICE_PORT + " : " + tte.getMessage(), tte);
				}
			}
		}){
		}.start();
	}
	
	/**
	 *  startup container scheduler and quartz jobs
	 */
	public void startupScheduler() {
		// container scheduler startup
		ContainerScheduler containerScheduler = new PriorityScheduler();
		containerScheduler.start();
		
		// quartz jobs startup
		StdSchedulerFactory factory = new StdSchedulerFactory();
		Scheduler quartz = null;
		try {
			quartz = factory.getScheduler();
			quartz.start();
		} catch (SchedulerException se) {
			LOGGER.error("Failed to startup quartz scheduler : " + se.getMessage(), se);
			throw new RuntimeException("Failed to startup quartz scheduler : " + se.getMessage(), se);
		}
	}
	
	/**
	 * startup context handler & result collector customers
	 */
	public void startupCustomers() {
		ContextHandlerCustomer contextHandlerCustomer = new ContextHandlerCustomer();
		contextHandlerCustomer.start();
		ResultCollectorCustomer resultCollectorCustomer = new ResultCollectorCustomer();
		resultCollectorCustomer.start();
		
	}
	
	private void startupNodeManager() {
		startupScheduler();
		startupCustomers();
		startupTaskServiceListener();
		startupNodeClientServiceListener();
		startupProgramTrackerServiceListener();
	}
	
	/**
	 * main function & node manager entry
	 * @param
	 */
	public static void main(String[] args) {
		Startup startup = new Startup();
		startup.startupNodeManager();
	}

}
