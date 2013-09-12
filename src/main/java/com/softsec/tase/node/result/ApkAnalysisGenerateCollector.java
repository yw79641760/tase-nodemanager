/**
 * 
 */
package com.softsec.tase.node.result;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.dto.app.AppResult;
import com.softsec.tase.common.dto.app.apk.Apk;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.exception.ResultException;
import com.softsec.tase.node.util.domain.ApkHandler;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.util.fs.IOUtils;

/**
 * ApkDefaultGenerateCollector
 * <p> </p>
 * @author yanwei
 * @since 2013-8-9 下午4:26:02
 * @version
 */
public class ApkAnalysisGenerateCollector extends ResultCollectorService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApkAnalysisGenerateCollector.class);
	
	/* (non-Javadoc)
	 * @see com.softsec.tase.node.result.ResultCollectorService#validate(com.softsec.tase.node.domain.RawResult)
	 */
	@Override
	public int validate(RawResult rawResult) throws ResultException {
		
		int retCode = -1;
		
		AppResult apkDownload = (AppResult) IOUtils.getObject(rawResult.getContent().array());
		
		// validate AppDownload 's each path field
		String apkPath = apkDownload.getApkPath();
		if (StringUtils.isEmpty(apkPath)) {
			LOGGER.error("Failed to get result apk 's path : " + apkPath);
			throw new ResultException("Failed to get result apk 's path : " + apkPath);
		}
		
		// validate apk
		// extract apk 's signature, manifest, metadata and image metadata
		// upload apk and image to ftp server
		Apk apk = null;
		try {
			apk = ApkHandler.getApkFromApkFile(apkDownload, rawResult.getJobLifecycle().toString().toLowerCase());
		} catch (ParserException pe) {
			LOGGER.error("Failed to parse apk file [ " + apkPath + " ] : " + pe.getMessage(), pe);
			throw new ResultException("Failed to parse apk file [ " + apkPath + " ] : " + pe.getMessage(), pe);
		}
		
		// reset raw result content from Object AppDownload to Object Apk
		try {
			rawResult.setContent(ByteBuffer.wrap(IOUtils.getBytes(apk)));
			this.rawResult = rawResult;
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to wrap apk object into byte buffer [ " + apk + " ] : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to wrap apk object into byte buffer [ " + apk + " ] : " + ioue.getMessage(), ioue);
		}
		try {
			this.contentChecksum = IOUtils.getByteArrayMd5(rawResult.getContent().array());
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to calculate result content 's checksum [ " + rawResult.getTaskId() + " ] : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to calculate result content 's checksum [ " + rawResult.getTaskId() + " ] : " + ioue.getMessage(), ioue);
		}
		retCode = 0;
		
		return retCode;
	}

}
