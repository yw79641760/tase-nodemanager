/**
 * 
 */
package com.softsec.tase.node.util.domain;

import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softsec.tase.common.rpc.domain.app.AppAdvertiser;
import com.softsec.tase.common.rpc.domain.app.AppAntivirus;
import com.softsec.tase.common.rpc.domain.app.AppComment;
import com.softsec.tase.common.rpc.domain.app.AppExternalLink;
import com.softsec.tase.common.rpc.domain.app.AppPermission;
import com.softsec.tase.common.rpc.domain.app.AppWeb;
import com.softsec.tase.common.rpc.domain.app.AppWeb._Fields;
import com.softsec.tase.node.exception.ParserException;

/**
 * AppHandler
 * <p> </p>
 * @author yanwei
 * @since 2013-8-30 下午2:51:35
 * @version
 * @param <T>
 * @param <T>
 */
public class AppHandler<T> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AppHandler.class);

	/**
	 * app web normalization
	 * @param appWeb
	 * @return
	 * @throws ParserException
	 */
	public static AppWeb normalizeAppWeb(AppWeb appWeb) throws ParserException {
		
		try {
			for (_Fields dataField : AppWeb._Fields.values()) {
				Object value = appWeb.getFieldValue(dataField);
				// normalize every field according to the type of field
				if (value != null && value instanceof String) {
					appWeb.setFieldValue(dataField, normalizeString(dataField, value));
				} else if (value != null && value instanceof List) {
					Class<?> type = ((List<?>) value).get(0).getClass();
					if (type.equals(String.class)) {
						normalizeStringList(value);
					} else if (type.equals(AppAdvertiser.class)) {
						normalizeAppElementList(value, AppAdvertiser._Fields.values());
					} else if (type.equals(AppAntivirus.class)) {
						normalizeAppElementList(value, AppAntivirus._Fields.values());
					} else if (type.equals(AppComment.class)) {
						normalizeAppElementList(value, AppComment._Fields.values());
					} else if (type.equals(AppExternalLink.class)) {
						normalizeAppElementList(value, AppExternalLink._Fields.values());
					} else if (type.equals(AppPermission.class)) {
						normalizeAppElementList(value, AppPermission._Fields.values());
					}
					appWeb.setFieldValue(dataField, value);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to normalize app web info [ " + appWeb.getAppChecksum() + " ] : " + e.getMessage(), e);
			throw new ParserException("Failed to normalize app web info [ " + appWeb.getAppChecksum() + " ] : " + e.getMessage(), e);
		}
		return appWeb;
	}

	/**
	 * string normalization
	 * @param field
	 * @param value
	 * @return
	 */
	private static String normalizeString(TFieldIdEnum field, Object value) {
		String content = ((String) value).trim();
		content = content.replaceAll("\\\\", "");
		if (content.startsWith("http")
				|| field.getFieldName().toLowerCase().contains("url")
				|| field.getFieldName().toLowerCase().contains("website")) {
			content = content.replaceAll("'", "&singlequote;");
		} else {
			content = content.replaceAll("'", "\"");
		}
		return content;
	}
	
	@SuppressWarnings("unchecked")
	private static List<String> normalizeStringList(Object value) {
		List<String> valueList = (List<String>) value;
		for (int index = 0; index < valueList.size(); index++) {
			valueList.set(index, normalizeString(null, valueList.get(index)));
		}
		return valueList;
	}
	
	/**
	 * thrift element normalization
	 * @param value
	 * @param fieldEnumArray
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List<TBase> normalizeAppElementList(Object value, TFieldIdEnum[] fieldEnumArray) {
		List<TBase> appElementList = (List<TBase>) value;
		if (appElementList != null && appElementList.size() != 0) {
			for (int index = 0; index < appElementList.size(); index++) {
				TBase appElement = appElementList.get(index);
				for (TFieldIdEnum field : fieldEnumArray) {
					if (appElement.getFieldValue(field).getClass().equals(String.class)) {
						appElement.setFieldValue(field, normalizeString(field, appElement.getFieldValue(field)));
					}
				}
				appElementList.set(index, appElement);
			}
		}
		return appElementList;
	}
	
	/**
	 * normalize app web element using Generic Java
	 * @param valueList
	 * @param fieldEnumArray
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T extends TBase<T, F>, F extends TFieldIdEnum> List<T> normalizeAppElementList(List<T> valueList, F[] fieldEnumArray) {
		if (valueList != null && valueList.size() != 0) {
			for (int index = 0; index < valueList.size(); index++) {
				T appElement = valueList.get(index);
				for (TFieldIdEnum field : fieldEnumArray) {
					if (appElement.getFieldValue((F) field).getClass().equals(String.class)) {
						appElement.setFieldValue((F) field, normalizeString(field, appElement.getFieldValue((F) field)));
					}
				}
				valueList.set(index, appElement);
			}
		}
		return valueList;
	}
}
