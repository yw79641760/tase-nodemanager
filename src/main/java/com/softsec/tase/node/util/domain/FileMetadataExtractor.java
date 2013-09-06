/**
 * 
 */
package com.softsec.tase.node.util.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;


import com.softsec.tase.common.dto.app.FileMetadata;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.store.util.fs.FileUtils;

/**
 * FileMetadataExtractor
 * <p> </p>
 * @author yanwei
 * @since 2013-8-19 上午9:18:57
 * @version
 */
public class FileMetadataExtractor {

	public static FileMetadata getFileMetadata(String actualFilePath, String repository) throws ParserException {
		
		File file = new File(actualFilePath);
		if (StringUtils.isEmpty(actualFilePath) || !file.exists() || !file.isFile()) {
			throw new ParserException("No such valid file found : " + actualFilePath);
		}
		String extension = actualFilePath.substring(actualFilePath.lastIndexOf(".") + 1).toLowerCase();
		
		String fileMd5 = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException fnfe) {
			throw new ParserException("Failed to found file [ " + file + " ] : " + fnfe.getMessage(), fnfe);
		}
		try {
			fileMd5 = FileUtils.getFileMd5(fis);
		} catch (NoSuchAlgorithmException nsae) {
			throw new ParserException("No such algorithm found : " + nsae.getMessage(), nsae);
		} catch (IOException ioe) {
			throw new ParserException("Failed to open file [ " + file + " ] : " + ioe.getMessage(), ioe);
		}
		
		FileMetadata fileMetadata = new FileMetadata();
		String fileName = fileMd5 + "." + extension;
		String firstSeparator = fileMd5.substring(0, 2);
		String secondSeparator = fileMd5.substring(2, 4);
		String filePath = "/" + repository + "/" + extension + "/" + firstSeparator + "/" + secondSeparator + "/" + fileName;
		if (!StringUtils.isEmpty(fileMd5)) {
			fileMetadata.setFileName(fileName);
			fileMetadata.setFilePath(filePath);
			fileMetadata.setFileChecksum(fileMd5);
			fileMetadata.setExtension(extension);
			fileMetadata.setLength(file.length());
			fileMetadata.setCreatedTime(file.lastModified());
			fileMetadata.setModifiedTime(file.lastModified());
		} else {
			throw new ParserException("Failed to get app file md5 [ " + actualFilePath + " ] .");
		}
		
		FileUtils.rename(actualFilePath, fileName);
		
		return fileMetadata;
	}
}
