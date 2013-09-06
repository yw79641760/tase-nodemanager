/**
 * 
 */
package com.softsec.tase.node.domain;

/**
 * ProcessStatus.java
 * @author yanwei
 * @date 2013-3-29 上午11:23:43
 * @description
 */
public enum ProcessStatus {
	
	CONCURRENT_RUNNING(1),
	EXCLUSIVE_WAITING(2),
	EXCLUSIVE_RUNNING(3);
	
	private int status;
	
	ProcessStatus(int status) {
		this.status = status;
	}
	
	public int getValue() {
		return status;
	}
	
	public ProcessStatus findByValue(int status) {
		switch(status) {
		case 1:
			return CONCURRENT_RUNNING;
		case 2:
			return EXCLUSIVE_WAITING;
		case 3:
			return EXCLUSIVE_RUNNING;
		default:
			return null;	
		}
	}
}
