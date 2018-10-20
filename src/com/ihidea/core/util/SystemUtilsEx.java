package com.ihidea.core.util;

import java.io.IOException;
import java.net.*;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihidea.core.support.exception.ServiceException;

/**
 * JDK 类型，版本，系统相关
 * 
 * @author TYOTANN
 */
public class SystemUtilsEx {
    
    private static Log logger = LogFactory.getLog(SystemUtilsEx.class);
    
    private static Boolean isInitJCE = false;
    
    private static String hostId;
    
    private static String hostIp;
    
    private static String hostName;
    
    /**
     * 是否使用IBM JDK
     * 
     * @return
     */
    public static boolean isIBM() {
        return SystemUtils.JAVA_VM_VENDOR.contains("IBM");
    }
    
    /**
     * 得到使用JCE的名称
     * 
     * @return
     */
    public static String getJCEName() {
        return SystemUtilsEx.isIBM() ? "IBMJCE" : "SunJCE";
    }
    
    /**
     * 初始化JCE
     */
    @SuppressWarnings("rawtypes")
    public static void initJCE() {
        
        if (!isInitJCE) {
            synchronized (isInitJCE) {
                if (!isInitJCE) {
                    Class providerClz = null;
                    
                    try {
                        if (SystemUtilsEx.isIBM()) {
                            providerClz = Class.forName("com.ibm.crypto.provider.IBMJCE");
                        } else {
                            providerClz = Class.forName("com.sun.crypto.provider.SunJCE");
                        }
                        logger.info("系统使用的JDK:" + SystemUtils.JAVA_VM_VENDOR + ",使用JCE:" + providerClz.getName());
                    } catch (ClassNotFoundException e) {
                        logger.error("系统使用的JDK:" + SystemUtils.JAVA_VM_VENDOR + ",无法找到对应的JCE类");
                        throw new ServiceException("系统使用的JDK:" + SystemUtils.JAVA_VM_VENDOR + ",无法找到对应的JCE类");
                    }
                    
                    Provider provider = null;
                    
                    try {
                        provider = (Provider)providerClz.newInstance();
                    } catch (Exception e) {
                        logger.error("创建对象:" + providerClz.getName() + "异常:" + e.getMessage());
                        throw new ServiceException("创建对象:" + providerClz.getName() + "异常:" + e.getMessage());
                    }
                    
                    Security.addProvider(provider);
                    
                    isInitJCE = true;
                }
            }
        }
    }
    
    /**
     * 得到主机IP地址
     * 
     * @return
     */
    public static String getHostIp() {
        
        if (hostIp == null) {
            
            hostIp = "0.0.0.0";
            
            Enumeration<NetworkInterface> netInterfaces = null;
            try {
                netInterfaces = NetworkInterface.getNetworkInterfaces();
                while (netInterfaces.hasMoreElements()) {
                    NetworkInterface ni = netInterfaces.nextElement();
                    Enumeration<InetAddress> ips = ni.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        String ip = ips.nextElement().getHostAddress();
                        if (!"127.0.0.1".equals(ip) && ip.length() <= 15 && ip.split("\\.").length == 4) {
                            hostIp = ip;
                            return hostIp;
                        }
                    }
                }
            } catch (SocketException e) {
                logger.error(e.getMessage(), e);
            }
        }
        
        return hostIp;
    }
    
    /**
     * 得到集群中主机的唯一标识
     * 
     * @return
     */
    public static String getHostId() {
        
        if (hostId == null) {
            hostId = getHostIp() + "#" + StringUtilsEx.getUUID();
        }
        
        return hostId;
    }
    
    public static String getHostName() {
        
        if (hostName == null) {
            
            hostName = StringUtils.EMPTY;
            
            InetAddress addr;
            try {
                addr = InetAddress.getLocalHost();
                hostName = addr.getHostName();
            } catch (UnknownHostException e) {
                logger.error(e.getMessage(), e);
            }
            
        }
        
        return hostName;
    }
    
    /**
     * 得到客户端用户实际IP地址
     * 
     * @param request
     * @return
     */
    public static String getClientIp(HttpServletRequest request) {
        
        String ip = request.getHeader("x-forwarded-for");
        
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        return ip;
    }
    
    /**
     * 得到客户端用户实际IP地址
     * 
     * @param request
     * @return
     */
    public static String getClientIpSingle(HttpServletRequest request) {
        
        String ip = request.getHeader("x-forwarded-for");
        
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        if (ip != null) {
            String[] ipArray = ip.trim().split(",");
            ip = ipArray[ipArray.length - 1];
        }
        
        return ip;
    }
    
    /**
     * 得到操作系统名称
     * 
     * @return
     */
    public static String getOSName() {
        return SystemUtils.OS_NAME;
    }
    
    private static Boolean hasMagickImage = null;
    
    static {
        hasMagickImage = false;
        try {
            
            // System.out.println(System.getProperty("java.library.path"));
            
            System.setProperty("jmagick.systemclassloader", "no");
            
            System.loadLibrary("JMagick");
            
            hasMagickImage = true;
            logger.info("系统已安装图形处理MagickImage...");
        } catch (Throwable e) {
            logger.info("系统未安装图形处理MagickImage,会使用默认图形处理,Linux下压缩图片可能会变红..." + e.getMessage());
        }
    }
    
    // 判断系统是否安装MagickImage
    public static boolean hasMagickImage() {
        return hasMagickImage;
    }
    
    public static String getMainestInfoInJar(Class<?> clz, String attrType, String attrName, String attr) {
        
        Attributes attributes = null;
        
        try {
            Enumeration<URL> resources = clz.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                try {
                    attributes = new Manifest(resources.nextElement().openStream()).getMainAttributes();
                    if (attrName.equals(attributes.get(new Name(attrType)))) {
                        return attributes.get(new Name(attr)).toString();
                    }
                } catch (IOException E) {
                }
            }
        } catch (Exception e) {
        }
        
        return null;
    }


    public static String dockerParentHostIp ;


    /**
     * 获取docker素宿主机ip
     * @param startsWith ip前缀，不符合不返回
     * @return
     */
    public static String getDockerParentHostIp(String startsWith) {

        if(dockerParentHostIp == null) {
            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                NetworkInterface networkInterface;
                Enumeration<InetAddress> inetAddresses;
                InetAddress inetAddress;
                String ip;
                while (networkInterfaces.hasMoreElements()) {
                    networkInterface = networkInterfaces.nextElement();
                    inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        inetAddress = inetAddresses.nextElement();
                        if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                            ip = inetAddress.getHostAddress();
                            if(ip.startsWith(startsWith)) {
                                dockerParentHostIp = ip;
                                break;
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                logger.error("获取docker宿主机ip异常", e);
            }
        }

        return dockerParentHostIp;

    }


    public static void main(String[] args) {
        System.out.println(getDockerParentHostIp(""));
    }
    
}
