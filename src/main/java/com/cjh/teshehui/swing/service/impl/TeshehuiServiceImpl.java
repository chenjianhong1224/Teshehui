package com.cjh.teshehui.swing.service.impl;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
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
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjh.teshehui.swing.app.AppContext;
import com.cjh.teshehui.swing.app.AppFactory;
import com.cjh.teshehui.swing.bean.Coupon;
import com.cjh.teshehui.swing.bean.PasswdLoginBean;
import com.cjh.teshehui.swing.bean.Proxys;
import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.bean.SkuBean;
import com.cjh.teshehui.swing.bean.TaskResultStatistic;
import com.cjh.teshehui.swing.bean.UserBean;
import com.cjh.teshehui.swing.service.TeshehuiService;
import com.cjh.teshehui.swing.session.TeshehuiSession;
import com.google.common.collect.Lists;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class TeshehuiServiceImpl {

	public TeshehuiServiceImpl() {
		this.teshehuiSession = new TeshehuiSession();
	}

	public TeshehuiServiceImpl(TeshehuiSession teshehuiSession) {
		this.teshehuiSession = teshehuiSession;
	}

	private TeshehuiSession teshehuiSession;

	public TeshehuiSession getTeshehuiSession() {
		return this.teshehuiSession;
	}

	public static String DLLPATH = "yundamaAPI-x64.dll";

	public interface YDM extends Library {
		YDM INSTANCE = (YDM) Native.loadLibrary(DLLPATH, YDM.class);

		public void YDM_SetBaseAPI(String lpBaseAPI);

		public void YDM_SetAppInfo(int nAppId, String lpAppKey);

		public int YDM_Login(String lpUserName, String lpPassWord);

		public int YDM_DecodeByPath(String lpFilePath, int nCodeType, byte[] pCodeResult);

		public int YDM_UploadByPath(String lpFilePath, int nCodeType);

		public int YDM_EasyDecodeByPath(String lpUserName, String lpPassWord, int nAppId, String lpAppKey,
				String lpFilePath, int nCodeType, int nTimeOut, byte[] pCodeResult);

		public int YDM_DecodeByBytes(byte[] lpBuffer, int nNumberOfBytesToRead, int nCodeType, byte[] pCodeResult);

		public int YDM_UploadByBytes(byte[] lpBuffer, int nNumberOfBytesToRead, int nCodeType);

		public int YDM_EasyDecodeByBytes(String lpUserName, String lpPassWord, int nAppId, String lpAppKey,
				byte[] lpBuffer, int nNumberOfBytesToRead, int nCodeType, int nTimeOut, byte[] pCodeResult);

		public int YDM_GetResult(int nCaptchaId, byte[] pCodeResult);

		public int YDM_Report(int nCaptchaId, boolean bCorrect);

		public int YDM_EasyReport(String lpUserName, String lpPassWord, int nAppId, String lpAppKey, int nCaptchaId,
				boolean bCorrect);

		public int YDM_GetBalance(String lpUserName, String lpPassWord);

		public int YDM_EasyGetBalance(String lpUserName, String lpPassWord, int nAppId, String lpAppKey);

		public int YDM_SetTimeOut(int nTimeOut);

		public int YDM_Reg(String lpUserName, String lpPassWord, String lpEmail, String lpMobile, String lpQQUin);

		public int YDM_EasyReg(int nAppId, String lpAppKey, String lpUserName, String lpPassWord, String lpEmail,
				String lpMobile, String lpQQUin);

		public int YDM_Pay(String lpUserName, String lpPassWord, String lpCard);

		public int YDM_EasyPay(String lpUserName, String lpPassWord, long nAppId, String lpAppKey, String lpCard);
	}

	Logger log = LoggerFactory.getLogger(TeshehuiService.class);

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
						userBean.setNickName(jsonObject.getString("nickName"));
						resultBean.setReturnObj(teshehuiSession);
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

	public ReturnResultBean getAddress() {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取收货地址失败");
		String url = "https://m.teshehui.com/user/address/list";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			log.error("获取收货地址失败", e);
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
		CloseableHttpClient httpClient = HttpClients.custom()
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
					resultBean.setResultCode(0);
				}
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("获取收货地址失败 " + e.getMessage());
		}
		return resultBean;
	}

	public ReturnResultBean getUserInfo() {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取用户信息失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader(HttpHeaders.REFERER, "https://m.teshehui.com/user"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(teshehuiSession.getCookieStore())
				.setDefaultHeaders(headerList).build();
		String url = "https://m.teshehui.com/user/get_user_info";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取用户信息失败 " + e.getMessage());
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
					if (jsonObject.getInteger("status") == 200) {
						UserBean userBean = new UserBean();
						resultBean.setResultCode(0);
						userBean.setUserId(jsonObject.getString("userId"));
						userBean.setUserType(jsonObject.getInteger("userType"));
						userBean.setIsAgency(jsonObject.getInteger("isAgency"));
						userBean.setMobilePhone(jsonObject.getString("mobilePhone"));
						userBean.setNickName(jsonObject.getString("nickName"));
						List<Cookie> cookies = teshehuiSession.getCookieStore().getCookies();
						for (Cookie cookie : cookies) {
							if (cookie.getName().equals("skey")) {
								userBean.setToken(cookie.getValue());
							}
						}
						resultBean.setReturnObj(userBean);
						teshehuiSession.setAuth(userBean.getToken());
						teshehuiSession.setUserBean(userBean);
						resultBean.setResultCode(0);
					} else {
						resultBean.setReturnMsg(resultBean.getReturnMsg() + jsonObject.getInteger("message"));
					}
				}
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("获取用户信息失败 " + e.getMessage());
		}
		return resultBean;
	}

	public ReturnResultBean getCheckCode() {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("验证码失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = null;

		CookieStore cookieStore = teshehuiSession.getCookieStore();
		httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).setDefaultHeaders(headerList).build();
		URI uri = null;
		try {
			uri = new URIBuilder("https://m.teshehui.com/user/validate/get_check_code?source=1").build();
		} catch (Exception e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取库存失败 " + e.getMessage());
			log.error("获取库存失败 ", e);
			return resultBean;
		}
		try {
			HttpUriRequest httpUriRequest = RequestBuilder.get().setUri(uri).build();
			HttpClientContext httpClientContext = HttpClientContext.create();
			HttpResponse response = httpClient.execute(httpUriRequest, httpClientContext);
			HttpEntity entity = response.getEntity();
			if (response.getStatusLine().getStatusCode() == 200) {
				if (entity != null) {
					byte[] content = EntityUtils.toByteArray(entity);
					resultBean.setReturnObj(content);
					resultBean.setResultCode(0);
				} else {
					resultBean.setReturnMsg("获取人机验证码失败 ");
				}
			} else {
				log.error("获取人机验证码失败 http 返回码 {}", response.getStatusLine().getStatusCode());
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("获取人机验证码失败 " + e.getMessage());
			log.error("获取人机验证码失败 ", e);
		}
		return resultBean;
	}

	public ReturnResultBean getProductStockInfo(String url) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取库存失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = null;

		CookieStore cookieStore = teshehuiSession.getCookieStore();
		httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).setDefaultHeaders(headerList).build();

		// // 设置代理IP、端口、协议（请分别替换）
		// String[] addr = Proxys.getInstance().getProxys().split(":");
		// System.out.println("代理为=" + addr[0] + ":" + addr[1]);
		// HttpHost proxy = new HttpHost(addr[0], Integer.valueOf(addr[1]),
		// "http");
		// // 把代理设置到请求配置
		// RequestConfig defaultRequestConfig =
		// RequestConfig.custom().setProxy(proxy).build();
		// httpClient =
		// HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).setDefaultHeaders(headerList)
		// .build();
		URI uri = null;
		try {
			String[] tmps = url.split("/");
			String productCode = tmps[tmps.length - 1];
			uri = new URIBuilder("https://m.teshehui.com/goods/isshelves?productCode=" + productCode).build();
		} catch (Exception e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取库存失败 " + e.getMessage());
			log.error("获取库存失败 ", e);
			return resultBean;
		}
		try {
			HttpUriRequest httpUriRequest = RequestBuilder.get().setUri(uri).build();
			HttpClientContext httpClientContext = HttpClientContext.create();
			HttpResponse response = httpClient.execute(httpUriRequest, httpClientContext);
			HttpEntity entity = response.getEntity();
			if (response.getStatusLine().getStatusCode() == 200) {
				if (entity != null) {
					String content = EntityUtils.toString(entity);
					JSONObject jsonObject = JSON.parseObject(content);
					if (jsonObject.getInteger("status") == 200) {
						List skuList = Lists.newArrayList();
						JSONArray jsonArray = jsonObject.getJSONArray("skuList");
						for (int i = 0; i < jsonArray.size(); i++) {
							SkuBean skuBean = new SkuBean();
							skuBean.setSupplierId(jsonObject.getString("supplierId"));
							skuBean.setProductName(jsonObject.getString("productName"));
							skuBean.setSkuId(jsonArray.getJSONObject(i).getInteger("skuId"));
							skuBean.setProductCode(jsonArray.getJSONObject(i).getString("productCode"));
							skuBean.setSkuCode(jsonArray.getJSONObject(i).getString("skuCode"));
							skuBean.setSkuStock(jsonArray.getJSONObject(i).getInteger("skuStock"));
							skuBean.setLimitNum(jsonArray.getJSONObject(i).getInteger("limitNum"));
							skuBean.setStoreId(jsonObject.getString("storeId"));
							skuBean.setMemberPrice(jsonArray.getJSONObject(i).getString("memberPrice"));
							try {
								JSONObject attrObj = jsonArray.getJSONObject(i).getJSONObject("attr1");
								skuBean.setAttrValue(attrObj.getString("attrValue"));
							} catch (Exception e1) {

							}
							skuList.add(skuBean);
						}
						resultBean.setReturnObj(skuList);
						resultBean.setResultCode(0);
					} else {
						log.error("获取库存返回内容={}", content);
						resultBean.setReturnMsg("获取库存失败 " + jsonObject.getString("message"));
						if (jsonObject.getString("code").equals("20416014")) {
							resultBean.setResultCode(7777);
						}
					}
				}
			} else {
				log.error("获取库存失败 http 返回码 {}, 返回内容={}", response.getStatusLine().getStatusCode(),
						EntityUtils.toString(entity));
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("获取库存失败 " + e.getMessage());
			log.error("获取库存失败 ", e);
		}
		return resultBean;
	}

	public ReturnResultBean getOrderRealFreight(SkuBean bean, String num) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取实际运费失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = null;

		CookieStore cookieStore = teshehuiSession.getCookieStore();
		httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).setDefaultHeaders(headerList).build();
		URI uri = null;
		try {
			uri = new URIBuilder("https://m.teshehui.com/order/confirmorder")
					.addParameter("skuList",
							"[{\"productCode\": \"" + bean.getProductCode() + "\",\"productSkuCode\": \""
									+ bean.getSkuCode() + "\",\"quantity\": \"" + num + "\"}]")
					.addParameter("buyType", "2").build();
		} catch (Exception e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取实际运费失败 " + e.getMessage());
			log.error("获取实际运费失败 ", e);
			return resultBean;
		}
		try {
			HttpUriRequest httpUriRequest = RequestBuilder.get().setUri(uri).build();
			HttpClientContext httpClientContext = HttpClientContext.create();
			HttpResponse response = httpClient.execute(httpUriRequest, httpClientContext);
			HttpEntity entity = response.getEntity();
			if (response.getStatusLine().getStatusCode() == 200) {
				if (entity != null) {
					String content = EntityUtils.toString(entity);
					int index = content.indexOf("\"freightAmount\":\"");
					if (index >= 0) {
						int begin = content.indexOf("\"", index + 16);
						int end = content.indexOf("\"", begin + 1);
						String freight = content.substring(begin + 1, end);
						resultBean.setResultCode(0);
						resultBean.setReturnObj(freight);
					}
				}
			} else {
				log.error("获取库存失败 http 返回码 {}, 返回内容={}", response.getStatusLine().getStatusCode(),
						EntityUtils.toString(entity));
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("获取库存失败 " + e.getMessage());
			log.error("获取库存失败 ", e);
		}
		return resultBean;
	}

	public ReturnResultBean createOrder(SkuBean bean, String num) {
		num = bean.getOrderNum();
		ReturnResultBean returnFreightBean = getFreightAmount(bean);
		if (returnFreightBean.getResultCode() != 0) {
			returnFreightBean.setResultCode(-1);
			returnFreightBean.setReturnMsg("获取运费失败:" + returnFreightBean.getReturnMsg());
			return returnFreightBean;
		}
		if (bean.getForceFreightMoney() != null) {
			bean.setFreightMoney(Integer.valueOf(bean.getForceFreightMoney()));
		} else {
			returnFreightBean = getOrderRealFreight(bean, num);
			if (returnFreightBean.getResultCode() != 0) {
				returnFreightBean.setResultCode(-1);
				returnFreightBean.setReturnMsg("获取运费失败:" + returnFreightBean.getReturnMsg());
				return returnFreightBean;
			}
			bean.setFreightMoney(Integer.valueOf((String) returnFreightBean.getReturnObj()));
		}
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("下单失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(5000)
						.setConnectionRequestTimeout(3000).setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(teshehuiSession.getCookieStore())
				.setDefaultHeaders(headerList).build();
		String url = "https://m.teshehui.com/order/createorder";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("下单失败 " + e.getMessage());
			return resultBean;
		}
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("orderPayAmount",
				String.valueOf(Double.valueOf(bean.getMemberPrice()) * Integer.valueOf(num) + bean.getFreightMoney())));
		params.add(new BasicNameValuePair("userType", "0"));
		params.add(new BasicNameValuePair("userAddressId", teshehuiSession.getUserBean().getAddressId()));
		params.add(new BasicNameValuePair("payPoint", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][freeAmount]", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][freightAmount]", bean.getFreightMoney() + ""));
		params.add(new BasicNameValuePair("scheduleOrderList[0][storeId]", bean.getStoreId()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][userCouponCode]", ""));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productType]", "1"));
		params.add(
				new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][costPrice]", bean.getMemberPrice()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][costTB]", "0"));
		Date now = new Date();
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][createTime]", now.getTime() + ""));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productName]",
				bean.getProductName()));
		params.add(
				new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productSkuCode]", bean.getSkuCode()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productCode]",
				bean.getProductCode()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][quantity]", num));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][payAmount]",
				String.valueOf(Double.valueOf(bean.getMemberPrice()) * Integer.valueOf(num))));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][payPoint]", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][userActivityCode]", "A001303"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][isPresent]", "0"));
		params.add(new BasicNameValuePair("buyType", "2"));
		params.add(new BasicNameValuePair("tshAmount", "0"));
		params.add(new BasicNameValuePair("deliveryType", "2"));
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
						resultBean.setResultCode(0);
					} else {
						if ((jsonObject.getInteger("status") == 500)
								&& (jsonObject.getString("code").equals("20416014"))) {
							checkYanZ();
						}
						resultBean.setReturnMsg(jsonObject.getString("message"));
					}
				}
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("下单失败 " + e.getMessage());
		}
		return resultBean;
	}

	private ReturnResultBean getFreightAmount(SkuBean bean) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取运费失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
						.setConnectionRequestTimeout(5000).setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(teshehuiSession.getCookieStore())
				.setDefaultHeaders(headerList).build();
		String url = "https://m.teshehui.com/cgi/getProductFreightFee";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取运费失败 " + e.getMessage());
			return resultBean;
		}
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("productCode", bean.getProductCode()));
		params.add(new BasicNameValuePair("supplierId", bean.getSupplierId()));
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
						if (jsonObject.getJSONObject("data").getJSONObject("productInfo").getInteger("shelves") != 0) {
							JSONArray skuList = jsonObject.getJSONObject("data").getJSONObject("productInfo")
									.getJSONArray("skuList");
							if (skuList != null) {
								for (int j = 0; j < skuList.size(); j++) {
									if (bean.getSkuCode().equals(skuList.getJSONObject(j).getString("skuCode"))) {
										if (skuList.getJSONObject(j).getInteger("skuStock") > 0) {
											resultBean.setReturnObj(jsonObject.getJSONObject("data")
													.getJSONObject("productInfo").getInteger("freightMoney") / 100);
											resultBean.setResultCode(0);
										}
									}
								}
							}
						} else {
							resultBean.setReturnMsg("该商品还未上架");
						}
					} else {
						resultBean.setReturnMsg(jsonObject.getString("message"));
					}
				}
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("获取运费失败 " + e.getMessage());
		}
		return resultBean;
	}

	public ReturnResultBean checkCode(String verifyImgCode, String sourceCode) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("人机验证码校验失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = null;

		CookieStore cookieStore = teshehuiSession.getCookieStore();
		httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).setDefaultHeaders(headerList).build();
		String url = "https://m.teshehui.com/user/validate/check_code";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取短信失败 " + e.getMessage());
			return resultBean;
		}
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("code", verifyImgCode));
		params.add(new BasicNameValuePair("source", sourceCode));
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
					if ((jsonObject.getInteger("status") == 200) && jsonObject.getString("isPass").equals("true")) {
						resultBean.setResultCode(0);
					} else {
						resultBean.setReturnMsg(resultBean.getReturnMsg() + jsonObject.getInteger("message"));
					}
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("人机验证码校验失败" + e.getMessage());
		}
		return resultBean;
	}

	public ReturnResultBean getCoupon(String couponBatchCode) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取优惠券失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = null;

		CookieStore cookieStore = teshehuiSession.getCookieStore();
		httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).setDefaultHeaders(headerList).build();
		String url = "https://m.teshehui.com/cgi/getcoupon";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取优惠券失败" + e.getMessage());
			return resultBean;
		}
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("couponBatchCode", couponBatchCode));
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
					if ((jsonObject.getInteger("status") == 200)
							&& jsonObject.getString("isObtainSuccess").equals("1")) {
						Coupon coupon = new Coupon();
						coupon.setUseFlag(false);
						coupon.setCouponBatchCode(couponBatchCode);
						coupon.setCouponCode(jsonObject.getString("couponCode"));
						teshehuiSession.addCoupon(coupon);
						resultBean.setResultCode(0);
					} else {
						resultBean.setReturnMsg(resultBean.getReturnMsg() + jsonObject.getInteger("couponMessage"));
					}
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("获取优惠券失败" + e.getMessage());
		}
		return resultBean;
	}

	public ReturnResultBean getMyCoupon(String couponBatchCode) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取拥有的优惠券失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = null;

		CookieStore cookieStore = teshehuiSession.getCookieStore();
		httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).setDefaultHeaders(headerList).build();
		String url = "https://m.teshehui.com/cgi/getcouponlist";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取拥有的优惠券失败" + e.getMessage());
			return resultBean;
		}
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("pageNo", "1"));
		params.add(new BasicNameValuePair("pageSize", "10"));
		params.add(new BasicNameValuePair("status", "10"));
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
					if (jsonObject.getInteger("status") == 200) {
						JSONArray items = jsonObject.getJSONObject("pageModel").getJSONArray("items");
						boolean getSuccess = false;
						List<Coupon> couponCodes = Lists.newArrayList();
						if (items.size() > 0) {
							for (int i = 0; i < items.size(); i++) {
								if (items.getJSONObject(i).getString("couponBatchCode").equals(couponBatchCode)) {
									if (items.getJSONObject(i).getString("status").equals("10")) {
										Coupon coupon = new Coupon();
										coupon.setUseFlag(false);
										coupon.setCouponBatchCode(couponBatchCode);
										coupon.setCouponCode(items.getJSONObject(i).getString("couponCode"));
										couponCodes.add(coupon);
										getSuccess = true;
									}
								}
							}
						}
						if (getSuccess) {
							resultBean.setReturnObj(couponCodes);
							resultBean.setResultCode(0);
							return resultBean;
						}
					} else {
						resultBean.setReturnMsg("无优惠券");
					}
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("获取拥有的优惠券失败" + e.getMessage());
		}
		return resultBean;
	}

	private ReturnResultBean createOrderUseCoupon(SkuBean bean, Coupon coupon) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("下单失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(5000)
						.setConnectionRequestTimeout(3000).setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(teshehuiSession.getCookieStore())
				.setDefaultHeaders(headerList).build();
		String url = "https://m.teshehui.com/order/createorder";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("下单失败 " + e.getMessage());
			return resultBean;
		}
		List<NameValuePair> params = Lists.newArrayList();
		if (coupon != null) {
			params.add(new BasicNameValuePair("userCouponList[]", coupon.getCouponCode()));
			params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][userCouponCode]",
					coupon.getCouponCode()));
		}
		params.add(new BasicNameValuePair("buyType", "2"));
		params.add(new BasicNameValuePair("deliveryType", "2"));
		params.add(new BasicNameValuePair("payPoint", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][freeAmount]", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][freightAmount]", bean.getFreightMoney() + ""));
		params.add(
				new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][costPrice]", bean.getMemberPrice()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][costTB]", "0"));
		Date now = new Date();
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][createTime]", now.getTime() + ""));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][isPresent]", "0"));
		if (coupon == null) {
			params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][payAmount]",
					bean.getMemberPrice()));
			params.add(new BasicNameValuePair("orderPayAmount", bean.getMemberPrice()));
		} else {
			params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][payAmount]",
					(Long.valueOf(bean.getMemberPrice()) - 500) + ""));
			params.add(new BasicNameValuePair("orderPayAmount", (Long.valueOf(bean.getMemberPrice()) - 500) + ""));

		}
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][payPoint]", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productCode]",
				bean.getProductCode()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productName]",
				bean.getProductName()));
		params.add(
				new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productSkuCode]", bean.getSkuCode()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productType]", "1"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][quantity]", "1"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][userActivityCode]", "A001303"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][storeId]", bean.getStoreId()));
		params.add(new BasicNameValuePair("tshAmount", "0"));
		params.add(new BasicNameValuePair("userAddressId", teshehuiSession.getUserBean().getAddressId()));
		params.add(new BasicNameValuePair("userType", "0"));
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
						resultBean.setResultCode(0);
					} else {
						if ((jsonObject.getInteger("status") == 500)
								&& (jsonObject.getString("code").equals("20416014"))) {
							checkYanZ();
						}
						resultBean.setReturnMsg(jsonObject.getString("message"));
					}
				}
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("下单失败 " + e.getMessage());
		}
		return resultBean;
	}

	public ReturnResultBean createOrderUseMyCoupon(SkuBean bean) {
		ReturnResultBean returnFreightBean = getFreightAmount(bean);
		if (returnFreightBean.getResultCode() != 0) {
			returnFreightBean.setResultCode(-1);
			returnFreightBean.setReturnMsg("获取运费失败:" + returnFreightBean.getReturnMsg());
			return returnFreightBean;
		}
		bean.setFreightMoney((Integer) returnFreightBean.getReturnObj());
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("下单失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(5000)
						.setConnectionRequestTimeout(3000).setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(teshehuiSession.getCookieStore())
				.setDefaultHeaders(headerList).build();
		String url = "https://m.teshehui.com/order/createorder";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("下单失败 " + e.getMessage());
			return resultBean;
		}
		Coupon coupon = teshehuiSession.useCoupon();
		List<NameValuePair> params = Lists.newArrayList();
		if (coupon != null) {
			params.add(new BasicNameValuePair("userCouponList[]", coupon.getCouponCode()));
			params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][userCouponCode]",
					coupon.getCouponCode()));
		}
		params.add(new BasicNameValuePair("buyType", "2"));
		params.add(new BasicNameValuePair("deliveryType", "2"));
		params.add(new BasicNameValuePair("payPoint", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][freeAmount]", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][freightAmount]", bean.getFreightMoney() + ""));
		params.add(
				new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][costPrice]", bean.getMemberPrice()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][costTB]", "0"));
		Date now = new Date();
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][createTime]", now.getTime() + ""));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][isPresent]", "0"));
		if (coupon == null) {
			params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][payAmount]",
					bean.getMemberPrice()));
			params.add(new BasicNameValuePair("orderPayAmount", bean.getMemberPrice()));
		} else {
			params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][payAmount]",
					(Long.valueOf(bean.getMemberPrice()) - 500) + ""));
			params.add(new BasicNameValuePair("orderPayAmount", (Long.valueOf(bean.getMemberPrice()) - 500) + ""));

		}
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][payPoint]", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productCode]",
				bean.getProductCode()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productName]",
				bean.getProductName()));
		params.add(
				new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productSkuCode]", bean.getSkuCode()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][productType]", "1"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][quantity]", "1"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][userActivityCode]", "A001303"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][storeId]", bean.getStoreId()));
		params.add(new BasicNameValuePair("tshAmount", "0"));
		params.add(new BasicNameValuePair("userAddressId", teshehuiSession.getUserBean().getAddressId()));
		params.add(new BasicNameValuePair("userType", "0"));
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
						resultBean.setResultCode(0);
					} else {
						if ((jsonObject.getInteger("status") == 500)
								&& (jsonObject.getString("code").equals("20416014"))) {
							checkYanZ();
							resultBean = createOrderUseCoupon(bean, coupon);
							if (resultBean.getResultCode() == 0) {
								httpClient.close();
								return resultBean;
							}
						}
						resultBean.setReturnMsg(jsonObject.getString("message"));
						teshehuiSession.useCouponFail(coupon);
					}
				}
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("下单失败 " + e.getMessage());
		}
		return resultBean;
	}

	public ReturnResultBean getPromotioninf(String skuCode) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取促销信息失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = null;

		CookieStore cookieStore = teshehuiSession.getCookieStore();
		httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).setDefaultHeaders(headerList).build();
		String url = "https://m.teshehui.com/cgi/getpromotioninf";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取促销信息失败" + e.getMessage());
			return resultBean;
		}
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("promotionGoodsType", "40"));
		params.add(new BasicNameValuePair("promotionType", "10"));
		params.add(new BasicNameValuePair("promotionGoodsTypeFlag", skuCode));
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
					if (jsonObject.getInteger("status") == 200) {
						JSONArray array = jsonObject.getJSONArray("couponBatchArray");
						if (array != null && array.size() > 0) {
							resultBean.setResultCode(0);
							resultBean.setReturnObj(array.getJSONObject(0).getString("code"));
						} else {
							resultBean.setResultCode(8888);
						}
					} else {
						resultBean.setReturnMsg("");
					}
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("获取促销信息失败" + e.getMessage());
		}
		return resultBean;
	}

	public ReturnResultBean loginByPasswd(String phoneNo, String passwd) {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("登录失败");
		PasswdLoginBean bean = new PasswdLoginBean();
		bean.setNumber(phoneNo);
		bean.setPassword(DigestUtils.md5Hex(passwd));
		bean.setAppType("tsh");
		bean.setBusinessType("01");
		bean.setClientType("ANDROID");
		bean.setClientVersion("7.1.7");
		bean.setDitchCode("xiaomi");
		bean.setNetwork("WIFI");
		bean.setRequestClassName("com.teshehui.portal.client.user.request.PortalUserLoginRequest");
		Date now = new Date();
		bean.setTimestamp(now.getTime());
		bean.setUrl("/user/userLogin");
		bean.setVersion("1.0.0");
		AppFactory factory = new AppFactory();
		AppContext context = factory.getNewApp();
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "Keep-Alive"));
		headerList.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "portal-api.teshehui.com"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT, "okhttp/3.3.1"));
		CookieStore cookieStore = new BasicCookieStore();
		HttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).build();
		String url = "https://portal-api.teshehui.com/client";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("登录失败 " + e.getMessage());
		}
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("xuid", context.getXuid()));
		params.add(new BasicNameValuePair("qd", "xiaomi"));
		params.add(new BasicNameValuePair("plf", "2"));
		params.add(new BasicNameValuePair("av", "98"));
		System.out.println(JSON.toJSONString(bean));
		params.add(new BasicNameValuePair("requestObj", JSON.toJSONString(bean)));
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
						String token = jsonObject.getString("token");
						BasicClientCookie cookie = new BasicClientCookie("skey", token);
						cookie.setDomain("teshehui.com");
						cookie.setVersion(0);
						cookie.setPath("/");
						cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "teshehui.com");
						cookie.setAttribute(ClientCookie.PATH_ATTR, "/");
						cookie.setAttribute("skey", token);
						cookieStore.addCookie(cookie);
						BasicClientCookie cookie2 = new BasicClientCookie("userid", jsonObject.getString("userId"));
						cookie2.setDomain("teshehui.com");
						cookie2.setPath("/");
						cookie2.setVersion(0);
						cookie2.setAttribute(ClientCookie.DOMAIN_ATTR, "teshehui.com");
						cookie2.setAttribute(ClientCookie.PATH_ATTR, "/");
						cookie2.setAttribute("userid", jsonObject.getString("userId"));
						cookieStore.addCookie(cookie2);
						teshehuiSession.setCookieStore(cookieStore);
						resultBean.setResultCode(0);
						UserBean userBean = new UserBean();
						userBean.setUserId(jsonObject.getString("userId"));
						userBean.setUserType(jsonObject.getInteger("userType"));
						userBean.setIsAgency(jsonObject.getInteger("isAgency"));
						userBean.setMobilePhone(jsonObject.getString("mobilePhone"));
						userBean.setToken(jsonObject.getString("token"));
						userBean.setNickName(jsonObject.getString("nickName"));
						teshehuiSession.setAuth(userBean.getToken());
						teshehuiSession.setUserBean(userBean);
						resultBean.setReturnObj(teshehuiSession);
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

	public ReturnResultBean checkYanZ() {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("验证码失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = null;

		CookieStore cookieStore = teshehuiSession.getCookieStore();
		httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).setDefaultHeaders(headerList).build();
		URI uri = null;
		try {
			uri = new URIBuilder("https://m.teshehui.com/user/validate/get_check_code?source=3").build();
		} catch (Exception e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取验证码失败 " + e.getMessage());
			log.error("获取验证码失败 ", e);
			return resultBean;
		}
		try {
			HttpUriRequest httpUriRequest = RequestBuilder.get().setUri(uri).build();
			HttpClientContext httpClientContext = HttpClientContext.create();
			HttpResponse response = httpClient.execute(httpUriRequest, httpClientContext);
			HttpEntity entity = response.getEntity();
			if (response.getStatusLine().getStatusCode() == 200) {
				if (entity != null) {
					byte[] content = EntityUtils.toByteArray(entity);
					byte[] byteResult = new byte[30];
					int cid = YDM.INSTANCE.YDM_DecodeByBytes(content, content.length, 1004, byteResult);
					if (cid > 0) {
						String strResult = new String(byteResult, "UTF-8").trim();
						resultBean = checkCode(strResult, "3");
						resultBean.setReturnObj(content);
						resultBean.setResultCode(0);
					}
				} else {
					resultBean.setReturnMsg("获取人机验证码失败 ");
				}
			} else {
				log.error("获取人机验证码失败 http 返回码 {}", response.getStatusLine().getStatusCode());
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("获取人机验证码失败 " + e.getMessage());
			log.error("获取人机验证码失败 ", e);
		}
		return resultBean;
	}

	static public ReturnResultBean loginYDM(String userName, String passwd) {
		ReturnResultBean returnBean = new ReturnResultBean();
		returnBean.setResultCode(-1);
		int appid = 6504;
		String appkey = "ec22306220a76668b75a32bab669ff43";
		int uid = 0;
		YDM.INSTANCE.YDM_SetAppInfo(appid, appkey); // 设置软件ID和密钥
		uid = YDM.INSTANCE.YDM_Login(userName, passwd); // 登陆到云打码
		if (uid > 0) {
			returnBean.setResultCode(0);
		} else {
			returnBean.setReturnMsg("登录失败，错误代码为" + uid);
		}
		return returnBean;
	}

	public ReturnResultBean checkNonPaymentOrder() {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("无待付款订单");
		String url = "https://m.teshehui.com/order/getorderlist?pagesize=10&type=1&pageno=1";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			log.error("获取待付款订单失败", e);
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
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CloseableHttpClient httpClient = HttpClients.custom()
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
					JSONObject jsonObject = JSON.parseObject(content);
					if ((jsonObject.getInteger("status") == 200)) {
						if (jsonObject.getJSONObject("data2") != null) {
							Integer totalCount = jsonObject.getJSONObject("data2").getInteger("totalCount");
							if (totalCount != null) {
								resultBean.setResultCode(0);
							}
						}
					}
				}
			}
			httpClient.close();
		} catch (Exception e) {
			resultBean.setReturnMsg("获取待付款订单失败 " + e.getMessage());
		}
		return resultBean;
	}
}
