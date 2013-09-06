/**
 * 
 */
package com.softsec.tase.node.util.domain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.tools.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.dto.app.AppResult;
import com.softsec.tase.common.dto.app.FileMetadata;
import com.softsec.tase.common.dto.app.apk.Apk;
import com.softsec.tase.common.dto.app.apk.ApkManifest;
import com.softsec.tase.common.dto.app.apk.ApkSignature;
import com.softsec.tase.common.util.StringUtils;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.exception.ResultException;
import com.softsec.tase.store.exception.FtpUtilsException;
import com.softsec.tase.store.exception.IOUtilsException;
import com.softsec.tase.store.exception.ZipUtilsException;
import com.softsec.tase.store.util.fs.FileUtils;
import com.softsec.tase.store.util.fs.IOUtils;
import com.softsec.tase.store.util.fs.ZipUtils;
import com.softsec.tase.store.util.net.FtpConnFactory;
import com.softsec.tase.store.util.net.FtpUtils;

/**
 * ApkHandler
 * <p> Apk object extractor and apk & image uploader </p>
 * @author yanwei
 * @since 2013-8-21 上午8:39:34
 * @version
 */
public class ApkHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApkHandler.class);
	
	/**
	 * extract apk object from apk and image files
	 * upload validated apk and image files to ftp server
	 * delete all local files
	 * @param apkPath
	 * @param imagePathList
	 * @return
	 */
	public static Apk getApkFromApkFile(AppResult appResult, String repository) throws ParserException, ResultException{
		
		// validate apk
		// extract apk 's signature, manifest, and metadata
		Apk apk = new Apk();
		
		// get apk 's manifest
		try {
			ApkManifest apkManifest = ApkManifestParser.getApkManifest(appResult.getApkPath());
			if (apkManifest != null) {
				apk.setApkManifest(apkManifest);
			} else {
				deleteAllFiles(appResult);
				LOGGER.error("Failed to parse apk manifest [ " + appResult.getApkPath() + " ].");
				throw new ParserException("Faile to parse apk manifest [ " + appResult.getApkPath() + " ].");
			}
		} catch (ParserException pe) {
			deleteAllFiles(appResult);
			LOGGER.error("Failed to parse apk manifest [ " + appResult.getApkPath() + " ] : " + pe.getMessage(), pe);
			throw new ParserException("Faile to parse apk manifest [ " + appResult.getApkPath() + " ] : " + pe.getMessage(), pe);
		}
		
		// get apk 's signature
		try {
			ApkSignature apkSignature = ApkSignatureExtractor.getApkSignature(appResult.getApkPath());
			if (apkSignature != null) {
				apk.setApkSignature(apkSignature);
			} else {
				deleteAllFiles(appResult);
				LOGGER.error("Failed to parse apk 's signature [ " + appResult.getApkPath() + " ].");
				throw new ParserException("Failed to parse apk 's signature [ " + appResult.getApkPath() + " ].");
			}
		} catch (Exception pe) {
			deleteAllFiles(appResult);
			LOGGER.error("Failed to parse apk 's signature [ " + appResult.getApkPath() + " ] : " + pe.getMessage(), pe);
			throw new ParserException("Failed to parse apk 's signature [ " + appResult.getApkPath() + " ] : " + pe.getMessage(), pe);
		}
		
		// get apk classes.dex 's checksum and magic number
		String dexMd5 = null;
		String dexMagicNumber = null;
		InputStream dexInputStream = null;
		ZipFile apkZipFile = null;
		try {
			apkZipFile = new ZipFile(appResult.getApkPath());
		} catch (IOException ioe) {
			LOGGER.error("Failed to get apk zip file [ " + appResult.getApkPath() + " ] : " + ioe.getMessage(), ioe);
			throw new ParserException("Failed to get apk zip file [ " + appResult.getApkPath() + " ] : " + ioe.getMessage(), ioe);
		}
		try {
			dexInputStream = ZipUtils.getInputStreamByEntryName(apkZipFile, "classes.dex");
		} catch (ZipUtilsException zue) {
			LOGGER.error("Failed to get apk zip input stream [ " + appResult.getApkPath() + " ] : " + zue.getMessage(), zue);
			throw new ParserException("Failed to get apk zip input stream [ " + appResult.getApkPath() + " ] : " + zue.getMessage(), zue);
		}
		try {
			dexMd5 = IOUtils.getInputStreamMd5(dexInputStream);
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to get apk input stream md5 [ " + appResult.getApkPath() + " ] : " + ioue.getMessage(), ioue);
			throw new ParserException("Failed to get apk input stream md5 [ " + appResult.getApkPath() + " ] : " + ioue.getMessage(), ioue);
		} finally {
			dexInputStream = null;
		}
		// get classes.dex 's input stream again
		try {
			dexInputStream = ZipUtils.getInputStreamByEntryName(apkZipFile, "classes.dex");
		} catch (ZipUtilsException zue) {
			LOGGER.error("Failed to get apk zip input stream [ " + appResult.getApkPath() + " ] : " + zue.getMessage(), zue);
			throw new ParserException("Failed to get apk zip input stream [ " + appResult.getApkPath() + " ] : " + zue.getMessage(), zue);
		}
		try {
			dexMagicNumber = IOUtils.getMagicNumber("APK", dexInputStream);
		} catch (IOUtilsException ioue) {
			LOGGER.error("Failed to get apk classes.dex magic number [ " + appResult.getApkPath() + " ] : " + ioue.getMessage(), ioue);
			throw new ParserException("Failed to get apk classes.dex magic number [ " + appResult.getApkPath() + " ] : " + ioue.getMessage(), ioue);
		} finally {
			dexInputStream = null;
			try {
				apkZipFile.close();
			} catch (IOException ioe) {
				LOGGER.error("Failed to close apk zip file [ " + appResult.getApkPath() + " ] : " + ioe.getMessage(), ioe);
				throw new ParserException("Failed to close apk zip file [ " + appResult.getApkPath() + " ] : " + ioe.getMessage(), ioe);
			}
		}
		if (!StringUtils.isEmpty(dexMd5) && !StringUtils.isEmpty(dexMagicNumber)) {
			apk.setBundleChecksum(dexMd5);
			apk.setBundleMagicNumber(dexMagicNumber);
		} else {
			deleteAllFiles(appResult);
			LOGGER.error("Failed to get dex md5 [ " + dexMd5 + " ] or dex magic number [ " + dexMagicNumber + " ].");
			throw new ParserException("Failed to get dex md5 [ " + dexMd5 + " ] or dex magic number [ " + dexMagicNumber + " ].");
		}
		
		// get apk 's metadata
		FileMetadata fileMetadata = null;
		try {
			fileMetadata = FileMetadataExtractor.getFileMetadata(appResult.getApkPath(), repository);
			if (fileMetadata != null) {
				apk.setFileMetadata(fileMetadata);
			}
		} catch (ParserException pe) {
			deleteAllFiles(appResult);
			LOGGER.error("Failed to get apk file metadata [ " + appResult.getApkPath() + " ] : " + pe.getMessage(), pe);
			throw new ParserException("Failed to get apk file metadata [ " + appResult.getApkPath() + " ] : " + pe.getMessage(), pe);
		}
		
		uploadFile(appResult, repository, apk);
		
		return apk;
	}

	/**
	 * parse image list
	 * upload apk and image file to ftp server
	 * @param appResult
	 * @param repository
	 * @param apk
	 * @throws ResultException
	 */
	private static void uploadFile(AppResult appResult, String repository, Apk apk) throws ResultException {
		
		String localFilePath = appResult.getApkPath().substring(0, appResult.getApkPath().lastIndexOf("/") + 1) + apk.getFileMetadata().getFileName();
		String remoteFileDir = apk.getFileMetadata().getFilePath().substring(0, apk.getFileMetadata().getFilePath().lastIndexOf("/") + 1);
		
		// upload validated file to ftp server
		String ftpServer = FtpConnFactory.getRandomFtpHost();
		FTPClient ftpClient = null;
		try {
			ftpClient = FtpConnFactory.connect(ftpServer);
		} catch (FtpUtilsException fue) {
			LOGGER.error("Failed to connect ftp server [ " + ftpServer + " ] : " + fue.getMessage(), fue);
			throw new ResultException("Failed to connect ftp server [ " + ftpServer + " ] : " + fue.getMessage(), fue);
		}
		
		if (apk.getFileMetadata() != null
				&& !FtpUtils.isFileExist(ftpClient, apk.getFileMetadata().getFilePath())) {
			try {
				if (!FtpUtils.upload(ftpClient, localFilePath, remoteFileDir)) {
					LOGGER.error("Failed to upload apk [ " + localFilePath + " ] to apk repo.");
					throw new ResultException("Failed to upload apk [ " + localFilePath + " ] to apk repo.");
				}
			} catch (FtpUtilsException fue) {
				LOGGER.error("Failed to upload apk [ " + localFilePath + " ] to apk repo : " + fue.getMessage(), fue);
				throw new ResultException("Failed to upload apk [ " + localFilePath + " ] to apk repo : " + fue.getMessage(), fue);
			}
			// delete the apk file whether upload operation is succeed or not
			FileUtils.deleteFile(new File(localFilePath));
		} else {
			FileUtils.deleteFile(new File(appResult.getApkPath()));
		}
		
		// get image list file metadata
		if (appResult.getImagePathList() != null && appResult.getImagePathList().size() != 0) {
			
			for (String imagePath : appResult.getImagePathList()) {
				FileMetadata imageMetadata = null;
				try {
					imageMetadata = FileMetadataExtractor.getFileMetadata(imagePath, repository);
				} catch (ParserException pe) {
					LOGGER.error("Failed to get image file metadata [ " + imagePath + " ] : " + pe.getMessage(), pe);
					// image parser exception do not interrupt result collection
//				throw new ParserException("Failed to get image file metadata [ " + imagePath + " ] : " + pe.getMessage(), pe);
					if (imageMetadata == null) {
						appResult.getImagePathList().remove(imagePath);
						FileUtils.deleteFile(new File(imagePath));
					}
				}
				
				if (apk.getImageMetadataList() == null) {
					apk.setImageMetadataList(new ArrayList<FileMetadata>());
				}
				
				if (imageMetadata != null) {
					String localImagePath = imagePath.substring(0, imagePath.lastIndexOf("/") + 1) + imageMetadata.getFileName();
					String remoteImageDir = imageMetadata.getFilePath().substring(0, imageMetadata.getFilePath().lastIndexOf("/") + 1);
					apk.getImageMetadataList().add(imageMetadata);
					if (!FtpUtils.isFileExist(ftpClient, imageMetadata.getFilePath())) {
						FtpUtils.upload(ftpClient, localImagePath, remoteImageDir);
					}
					FileUtils.deleteFile(new File(localImagePath));
				} else {
					FileUtils.deleteFile(new File(imagePath));
				}
				
			}
		}
		
		if (!StringUtils.isEmpty(appResult.getLogoPath())) {
			FileMetadata logoMetadata = null;
			try {
				logoMetadata = FileMetadataExtractor.getFileMetadata(appResult.getLogoPath(), repository);
			} catch (ParserException pe) {
				LOGGER.error("Failed to get logo file metadata [ " + appResult.getLogoPath() +  " ] : " + pe.getMessage(), pe);
				// logo parser exception do not interrupt result collection
			}
			if (logoMetadata != null) {
				String localLogoPath = appResult.getLogoPath().substring(0, appResult.getLogoPath().lastIndexOf("/") + 1) + logoMetadata.getFileName();
				String remoteLogoDir = logoMetadata.getFilePath().substring(0, logoMetadata.getFilePath().lastIndexOf("/") + 1);
				
				if (!FtpUtils.isFileExist(ftpClient, logoMetadata.getFilePath())) {
					FtpUtils.upload(ftpClient, localLogoPath, remoteLogoDir);
				}
				
				FileUtils.deleteFile(new File(localLogoPath));
			} else {
				FileUtils.deleteFile(new File(appResult.getLogoPath()));
			}
		}
		
		// disconnect from ftp server
		try {
			FtpUtils.disconnect(ftpClient);
		} catch (FtpUtilsException fue) {
			LOGGER.error("Failed to disconnect ftp server [ " + ftpServer + " ] : " + fue.getMessage(), fue);
		}
	}
	
	/**
	 * delete all local files
	 * @param appDownload
	 */
	private static void deleteAllFiles(AppResult appDownload) {
		if (!StringUtils.isEmpty(appDownload.getApkPath())) {
			File apkFile = new File(appDownload.getApkPath());
			if (apkFile.exists()) {
				FileUtils.deleteFile(apkFile);
			}
		}
		if (appDownload.getImagePathList() != null && appDownload.getImagePathList().size() != 0) {
			for (String imagePath : appDownload.getImagePathList()) {
				File imageFile = new File(imagePath);
				if (imageFile.exists()) {
					FileUtils.deleteFile(imageFile);
				}
			}
		}
		if (!StringUtils.isEmpty(appDownload.getLogoPath())) {
			File logoFile = new File(appDownload.getLogoPath());
			if (logoFile.exists()) {
				FileUtils.deleteFile(logoFile);
			}
		}
	}
}
