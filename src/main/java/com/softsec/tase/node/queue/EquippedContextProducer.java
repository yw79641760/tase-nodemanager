/**
 * 
 */
package com.softsec.tase.node.queue;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.container.Context;
import com.softsec.tase.common.rpc.domain.job.ContextParameter;
import com.softsec.tase.common.rpc.domain.job.JobParameter;
import com.softsec.tase.node.Constants;
import com.softsec.tase.node.detector.ProgramDetector;
import com.softsec.tase.node.exception.PreparationException;
import com.softsec.tase.store.Configuration;
import com.softsec.tase.store.exception.FtpUtilsException;
import com.softsec.tase.store.util.PathUtils;
import com.softsec.tase.store.util.net.FtpConnFactory;
import com.softsec.tase.store.util.net.FtpUtils;

/**
 * EquippedContextProduer.java
 * @author yanwei
 * @date 2013-3-28 上午8:48:50
 * @description
 */
public class EquippedContextProducer implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EquippedContextProducer.class);
	
	private static final String PROGRAM_REPO = PathUtils.formatPath4File(Configuration.get(Constants.EXECUTION_TEMP_DIR, "../"));

	private PriorityBlockingQueue<Context> equippedContextQueue = ContextQueue.getInstance().getEquippedContextQueue();
	
	private BlockingQueue<Context> queue;
	
	private volatile boolean stop = false;
	
	public EquippedContextProducer() {
	}
	
	public EquippedContextProducer(BlockingQueue<Context> queue) {
		this.queue = queue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		LOGGER.info("Start scheduling context ...");
		
		while(!Thread.currentThread().isInterrupted() && !stop) {
			Context context = null;
			try {
				context = queue.take();
				if (context != null) {
					try {
						Context preparedContext = load(context);
						equippedContextQueue.add(preparedContext);
					} catch (PreparationException pe) {
						LOGGER.error("Failed to prepare context for task [ " + context.getTaskId() + " ] : " + pe.getMessage(), pe);
					}
				} else {
					LOGGER.info("Global context queue is EMPTY.");
				}
			} catch (InterruptedException ie) {
				LOGGER.info("Failed to get context from global context queue : " + ie.getMessage(), ie);
			}
		}
	}
	
	public Context load(Context context) throws PreparationException {
		
		String ftpHost = FtpConnFactory.getRandomFtpHost();
		FTPClient ftpClient = null;
		try {
			ftpClient = FtpConnFactory.connect(ftpHost);
		} catch (FtpUtilsException fue) {
			LOGGER.error("Failed to connect ftp server [ " + ftpHost + " ] : " + fue.getMessage(), fue);
			throw new PreparationException("Failed to connect ftp server [ " + ftpHost + " ] : " + fue.getMessage(), fue);
		}
		
		// detect program existance
		// download script & program to local PROGRAM_REPO if local does not exist
		context.setScriptName("/home/ss/V2_SoftSec_Reinforce_generate_auto/build/build-target/releaseProject/bin/start.sh");
//		if (!ProgramDetector.getLocalProgramIdList().contains(context.getProgramId())) {
//			File programDir = new File(String.valueOf(context.getProgramId()));
//			programDir.mkdir();
//			try {
//				FtpUtils.download(ftpClient, context.getScriptPath(), PROGRAM_REPO + "/" + context.getProgramId());
//				FtpUtils.download(ftpClient, context.getExecutablePath(), PROGRAM_REPO + "/" + context.getProgramId());
//			} catch (FtpUtilsException fue) {
//				LOGGER.error("Failed to download program [ " + context.getProgramId() + " : " + context.getProgramName() 
//						+ " ] to local : " + fue.getMessage(), fue);
//				throw new PreparationException("Failed to download program [ " + context.getProgramId() + " : " + context.getProgramName() 
//						+ " ] to local : " + fue.getMessage(), fue);
//			}
//		}
		
		// TODO check script & program integrity
		// if md5 does not match then throw PreparationException
		
		// prepare parameter to local
		JobParameter jobParameter = context.getParameter();
		List<ContextParameter> parameterList = jobParameter.getContextParameterList();
		if (parameterList != null && parameterList.size() != 0) {
			for (ContextParameter parameter : parameterList) {
				if (parameter.isNeedDownload()) {
					try {
//						FtpUtils.download(ftpClient, parameter.getContent(), PROGRAM_REPO + "/" + context.getProgramId());
						String localFilePath = FtpUtils.download(ftpClient, parameter.getContent(), PROGRAM_REPO + "/" + context.getTaskId());
						if(!localFilePath.equals(null)){
							parameter.setContent(localFilePath);
						}else{
							LOGGER.error("Failed to download parameter [ " + parameter.getContent() + " ] ");
							throw new PreparationException("Failed to download parameter [ " + parameter.getContent() + " ] " );
						}
					} catch (FtpUtilsException fue) {
						LOGGER.error("Failed to download parameter [ " + parameter.getContent() + " ] : " + fue.getMessage(), fue);
						throw new PreparationException("Failed to download parameter [ " + parameter.getContent() + " ] : " + fue.getMessage(), fue);
					}
				}
			}
		}
		
		try {
			FtpUtils.disconnect(ftpClient);
		} catch (FtpUtilsException fue) {
			LOGGER.error("Failed to disconnect ftp server [ " + ftpHost + " ] : " + fue.getMessage(), fue);
		}
		return context;
	}
	
	public void shutdown() {
		this.stop = true;
		Thread.currentThread().interrupt();
	}
}
