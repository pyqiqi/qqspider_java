package com.myspider.qzone.spider.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FetchUtils {
	
	private static final Logger log = LoggerFactory.getLogger(FetchUtils.class);
	/**
	 * post请求,使用json格式传参
	 * 
	 * @param url
	 * @param headers
	 * @param data
	 * @return
	 */
	public static String postJson(CloseableHttpClient client, String url, Map<String, Object> headers, String data) {
		log.info("post url:" + url + " " + System.currentTimeMillis());
		String content = null;
		HttpRequest request = new HttpPost(url);
		if (headers != null && !headers.isEmpty()) {
			request = setHeaders(headers, request);
		}
		HttpPost post = (HttpPost) request;
		post.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			content = client.execute(post, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			post.releaseConnection();
		}
		log.info("end post url:" + url + " " + System.currentTimeMillis());
		return content;
	}
	public static String get(CloseableHttpClient client, String url){
		
		return get(client, url, "UTF-8");
	}
	public static String get(CloseableHttpClient client, String url, String charset){
		return get(client, url, charset, null);
	}
	public static String get(CloseableHttpClient client, String url, String charset,Map<String, String> params){
		return get(client, url, charset, params,null);
	}
	/**
	 * get请求
	 * 
	 * @return
	 */
	public static String get(CloseableHttpClient client, String url, String charset, Map<String, String> params,Map<String, String> headers) {
		log.info("get url:" + url + " " + System.currentTimeMillis());
		String content = null;
		List<NameValuePair> qparams = getParamsList(params);
		if (qparams != null && qparams.size() > 0) {
			String formatParams = URLEncodedUtils.format(qparams, charset);
			url = (url.indexOf("?")) < 0 ? (url + "?" + formatParams)
					: (url.substring(0, url.indexOf("?") + 1) + formatParams);
		}
		HttpRequest request = new HttpGet(url);
		HttpGet get = (HttpGet) request;
		try {
			if(headers != null){
				get.setHeaders(assembHead(headers));
			}
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			content = client.execute(get, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			get.releaseConnection();
		}
		log.info("end get url:" + url + " " + System.currentTimeMillis());
		return content;
	}

	/**
	 * post请求
	 * 
	 * @param url
	 * @param headers
	 * @param inputs
	 * @return
	 */
	public static String post(CloseableHttpClient client, String url, Map<String, Object> headers,
			Map<String, String> inputs) {
		log.info("start post url:" + url + " " + System.currentTimeMillis());
		String content = null;
		HttpRequest request = new HttpPost(url);
		if (headers != null && !headers.isEmpty()) {
			request = setHeaders(headers, request);
		}
		HttpPost post = (HttpPost) request;
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (String key : inputs.keySet()) {
			nameValuePairs.add(new BasicNameValuePair(key, inputs.get(key)));
		}
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8")); 
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			content = client.execute(post, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			post.releaseConnection();
		}
		log.info("end post url:" + url + " " + System.currentTimeMillis());
		return content;
	}

	/**
	 * 根据地址下载图片获取图片数据流
	 * @param strUrl 网络连接地址
	 * @return
	 */
	public static byte[] getImageFromNetByUrl(String strUrl){
		try {
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			conn.setReadTimeout(5*1000);
			InputStream inStream = conn.getInputStream();//通过输入流获取图片数据
			byte[] btImg = readInputStream(inStream);//得到图片的二进制数据
			return btImg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 从输入流中获取数据
	 * @param inStream 输入流
	 * @return
	 * @throws Exception
	 */
	public static byte[] readInputStream(InputStream inStream) throws Exception{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while( (len=inStream.read(buffer)) != -1 ){
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}

	/**
	 * 执行文件下载
	 * 
	 * @param httpClient
	 *            HttpClient客户端实例，传入null会自动创建一个
	 * @param remoteFileUrl
	 *            远程下载文件地址
	 * @param localFilePath
	 *            本地存储文件地址
	 * @param charset
	 *            请求编码，默认UTF-8
	 */
	public static boolean executeDownloadFile(CloseableHttpClient client, String remoteFileUrl, String localFilePath) {
		boolean isRequest = false;
		CloseableHttpResponse response = null;
		InputStream in = null;
		FileOutputStream fout = null;
		HttpRequest request = new HttpGet(remoteFileUrl);
		HttpGet get = (HttpGet) request;
		try {
			response = client.execute(get);
			HttpEntity entity = response.getEntity();
			if (entity == null) {
				return isRequest;
			}
			in = entity.getContent();
			File file = new File(localFilePath);
			fout = new FileOutputStream(file);
			int l = -1;
			byte[] tmp = new byte[1024];
			while ((l = in.read(tmp)) != -1) {
				fout.write(tmp, 0, l);
			}
			// 将文件输出到本地
			fout.flush();
			EntityUtils.consume(entity);
			isRequest = true;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭低层流。
			if (fout != null) {
				try {
					fout.close();
				} catch (Exception e) {
				}
			}
			if (response != null) {
				try {
					response.close();
				} catch (Exception e) {
				}
			}
			get.releaseConnection();
		}
		return isRequest;
	}

	/**
	 * 设置请求头部
	 * 
	 * @param headers
	 * @param request
	 * @return
	 */
	private static HttpRequest setHeaders(Map<String, Object> headers, HttpRequest request) {
		for (Map.Entry entry : headers.entrySet()) {
			if (!entry.getKey().equals("Cookie")) {
				request.addHeader((String) entry.getKey(), (String) entry.getValue());
			} else {
				Map<String, Object> Cookies = (Map<String, Object>) entry.getValue();
				for (Map.Entry entry1 : Cookies.entrySet()) {
					request.addHeader(new BasicHeader("Cookie", (String) entry1.getValue()));
				}
			}
		}
		return request;
	}

	public static List<NameValuePair> getParamsList(Map<String, String> paramsMap) {
		if (paramsMap == null || paramsMap.size() == 0) {
			return null;
		}
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> map : paramsMap.entrySet()) {
			params.add(new BasicNameValuePair(map.getKey(), map.getValue()));
		}
		return params;
	}
	
	public static String getCookie(String key,BasicCookieStore cookieStore){
		List<Cookie> cookiess = cookieStore.getCookies();
		System.out.println("++++++++++++++++++++getCookies++++++++++++++++++++++++++");
		System.out.println(cookiess);
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		String v = null;
        for (int i = 0; i < cookiess.size(); i++) {
        	if(cookiess.get(i).getName().equals(key)){
        		v = cookiess.get(i).getValue();
        	}
        	
        }
        return v;
	}
	
	/**
	 * 组装Header信息
	 * @param headers
	 * @return Header[]
	 */
	public static Header[] assembHead(Map<String, String> headers){
		Header[] allHeader = new BasicHeader[headers.size()];
		int i = 0;
		for (Map.Entry<String, String> stringStringEntry : headers.entrySet()) {
            allHeader[i] = new BasicHeader(stringStringEntry.getKey(), stringStringEntry.getValue());
            i++;
        }
		return allHeader;
	}

}
