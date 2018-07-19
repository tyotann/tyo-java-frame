package com.ihidea.component.api;

public interface IAPISign {
    
    /**
     * 
     * @param paramsStr 签名字符串
     * @param sign 客户端上传的签名值
     * @return 是否签名成功
     */
    public boolean sign(String appVersion, String paramsStr, String timestamp, String sign) throws Exception;
    
}
