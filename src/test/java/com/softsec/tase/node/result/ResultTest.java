/**
 * 
 */
package com.softsec.tase.node.result;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.common.rpc.domain.app.AppType;
import com.softsec.tase.common.rpc.domain.app.AppWeb;
import com.softsec.tase.common.rpc.domain.app.OriginType;
import com.softsec.tase.node.domain.RawResult;
import com.softsec.tase.store.util.fs.IOUtils;

/**
 * ResultTest
 * <p> </p>
 * @author yanwei
 * @since 2013-8-9 下午6:16:41
 * @version
 */
public class ResultTest extends TestCase{

	@Test
	public void testStringResult() {
		String home = "/Users/yanwei";
		RawResult rawResult = new RawResult();
		rawResult.setContent(ByteBuffer.wrap(IOUtils.getBytes(new String(home))));
		System.out.println((String)IOUtils.getObject(rawResult.getContent().array()));
		System.out.println(IOUtils.getByteArrayMd5(rawResult.getContent().array()));
		System.out.println(rawResult.getContent().array().length);
		for (byte b : rawResult.getContent().array()) {
			System.out.print(b + " ");
		}
		System.out.println();
		System.out.println(IOUtils.getByteArrayMd5(new String(home).getBytes()));
		System.out.println(home.getBytes().length);
		for (byte c : home.getBytes()) {
			System.out.print(c + " ");
		}
		System.out.println();
		System.out.println((String)IOUtils.getObject(rawResult.getContent().array()));
		System.out.println(((String)IOUtils.getObject(rawResult.getContent().array())).getBytes().length);
		System.out.println(IOUtils.getByteArrayMd5(((String)IOUtils.getObject(rawResult.getContent().array())).getBytes()));
	}
	
	@Test
	public void testAppWebResult() {
		RawResult rawResult = new RawResult();
		AppWeb appWeb = new AppWeb();
		appWeb.setAppType(AppType.APK);
		appWeb.setStoreName("Test");
		appWeb.setOriginType(OriginType.OFFICIAL_STORE);
		appWeb.setAppName("Test");
		appWeb.setCategory("Test");
		appWeb.setAppVersion("1.0.0.1");
		appWeb.setUpdatedTime(System.currentTimeMillis());
		appWeb.setCollectedTime(System.currentTimeMillis());
		appWeb.setUrl("http://test.com");
		appWeb.setDownloadUrl("http://test.com/test.apk");
		appWeb.setAppChecksum("0123");
		// reset raw result content
		rawResult.setContent(ByteBuffer.wrap(IOUtils.getBytes(appWeb)));
		System.out.println((AppWeb)IOUtils.getObject(rawResult.getContent().array()));
		// calculate raw result content checksum
		System.out.println(IOUtils.getByteArrayMd5(rawResult.getContent().array()));
		System.out.println(rawResult.getContent().array().length);
		System.out.println(IOUtils.getBytes((AppWeb)IOUtils.getObject(rawResult.getContent().array())).length);
		// check raw result content checksum
		System.out.println(IOUtils.getByteArrayMd5(IOUtils.getBytes((AppWeb)IOUtils.getObject(rawResult.getContent().array()))));
		// get object from raw result content
		appWeb = (AppWeb) IOUtils.getObject(rawResult.getContent().array());
		System.out.println(IOUtils.getByteArrayMd5(IOUtils.getBytes(appWeb)));
	}
}
