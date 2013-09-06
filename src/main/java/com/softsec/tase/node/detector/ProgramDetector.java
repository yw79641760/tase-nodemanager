/**
 * 
 */
package com.softsec.tase.node.detector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.job.JobLifecycle;
import com.softsec.tase.common.rpc.domain.job.JobPhase;
import com.softsec.tase.common.util.domain.ProgramUtils;
import com.softsec.tase.node.Configuration;
import com.softsec.tase.node.Constants;
import com.softsec.tase.store.util.DirectoryUtils;

/**
 * ProgramDetector.java
 * @description
 * @todo
 * @author yanwei
 * @date 2013-4-2 下午9:54:16
 */
public class ProgramDetector {

	private static final String EXECUTION_TEMP_DIR = Configuration.get(Constants.EXECUTION_TEMP_DIR, "../../");
	
	private static final String PREFERRED_PROGRAM_TYPE = Configuration.get(Constants.PROGRAM_PREFERRED_TYPE, null);
	
	/**
	 * get local directory program type set
	 * @return
	 */
	public static List<Integer> getPreferredProgramTypeList() {
		
		List<Integer> preferredProgramTypeList = null;
		
		if (!StringUtils.isEmpty(PREFERRED_PROGRAM_TYPE)) {
			
			preferredProgramTypeList = new ArrayList<Integer>();
			String[] preferredProgramTypeArray = PREFERRED_PROGRAM_TYPE.trim().split(",");
			
			for (String preferredProgramType : preferredProgramTypeArray) {
				String[] programTypeArray = preferredProgramType.trim().split("-");
				AppType appType = AppType.valueOf(programTypeArray[0].toUpperCase());
				JobLifecycle jobLifecycle = JobLifecycle.valueOf(programTypeArray[1].toUpperCase());
				JobPhase jobPhase = JobPhase.valueOf(programTypeArray[2].toUpperCase());
				preferredProgramTypeList.add(ProgramUtils.getProgramType(appType, jobLifecycle, jobPhase));
			}
		}
		
		return preferredProgramTypeList;
	}
	
	/**
	 * get local directory program id list
	 * @return
	 */
	public static List<Long> getLocalProgramIdList() {
		
		List<Long> localProgramIdList = new ArrayList<Long>();
		
		List<String> localDirectoryEntryList = DirectoryUtils.listDirs(new File(EXECUTION_TEMP_DIR));
		for (String dirEntry : localDirectoryEntryList) {
			try {
				localProgramIdList.add(Long.parseLong(dirEntry));
			} catch (NumberFormatException nfe) {
				// ignore program id parse error
			}
		}
		
		return localProgramIdList;
	}
}
