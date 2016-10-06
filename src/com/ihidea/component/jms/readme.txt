在web.xml的
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>
		     classpath*:/spring/applicationContext.xml
		</param-value>
	</context-param>
	
中加入:
classpath:com/finstone/house/component/jms/spring-jms.xml

如下:
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>
		     classpath*:/spring/applicationContext.xml,
		     classpath:com/finstone/house/component/jms/spring-jms.xml
		</param-value>
	</context-param>