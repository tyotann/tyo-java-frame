package com.ihidea.core.util;


import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class AsyncHttpClientPool {


    private final static Logger logger = LoggerFactory.getLogger(AsyncHttpClientPool.class);

    private static AsyncHttpClient asyncHttpClient = new AsyncHttpClient(
            new AsyncHttpClientConfig.Builder().setMaxConnections(100).setConnectTimeout(20000).setRequestTimeout(20000).build());

    @PreDestroy
    private void destroy() {
        if (asyncHttpClient != null) {
            asyncHttpClient.close();
            logger.info("Lock asyncHttpClient停止");
        }
    }

    public static AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClient;
    }

}
