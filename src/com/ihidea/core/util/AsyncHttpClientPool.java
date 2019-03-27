package com.ihidea.core.util;


import com.ihidea.core.support.exception.ServiceException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

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



}
