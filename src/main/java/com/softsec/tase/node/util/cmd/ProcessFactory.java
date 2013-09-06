/**
 * 
 */
package com.softsec.tase.node.util.cmd;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ProcessFactory.java
 * @author yanwei
 * @date 2013-2-8 下午2:30:35
 * @description
 */
public class ProcessFactory {
	
	/**
	 * generate process using commands and environment variables
	 *  in specific working directory
	 * @param commandList
	 * @param envMap
	 * @param workingDir
	 * @return
	 * @throws IOException
	 */
	public static Process getProcess(List<String> commandList, Map<String, String> envMap, File workingDir) throws IOException {
		
		ProcessBuilder pbuilder = new ProcessBuilder(commandList);
		
		if (workingDir != null) {
			pbuilder.directory(workingDir);
		}
		
		if (envMap != null) {
			pbuilder.environment().putAll(envMap);
		}
		
		if (!pbuilder.redirectErrorStream()) {
			pbuilder.redirectErrorStream(true);
		}
		
		return pbuilder.start();
	}
	
	/**
	 * generate new process using commands and environment variables
	 * @param commandList
	 * @param envMap
	 * @return
	 * @throws IOException
	 */
	public static Process getProcess(List<String> commandList, Map<String, String> envMap) throws IOException {
		return getProcess(commandList, envMap, null);
	}
	
	/**
	 * generate new process using commands in specific working directory
	 * @param commandList
	 * @param workingDir
	 * @return
	 * @throws IOException
	 */
	public static Process getProcess(List<String> commandList, File workingDir) throws IOException {
		return getProcess(commandList, null, workingDir);
	}
	
	/**
	 * generate new process using commands
	 * @param commandList
	 * @return
	 * @throws IOException
	 */
	public static Process getProcess(List<String> commandList) throws IOException {
		return getProcess(commandList, null, null);
	}
}
