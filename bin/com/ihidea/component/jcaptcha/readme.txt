web.xml中加入


	<!-- jcaptcha -->
	<servlet>
		<servlet-name>jcaptcha</servlet-name>
		<servlet-class>com.ihidea.component.jcaptcha.ImageServlet</servlet-class>
		<load-on-startup>1</load-on-startup>
	</servlet>
	<servlet-mapping>
		<servlet-name>jcaptcha</servlet-name>
		<url-pattern>/jcaptcha</url-pattern>
	</servlet-mapping>
	
