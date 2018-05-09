package com.ihidea.core.support.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.invoke.controller.ResultEntity;
import com.ihidea.core.util.ServletUtilsEx;

/**
 * 把session信息放入threadLocal
 * 
 * @author TYOTANN
 */
public class SessionHolderFilter implements Filter {
    
    protected Log logger = LogFactory.getLog(getClass());
    
    private final static ThreadLocal<SessionInfo> threadLocal = new ThreadLocal<SessionInfo>();
    
    private final static Set<String> sessionUncheckSet = new HashSet<String>();
    
    static {
        
        for (String key : CoreConstants.SESSION_UNCHECK) {
            
            if (StringUtils.isNotBlank(key)) {
                sessionUncheckSet.add(key.trim());
            }
        }
        
        // 默认框架使用，不检查session的方法
        {
            sessionUncheckSet.add("/healthCheck.do");
            sessionUncheckSet.add("/uploadFile.do");
            sessionUncheckSet.add("/downloadFile.do");
            sessionUncheckSet.add("/jcaptcha.do");
            sessionUncheckSet.add("/session_out.do");
            sessionUncheckSet.add("/register.do");
            sessionUncheckSet.add("/login.do");
            sessionUncheckSet.add("/wap.do");
        }
        
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest)request;
        
        try {
            
            setContext(SessionContext.getSessionInfoByReq(req));
            
            if (getContext() == null) {
                
                if (sessionCheck(req)) {
                    
                    String sessionoutPage = CoreConstants.SESSION_OUTPAGE.indexOf("http") == 0 ? CoreConstants.SESSION_OUTPAGE
                        : ServletUtilsEx.getHostURLWithContextPath((HttpServletRequest)request) + "/" + CoreConstants.SESSION_OUTPAGE;
                    
                    logger.debug("用户session失效，跳转到页面：" + sessionoutPage);
                    
                    // 如果是请求的service
                    if (((HttpServletRequest)request).getServletPath().indexOf("invoke.do") > -1) {
                        
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("sessionoutPage", sessionoutPage);
                        ServletUtilsEx.renderJson((HttpServletResponse)response, new ResultEntity("-98", "session失效", data));
                    } else {
                        ((HttpServletResponse)response).sendRedirect(sessionoutPage);
                    }
                    
                    return;
                }
            }
            
            chain.doFilter(request, response);
        } finally {
            setContext(null);
        }
    }
    
    /**
     * <pre>
     * 首页面与首页面调用的服务(LoginService)不进行session检查
     * </pre>
     * 
     * @param request
     * @return
     */
    private boolean sessionCheck(HttpServletRequest request) {
        
        String reqPath = ((HttpServletRequest)request).getRequestURI();
        
        if (sessionUncheckSet.contains(reqPath)) {
            return false;
        }
        
        if (reqPath.indexOf("invoke.do") > -1) {
            
            // 下载模式不检查session
            if ("2".equals(request.getParameter("FRAMEactionMode"))) {
                return false;
            }
            
            // 显式设置不检查session的话,不做session检查,否则都做检查
            if ("false".equals(request.getParameter("FRAMEsessionCheck"))) {
                return false;
            }
        }
        
        return true;
    }
    
    private static void setContext(SessionInfo sc) {
        threadLocal.set(sc);
    }
    
    public static SessionInfo getContext() {
        return threadLocal.get();
    }
    
    public void destroy() {
    }
    
    public void init(FilterConfig arg0) throws ServletException {
    }
    
}
