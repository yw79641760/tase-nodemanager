/**
 * 
 */
package com.softsec.tase.node.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.app.AppWeb;
import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.exception.ResultException;
import com.softsec.tase.node.util.domain.AppHandler;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.util.fs.IOUtils;

/**
 * ApkDefaultInitializeCollector
 * <p> </p>
 * @author yanwei
 * @since 2013-8-8 上午8:48:32
 * @version
 */
public class ApkDefaultInitializeCollector extends ResultCollectorService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApkDefaultInitializeCollector.class);

	/* (non-Javadoc)
	 * @see com.softsec.tase.node.result.ResultCollectorService#validate(com.softsec.tase.node.domain.RawResult)
	 */
	@Override
	public int validate(RawResult rawResult) throws ResultException {
		
		int retCode = -1;
		this.rawResult = rawResult;
		AppWeb appWeb = null;
		try {
			appWeb = (AppWeb) IOUtils.getObject(rawResult.getContent().array());
			appWeb = AppHandler.normalizeAppWeb(appWeb);
			this.contentChecksum = IOUtils.getByteArrayMd5(IOUtils.getBytes(appWeb));
			retCode = 0;
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to validate result [ " + rawResult.getTaskId() + " ] : " + ioue.getMessage(), ioue);
			throw new ResultException("Failed to validate result [ " + rawResult.getTaskId() + " ] : " + ioue.getMessage(), ioue);
		} catch (ParserException pe) {
			LOGGER.error("Failed to normalize app web [ " + appWeb.getAppChecksum() + " ] : " + pe.getMessage(), pe);
			throw new ResultException("Failed to normalize app web [ " + appWeb.getAppChecksum() + " ] : " + pe.getMessage(), pe);
		}
		return retCode;
	}

}
