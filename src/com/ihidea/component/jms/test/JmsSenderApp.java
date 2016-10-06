package com.ihidea.component.jms.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.ihidea.component.jms.MessageProducer;

public class JmsSenderApp {

	public static void main(String[] args) {

		ApplicationContext ac = new FileSystemXmlApplicationContext("applicationContext.xml");

		MessageProducer producer = ac.getBean(MessageProducer.class);
		System.out.println("begin");
		producer.send("=================");
		System.out.println("end");
	}
}
