/**
 * 
 */
package com.softsec.tase.node.result;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.dto.app.AppResult;
import com.softsec.tase.common.dto.app.apk.Apk;
import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.exception.ResultException;
import com.softsec.tase.node.util.domain.ApkHandler;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.util.fs.IOUtils;

/**
 * ApkReinforceGenerateCollector
 * <p> </p>
 * @author yanwei
 * @since 2013-8-29 上午10:33:04
 * @version
 */
public class ApkReinforceGenerateCollector extends ResultCollectorService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApkReinforceGenerateCollector.class);

	/* (non-Javadoc)
	 * @see com.softsec.tase.node.result.ResultCollectorService#validate(com.softsec.tase.node.domain.RawResult)
	 */
	@Override
	public int validate(RawResult rawResult) throws ResultException {

		int retCode = -1;
		
		String reinforcedApkPath = null;
	
		try {
			reinforcedApkPath = ((AppResult) IOUtils.getObject(rawResult.getContent().array())).getApkPath();
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to get job reinforce request from [ " + rawResult.getIdentifier() + " ] : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to get job reinforce request from [ " + rawResult.getIdentifier() + " ] : " + ioue.getMessage(), ioue);
		}
		
		Apk apk = null;
		try {
			apk = ApkHandler.getApkFromApkFile(new AppResult(reinforcedApkPath), "reinforced");
		} catch (ParserException pe) {
			LOGGER.error("Failed to parse apk object from file [ " + reinforcedApkPath + " ] : " + pe.getMessage(), pe);
			throw new ResultException("Failed to parse apk object from file [ " + reinforcedApkPath + " ] : " + pe.getMessage(), pe);
		}
		
		// calculate jobReinforceRequest 's checksum
		try {
			rawResult.setContent(ByteBuffer.wrap(IOUtils.getBytes(apk)));
			this.rawResult = rawResult;
			this.contentChecksum = IOUtils.getByteArrayMd5(IOUtils.getBytes(apk));
			retCode = 0;
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to calculate jobReinforceRequest checksum [ " + rawResult.getIdentifier() + " ].");
			throw new ResultException("Failed to calculate jobReinforceRequest checksum [ " + rawResult.getIdentifier() + " ].");
		}
		
		return retCode;
	}

}
