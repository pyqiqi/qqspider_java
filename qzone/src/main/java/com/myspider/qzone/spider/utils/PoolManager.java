package com.myspider.qzone.spider.utils;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;

public class PoolManager {
    private static final Logger LOG = Logger.getLogger(PoolManager.class);
    private static final String UserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36";

    //使用实例，在定义的新类当中加入getInstance()方法，返回值即为该类的一个对象，此方法常常应用在Java的单例模式当中
    private static PoolManager poolManager = null;
    public static PoolingHttpClientConnectionManager connManager = null;
    private static RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).setSocketTimeout(5000).setConnectTimeout(5000).setConnectionRequestTimeout(5000).build();


    public static synchronized PoolManager getInstance(){
        if(poolManager == null){
            connManager = new PoolingHttpClientConnectionManager();
        }
        return poolManager;
    }

    //获取一个连接
    public static CloseableHttpClient getHttpClient(BasicCookieStore cookieStore){
        if(connManager == null){
            getInstance();
        }
        CloseableHttpClient client = null;
        try {
            LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();
            client = HttpClients.custom().setConnectionManager(connManager).setDefaultRequestConfig(config).setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(5000).build())
                    .setRedirectStrategy(redirectStrategy).setUserAgent(UserAgent).setDefaultCookieStore(cookieStore).build();

        }catch (Exception e){
            LOG.error("开启一个连接错误："+e);
        }
        return client;
    }

}
