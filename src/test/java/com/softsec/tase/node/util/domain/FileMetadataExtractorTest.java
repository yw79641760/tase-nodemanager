/**
 * 
 */
package com.softsec.tase.node.util.domain;

import junit.framework.TestCase;

import org.junit.Test;

import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.util.domain.FileMetadataExtractor;

/**
 * FileMetadataExtractor
 * <p> </p>
 * @author yanwei
 * @since 2013-8-20 下午12:24:06
 * @version
 */
public class FileMetadataExtractorTest extends TestCase {

	@Test
	public void testGetFileMetadata() {
		String filePath = "/Users/yanwei/Downloads/2.4.release.zip";
		try {
			System.out.println(FileMetadataExtractor.getFileMetadata(filePath, "default"));
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}
}
