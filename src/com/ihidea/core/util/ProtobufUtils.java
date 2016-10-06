package com.ihidea.core.util;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.ihidea.core.support.exception.ServiceException;

/**
 * google protobuf 序列化反序列化
 * @author TYOTANN
 */
public class ProtobufUtils {

	private Log logger = LogFactory.getLog(ProtobufUtils.class);

	/**
	 * 序列化
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static byte[] serialize(GeneratedMessage.Builder obj) throws Exception {

		ByteArrayOutputStream baos = null;

		try {
			baos = new ByteArrayOutputStream();

			obj.build().writeTo(baos);

			return baos.toByteArray();
		} finally {

			if (baos != null) {
				baos.close();
			}
		}
	}

	/**
	 * 反序列化成protobuf对象，暂不支持嵌套反射
	 * @param srcObj
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static GeneratedMessage.Builder deserialize(Object srcObj, GeneratedMessage.Builder descObj) throws Exception {

		try {

			if (srcObj instanceof byte[]) {
				descObj.mergeFrom((byte[]) srcObj);
			} else if (srcObj instanceof ByteString) {
				descObj.mergeFrom((ByteString) srcObj);

				// TODO list类型暂时不导入
			} else if (srcObj instanceof List) {

				// TODO list类型暂时不导入
			} else {

				Set<String> srcMethodSet = BeanUtils.describe(srcObj).keySet();

				// 遍历protobuf所有的对象
				for (FieldDescriptor fieldDescriptor : descObj.getDescriptorForType().getFields()) {

					Object value = null;

					// 如果存在此属性,取值
					if (srcMethodSet.contains(fieldDescriptor.getName())) {
						value = PropertyUtils.getProperty(srcObj, fieldDescriptor.getName());
					}

					if (value != null) {

						if (fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING) {
							value = BeanUtilsEx.convert(value, String.class);
						} else if (fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.INT) {
							value = BeanUtilsEx.convert(value, Integer.class);
						} else if (fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.BYTE_STRING) {
							if (value instanceof byte[]) {
								value = ByteString.copyFrom((byte[]) value);
							} else {
								throw new ServiceException("protobuf反射错误:[" + fieldDescriptor.getName() + "]为byteString类型,必须输入byte[]数组格式");
							}
						} else if (fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
							// value = ProtobufUtils
							// .deserialize(value, (GeneratedMessage.Builder) descObj.newBuilderForField(fieldDescriptor));
						} else {
							throw new ServiceException("protobuf反射错误:[" + fieldDescriptor.getName() + "]未知的类型"
									+ fieldDescriptor.getJavaType());
						}
					}

					// 非repeate
					if (!fieldDescriptor.isRepeated()) {

						if (value != null) {
							descObj.setField(fieldDescriptor, value);

						} else if (fieldDescriptor.isRequired()
								&& fieldDescriptor.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {

							// 如果为必须项且非message类型
							descObj.setField(fieldDescriptor, fieldDescriptor.getDefaultValue());
						}
					}
				}
			}

			return descObj;
		} catch (Exception e) {
			throw new ServiceException("protobuf反序列化出错!" + e.getMessage());
		}

	}

	/**
	 * 反序列化成protobuf对象
	 * @param srcObj
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static GeneratedMessage.Builder deserialize(Object srcObj, Class<? extends GeneratedMessage> clz) throws Exception {

		Method method = clz.getMethod("newBuilder");
		GeneratedMessage.Builder descObj = (GeneratedMessage.Builder) method.invoke(clz);

		return ProtobufUtils.deserialize(srcObj, descObj);
	}

	/**
	 * protobuf对象转为Map对象
	 * @param protobufObj
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, Object> toProtobuf(GeneratedMessage.Builder protobufObj) {

		Map<String, Object> result = new HashMap<String, Object>();

		if (protobufObj != null) {

		}

		return result;
	}
}
