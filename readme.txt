##################################################多数据源###########################################################

<aop:pointcut id="dynamicDataSourceWithAnnotation" expression="@annotation(com.ihidea.core.support.dataSource.DynamicDataSource)" />
<aop:advisor advice-ref="dynamicDataSourceInterceptor" pointcut-ref="dynamicDataSourceWithAnnotation" order="1"/>
<bean id="dynamicDataSourceInterceptor" class="com.ihidea.core.support.dataSource.DynamicDataSourceInterceptor" />




##################################################服务发布###########################################################

springboot打包成Linux的service方式
1.
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <configuration>
    <executable>true</executable>
  </configuration>
</plugin>
2.sudo ln -s /opt/open.starcharge.com/starcharge.open.jar /etc/init.d/starcharge.open
3.chmod 777 /opt/open.starcharge.com/starcharge.open.jar
4.vi /opt/open.starcharge.com/starcharge.open.conf
JAVA_HOME=/opt/jdk1.7.0_79
JAVA_OPTS="-Dfile.encoding=UTF-8 -server -Xms1024m -Xmx1024m -XX:MaxPermSize=400m -Xverify:none"
LOG_FOLDER=/dev/null

然后就可以service starcharge.open start|stop|restart 启动

jenkis打包后，直接替换jar文件，然后重启服务

##################################################推送############################################################

#是否是生产模式
mobile.notify.ios.production=true

#推送多通道以,隔开
push.channel=aliPush,jpush_android,jpush_iOS

#aliPush
push.aliPush.appkey=24453402
push.aliPush.accessKeyId=IWg0bNKL6dDrswgh
push.aliPush.accessKeySecret=DbZhDxLD373f2dy6Vv7FbPdaoiyClW
push.aliPush.region=cn-hangzhou

#消息类型 MESSAGE NOTICE
push.aliPush.pushType=NOTICE

#设备类型 ANDROID iOS ALL.
push.aliPush.deviceType=ANDROID

#JPush
push.jPush.iOS.appkey=
push.jPush.iOS.secret=
push.jPush.android.appkey=
push.jPush.android.secret=

##################################################支持重放签名（支持多版本多套秘钥）、IP限制、多个servlet区分############################################################
	<servlet>
		<servlet-name>api2</servlet-name>
		<servlet-class>com.ihidea.component.api.json.OpenAPIJsonServlet</servlet-class>
		<load-on-startup>1</load-on-startup>
		<init-param>
			<param-name>signkey</param-name>
			<param-value>TYO</param-value>
		</init-param>
		<init-param>
			<param-name>signkey-4.0.0.1</param-name>
			<param-value>TYO1</param-value>
		</init-param>
		<init-param>
			<param-name>signkey-4.0.0.2</param-name>
			<param-value>TYO2</param-value>
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

如果头部有参数:appVersion，则key可以根据版本号来自定义，比如
		<init-param>
			<param-name>signkey-4.0.0.1</param-name>
			<param-value>TYO1</param-value>
		</init-param>
		对应的版本号是4.0.0.1，对应的加密秘钥是TYO1


签名规则:
http://192.168.2.91:18080/charge/api/refreshToken?appkey=1&appsecret=1

X-Ca-Signature:签名，签名= byte2hex(md5('appkey=1&appsecret=1'+#+{X-Ca-Timestamp}+#+{request.enable.sign.key}))
签名字段：appkey=1&appsecret=1#1488339142196#"TYO"
签名结果：7C4A5C78DE1D6377860FF9DA6076D330
##############################################################################################################