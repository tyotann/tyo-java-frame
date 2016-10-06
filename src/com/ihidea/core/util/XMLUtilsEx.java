package com.ihidea.core.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.ihidea.core.support.exception.ServiceException;

public class XMLUtilsEx {

	private static XmlMapper mapper = new XmlMapper();

	/**
	 * POJO-->XML (POJO如果为List等容器对象,必须设置propertyName,不设置XML出来节点有问题)
	 * @param obj
	 * @return
	 */
	public static String serialize(Object obj) {
		return XMLUtilsEx.serialize(obj, null);
	}

	/**
	 * POJO-->XML (POJO如果为List等容器对象,必须设置propertyName,不设置XML出来节点有问题)
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String serialize(Object obj, String propertyName) {

		// 如果没有值的话,直接返回
		if (obj == null || (obj instanceof Map && ((Map) obj).size() == 0) || (obj instanceof Collection && ((Collection) obj).size() == 0)) {
			return StringUtils.EMPTY;
		}

		if (obj instanceof Collection || !StringUtils.isBlank(propertyName)) {

			// 如果传入的是容器对象,且属性名为空,则默认设置属性名为data
			if (StringUtils.isBlank(propertyName)) {
				propertyName = "array";
			}

			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put(propertyName, obj);
			obj = tmpMap;
		}

		try {

			// 去除xml的头与尾
			String xml = mapper.writeValueAsString(obj);
			return xml.substring(xml.indexOf(">") + 1, xml.lastIndexOf("</"));
		} catch (Exception e) {
			throw new ServiceException("XML序列化结果异常:" + e.getMessage());
		}
	}

	/**
	 * JSON字符串反序列化成对象
	 * @param xmlStr
	 * @param clazz
	 * @return
	 */
	public static <T> T deserialize(String xmlStr, Class<T> clazz) {

		if (StringUtils.isBlank(xmlStr)) {
			return null;
		}

		try {
			return mapper.readValue(xmlStr.replace("\n", ""), clazz);
		} catch (Exception e) {
			throw new ServiceException("JSON反序列化结果异常:" + e.getMessage());
		}
	}
}
