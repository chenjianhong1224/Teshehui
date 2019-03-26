package com.cjh.teshehui.swing.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.session.BeidianSession;
import com.google.common.collect.Lists;

public class BeidianServiceImpl {

	public BeidianServiceImpl() {
		this.beidianSession = new BeidianSession();
	}

	public BeidianServiceImpl(BeidianSession beidianSession) {
		this.beidianSession = beidianSession;
	}
	
	public BeidianSession getBeidianSession() {
		return this.beidianSession;
	}

	private BeidianSession beidianSession;

	public String getAbr(List<String> paramList, String method)
			throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
		String params = "";
		for (String param : paramList) {
			params += param + "&";
		}
		params = DigestUtils.md5Hex(params.substring(0, params.length() - 1));
		String path = "/mroute.html?method=" + method;
		int time = Math.round((new Date().getTime() / 1000));
		String timeHex = Integer.toHexString(time);
		String src = "01" + "\n" + "POST" + "\n" + params + "\n" + path + "\n" + time;
		SecretKeySpec signingKey = new SecretKeySpec("ytU7vwqIx2UXQNsi".getBytes("utf-8"), "HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(signingKey);
		byte[] rawHmac = mac.doFinal(src.getBytes("utf-8"));
		String token = bytesToHexString(rawHmac);
		return "01" + token + timeHex;
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public ReturnResultBean getLoginSmsCode(String phoneNo) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取短信失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "api.beidian.com"));
		headerList.add(new BasicHeader(HttpHeaders.REFERER, "https://m.beidian.com/login/fast_login.html"));
		headerList.add(new BasicHeader("Origin", "https://m.beidian.com"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		HttpClient httpClient = HttpClients.custom().setDefaultHeaders(headerList).build();
		List<String> paramList = Lists.newArrayList();
		paramList.add("tel=" + phoneNo);
		paramList.add("key=quick_access");
		URI uri = null;
		try {
			String abr = getAbr(paramList, "beidian.user.code.send");
			String url = "https://api.beidian.com/mroute.html?method=beidian.user.code.send&_abr_=" + abr;
			uri = new URIBuilder(url).build();
		} catch (Exception e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取短信失败 " + e.getMessage());
			return resultBean;
		}
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("tel", phoneNo));
		params.add(new BasicNameValuePair("key", "quick_access"));
		HttpUriRequest httpUriRequest;
		try {
			httpUriRequest = RequestBuilder.post().setEntity(new UrlEncodedFormEntity(params, "UTF-8")).setUri(uri)
					.build();
			HttpClientContext httpClientContext = HttpClientContext.create();
			HttpResponse response = httpClient.execute(httpUriRequest, httpClientContext);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String content = EntityUtils.toString(entity);
					JSONObject jsonObject = JSON.parseObject(content);
					if (jsonObject.getBooleanValue("success") == true) {
						resultBean.setResultCode(0);
					} else {
						resultBean.setReturnMsg(resultBean.getReturnMsg() + jsonObject.getString("err_msg"));
					}
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("获取短信失败 " + e.getMessage());
		}
		return resultBean;
	}

	public ReturnResultBean login(String phoneNo, String smsCode) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("登录失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "api.beidian.com"));
		headerList.add(new BasicHeader(HttpHeaders.REFERER, "https://m.beidian.com/login/fast_login.html"));
		headerList.add(new BasicHeader("Origin", "https://m.beidian.com"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		CookieStore cookieStore = new BasicCookieStore();
		HttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).build();
		List<String> paramList = Lists.newArrayList();
		paramList.add("tel=" + phoneNo);
		paramList.add("code=" + smsCode);
		URI uri = null;
		try {
			String abr = getAbr(paramList, "beidian.auth.quick.web");
			String url = "https://api.beidian.com/mroute.html?method=beidian.auth.quick.web&_abr_=" + abr;
			uri = new URIBuilder(url).build();
		} catch (Exception e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("登录失败 " + e.getMessage());
			return resultBean;
		}
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("tel", phoneNo));
		params.add(new BasicNameValuePair("code", smsCode));
		HttpUriRequest httpUriRequest;
		try {
			httpUriRequest = RequestBuilder.post().setEntity(new UrlEncodedFormEntity(params, "UTF-8")).setUri(uri)
					.build();
			HttpClientContext httpClientContext = HttpClientContext.create();
			HttpResponse response = httpClient.execute(httpUriRequest, httpClientContext);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String content = EntityUtils.toString(entity);
					JSONObject jsonObject = JSON.parseObject(content);
					if (jsonObject.getBooleanValue("success") == true) {
						resultBean.setResultCode(0);
						beidianSession.setCookieStore(cookieStore);
					} else {
						resultBean.setReturnMsg(resultBean.getReturnMsg() + jsonObject.getString("err_msg"));
					}
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("登录失败 " + e.getMessage());
		}
		return null;
	}

}
