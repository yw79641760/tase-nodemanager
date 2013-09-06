/**
 * 
 */
package com.softsec.tase.node.exception;

/**
 * PreparationException.java
 * @author yanwei
 * @date 2013-3-28 上午8:35:24
 * @description
 */
public class PreparationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1359510624246232506L;

	public PreparationException() {
		super();
	}
	
	public PreparationException(String msg) {
		super(msg);
	}
	
	public PreparationException(Throwable cause) {
		super(cause);
	}
	
	public PreparationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
