package com.softsec.tase.node.util.domain;

/**
 * apk signature result
 * @author ������
 * @since 2013-04-27 11:38:00
 */
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.softsec.tase.common.dto.app.apk.ApkSignature;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.node.util.fs.CertificateUtils;

public class ApkSignatureExtractor {

	public static ApkSignature getApkSignature(String apkPath) throws ParserException {
		
		String certType=null;
		String publicKeyString=null;
		String publicKeyModulus=null;
		String publicExponent=null;
		String algorithm=null;
		int hashCode=0;
		@SuppressWarnings("unused")
		long publicKeyExponent=0;
		PublicKey publicKey=null;
		Certificate[] certificates=null;
		
		ApkSignature apkSignature = null;
		try {
			certificates = CertificateUtils.getCertificates(apkPath);
		} catch (Exception e) {
			throw new ParserException("Failed to extract apk signatrue [ " + apkPath + " ] : " + e.getMessage(), e);
		}
		
		if (certificates!=null && certificates.length>0) {
			apkSignature = new ApkSignature();
			for(int i=0;i<certificates.length;i++){
				
				certType = certificates[i].getType();
				if (certType!=null && !certType.isEmpty()) {
					apkSignature.setCertificateType(certType);
				}
				
				publicKey = certificates[i].getPublicKey();
				publicKeyString=publicKey.toString();
				if (publicKeyString!=null && !publicKeyString.isEmpty()) {
					Matcher modulusMatcher= Pattern.compile("modulus:\\s+(\\d+)\\s+public").matcher(publicKeyString);
					while(modulusMatcher.find()){
						publicKeyModulus=modulusMatcher.group(1);
					}
				    if(publicKeyModulus!=null && !publicKeyModulus.isEmpty()){
				    	apkSignature.setPublicKeyModulus(publicKeyModulus);
				    }
					
					Matcher exponentMatcher = Pattern.compile("exponent:\\s+(\\d+)").matcher(publicKeyString);
					while(exponentMatcher.find()){
						publicExponent=exponentMatcher.group(1);
					}
	                if (publicExponent!=null && !publicExponent.isEmpty()) {
//	                	publicKeyExponent=Long.parseLong(publicExponent);
	                	apkSignature.setPublicKeyExponent(publicExponent);
					}
				}
				
				algorithm = publicKey.getAlgorithm();
				if(algorithm!=null && ! algorithm.isEmpty()){
					apkSignature.setAlgorithm(algorithm);
				}
				
				hashCode = certificates[i].hashCode();
				apkSignature.setCertificateHashCode(hashCode);
			}	
		}
		return apkSignature;
	}

}
