/**
 * 
 */
package com.softsec.tase.node.util.domain;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.common.dto.app.AppResult;
import com.softsec.tase.common.dto.app.apk.Apk;
import com.softsec.tase.node.util.domain.ApkHandler;

/**
 * ApkExtractorTest
 * <p> </p>
 * @author yanwei
 * @since 2013-8-21 上午10:45:46
 * @version
 */
public class ApkHandlerTest extends TestCase {

	@Test
	public void testGetApk() {
		String apkPath = "/Users/yanwei/temp/16c2c73c9bddf1a571c98b5b237427e3.apk";
		Apk apk = null;
		try {
			apk = ApkHandler.getApkFromApkFile(new AppResult(apkPath), "/default");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println(apk);
		}
	}
}
