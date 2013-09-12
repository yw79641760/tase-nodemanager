/**
 * 
 */
package com.softsec.tase.node.result;

import java.nio.ByteBuffer;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.dto.app.AppResult;
import com.softsec.tase.common.dto.app.apk.Apk;
import com.softsec.tase.common.rpc.domain.job.JobReinforceRequest;
import com.softsec.tase.node.Constants;
import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.exception.ResultException;
import com.softsec.tase.node.util.domain.ApkHandler;
import com.softsec.tase.store.Configuration;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.util.fs.IOUtils;

/**
 * ApkReinforceInitializeCollector
 * <p> </p>
 * @author yanwei
 * @since 2013-8-22 上午9:03:28
 * @version
 */
public class ApkReinforceInitializeCollector extends ResultCollectorService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApkReinforceInitializeCollector.class);
	
	/* (non-Javadoc)
	 * @see com.softsec.tase.node.result.ResultCollectorService#validate(com.softsec.tase.node.domain.RawResult)
	 */
	@Override
	public int validate(RawResult rawResult) throws ResultException {
		
		int retCode = -1;
		String reinforceRepo = Configuration.get(Constants.FTP_REINFORCE_REPO, "reinforce");
		
		JobReinforceRequest jobReinforceRequest = new JobReinforceRequest();
		TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
		try {
			deserializer.deserialize(jobReinforceRequest, rawResult.getContent().array());
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to get job reinforce request from [ " + rawResult.getIdentifier() + " ] : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to get job reinforce request from [ " + rawResult.getIdentifier() + " ] : " + ioue.getMessage(), ioue);
		} catch (TException te) {
			LOGGER.error("Failed to get job reinforce request from [ " + rawResult.getIdentifier() + " ] : " + te.getMessage(), te);
			throw new ResultException("Failed to get job reinforce request from [ " + rawResult.getIdentifier() + " ] : " + te.getMessage(), te);
		}
		
		Apk apk = null;
		try {
			apk = ApkHandler.getApkFromApkFile(new AppResult(jobReinforceRequest.getAppPath()), reinforceRepo);
		} catch (ParserException pe) {
			LOGGER.error("Failed to parse apk object from file [ " + jobReinforceRequest.getAppPath() + " ] : " + pe.getMessage(), pe);
			throw new ResultException("Failed to parse apk object from file [ " + jobReinforceRequest.getAppPath() + " ] : " + pe.getMessage(), pe);
		}
		
		// fill jobReinforceRequest 's appInfo field
		if (apk != null) {
			jobReinforceRequest.setAppInfo(ByteBuffer.wrap(IOUtils.getBytes(apk)));
		} else{
			LOGGER.error("Failed to get apk info [ " + jobReinforceRequest.getAppPath() + " ].");
			throw new ResultException("Failed to get apk info [ " + jobReinforceRequest.getAppPath() + " ].");
		}

		// calculate jobReinforceRequest 's checksum
		try {
			rawResult.setContent(ByteBuffer.wrap(IOUtils.getBytes(jobReinforceRequest)));
			this.rawResult = rawResult;
			this.contentChecksum = IOUtils.getByteArrayMd5(IOUtils.getBytes(jobReinforceRequest));
			retCode = 0;
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to calculate jobReinforceRequest checksum [ " + rawResult.getIdentifier() + " ].");
			throw new ResultException("Failed to calculate jobReinforceRequest checksum [ " + rawResult.getIdentifier() + " ].");
		}
		
		return retCode;
	}
}
