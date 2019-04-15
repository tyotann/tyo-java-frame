package com.ihidea.core.util;


import com.ihidea.core.support.exception.ServiceException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Param;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public static AsyncHttpClient getAsyncHttpClient() {
        if(asyncHttpClient == null) {
            throw new ServiceException("AsyncHttpClient连接池未初始化");
        }
        return asyncHttpClient;
    }


    /**
     * post请求
     * @param url
     * @param paramMap
     * @param body
     * @return
     */
    public String post(String url, Map<String, String> paramMap, String body) {

        List<Param> params = null;
        if(paramMap != null && paramMap.size() > 0) {
            params = new ArrayList<>();
            for(Map.Entry<String, String> entry : paramMap.entrySet()) {
                params.add(new Param(entry.getKey(), entry.getValue()));
            }
        }

        String result = null;

        try {
            logger.debug("[AsyncHttpClient]访问地址:{},参数:{},body:{}", new Object[]{url, JSONUtilsEx.serialize(paramMap), body});

            Response response = null;
            if(params != null) {
                response = asyncHttpClient.preparePost(url).addHeader("Content-Type", "application/json;charset=utf-8")
                        .addQueryParams(params).execute().get();
            } else {
                response = asyncHttpClient.preparePost(url).addHeader("Content-Type", "application/json;charset=utf-8")
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


}
