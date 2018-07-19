package com.ihidea.component.api.json;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.pageLimit.PageLimit;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.util.BeanUtilsEx;
import com.ihidea.core.util.JSONUtilsEx;

/**
 * controller调用返回对象
 * 
 * @author TYOTANN
 */
public class MJSONResultEntity implements Serializable {
    
    private static final long serialVersionUID = -115509026625589704L;
    
    // 正常
    public final static String RESULT_SUCCESS = "200";
    
    // 警告
    public final static String RESULT_WARN = "300";
    
    // 业务逻辑异常
    public final static String RESULT_LOGIC_ERROR = "400";
    
    // 系统异常
    public final static String RESULT_EXCEPTION = "401";
    
    // Session 超时
    public final static String RESULT_SESSION_ERROR = "402";
    
    // 签名信息异常
    public final static String REQUEST_SIGN_ERROR = "403";
    
    // 签名时间异常
    public final static String REQUEST_SIGN_TIME_ERROR = "404";
    
    /**
     * <pre>
     * 请求结果状态位
     * </pre>
     */
    private String code = MJSONResultEntity.RESULT_SUCCESS;
    
    /**
     * 警告或者异常信息，正常无信息
     */
    private String text;
    
    /**
     * 正常结束返回值
     */
    private Object data;
    
    private PageLimit pageLimit;
    
    /**
     * 分页信息
     * 
     * @return
     */
    public PageLimit getPageLimit() {
        
        if (pageLimit == null) {
            pageLimit = PageLimitHolderFilter.getContext();
        }
        
        return (pageLimit != null && pageLimit.limited()) ? pageLimit : null;
    }
    
    public void setPageLimit(PageLimit pageLimit) {
        this.pageLimit = pageLimit;
    }
    
    // ------------------------------构造函数------------------------------//
    public MJSONResultEntity() {
    }
    
    public MJSONResultEntity(String code) {
        this.code = code;
    }
    
    public MJSONResultEntity(Object data) {
        this.data = data;
    }
    
    public MJSONResultEntity(String code, String text) {
        this.code = code;
        this.text = text;
    }
    
    public MJSONResultEntity(String code, String text, Object data) {
        this.code = code;
        this.text = text;
        this.data = data;
    }
    
    // ------------------------------构造函数------------------------------//
    
    public String getCode() {
        return code;
    }
    
    public Object getData() {
        return data;
    }
    
    public String getText() {
        return text;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    @SuppressWarnings("unchecked")
    public static MJSONResultEntity deserialize(String jsonStr) {
        
        if (StringUtils.isBlank(jsonStr) || jsonStr.contains("<html>")) {
            throw new ServiceException("平台服务器无法访问");
        }
        Map<String, ?> responseMap = JSONUtilsEx.deserialize(jsonStr, Map.class);
        
        // 解析MJSONResultEntity，如果异常直接抛出，如果正常则把data转型成clz。这里find和get的区别是转型是转object还是转list
        MJSONResultEntity result = new MJSONResultEntity();
        result.setCode(BeanUtilsEx.convert(responseMap.get("code"), String.class));
        result.setText(BeanUtilsEx.convert(responseMap.get("text"), String.class));
        
        if (!"200".equals(result.getCode())) {
            throw new ServiceException(result.getCode(), result.getText());
        }
        result.setData(responseMap.get("data"));
        
        // 分页信息
        Map<String, Object> pageLimitMap = (Map<String, Object>)responseMap.get("pageLimit");
        
        if (pageLimitMap != null) {
            
            PageLimit pageLimit = new PageLimit(BeanUtilsEx.convert(pageLimitMap.get("limited"), Boolean.class),
                BeanUtilsEx.convert(pageLimitMap.get("currentPageNo"), Integer.class),
                BeanUtilsEx.convert(pageLimitMap.get("pageLength"), Integer.class),
                BeanUtilsEx.convert(pageLimitMap.get("totalCount"), Integer.class));
            
            result.setPageLimit(pageLimit);
            
            // 设置到本地threadLocal的pageLimit属性
            {
                PageLimit threadLocalPageLimit = PageLimitHolderFilter.getContext();
                
                if (threadLocalPageLimit != null) {
                    threadLocalPageLimit.setTotalCount(BeanUtilsEx.convert(pageLimitMap.get("totalCount"), Integer.class));
                    threadLocalPageLimit.setLimited(true);
                }
            }
        }
        
        return result;
    }
    
}
