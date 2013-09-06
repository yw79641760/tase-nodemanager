/**
 * 
 */
package com.softsec.tase.node.exception;

/**
 * ExecutionException.java
 * @author yanwei
 * @date 2013-2-7 下午10:53:11
 * @description
 */
public class ExecutionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4261091723820590728L;

	public ExecutionException() {
		super();
	}
	
	public ExecutionException(String msg) {
		super(msg);
	}
	
	public ExecutionException(Throwable cause) {
		super(cause);
	}
	
	public ExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
