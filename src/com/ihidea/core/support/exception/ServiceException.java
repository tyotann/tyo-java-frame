package com.ihidea.core.support.exception;

import org.apache.commons.lang.StringUtils;

import com.ihidea.core.util.I18NUtils;

/**
 * 业务异常
 * 
 * @author TYOTANN
 */
public class ServiceException extends RuntimeException {
    
    private String code;
    
    private Object data;
    
    public String getCode() {
        return code;
    }
    
    public Object getData() {
        return data;
    }
    
    public ServiceException() {
    }
    
    public ServiceException(Throwable e) {
        super(e);
    }
    
    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public ServiceException(Integer code, String message) {
        super(message);
        this.code = String.valueOf(code);
    }
    
    public ServiceException(String code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }
    
    public ServiceException(String message, Throwable e) {
        super(message, e);
    }
    
    /**
     * 如果存在code,但是无text,且定义了message_i18n,则从message中取得具体的text值
     */
    public String getMessage() {
        
        if (StringUtils.isNotBlank(getCode()) && StringUtils.isBlank(super.getMessage())) {
            return I18NUtils.getMessage(code);
        }
        
        return super.getMessage();
    }
}
