package com.ihidea.component.api.json;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ihidea.component.api.IAPIVerify;
import com.ihidea.component.api.MobileInfo;
import com.ihidea.component.api.OpenAPINoneVerify;
import com.ihidea.component.api.annotation.OpenAPI;
import com.ihidea.component.api.annotation.OpenAPIMethod;
import com.ihidea.core.CoreConstants;
import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.exception.ServiceWarn;
import com.ihidea.core.support.local.LocalAttributeHolder;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.util.ClassUtilsEx;
import com.ihidea.core.util.CommonUtilsEx;
import com.ihidea.core.util.DigitalUtils;
import com.ihidea.core.util.JSONUtilsEx;
import com.ihidea.core.util.ServletUtilsEx;
import com.ihidea.core.util.SignatureUtils;
import com.ihidea.core.util.StringUtilsEx;
import com.ihidea.core.util.SystemUtilsEx;

/**
 * 手机服务器端请求
 * 
 * @author TYOTANN
 */
public class OpenAPIJsonServlet extends HttpServlet {
    
    private static final long serialVersionUID = -7813617057723596671L;
    
    private static Logger logger = LoggerFactory.getLogger(OpenAPIJsonServlet.class);
    
    private Map<String, Method> mobileMethodMap = new HashMap<String, Method>();
    
    private Map<String, OpenAPIMethod> mobileMethodAnnoMap = new HashMap<String, OpenAPIMethod>();
    
    private Map<String, String> signkeyMap = new HashMap<String, String>();
    
    private String servletName;
    
    private String ipRanges;
    
    /**
     * 初始化，扫描系统中所有的mobileMethod
     */
    @Override
    public void init() throws ServletException {
        
        try {
            
            Enumeration<String> paramEnum = getInitParameterNames();
            
            if (paramEnum != null) {
                while (paramEnum.hasMoreElements()) {
                    String paramName = paramEnum.nextElement();
                    if ("signkey".equals(paramName)) {
                        signkeyMap.put("default", getInitParameter(paramName));
                    } else if (StringUtils.startsWith(paramName, "signkey-")) {
                        signkeyMap.put(StringUtils.substring(paramName, "signkey-".length()), getInitParameter(paramName));
                    }
                }
            }
            
            this.servletName = StringUtils.defaultIfEmpty(getInitParameter("servletName"), StringUtils.EMPTY);
            this.ipRanges = getInitParameter("ipRanges");
            
            Map<String, Object> openClz = SpringContextLoader.getBeansWithAnnotation(OpenAPI.class);
            
            if (openClz != null) {
                for (Object clzObj : openClz.values()) {
                    
                    List<Method> methodList = ClassUtilsEx.getClassMethodByAnnotation(clzObj.getClass(), OpenAPIMethod.class);
                    
                    for (Method method : methodList) {
                        
                        OpenAPIMethod methodAnno = method.getAnnotation(OpenAPIMethod.class);
                        
                        String methodName = methodAnno.methodName();
                        
                        if (StringUtils.isBlank(methodName)) {
                            methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
                        }
                        
                        if (servletName.equals(methodAnno.servletName())) {
                            
                            if (mobileMethodMap.containsKey(methodName) || mobileMethodAnnoMap.containsKey(methodName)) {
                                throw new ServiceException("API接口:" + methodName + "有重复定义,请检查代码!");
                            }
                            
                            mobileMethodMap.put(methodName, method);
                            mobileMethodAnnoMap.put(methodName, methodAnno);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        MJSONResultEntity result = new MJSONResultEntity();
        
        // 得到参数
        Map<String, Object> paramMap = null;
        
        // 函数名
        String methodName = null;
        
        try {
            
            // 访问IP检查
            if (StringUtils.isNotBlank(ipRanges) && !CommonUtilsEx.isIPAddressInRange(SystemUtilsEx.getClientIpSingle(request), ipRanges)) {
                throw new ServiceException("IP地址不允许访问");
            }
            
            // 得到参数
            paramMap = getParam(request, response);
            
            if (StringUtils.isBlank(request.getPathInfo())) {
                throw new ServiceException("请求的接口格式错误");
            }
            
            // 移除调用函数名
            methodName = request.getPathInfo().substring(1);
            
            paramMap.put("FRAMEmethodName", methodName);
            
            OpenAPIMethod mobileMethod = mobileMethodAnnoMap.get(methodName);
            
            // 基本检查
            {
                if (mobileMethod == null) {
                    throw new ServiceException("请求的接口:" + methodName + "在服务器端没有定义,请检查!");
                }
            }
            
            // 校验检查
            if (mobileMethod.verify() != OpenAPINoneVerify.class) {
                
                IAPIVerify verifyMethod = SpringContextLoader.getBean(mobileMethod.verify());
                
                if (verifyMethod != null) {
                    boolean isVerify = verifyMethod.verify(paramMap);
                    
                    if (!isVerify) {
                        throw new ServiceException(MJSONResultEntity.RESULT_SESSION_ERROR, "登录失败,请登录!");
                    }
                } else {
                    throw new ServiceException("未找到定义的类:" + mobileMethod.verify());
                }
            }
            
            // 请求方法
            result.setData(requestMethod(methodName, mobileMethod, paramMap));
            
        } catch (Exception e) {
            
            Throwable rootThrowable = ExceptionUtils.getRootCause(e) == null ? e : ExceptionUtils.getRootCause(e);
            
            String errorText = rootThrowable.getMessage() == null ? String.valueOf(rootThrowable) : rootThrowable.getMessage();
            
            if (rootThrowable instanceof ServiceException) {
                
                ServiceException se = ((ServiceException)rootThrowable);
                
                // 如果存在code,但是无text,且定义了message_i18n,则从message中取得具体的text值
                errorText = se.getMessage();
                
                logger.debug(errorText);
                
                if (StringUtils.isNotBlank(se.getCode())) {
                    result.setCode(se.getCode());
                } else {
                    result.setCode(MJSONResultEntity.RESULT_LOGIC_ERROR);
                }
                
                if (se.getData() != null) {
                    result.setData(se.getData());
                }
            } else if (rootThrowable instanceof ServiceWarn) {
                logger.debug(rootThrowable.getMessage());
                result.setCode(MJSONResultEntity.RESULT_WARN);
            } else {
                logger.error(rootThrowable.getMessage(), rootThrowable);
                result.setCode(MJSONResultEntity.RESULT_EXCEPTION);
            }
            
            result.setText(errorText);
        } finally {
            ServletUtilsEx.renderJson(response, result);
        }
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    
    @Override
    public void destroy() {
        mobileMethodMap.clear();
        mobileMethodAnnoMap.clear();
    }
    
    /**
     * 得到request中的参数
     * 
     * @param request
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getParam(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        // web中接口请求，用jquery的ajax post参数后发现如果参数中有多个且有数组，则参数名是xxx[1]这样的格式
        String paramJsonStr = request.getParameter("FRAMEparams");
        
        // 得到参数
        Map<String, Object> paramMap = null;
        
        if (StringUtils.isNotBlank(paramJsonStr)) {
            paramMap = JSONUtilsEx.deserialize(paramJsonStr, Map.class);
        } else {
            
            paramMap = new HashMap<String, Object>();
            
            for (String paramName : (Set<String>)request.getParameterMap().keySet()) {
                
                if (StringUtils.isNotBlank(paramName)) {
                    paramMap.put(paramName, request.getParameter(paramName));
                }
            }
        }
        
        String ipAddress = SystemUtilsEx.getClientIpSingle(request);
        String userAgent = request.getHeader("user-agent");
        
        // header中加入：X-Ca-Timestamp，如果时间戳校验失败，返回当前时间
        // header中加入：X-Ca-Signature，签名 md5(timestamp,data,固定值)
        // 框架读取配置文件，如果设置了某个属性，则说明必须强制使用上面签名
        if (signkeyMap.size() > 0) {
            
            Long nowTime = new Date().getTime();
            
            String timestamp = request.getHeader("X-Ca-Timestamp");
            
            String sign = request.getHeader("X-Ca-Signature");
            
            String appVersion = request.getHeader("appVersion");
            
            if (StringUtils.isBlank(timestamp) || StringUtils.isBlank(sign)) {
                throw new ServiceException(MJSONResultEntity.REQUEST_SIGN_ERROR, "缺少签名信息");
            }
            
            try {
                if (Math.abs(nowTime - Long.valueOf(timestamp)) > 900000) {
                    throw new ServiceException(MJSONResultEntity.REQUEST_SIGN_TIME_ERROR, String.valueOf(nowTime));
                }
            } catch (NumberFormatException e) {
                logger.error("[安全检查]-签名时间错误,当前时间:{},签名时间:{},IP地址:{},userAgent:{}", new Object[]{nowTime, timestamp, ipAddress, userAgent});
                throw new ServiceException(MJSONResultEntity.REQUEST_SIGN_TIME_ERROR, "签名时间错误");
            }
            
            // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
            String paramStr = SignatureUtils.createLinkString(paramMap);
            
            // 如果没有版本信息或版本信息未独立定义key，则使用默认的key，否则使用对应的key
            String signkey = getSignKey(appVersion);
            
            String serverSign = DigitalUtils.byte2hex(SignatureUtils.md5(paramStr + "#" + timestamp + "#" + signkey, "UTF-8"));
            
            if (!sign.equalsIgnoreCase(serverSign)) {
                logger.error("[安全检查]-签名错误,服务器端签名:{},客户端签名:{},IP地址:{},userAgent:{},版本号:{}",
                    new Object[]{serverSign, sign, ipAddress, userAgent, appVersion});
                throw new ServiceException(MJSONResultEntity.REQUEST_SIGN_ERROR, "签名错误");
            }
        }
        
        // 分页处理
        if (paramMap.containsKey("page")) {
            
            if (!paramMap.containsKey("pagecount")) {
                throw new ServiceException("参数列表中存在参数page时,请同时传入分页参数:pagecount!");
            }
            
            // 手机端如果需要总页数,传入totalCount=null，如果不传则代表不需要总页数信息）(设置为0，这样不会再算一遍count)
            Integer totalCount = null;
            if (paramMap.get("totalcount") != null) {
                totalCount = Integer.valueOf(String.valueOf(paramMap.get("totalcount")));
            }
            
            PageLimitHolderFilter.setContext(Integer.valueOf(String.valueOf(paramMap.get("page"))),
                Integer.valueOf(String.valueOf(paramMap.get("pagecount"))), totalCount);
            PageLimitHolderFilter.getContext().setLimited(true);
        }
        
        String appid = StringUtils.isNotBlank(CoreConstants.getProperty("application.appid"))
            ? CoreConstants.getProperty("application.appid") : (String)paramMap.get("appid");
        
        // 建立mobileInfo对象
        MobileInfo mobileInfo = new MobileInfo((String)paramMap.get("userId"), (String)paramMap.get("deviceid"), appid);
        paramMap.put("mobileInfo", mobileInfo);
        
        // 塞入线程变量
        LocalAttributeHolder.getContext().put("appid", appid);
        LocalAttributeHolder.getContext().put("userid", paramMap.get("userId"));
        LocalAttributeHolder.getContext().put("mobileInfo", mobileInfo);
        LocalAttributeHolder.getContext().put("ipAddress", ipAddress);
        
        if (!StringUtils.isBlank(userAgent)) {
            
            userAgent = userAgent.toLowerCase();
            
            String platform = "mobile";
            
            if (userAgent.indexOf("iphone") > -1) {
                platform = "iphone";
            } else if (userAgent.indexOf("ipad") > -1) {
                platform = "ipad";
            } else if (userAgent.indexOf("android") > -1 || userAgent.indexOf("apache-httpclient") > -1) {
                platform = "android";
            }
            LocalAttributeHolder.getContext().put("platform", platform);
        }
        
        // 如果传入参数是String类型，则做xss过滤
        for (String key : paramMap.keySet()) {
            if (paramMap.get(key) != null && paramMap.get(key) instanceof String) {
                paramMap.put(key, StringUtilsEx.escapeXss((String)paramMap.get(key)));
            }
        }
        
        return paramMap;
    }
    
    /**
     * 请求服务内容并反射后到protobuf
     * 
     * @param methodName
     * @param openMethod
     * @param paramMap
     * @return
     * @throws Exception
     */
    private Object requestMethod(String methodName, OpenAPIMethod openMethod, Map<String, Object> paramMap) throws Exception {
        
        // 请求服务
        Method method = mobileMethodMap.get(methodName);
        
        // 开始支持分页，之前的信息查詢不需要分頁
        PageLimitHolderFilter.getContext().setLimited(false);
        
        return ClassUtilsEx.invokeMethod(method.getDeclaringClass().getSimpleName(), method.getName(), paramMap);
    }
    
    // 如果没有版本信息（使用默认key） 黎江 2017-11-24
    // 有版本信息有对应key（使用对应key）
    // 有版本信息没有对应key但是有大版本信息（使用对应大版本对应key）
    // 有版本信息没有对应key也没有大版本信息（使用默认key）
    private String getSignKey(String appVersion) {
        
        String signkey = signkeyMap.get("default");
        
        if (StringUtils.isNotBlank(appVersion)) {
            
            if (signkeyMap.containsKey(appVersion)) {
                signkey = signkeyMap.get(appVersion);
            } else {
                String[] versionArray = appVersion.split("\\.");
                String mainVersion = "";
                for (int i = 0; i < versionArray.length; i++) {
                    if (i == 2) {
                        mainVersion += "0";
                    } else {
                        mainVersion += versionArray[i];
                    }
                    if (i < (versionArray.length - 1)) {
                        mainVersion += ".";
                    }
                }
                if (signkeyMap.containsKey(mainVersion)) {
                    signkey = signkeyMap.get(mainVersion);
                }
            }
        }
        
        return signkey;
    }
    
}
