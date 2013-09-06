/**
 * 
 */
package com.softsec.tase.node.exception;

/**
 * ParserException.java
 * @author yanwei
 * @date 2013-4-1 上午9:16:46
 * @description
 */
public class ParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8232808918022149286L;

	public ParserException() {
		super();
	}
	
	public ParserException(String msg) {
		super(msg);
	}
	
	public ParserException(Throwable cause) {
		super(cause);
	}
	
	public ParserException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
