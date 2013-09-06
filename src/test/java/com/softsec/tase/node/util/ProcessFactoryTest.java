/**
 * 
 */
package com.softsec.tase.node.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.node.util.cmd.ProcessFactory;

/**
 * ShellTest.java
 * @author yanwei
 * @date 2013-2-9 上午12:55:53
 * @description
 */
public class ProcessFactoryTest extends TestCase {

	@Test
	public void testRunCommand() {
		List<String> commandList = new ArrayList<String>();
		commandList.add("notepad.exe");
		Process process = null;
		try {
			process = ProcessFactory.getProcess(commandList);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis());
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.println(process.getErrorStream());
		System.out.println(process.getInputStream());
		process.destroy();
		System.out.println(System.currentTimeMillis());
		
	}
}
