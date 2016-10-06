在web.xml的
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>
		     classpath*:/spring/applicationContext.xml
		</param-value>
	</context-param>
	
中加入:
classpath:classpath*:/spring/applicationContext-ws.xml,

如下:
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>
		     classpath*:/spring/applicationContext.xml,
		     classpath*:/spring/applicationContext-ws.xml
		</param-value>
	</context-param>
	
	
并在web.xml中放开<!-- cxf web service -->下的注释