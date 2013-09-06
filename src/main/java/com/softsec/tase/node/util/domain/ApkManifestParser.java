/**
 * 
 */
package com.softsec.tase.node.util.domain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.tools.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.TypedValue;
import brut.androlib.res.decoder.AXmlResourceParser;

import com.softsec.tase.common.dto.app.apk.ApkActivity;
import com.softsec.tase.common.dto.app.apk.ApkIntentFilterAction;
import com.softsec.tase.common.dto.app.apk.ApkManifest;
import com.softsec.tase.common.dto.app.apk.ApkPermission;
import com.softsec.tase.common.dto.app.apk.ApkUsesFeature;
import com.softsec.tase.common.dto.app.apk.ApkUsesLibrary;
import com.softsec.tase.common.dto.app.apk.ApkUsesPermission;
import com.softsec.tase.common.dto.app.apk.ApkUsesSdk;
import com.softsec.tase.node.exception.ParserException;
import com.softsec.tase.store.exception.ZipUtilsException;
import com.softsec.tase.store.util.fs.ZipUtils;

/**
 * ApkManifestExtractor
 * <p> </p>
 * @author yanwei
 * @since 2013-5-7 上午8:47:44
 * @version
 */
public class ApkManifestParser {
	
    /**
     * get the apk Manifest Info
     * @author xuxiaodong
     * @param filePath
     * @return apkManifest
     */
    public static ApkManifest getApkManifest(String apkfilePath) throws ParserException{
    	InputStream androidManifestInputStream = null;
    	
    	File file = new File(apkfilePath);
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
		} catch (IOException ioe) {
			throw new ZipUtilsException("Failed to open zip file : " + file.getName() + " : "+ ioe.getMessage(), ioe);
		}

    	try {
    		androidManifestInputStream = ZipUtils.getInputStreamByEntryName(zipFile, "AndroidManifest.xml");
			return getApkManifest(androidManifestInputStream);
		} catch (Exception e) {
			throw new ParserException("Failed to parse apk manifest : " + e.getMessage(), e);
		} finally {
			if (androidManifestInputStream!=null) {
				try {
					androidManifestInputStream.close();
				} catch (IOException ioe) {
					throw new ParserException("Failed to close apk manifest stream : " + ioe.getMessage(), ioe);
				}
			}
			try {
				zipFile.close();
			} catch (IOException ioe) {
				throw new ParserException("Failed to close apk zip file : " + ioe.getMessage(), ioe);
			}
		}
    }
	
	public static ApkManifest getApkManifest(InputStream inputStream) throws ParserException {
		
		ApkManifest apkManifest = null;
		AXmlResourceParser parser = new AXmlResourceParser();
		parser.open(inputStream);
		apkManifest = new ApkManifest();
		
		while(true) {
			
			int type = -1;
			try {
				type = parser.next();
			} catch (XmlPullParserException xppe) {
				throw new ParserException("Failed to parse android manifest : " + xppe.getMessage(), xppe);
			} catch (IOException ioe) {
				throw new ParserException("Failed to open stream : " + ioe.getMessage(), ioe);
			}
			
			if (type == XmlPullParser.END_DOCUMENT) {
				break;
			} else if (type == XmlPullParser.START_TAG){
				
				int namespaceCountBefore = 0;
				try {
					namespaceCountBefore = parser.getNamespaceCount(parser.getDepth() - 1);
				} catch (XmlPullParserException xppe) {
					throw new ParserException("Failed to parse android manifest : " + xppe.getMessage(), xppe);
				}
				
				int namespaceCount = 0;
				try {
					namespaceCount = parser.getNamespaceCount(parser.getDepth());
				} catch (XmlPullParserException xppe) {
					throw new ParserException("Failed to parse android manifest : " + xppe.getMessage(), xppe);
				}
				
				for (int i = namespaceCountBefore; i < namespaceCount; i++) {
					try {
						if (parser.getNamespacePrefix(i).equals("android")) {
							try {
								apkManifest.setXmlns(parser.getNamespaceUri(i));
							} catch (XmlPullParserException xppe) {
								throw new ParserException("Failed to parse android manifest : " + xppe.getMessage(), xppe);
							}
						}
					} catch (XmlPullParserException xppe) {
						throw new ParserException("Failed to parse android manifest : " + xppe.getMessage(), xppe);
					}
				}
				
				for (int j = 0; j < parser.getAttributeCount(); j++) {
					
					// parse manifest tag
					if (parser.getName().equals("manifest")) {
						if (parser.getAttributeName(j).equals("package")) {
							apkManifest.setPackageName(getAttributeValue(parser, j));
						} else if (parser.getAttributeName(j).equals("sharedUserId")) {
							apkManifest.setSharedUserId(getAttributeValue(parser, j));
						} else if (parser.getAttributeName(j).equals("sharedUserLabel")) {
							apkManifest.setSharedUserLabel(getAttributeValue(parser, j));
						} else if (parser.getAttributeName(j).equals("versionCode")) {
							apkManifest.setVersionCode(Long.parseLong(getAttributeValue(parser, j).startsWith("0x") ? "0L" : getAttributeValue(parser, j)));
						} else if (parser.getAttributeName(j).equals("versionName")){
							apkManifest.setVersionName(getAttributeValue(parser, j));
						} else if (parser.getAttributeName(j).equals("installLocation")) {
							apkManifest.setInstallLocation(getAttributeValue(parser, j));
						}
					}
					
					// parse uses-permission tag
					if (parser.getName().equals("uses-permission")) {
						if (apkManifest.getApkUsesPermissionList() == null) {
							apkManifest.setApkUsesPermissionList(new ArrayList<ApkUsesPermission>());
						}
						if (parser.getAttributeName(j).equals("name")) {
							apkManifest.getApkUsesPermissionList().add(new ApkUsesPermission(getAttributeValue(parser, j)));
						}
					}
					
					// parse permission tag
					if (parser.getName().equals("permission")) {
						if (apkManifest.getApkPermissionList() == null) {
							apkManifest.setApkPermissionList(new ArrayList<ApkPermission>());
						}
						if (parser.getAttributeName(j).equals("name")
								&& (j + 1 < parser.getAttributeCount())
								&& parser.getAttributeName(j + 1).equals("protectionLevel")) {
							apkManifest.getApkPermissionList().add(new ApkPermission(getAttributeValue(parser, j), getAttributeValue(parser, j + 1)));
						} else if (parser.getAttributeName(j).equals("name")) {
							apkManifest.getApkPermissionList().add(new ApkPermission(getAttributeValue(parser, j)));
						}
					}
					
					// parse uses-sdk tag
					if (parser.getName().equals("uses-sdk")) {
						if (apkManifest.getApkUsesSdk() == null) {
							apkManifest.setApkUsesSdk(new ApkUsesSdk());
						}
						if (parser.getAttributeName(j).equals("minSdkVersion")) {
							apkManifest.getApkUsesSdk().setMinSdkVersion(Integer.parseInt(getAttributeValue(parser, j)));
						} else if (parser.getAttributeName(j).equals("targetSdkVersion")) {
							apkManifest.getApkUsesSdk().setTargetSdkVersion(Integer.parseInt(getAttributeValue(parser, j)));
						} else if (parser.getAttributeName(j).equals("maxSdkVersion")) {
							apkManifest.getApkUsesSdk().setMaxSdkVersion(Integer.parseInt(getAttributeValue(parser, j)));
						}
					}
					
					// parse uses-library tag
					if (parser.getName().equals("uses-library")) {
						if (apkManifest.getApkUsesLibraryList() == null) {
							apkManifest.setApkUsesLibraryList(new ArrayList<ApkUsesLibrary>());
						}
						if (parser.getAttributeName(j).equals("name")
								&& (j + 1 < parser.getAttributeCount())
								&& parser.getAttributeName(j + 1).equals("required")) {
							apkManifest.getApkUsesLibraryList().add(
									new ApkUsesLibrary(getAttributeValue(parser, j), Boolean.parseBoolean(getAttributeValue(parser, j + 1))));
						} else if (parser.getAttributeName(j).equals("name")) {
							apkManifest.getApkUsesLibraryList().add(new ApkUsesLibrary(getAttributeValue(parser, j)));
						}
					}
					
					// parse uses-feature tag
					if (parser.getName().equals("uses-feature")) {
						if (apkManifest.getApkUsesFeatureList() == null) {
							apkManifest.setApkUsesFeatureList(new ArrayList<ApkUsesFeature>());
						}
						if (parser.getAttributeName(j).equals("name")
								&& (j + 2 < parser.getAttributeCount())
								&& parser.getAttributeName(j + 1).equals("required")
								&& parser.getAttributeName(j + 2).equals("glEsVersion")) {
							apkManifest.getApkUsesFeatureList().add(
									new ApkUsesFeature(getAttributeValue(parser, j), Boolean.parseBoolean(getAttributeValue(parser, j + 1)), getAttributeValue(parser, j + 2)));
						} else if (parser.getAttributeName(j).equals("name")
								&& (j + 1 < parser.getAttributeCount())
								&& parser.getAttributeName(j + 1).equals("required")) {
							apkManifest.getApkUsesFeatureList().add(
									new ApkUsesFeature(getAttributeValue(parser, j), Boolean.parseBoolean(getAttributeValue(parser, j + 1))));
						} else if (parser.getAttributeName(j).equals("name")) {
							apkManifest.getApkUsesFeatureList().add(
									new ApkUsesFeature(getAttributeValue(parser, j)));
						}
					}
					
					// parse activity tag
					if (parser.getName().equals("activity")) {
						if (apkManifest.getApkActivityList() == null) {
							apkManifest.setApkActivityList(new ArrayList<ApkActivity>());
						}
						if (parser.getAttributeName(j).equals("name")) {
							apkManifest.getApkActivityList().add(
									new ApkActivity(getAttributeValue(parser, j)));
						}
					}
					
					// parse action tag
					if (parser.getName().equals("action")) {
						if (apkManifest.getApkIntentFilterActionList() == null) {
							apkManifest.setApkIntentFilterActionList(new ArrayList<ApkIntentFilterAction>());
						}
						if (parser.getAttributeName(j).equals("name")) {
							apkManifest.getApkIntentFilterActionList().add(
									new ApkIntentFilterAction(getAttributeValue(parser, j)));
						}
					}
				}
			}
		}
		return apkManifest;
	}
	
    private static String getAttributeValue(AXmlResourceParser parser,int index) {
    	
        int type=parser.getAttributeValueType(index);
        int data=parser.getAttributeValueData(index);
        if (type==TypedValue.TYPE_STRING) {
                return parser.getAttributeValue(index);
        }
        if (type==TypedValue.TYPE_ATTRIBUTE) {
                return String.format("?%s%08X",getPackage(data),data);
        }
        if (type==TypedValue.TYPE_REFERENCE) {
                return String.format("@%s%08X",getPackage(data),data);
        }
        if (type==TypedValue.TYPE_FLOAT) {
                return String.valueOf(Float.intBitsToFloat(data));
        }
        if (type==TypedValue.TYPE_INT_HEX) {
                return String.format("0x%08X",data);
        }
        if (type==TypedValue.TYPE_INT_BOOLEAN) {
                return data!=0?"true":"false";
        }
        if (type==TypedValue.TYPE_DIMENSION) {
                return Float.toString(complexToFloat(data))+
                        DIMENSION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
        }
        if (type==TypedValue.TYPE_FRACTION) {
                return Float.toString(complexToFloat(data))+
                        FRACTION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
        }
        if (type>=TypedValue.TYPE_FIRST_COLOR_INT && type<=TypedValue.TYPE_LAST_COLOR_INT) {
                return String.format("#%08X",data);
        }
        if (type>=TypedValue.TYPE_FIRST_INT && type<=TypedValue.TYPE_LAST_INT) {
                return String.valueOf(data);
        }
        return String.format("<0x%X, type 0x%02X>",data,type);
    }

    private static String getPackage(int id) {
        if (id>>>24==1) {
                return "android:";
        }
        return "";
    }
    
    public static float complexToFloat(int complex) {
        return (float)(complex & 0xFFFFFF00)*RADIX_MULTS[(complex>>4) & 3];
	}
	
	private static final float RADIX_MULTS[]={
	        0.00390625F,3.051758E-005F,1.192093E-007F,4.656613E-010F
	};
	private static final String DIMENSION_UNITS[]={
	        "px","dip","sp","pt","in","mm","",""
	};
	private static final String FRACTION_UNITS[]={
	        "%","%p","","","","","",""
	};
}
