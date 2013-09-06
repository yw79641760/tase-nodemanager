/**
 * 
 */
package com.softsec.tase.node.domain;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * ProcessResult.java
 * @author yanwei
 * @date 2013-4-1 下午5:47:15
 * @description
 */
public class ProcessResult {
	
	public static final int EXIT_CODE_BIT = 0;
	
	public static final int INTERRUPT_CODE_BIT = 1;
	
	public static final int TIMEOUT_CODE_BIT = 2;
	
	// process regular info

	private String command;
	
	private String input;
	
	private int successCode;
	
	private long timeout;

	// process error info

	/**
	 * exception notifier <br />
	 * | EXIT_CODE | INTERRUPT_CODE | TIMEOUT_CODE |
	 */
	private BitSet processFlagSet;
	
	private String output;
	
	private String error;
	
	private int retCode;
	
	private List<Throwable> throwableList;
	
	public ProcessResult(String command, String input, int successCode, long timeout) {
		this.command = command;
		this.input = input;
		this.successCode = successCode;
		this.timeout = timeout;
		this.processFlagSet = new BitSet(3);
		this.throwableList = new ArrayList<Throwable>();
	}
	
	public synchronized void setProcessFlagCode(int index) {
		processFlagSet.set(index);
	}
	
	public synchronized boolean getProcessFlagCode(int index) {
		return processFlagSet.get(index);
	}
	
	public synchronized void addThrowable(Throwable cause) {
		throwableList.add(cause);
	}

	@Override
	public String toString() {
		return "ProcessResult [command=" + command + ", input=" + input
				+ ", successCode=" + successCode + ", timeout=" + timeout
				+ ", processFlagSet=" + processFlagSet + ", output=" + output
				+ ", error=" + error + ", retCode=" + retCode + ", throwable="
				+ throwableList + "]";
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public int getSuccessCode() {
		return successCode;
	}

	public void setSuccessCode(int successCode) {
		this.successCode = successCode;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public BitSet getProcessFlagSet() {
		return processFlagSet;
	}

	public void setProcessFlagSet(BitSet processFlagSet) {
		this.processFlagSet = processFlagSet;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public int getRetCode() {
		return retCode;
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

	public List<Throwable> getThrowableList() {
		return throwableList;
	}

	public void setThrowableList(List<Throwable> throwableList) {
		this.throwableList = throwableList;
	}
}
