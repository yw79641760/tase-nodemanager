package com.softsec.tase.node.result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobDistributionMode;
import com.softsec.tase.common.rpc.domain.job.JobExecutionMode;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobOperationRequirement;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.rpc.domain.job.JobPriority;
import com.softsec.tase.common.rpc.domain.job.JobReinforceRequest;
import com.softsec.tase.common.rpc.domain.job.JobResourceRequirement;
import com.softsec.tase.common.rpc.domain.job.JobReturnMode;
import com.softsec.tase.common.rpc.service.node.ProgramTrackerService;
import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.node.exception.ResultException;
import com.softsec.tase.store.util.fs.IOUtils;

import junit.framework.TestCase;

public class ApkReinforceInitializeCollectorTest extends TestCase {

	@Test
	public void testValidate() {
		RawResult rawResult = new RawResult();
		rawResult.setAppType(AppType.APK);
		rawResult.setJobLifecycle(JobLifecycle.REINFORCE);
		rawResult.setResultType(JobPhase.INITIALIZE);
		rawResult.setTaskId(1111L);
		rawResult.setIdentifier("Test");
		String sourceApkPath = "/home/ss/V2_SoftSec_Reinforce_generate_auto/build/build-target/releaseProject/Cut_the_rope.apk";
		JobReinforceRequest request = new JobReinforceRequest();
		request.setUserId(100);
		request.setAppType(AppType.APK);
		request.setJobLifecycle(JobLifecycle.REINFORCE);
		List<JobPhase> jobPhaseList = new ArrayList<JobPhase>();
		jobPhaseList.add(JobPhase.INITIALIZE);
		request.setJobPhaseList(jobPhaseList);
		request.setJobPriority(JobPriority.MEDIUM);
		request.setJobDistributionMode(JobDistributionMode.PARALLEL);
		request.setJobOperationRequirementList(new ArrayList<JobOperationRequirement>());
		request.setJobResourceRequirementList(new ArrayList<JobResourceRequirement>());
		request.setAppPath(sourceApkPath);
		request.setImpatienceTime(100000L);
		try {
			rawResult.setContent(ByteBuffer.wrap(new TSerializer((new TBinaryProtocol.Factory())).serialize(request)));
		} catch (TException e1) {
			e1.printStackTrace();
		}
		
		ApkReinforceInitializeCollector collector = new ApkReinforceInitializeCollector();
		try {
			collector.validate(rawResult);
		} catch (ResultException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void reinforceTest() {
		try {
			// ���õ��õķ����ַΪ���أ��˿�Ϊ 7911
			TTransport transport = new TSocket("localhost", 7020);
			transport.open();
			// ���ô���Э��Ϊ TBinaryProtocol
			TProtocol protocol = new TBinaryProtocol(transport);
			ProgramTrackerService.Client client = new ProgramTrackerService.Client(
					protocol);
			
			List<JobPhase> jobPhaseList = new ArrayList<JobPhase>();
			jobPhaseList.add(JobPhase.INITIALIZE);
			
			List<JobOperationRequirement> operationList = new ArrayList<JobOperationRequirement>();
			JobOperationRequirement operation = new JobOperationRequirement();
			operation.setJobLifecycle(JobLifecycle.REINFORCE);
			operation.setJobPhase(JobPhase.INITIALIZE);
			operation.setJobExecutionMode(JobExecutionMode.EXCLUSIVE); // serial execution
			operation.setJobReturnMode(JobReturnMode.ACTIVE);
			operation.setTimeout(30 * 60 * 1000); // 30 minutes
			operationList.add(operation);
			
			JobReinforceRequest reinforceRequest = new JobReinforceRequest();
			reinforceRequest.setAppPath("E:\\��Ŀ��\\softsec-reinforce\\apk\\new (7).apk");
			reinforceRequest.setUserId(123);
			reinforceRequest.setAppType(AppType.APK);
			reinforceRequest.setJobLifecycle(JobLifecycle.REINFORCE);
			reinforceRequest.setJobPhaseList(jobPhaseList);
			reinforceRequest.setJobDistributionMode(JobDistributionMode.SERIAL);
			reinforceRequest.setJobPriority(JobPriority.MEDIUM);			
			reinforceRequest.setJobOperationRequirementList(operationList);
			client.transferResult(AppType.APK, JobLifecycle.REINFORCE,
					JobPhase.INITIALIZE,
					ByteBuffer.wrap(getBytes(reinforceRequest)), 0000, "8888");
			transport.close();
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void fetchAppTest(){
		try {
		TTransport transport = new TSocket("localhost", 7020);
		transport.open();
		// ���ô���Э��Ϊ TBinaryProtocol
		TProtocol protocol = new TBinaryProtocol(transport);
		ProgramTrackerService.Client client = new ProgramTrackerService.Client(
				protocol);
//		client.fetchApp("MD5.apk", "destinationFilePath");
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}

	public static byte[] getBytes(Serializable obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		byte[] bytes = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.flush();
			bytes = baos.toByteArray();
			baos.close();
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytes;
	}

}
