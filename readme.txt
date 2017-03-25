
##################################################支持重放签名############################################################
配置文件中设置参数:
request.enable.sign=true
request.enable.sign.key="TYO"


request头部需要增加参数:
X-Ca-Timestamp:时间戳,new Date().getTime()

如果X-Ca-Timestamp与服务器时间间隔超过15分钟,返回code=404同时返回服务器时间戳,其他如果签名检测错误返回403


签名规则:
http://192.168.2.91:18080/charge/api/refreshToken?appkey=1&appsecret=1

X-Ca-Signature:签名，签名= byte2hex(md5('appkey=1&appsecret=1'+#+{X-Ca-Timestamp}+#+{request.enable.sign.key}))
签名字段：appkey=1&appsecret=1#1488339142196#"TYO"
签名结果：7C4A5C78DE1D6377860FF9DA6076D330
##############################################################################################################