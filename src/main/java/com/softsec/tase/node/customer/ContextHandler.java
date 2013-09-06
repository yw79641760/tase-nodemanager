/**
 * 
 */
package com.softsec.tase.node.customer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.container.Context;
import com.softsec.tase.common.rpc.domain.job.ContextParameter;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.node.Constants;
import com.softsec.tase.node.domain.ProcessResult;
import com.softsec.tase.node.exception.ExecutionException;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.util.cmd.CmdGenerator;
import com.softsec.tase.node.util.cmd.ProcessFactory;
import com.softsec.tase.node.util.cmd.ProcessHandler;
import com.softsec.tase.store.Configuration;

/**
 * ContextExecutor.java
 * @author yanwei
 * @date 2013-2-7 下午8:00:27
 * @description
 */
public class ContextHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContextHandler.class);
	
	private static final String EXECUTION_TEMP_DIR = Configuration.get(Constants.EXECUTION_TEMP_DIR, "./");
	
	public void launch(Context context) throws ExecutionException {
		
		LOGGER.info("Start executing program " + context.getProgramId() + " : " + context.getProgramName());
		
		// prepare execute commands
		List<String> commandList = null;
		StringBuilder commandBuilder = new StringBuilder();
		// prepare parameter
		if (context.getParameter().getContextParameterList() != null
				&& context.getParameter().getContextParameterList().size() != 0) {
			String[] parameterArray = new String[context.getParameter().getContextParameterList().size() + 1];
			parameterArray[0] = context.getScriptName();
			for (ContextParameter parameter : context.getParameter().getContextParameterList()) {
				parameterArray[parameter.sequenceNum] = 
						(StringUtils.isEmpty(parameter.getOpt()) ? "" : parameter.getOpt())
						+ " " + (StringUtils.isEmpty(parameter.getContent()) ? "" : parameter.getContent());
			}
			for (String parameterContent : parameterArray) {
				commandBuilder.append(parameterContent + " ");
			}
			commandBuilder.append(context.getTaskId());
		} else {
			commandBuilder.append(context.getScriptName());			
		}
		commandList = CmdGenerator.getCommandList(commandBuilder.toString().trim());
		
		//prepare environment variables
		Map<String, String> envMap = null;
		try {
			envMap = CmdGenerator.getEnvMap(context.getEnvVariables(), context.getJobReturnMode());
		} catch (ParserException pe) {
			LOGGER.error("Failed to parse environment variables : " + context.getEnvVariables() + " : " + pe.getMessage(), pe);
			throw new ExecutionException("Failed to parse environment variables : " + context.getEnvVariables() + " : " + pe.getMessage(), pe);
		}

		// run command
		Process process = null;
		try {
//			process = ProcessFactory.getProcess(commandList, envMap, new File(EXECUTION_TEMP_DIR + "/" + context.getProgramId() + "/"));
			process = ProcessFactory.getProcess(commandList, envMap, new File(EXECUTION_TEMP_DIR + "/"));
		} catch (IOException ioe) {
			LOGGER.error("Failed to get process : " + ioe.getMessage(), ioe);
			throw new ExecutionException("Failed to get process : " + ioe.getMessage(), ioe);
		}
		
		// monitor process running status
		ProcessResult result = new ProcessHandler().launchContainer(process, commandList.toString(), null, 0, context.getTimeout(), "utf-8");
		
		LOGGER.info("Process exit with result : " + result.toString());
	}
}
