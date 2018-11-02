package com.cjh.teshehui.swing.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

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
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.bean.UserBean;
import com.cjh.teshehui.swing.service.TeshehuiService;
import com.cjh.teshehui.swing.session.TeshehuiSession;
import com.google.common.collect.Lists;

@Service
public class TeshehuiServiceImpl implements TeshehuiService {

	@Autowired
	private TeshehuiSession teshehuiSession;

	@Override
	public ReturnResultBean getLoginSmsCode(String phoneNo, String verifyImgCode) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取短信失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader(HttpHeaders.REFERER,
				"https://m.teshehui.com/user/login?redirect=%2Fuser&invitationCode="));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		HttpClient httpClient = HttpClients.custom().setDefaultHeaders(headerList).build();
		String url = "https://m.teshehui.com/user/get_verification_code?phone_mob=" + phoneNo + "&type=1&code="
				+ verifyImgCode;
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取短信失败 " + e.getMessage());
			return resultBean;
		}
		HttpUriRequest httpUriRequest = RequestBuilder.get().setUri(uri).build();
		HttpClientContext httpClientContext = HttpClientContext.create();
		try {
			HttpResponse response = httpClient.execute(httpUriRequest, httpClientContext);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String content = EntityUtils.toString(entity);
					JSONObject jsonObject = JSON.parseObject(content);
					if ((jsonObject.getInteger("status") == 200) && jsonObject.getString("data").equals("true")) {
						resultBean.setResultCode(0);
					} else {
						resultBean.setReturnMsg(resultBean.getReturnMsg() + jsonObject.getInteger("message"));
					}
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("获取短信失败 " + e.getMessage());
		}
		return resultBean;
	}

	@Override
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
		headerList.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader(HttpHeaders.REFERER,
				"https://m.teshehui.com/user/login?redirect=%2Fuser&invitationCode="));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CookieStore cookieStore = new BasicCookieStore();
		HttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).build();
		String url = "https://m.teshehui.com/user/login";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("登录失败 " + e.getMessage());
			return resultBean;
		}
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("checkCode", smsCode));
		params.add(new BasicNameValuePair("invitationCode", ""));
		params.add(new BasicNameValuePair("phoneMob", phoneNo));
		params.add(new BasicNameValuePair("registerInviter", ""));
		try {
			HttpUriRequest httpUriRequest;
			httpUriRequest = RequestBuilder.post().setEntity(new UrlEncodedFormEntity(params, "UTF-8")).setUri(uri)
					.build();
			HttpClientContext httpClientContext = HttpClientContext.create();
			HttpResponse response = httpClient.execute(httpUriRequest, httpClientContext);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String content = EntityUtils.toString(entity);
					JSONObject jsonObject = JSON.parseObject(content);
					if ((jsonObject.getInteger("status") == 200)) {
						UserBean userBean = new UserBean();
						resultBean.setResultCode(0);
						userBean.setUserId(jsonObject.getString("userId"));
						userBean.setUserType(jsonObject.getInteger("userType"));
						userBean.setIsAgency(jsonObject.getInteger("isAgency"));
						userBean.setMobilePhone(jsonObject.getString("mobilePhone"));
						userBean.setToken(jsonObject.getString("token"));
						resultBean.setReturnObj(userBean);
						teshehuiSession.setAuth(userBean.getToken());
						teshehuiSession.setUserBean(userBean);
						teshehuiSession.setCookieStore(cookieStore);
					} else {
						resultBean.setReturnMsg(jsonObject.getString("dialogMessage"));
					}
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("登录失败 " + e.getMessage());
		}
		return resultBean;
	}

	@Override
	public ReturnResultBean getAddress() {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取收货地址失败");
		String url = "https://m.teshehui.com/user/address/list";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg(resultBean.getReturnMsg() + e.getMessage());
			return resultBean;
		}
		List<Header> headerList = Lists.newArrayList();
		headerList.add(
				new BasicHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader(HttpHeaders.REFERER, "https://m.teshehui.com/user"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		HttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(teshehuiSession.getCookieStore()).build();
		HttpUriRequest httpUriRequest = RequestBuilder.get().setUri(uri).build();
		HttpClientContext httpClientContext = HttpClientContext.create();
		try {
			HttpResponse response = httpClient.execute(httpUriRequest, httpClientContext);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String content = EntityUtils.toString(entity);
					System.out.println(content);
					UserBean userBean = teshehuiSession.getUserBean();
					String beginString = "<span class=\"default\">[默认地址] </span>";
					String addressDetail = content.substring(content.indexOf(beginString) + beginString.length());
					addressDetail = addressDetail.substring(0, addressDetail.indexOf("\n"));
					userBean.setAddressDetail(addressDetail);
					beginString = "<a class=\"info\" data-addrid=";
					String addressId = content.substring(content.indexOf(beginString) + beginString.length());
					addressId = addressId.substring(0, addressId.indexOf(">"));
					userBean.setAddressId(addressId);
					teshehuiSession.setUserBean(userBean);
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("获取收货地址失败 " + e.getMessage());
		}
		return resultBean;
	}

}
