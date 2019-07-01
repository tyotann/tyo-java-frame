package com.ihidea.component.jms.kafka;

import org.apache.commons.lang.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KafkaConsumerMethod {

    String topicName() default StringUtils.EMPTY;

    // 对应的应用名,默认为当前方法名.函数名
    String methodName() default StringUtils.EMPTY;

}
