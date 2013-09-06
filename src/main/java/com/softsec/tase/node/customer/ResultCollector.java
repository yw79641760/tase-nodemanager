/**
 * 
 */
package com.softsec.tase.node.customer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.node.exception.ResultException;

/**
 * ResultCollector.java
 * @author yanwei
 * @date 2013-3-28 下午5:27:54
 * @description
 */
public class ResultCollector {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultCollector.class);
	
	private static final String NAMESPACE = "com.softsec.tase.node.result.";
	
	/**
	 * start specific result service by result type
	 * using java reflection
	 * @param rawResult
	 * @throws ResultException
	 */
	public void collect(RawResult rawResult) throws ResultException {
		
		LOGGER.info("Start collecting result [ " + rawResult.getTaskId() + " ] ...");
		
		String resultCollectorService = getResultCollectorService(rawResult.getAppType(), rawResult.getJobLifecycle(), rawResult.getResultType());
		
		Class<?> resultCollectorClass = null;
		try {
			resultCollectorClass = Class.forName(resultCollectorService);
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error("No such class found : " + resultCollectorService + " : " + cnfe.getMessage(), cnfe);
			throw new ResultException("No such class found : " + resultCollectorService + " : " + cnfe.getMessage(), cnfe);
		}
		
		Object resultCollectorObject = null;
		try {
			resultCollectorObject = resultCollectorClass.newInstance();
		} catch (InstantiationException ie) {
			LOGGER.error("Failed to instantiate class [ " + resultCollectorService + " ] : " + ie.getMessage(), ie);
			throw new ResultException("Failed to instantiate class [ " + resultCollectorService + " ] : " + ie.getMessage(), ie);
		} catch (IllegalAccessException iae) {
			LOGGER.error("Failed to access class [ " + resultCollectorService + " ] : " + iae.getMessage(), iae);
			throw new ResultException("Failed to access class [ " + resultCollectorService + " ] : " + iae.getMessage(), iae);
		}
		
		Method resultCollectorMethod = null;
		try {
			resultCollectorMethod = resultCollectorClass.getDeclaredMethod("validate", new Class<?>[]{RawResult.class});
		} catch (SecurityException se) {
			LOGGER.error("Failed to invoke method [ validate ] : " + se.getMessage(), se);
			throw new ResultException("Failed to invoke method [ validate ] : " + se.getMessage(), se);
		} catch (NoSuchMethodException nsme) {
			LOGGER.error("No such method found [ validate ] : " + nsme.getMessage(), nsme);
			throw new ResultException("No such method found [ validate ] : " + nsme.getMessage(), nsme);
		}
		
		int validateRetCode = -1;
		try {
			validateRetCode = (Integer) resultCollectorMethod.invoke(resultCollectorObject, rawResult);
		} catch (IllegalArgumentException iage) {
			LOGGER.error("Illegal argument used in method invocation : " + iage.getMessage(), iage);
			throw new ResultException("Illegal argument used in method invocation : " + iage.getMessage(), iage);
		} catch (IllegalAccessException iace) {
			LOGGER.error("Failed to access the method : " + iace.getMessage(), iace);
			throw new ResultException("Failed to access the method : " + iace.getMessage(), iace);
		} catch (InvocationTargetException ite) {
			LOGGER.error("Failed to invoke method correctly : " + ite.getMessage(), ite);
			throw new ResultException("Failed to invoke method correctly : " + ite.getMessage(), ite);
		}
		
		if (validateRetCode >= 0) { 
			
			// commit method exists in the father class
			resultCollectorClass = resultCollectorClass.getSuperclass();
			
			// commit method needs no parameter
			Class<?>[] commitParam = {};
			try {
				resultCollectorMethod = resultCollectorClass.getDeclaredMethod("commit", commitParam);
			} catch (SecurityException se) {
				LOGGER.error("Failed to invoke method [ commit ] " + se.getMessage(), se);
				throw new ResultException("Failed to invoke method [ commit ] " + se.getMessage(), se);
			} catch (NoSuchMethodException nsme) {
				LOGGER.error("No such method found [ commit ] " + nsme.getMessage(), nsme);
				throw new ResultException("No such method found [ commit ] " + nsme.getMessage(), nsme);
			}
			
			int commitRetCode = -1;
			try {
				commitRetCode = (Integer) resultCollectorMethod.invoke(resultCollectorObject, (Object[])null);
			} catch (IllegalArgumentException iage) {
				LOGGER.error("illegal argument used in method invocation : " + iage.getMessage(), iage);
				throw new ResultException("illegal argument used in method invocation : " + iage.getMessage(), iage);
			} catch (IllegalAccessException iace) {
				LOGGER.error("Failed to access the method : " + iace.getMessage(), iace);
				throw new ResultException("Failed to access the method : " + iace.getMessage(), iace);
			} catch (InvocationTargetException ite) {
				LOGGER.error("Failed to invoke method correctly : " + ite.getMessage(), ite);
				throw new ResultException("Failed to invoke method correctly : " + ite.getMessage(), ite);
			}
			
			if (commitRetCode < 0) {
				LOGGER.error("Failed to commit result [ " + rawResult.getTaskId() + " ].");
				throw new ResultException("Failed to commit result [ " + rawResult.getTaskId() + " ].");
			} else {
				LOGGER.info("Finished collecting result [ " + rawResult.getTaskId() + " ].");
			}
			
		} else {
			LOGGER.error("Failed to validate result [ " + rawResult.getTaskId() + " ].");
			throw new ResultException("Failed to validate result [ " + rawResult.getTaskId() + " ].");
		}
	}
	
	/**
	 * generate result collector service class name by job type
	 * @param appType
	 * @param jobLifecycle
	 * @param jobPhase
	 * @return
	 * @throws ResultException
	 */
	public static String getResultCollectorService(AppType appType, JobLifecycle jobLifecycle, JobPhase jobPhase) {
		
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append(NAMESPACE);
		
		if (appType != null) {
			String appTypeStr = appType.name().toLowerCase();
			sbuilder.append(appTypeStr.substring(0, 1).toUpperCase());
			sbuilder.append(appTypeStr.substring(1));
		}
		
		if (jobLifecycle != null) {
			String jobLifecycleStr = jobLifecycle.name().toLowerCase();
			sbuilder.append(jobLifecycleStr.substring(0, 1).toUpperCase());
			sbuilder.append(jobLifecycleStr.substring(1));
		}
		
		if (jobPhase != null) {
			String jobPhaseStr = jobPhase.name().toLowerCase();
			sbuilder.append(jobPhaseStr.substring(0, 1).toUpperCase());
			sbuilder.append(jobPhaseStr.substring(1));
		}
		sbuilder.append("Collector");
		
		return sbuilder.toString();
	}
}
