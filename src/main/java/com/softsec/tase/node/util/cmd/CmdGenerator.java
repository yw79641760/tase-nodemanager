/**
 * 
 */
package com.softsec.tase.node.util.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.softsec.tase.common.rpc.domain.job.JobReturnMode;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.node.Configuration;
import com.softsec.tase.node.Constants;
import com.softsec.tase.node.exception.ParserException;

/**
 * CmdGenerator.java
 * @author yanwei
 * @date 2013-4-2 上午9:25:01
 * @description
 */
public class CmdGenerator {
	
	private static final int PROGRAM_TRACKER_SERVICE_PORT = Configuration.getInt(Constants.PROGRAM_TRACKER_SERVICE_PORT, 7030);

	/**
	 * generate command list
	 * @param command
	 * @return
	 */
	public static List<String> getCommandList(String command) {
		
		List<String> commandList = new ArrayList<String>();
		
		String os = System.getProperty("os.name");
		if(os.startsWith("Windows")) {
			commandList.add("cmd.exe");
			commandList.add("/C");
		} else {
			commandList.add("/bin/sh");
			commandList.add("-c");
		}

		commandList.add(command);
		
		return commandList;
	}
	
	/**
	 * generate environment variable map
	 * and result listener port
	 * @param envVariables
	 * @return
	 * @throws ParserException
	 */
	public static Map<String, String> getEnvMap(String envVariables, JobReturnMode jobReturnMode) throws ParserException {
		
		Map<String, String> envMap = new HashMap<String, String>();
		if(!StringUtils.isEmpty(envVariables) 
				&& envVariables.contains(";")){
			String[] envVariableArray = envVariables.split(";");
			if (envVariableArray.length != 0) {
				for(String envVariable : envVariableArray) {
					String[] envArray = envVariable.split("=");
					if (envArray.length != 0 && envArray.length % 2 == 0) {
						envMap.put(envArray[0], envArray[1]);
					} else {
						throw new ParserException("Failed to parse environment variables : " + envArray);
					}
				}
			}
		}
		
		if(jobReturnMode == null || jobReturnMode.equals(JobReturnMode.PASSIVE)) {
			envMap.put("PROGRAM_TRACKER_SERVICE_PORT", String.valueOf(PROGRAM_TRACKER_SERVICE_PORT));
		}
		
		return envMap;
	}
}
