package com.softsec.tase.node.util.fs;

/**
 * 2013-04-27
 * @author xuxiaodong
 * the class of get the apk's certs
 */

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

public class CertificateUtils {

	private static final Object mSync = new Object();
	private static WeakReference<byte[]> mReadBuffer;
	
	private static final Logger LOGGER = Logger.getLogger(CertificateUtils.class);
	
	public static Certificate[] getCertificates(String apkName) throws RuntimeException {
		Certificate[] certs = null;
		String mArchiveSourcePath = apkName;

		WeakReference<byte[]> readBufferRef;
		byte[] readBuffer = null;
		
		synchronized (mSync) {
		    readBufferRef = mReadBuffer;
		    if (readBufferRef != null) {
		         mReadBuffer = null;
		         readBuffer = readBufferRef.get();
		    }
		    if (readBuffer == null) {
		         readBuffer = new byte[8192];
		         readBufferRef = new WeakReference<byte[]>(readBuffer);
		    }
		}
		  
		try {
		      JarFile jarFile = new JarFile(mArchiveSourcePath);

		      Enumeration<?> entries = jarFile.entries();
		      while (entries.hasMoreElements()) {
		          JarEntry je = (JarEntry) entries.nextElement();
		          
		          if (je.isDirectory()) {
		             continue;
		          }

		          if (je.getName().startsWith("META-INF/")) {
		             continue;
		          }
		          Certificate[] localCerts = loadCertificates(jarFile, je, readBuffer);
		          
//		          if (true) {
//		              System.out.println("File " + mArchiveSourcePath + " entry " + je.getName()
//		              + ": certs=" + certs + " (" + (certs != null ? certs.length : 0) + ")");
//		          }
		          if (localCerts == null) {
		              LOGGER.error("Package has no certificates at entry "
		              + je.getName() + "; ignoring!");
		              jarFile.close();
		              return null;
		          } else if (certs == null) {
		              certs = localCerts;
		          } else {
		             // Ensure all certificates match.
		             for (int i = 0; i < certs.length; i++) {
		                  boolean found = false;
		                  for (int j = 0; j < localCerts.length; j++) {
		                     if (certs[i] != null && certs[i].equals(localCerts[j])) {
		                        found = true;
		                        break;
		                     }
		                  }
		                 if (!found || certs.length != localCerts.length) {
		                     LOGGER.error("Package has mismatched certificates at entry "
		                     + je.getName() + "; ignoring!");
		                     jarFile.close();
		                     return null; // false
		                  }
		             }
		         }
		      }

		      jarFile.close();

		      synchronized (mSync) {
		          mReadBuffer = readBufferRef;
		      }
		      
		 } catch (IOException e) {
		   LOGGER.error("Exception reading " + mArchiveSourcePath + "\n" , e);
		   return null;
		 } catch (RuntimeException e) {
		   LOGGER.error("Exception reading " + mArchiveSourcePath + "\n" , e);
		   return null;
		 }
		 return certs;
	}
	/**
	private static char[] toChars(byte[] mSignature) {
		byte[] sig = mSignature;
		final int N = sig.length;
		final int N2 = N*2;
		char[] text = new char[N2];

		for (int j=0; j<N; j++) {
		    byte v = sig[j];
		    int d = (v>>4)&0xf;
		    text[j*2] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));
		    d = v&0xf;
		    text[j*2+1] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));
		}

	    return text;
    }*/

	private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) throws RuntimeException {
		  try {
		   // We must read the stream for the JarEntry to retrieve
		   // its certificates.
		       InputStream is = jarFile.getInputStream(je);
		       while (is.read(readBuffer, 0, readBuffer.length) != -1) {
		       // not using
		       }
		       is.close();

		       return (Certificate[]) (je != null ? je.getCertificates() : null);
		   } catch (IOException e) {
		       LOGGER.error("Exception reading " + je.getName() + " in "
		          + jarFile.getName() + ": " , e);
		     }
		  return null;
    }
}
