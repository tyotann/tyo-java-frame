package com.ihidea.component.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang.StringUtils;

import com.google.protobuf.GeneratedMessage;
import com.ihidea.component.api.IAPIVerify;
import com.ihidea.component.api.OpenAPINoneVerify;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OpenAPIMethodProto {

	// 对应的应用名,默认为当前方法名.函数名
	String methodName() default StringUtils.EMPTY;

	// 需要登录的页面，默认为true
	// boolean isLogin();

	Class<? extends IAPIVerify> verify() default OpenAPINoneVerify.class;

	// 请求protobuf类，默认为null
	// Class<?> requestClz() default Object.class;

	Class<? extends GeneratedMessage> requestClz() default GeneratedMessage.class;

	// 返回protobuf类，必需
	Class<? extends GeneratedMessage> responseClz() default GeneratedMessage.class;
	// Class<?> responseClz() default Object.class;

	// 是否有deviceId
	// boolean hasDeviceId() default true;

}
