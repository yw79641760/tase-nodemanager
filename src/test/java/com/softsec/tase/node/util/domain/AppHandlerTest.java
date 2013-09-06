/**
 * 
 */
package com.softsec.tase.node.util.domain;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.common.rpc.domain.app.AppExternalLink;
import com.softsec.tase.common.rpc.domain.app.AppWeb;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.util.domain.AppHandler;

/**
 * AppHandlerTest
 * <p> </p>
 * @author yanwei
 * @since 2013-8-30 下午2:59:37
 * @version
 */
public class AppHandlerTest extends TestCase {
	
	@Test
	public void testNormalize() {
		AppWeb appWeb = new AppWeb();
		appWeb.setStoreName("\\App 'Store'\\\\");
		appWeb.setUrl("http://as'.baidu.com/download.html");
		
		List<String> snapshotUrlList = new ArrayList<String>();
		snapshotUrlList.add("http://ad.b'aidu.com/\\\\'");
		snapshotUrlList.add("http://cn.bin'g.com/\'\'\'\'");
		appWeb.setSnapshotUrlList(snapshotUrlList);
		
		List<AppExternalLink> appExternalLinkList = new ArrayList<AppExternalLink>();
		AppExternalLink appExternalLink = new AppExternalLink();
		appExternalLink.setExternalStoreName("Test");
		appExternalLink.setExternalUrl("http://ad.oracle.com/\\\\'");
		appExternalLinkList.add(appExternalLink);
		appWeb.setAppExternalLinkList(appExternalLinkList);
		
		try {
			appWeb = AppHandler.normalizeAppWeb(appWeb);
		} catch (ParserException e) {
			e.printStackTrace();
		}
		System.out.println(appWeb);
	}
}
