package com.ihidea.core.util;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.local.LocalAttributeHolder;

public class I18NUtils {
    
    private static ResourceBundleMessageSource messageSource = null;
    
    private static boolean loadSrpingMessageSource = false;
    
    public static String getMessage(String code) {
        return getMessage(code, null);
    }
    
    public static String getMessage(String code, Object[] args) {
        
        if (StringUtils.isNotBlank(code)) {
            
            String text = null;
            
            if (messageSource == null && !loadSrpingMessageSource) {
                
                Object _messageSource = SpringContextLoader.getBean("messageSource");
                
                if (_messageSource != null && _messageSource instanceof ResourceBundleMessageSource) {
                    messageSource = (ResourceBundleMessageSource)_messageSource;
                }
                
                loadSrpingMessageSource = true;
            }
            
            if (messageSource != null) {
                text = messageSource.getMessage(code, args, getLocale());
            }
            
            return text;
        }
        
        return null;
    }
    
    public static Locale getLocale() {
        
        // 默认多语为zh
        Locale locale = Locale.CHINESE;
        
        if (LocalAttributeHolder.getContext().containsKey("locale")) {
            locale = org.springframework.util.StringUtils.parseLocaleString((String)LocalAttributeHolder.getContext().get("locale"));
        }
        
        return locale;
    }
    
}
