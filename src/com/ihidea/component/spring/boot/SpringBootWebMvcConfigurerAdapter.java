package com.ihidea.component.spring.boot;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

//@Configuration
//public class SpringBootWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {
//
//	// 添加自定义转换器
//	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//		converters.add(new MappingJackson2HttpMessageConverter());
//		super.configureMessageConverters(converters);
//	}
//
//}
