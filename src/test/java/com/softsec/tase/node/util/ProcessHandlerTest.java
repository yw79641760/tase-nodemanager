/**
 * 
 */
package com.softsec.tase.node.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.node.domain.ProcessResult;
import com.softsec.tase.node.util.cmd.ProcessFactory;
import com.softsec.tase.node.util.cmd.ProcessHandler;

/**
 * ProcessHandlerTest.java
 * @author yanwei
 * @date 2013-3-29 下午7:13:16
 * @description
 */
public class ProcessHandlerTest extends TestCase {

	@Test
	public void testProcessHandler() {
		List<String> commandList = new ArrayList<String>();
		commandList.add("nodepad.exe");
		Process process = null;
		try {
			process = ProcessFactory.getProcess(commandList);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ProcessHandler processHandler = new ProcessHandler();
		ProcessResult result = processHandler.launchContainer(process, "notepad", null, 5000);
		System.out.println(result.toString());
	}
}
