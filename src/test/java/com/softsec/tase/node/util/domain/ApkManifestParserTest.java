/**
 * 
 */
package com.softsec.tase.node.util.domain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.zip.ZipFile;
import org.junit.Test;

import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.util.domain.ApkManifestParser;
import com.softsec.tase.store.util.fs.ZipUtils;

/**
 * ApkManifestParserTest
 * <p> </p>
 * @author yanwei
 * @since 2013-6-3 下午5:57:29
 * @version
 */
public class ApkManifestParserTest extends TestCase {

	@Test
	public void testApkManifestParser() throws IOException {
		String filePath = "/Users/yanwei/Desktop/6d7339d77bf88af8e329415714a8a651.apk";
		try {
			InputStream inputStream = null;
			ZipFile zipFile = new ZipFile(new File(filePath));
			inputStream = ZipUtils.getInputStreamByEntryName(zipFile, "AndroidManifest.xml");
			System.out.println(ApkManifestParser.getApkManifest(inputStream));
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileUtils.delete(new File(filePath));
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}
}
