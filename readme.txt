
##################################################支持重放签名、IP限制、多个servlet区分############################################################
	<servlet>
		<servlet-name>api2</servlet-name>
		<servlet-class>com.ihidea.component.api.json.OpenAPIJsonServlet</servlet-class>
		<load-on-startup>1</load-on-startup>
		<init-param>
			<param-name>signkey</param-name>
			<param-value>TYO</param-value>
		</init-param>
		<init-param>
			<param-name>servletName</param-name>
			<param-value>xxx</param-value>
		</init-param>
		<init-param>
			<param-name>ipRanges</param-name>
			<param-value>10.0.0.0/8</param-value>
		</init-param>
	</servlet>
	<servlet-mapping>
		<servlet-name>api2</servlet-name>
		<url-pattern>/api2/*</url-pattern>
	</servlet-mapping>


request头部需要增加参数:
X-Ca-Timestamp:时间戳,new Date().getTime()

如果X-Ca-Timestamp与服务器时间间隔超过15分钟,返回code=404同时返回服务器时间戳,其他如果签名检测错误返回403


签名规则:
http://192.168.2.91:18080/charge/api/refreshToken?appkey=1&appsecret=1

X-Ca-Signature:签名，签名= byte2hex(md5('appkey=1&appsecret=1'+#+{X-Ca-Timestamp}+#+{request.enable.sign.key}))
签名字段：appkey=1&appsecret=1#1488339142196#"TYO"
签名结果：7C4A5C78DE1D6377860FF9DA6076D330
##############################################################################################################