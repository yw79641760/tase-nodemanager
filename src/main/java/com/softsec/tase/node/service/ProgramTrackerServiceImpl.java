/**
 * 
 */
package com.softsec.tase.node.service;

import java.io.File;
import java.nio.ByteBuffer;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.exception.InvalidRequestException;
import com.softsec.tase.common.rpc.exception.TimeoutException;
import com.softsec.tase.common.rpc.exception.UnavailableException;
import com.softsec.tase.common.rpc.service.node.ProgramTrackerService;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.common.util.domain.AppUtils;
import com.softsec.tase.common.util.domain.JobUtils;
import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.exception.PreparationException;
import com.softsec.tase.node.queue.RawResultQueue;
import com.softsec.tase.store.exception.FtpUtilsException;
import com.softsec.tase.store.util.net.FtpConnFactory;
import com.softsec.tase.store.util.net.FtpUtils;

/**
 * ProgramTrackerServiceImpl.java
 * @author yanwei
 * @date 2013-2-7 涓��2:05:21
 * @description
 */
public class ProgramTrackerServiceImpl implements ProgramTrackerService.Iface {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramTrackerServiceImpl.class);

	/**
	 * program 's result listener
	 */
	/* (non-Javadoc)
	 * @see com.softsec.tase.common.rpc.service.node.ProgramTrackerService.Iface#transferResult(com.softsec.tase.common.rpc.domain.app.AppType, com.softsec.tase.common.rpc.domain.job.JobLifecycle, com.softsec.tase.common.rpc.domain.job.JobPhase, java.nio.ByteBuffer, long, java.lang.String)
	 */
	@Override
	public int transferResult(AppType appType, JobLifecycle jobLifecycle,
			JobPhase resultType, ByteBuffer content, long taskId,
			String identifier) throws InvalidRequestException,
			UnavailableException, TimeoutException, TException {
		int retCode = -1;
		if (!AppUtils.isAppTypeMember(appType)
				|| !JobUtils.isJobLifecycleMember(jobLifecycle)
				|| !JobUtils.isJobPhaseMember(resultType)) {
			retCode = -1;
			throw new InvalidRequestException("Invalid appType, jobLifecycle or resultType : " + appType + " : " + jobLifecycle + " : " + resultType);
		} else if (content == null){
			retCode = -1;
			throw new InvalidRequestException("Result content must not be null : " + content);
		} else {
			RawResult rawResult = new RawResult();
			rawResult.setTaskId(taskId);
			rawResult.setAppType(appType);
			rawResult.setJobLifecycle(jobLifecycle);
			rawResult.setResultType(resultType);
			rawResult.setContent(content);
			rawResult.setIdentifier(identifier);
			
			RawResultQueue.getInstance().addToRawResultQueue(rawResult);
			LOGGER.info("Received result [ " + taskId + " ], adding it to result queue ...");
			retCode = 0;
		}
		return retCode;
	}

	/* (non-Javadoc)
	 * @see com.softsec.tase.common.rpc.service.node.ProgramTrackerService.Iface#fetchApp(java.lang.String, java.lang.String)
	 */
	@Override
	public int fetchApp(String repository, String sourceFileName, String destinationFilePath)
			throws InvalidRequestException, UnavailableException,
			TimeoutException, TException {
		
		int retValue = -1;
		if (StringUtils.isEmpty(sourceFileName) || StringUtils.isEmpty(destinationFilePath) || sourceFileName.length() < 4) {
			LOGGER.error("Source file name or path [ " + sourceFileName + " ] or destination file path [ " + destinationFilePath + " ] is invalid.");
			throw new InvalidRequestException("Source file name or path [ " + sourceFileName + " ] or destination file path [ " + destinationFilePath + " ] is invalid.");
		}
		
		//get remote absolute file path
		int lastSlashIndex = sourceFileName.lastIndexOf("/");
		sourceFileName = (lastSlashIndex == -1 ? sourceFileName.substring(lastSlashIndex) : sourceFileName.substring(lastSlashIndex + 1));
		String extension = sourceFileName.substring(sourceFileName.lastIndexOf(".") + 1).toLowerCase();
		String firstSeparator = sourceFileName.substring(0, 2);
		String secondSeparator = sourceFileName.substring(2, 4);
		String remoteFilePath = "/" + repository + "/" + extension + "/" + firstSeparator + "/" + secondSeparator + "/" + sourceFileName;
		
		String ftpServer = FtpConnFactory.getRandomFtpHost();
		FTPClient ftpClient = null;
		try{
			ftpClient = FtpConnFactory.connect(ftpServer);
		}catch (FtpUtilsException fue) {
			LOGGER.error("Failed to connect ftp server [ " + ftpServer + " ] : " + fue.getMessage(), fue);
			throw new UnavailableException("Failed to connect ftp server [ " + ftpServer + " ] : " + fue.getMessage());
		}
		try {
			FtpUtils.download(ftpClient, remoteFilePath, destinationFilePath);
		} catch (FtpUtilsException fue) {
			LOGGER.error("Failed to download parameter [ " + remoteFilePath + " ] : " + fue.getMessage(), fue);
			throw new UnavailableException("Failed to download parameter [ " + remoteFilePath + " ] : " + fue.getMessage());
		}
		try {
			FtpUtils.disconnect(ftpClient);
		} catch (FtpUtilsException fue) {
			LOGGER.error("Failed to close ftp server [ " + ftpServer + " ] : " + fue.getMessage(), fue);
		}
		retValue = 0;
		return retValue;
	}

}
