/**
 * 
 */
package com.softsec.tase.node.result;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.common.dto.app.AppResult;
import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.store.util.fs.IOUtils;

/**
 * ResultCollectorServiceTest
 * <p> </p>
 * @author yanwei
 * @since 2013-8-9 下午4:38:32
 * @version
 */
public class ResultCollectorServiceTest extends TestCase {

	@Test
	public void testCommit() {
		
		String resultCollectorService = "com.softsec.tase.node.result.ApkDefaultGenerateCollector";
		RawResult rawResult = new RawResult();
		rawResult.setAppType(AppType.APK);
		rawResult.setJobLifecycle(JobLifecycle.ANALYSIS);
		rawResult.setResultType(JobPhase.GENERATE);
		AppResult apkDownload = new AppResult();
		apkDownload.setApkPath("/Users/yanwei/temp/b13c976c8f078675bce8fe31e0433f5c.apk");
		rawResult.setContent(ByteBuffer.wrap(IOUtils.getBytes(apkDownload)));
		rawResult.setTaskId(10000587L);
		rawResult.setIdentifier("lalala");
		Class<?> resultCollectorClass = null;
		try {
			resultCollectorClass = Class.forName(resultCollectorService);
		} catch (ClassNotFoundException cnfe) {
		}
		
		Object resultCollectorObject = null;
		try {
			resultCollectorObject = resultCollectorClass.newInstance();
		} catch (InstantiationException ie) {
		} catch (IllegalAccessException iae) {
		}
		
		Method resultCollectorMethod = null;
		try {
			resultCollectorMethod = resultCollectorClass.getDeclaredMethod("validate", new Class<?>[]{RawResult.class});
		} catch (SecurityException se) {
		} catch (NoSuchMethodException nsme) {
		}
		
		int validateRetCode = -1;
		try {
			validateRetCode = (Integer) resultCollectorMethod.invoke(resultCollectorObject, rawResult);
		} catch (IllegalArgumentException iage) {
		} catch (IllegalAccessException iace) {
		} catch (InvocationTargetException ite) {
		}
		
		if (validateRetCode >= 0) {
			// commit method needs no parameter
			Class<?>[] commitParam = {};
			try {
				resultCollectorMethod = resultCollectorClass.getDeclaredMethod("commit", commitParam);
			} catch (SecurityException se) {
				se.printStackTrace();
			} catch (NoSuchMethodException nsme) {
				System.out.println("Get super class ...");
				resultCollectorClass = resultCollectorClass.getSuperclass();
						try {
							resultCollectorMethod = resultCollectorClass.getDeclaredMethod("commit", commitParam);
						} catch (SecurityException ise) {
							ise.printStackTrace();
						} catch (NoSuchMethodException insme) {
							insme.printStackTrace();
						}
			}
			
			int commitRetCode = -1;
			try {
				commitRetCode = (Integer) resultCollectorMethod.invoke(resultCollectorObject, (Object[])null);
			} catch (IllegalArgumentException iage) {
				iage.printStackTrace();
			} catch (IllegalAccessException iace) {
				iace.printStackTrace();
			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
			}
			
			if (commitRetCode < 0) {
				System.out.println("Commit code : " + commitRetCode);
			} else {
				System.out.println("Commit code : " + commitRetCode);
			}
			
		} else {
			System.out.println("Validate code : " + validateRetCode);
		}
	}
}
