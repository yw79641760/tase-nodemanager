/**
 * 
 */
package com.softsec.tase.node.domain;

import java.nio.ByteBuffer;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobPhase;

/**
 * RawResult.java
 * @author yanwei
 * @date 2013-3-28 下午4:59:46
 * @description
 */
public class RawResult {

	private long taskId;
	
	private AppType appType;
	
	private JobLifecycle jobLifecycle;
	
	private JobPhase resultType;
	
	private ByteBuffer content;
	
	private String identifier;

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public AppType getAppType() {
		return appType;
	}

	public void setAppType(AppType appType) {
		this.appType = appType;
	}

	public JobLifecycle getJobLifecycle() {
		return jobLifecycle;
	}

	public void setJobLifecycle(JobLifecycle jobLifecycle) {
		this.jobLifecycle = jobLifecycle;
	}

	public JobPhase getResultType() {
		return resultType;
	}

	public void setResultType(JobPhase resultType) {
		this.resultType = resultType;
	}

	public ByteBuffer getContent() {
		return content;
	}

	public void setContent(ByteBuffer content) {
		this.content = content;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RawResult [taskId=" + taskId + ", appType=" + appType
				+ ", jobLifecycle=" + jobLifecycle + ", resultType="
				+ resultType + ", content=" + content + ", identifier="
				+ identifier + "]";
	}
	
}
