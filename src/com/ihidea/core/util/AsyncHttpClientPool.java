package com.ihidea.core.util;


import com.ihidea.core.support.exception.ServiceException;
import com.ning.http.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AsyncHttpClientPool {


    private final static Logger logger = LoggerFactory.getLogger(AsyncHttpClientPool.class);

    private static AsyncHttpClient asyncHttpClient = null;

    @PreDestroy
    private void destroy() {
        if (asyncHttpClient != null) {
            asyncHttpClient.close();
            logger.info("Lock asyncHttpClient停止");
        }
    }

    /**
     * 初始化连接池
     * @param maxConnections  最大连接数
     * @param connectTimeout  建立连接超时时间
     * @param requestTimeout  请求超时时间
     */
    public static void initPool(Integer maxConnections, Integer connectTimeout, Integer requestTimeout) {
        asyncHttpClient = new AsyncHttpClient(
                new AsyncHttpClientConfig.Builder().setMaxConnections(maxConnections).setConnectTimeout(connectTimeout).setRequestTimeout(requestTimeout).build());
    }


    private List<Param> paramMap2List(Map<String, String> paramMap) {
        List<Param> params = null;
        if(paramMap != null && paramMap.size() > 0) {
            params = new ArrayList<>();
            for(Map.Entry<String, String> entry : paramMap.entrySet()) {
                params.add(new Param(entry.getKey(), entry.getValue()));
            }
        }
        return params;
    }

    private AsyncHttpClient.BoundRequestBuilder selectRequestBuilder(String url, HttpMethod method) {
        switch (method) {
            case GET:
                return asyncHttpClient.prepareGet(url);
            case POST:
                return asyncHttpClient.preparePost(url);
            case PUT:
                return asyncHttpClient.preparePut(url);
            case DELETE:
                return asyncHttpClient.prepareDelete(url);
            default:
                throw new ServiceException("不支持的request类型");
        }
    }

    /**
     * post请求
     * @param url
     * @param paramMap
     * @param body
     * @return
     */
    public String requestSync(String url, Map<String, String> paramMap, String body, HttpMethod method) {

        List<Param> params = paramMap2List(paramMap);

        String result = null;

        try {
            logger.debug("[AsyncHttpClient]访问地址:{},参数:{},body:{}", new Object[]{url, JSONUtilsEx.serialize(paramMap), body});

            Response response = null;
            if(params != null) {
                response = selectRequestBuilder(url, method).addHeader("Content-Type", "application/json;charset=utf-8")
                        .addQueryParams(params).execute().get();
            } else {
                response = selectRequestBuilder(url, method).addHeader("Content-Type", "application/json;charset=utf-8")
                        .setBody(body).execute().get();
            }

            logger.debug("[AsyncHttpClient]访问状态:{},结果:{}", new Object[]{response.getStatusCode(), response.getResponseBody("UTF-8")});

            if (response.getStatusCode() == 200) {

                result = response.getResponseBody("UTF-8");

            } else {
                logger.error("[AsyncHttpClient]请求失败, 返回response状态码:" + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("[AsyncHttpClient]请求出现异常:" + e.getMessage(), e);
        }

        return result;
    }


    public void requestAsync(String url, Map<String, String> paramMap, String body, AsyncCompletionHandler<Response> handler, HttpMethod method) {

        List<Param> params = paramMap2List(paramMap);

        try {
            logger.debug("[AsyncHttpClient]访问地址:{},参数:{},body:{}", new Object[]{url, JSONUtilsEx.serialize(paramMap), body});

            Response response = null;
            if(params != null) {
                selectRequestBuilder(url, method).addHeader("Content-Type", "application/json;charset=utf-8")
                        .addQueryParams(params).execute(handler);
            } else {
                selectRequestBuilder(url, method).addHeader("Content-Type", "application/json;charset=utf-8")
                        .setBody(body).execute(handler);
            }
        } catch (Exception e) {
            logger.error("[AsyncHttpClient]请求出现异常:" + e.getMessage(), e);
        }

    }


}
