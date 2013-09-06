/**
 * 
 */
package com.softsec.tase.node.customer;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.common.rpc.domain.container.Context;
import com.softsec.tase.common.rpc.domain.job.ContextParameter;
import com.softsec.tase.common.rpc.domain.job.JobParameter;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobReturnMode;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.node.exception.ExecutionException;

/**
 * ContextHandlerTest
 * <p> </p>
 * @author yanwei
 * @since 2013-8-24 下午1:34:31
 * @version
 */
public class ContextHandlerTest extends TestCase {
	
	@Test
	public void testReinforceInvoker() {
		String sourceApkPath = "/home/ss/V2_SoftSec_Reinforce_generate_auto/build/build-target/releaseProject/Cut_the_rope.apk";
		Context context = new Context();
		context.setTimeout(600);
		context.setTaskId(100000L);
		context.setEnvVariables(";");
		context.setJobReturnMode(JobReturnMode.PASSIVE);
		context.setScriptName("/home/ss/V2_SoftSec_Reinforce_generate_auto/build/build-target/releaseProject/bin/start.sh");
		JobParameter jobParameter = new JobParameter();
		jobParameter.setJobPhase(JobPhase.GENERATE);
		List<ContextParameter> contextParameterList = new ArrayList<ContextParameter>();
//		ContextParameter parameter0 = new ContextParameter();
//		parameter0.setSequenceNum(0);	
////		parameter1.setOption(option)
//		parameter0.setContent("/home/ss/V2_SoftSec_Reinforce_generate_auto/build/build-target/releaseProject/bin/start.sh");
//		parameter0.setNeedDownload(false);
		ContextParameter parameter1 = new ContextParameter();
		parameter1.setSequenceNum(1);	
//		parameter1.setOption(option)
		parameter1.setContent(sourceApkPath);
		parameter1.setNeedDownload(false);
		ContextParameter parameter2 = new ContextParameter();
		parameter2.setSequenceNum(2);
		parameter2.setContent("/home/ss/V2_SoftSec_Reinforce_generate_auto/build/build-target/releaseProject/target.apk");
		parameter2.setNeedDownload(false);
		contextParameterList.add(parameter1);
		contextParameterList.add(parameter2);
		jobParameter.setContextParameterList(contextParameterList);
		context.setParameter(jobParameter);
		
		ContextHandler handler = new ContextHandler();
		try {
			handler.launch(context);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testParameterArray() {
		String[] parameterArray = new String[1];
		ContextParameter parameter = new ContextParameter();
		parameter.setSequenceNum(0);
		parameter.setOpt("-f");
		parameter.setContent("test.sh");
		parameter.setNeedDownload(true);
		parameterArray[parameter.getSequenceNum()] = 
				(StringUtils.isEmpty(parameter.getOpt()) ? "" : parameter.getOpt())
						+ " " + (StringUtils.isEmpty(parameter.getContent()) ? "" : parameter.getContent());
		System.out.println(parameterArray[parameter.getSequenceNum()]);
	}
}
