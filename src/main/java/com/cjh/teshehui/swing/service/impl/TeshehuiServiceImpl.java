package com.cjh.teshehui.swing.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import com.cjh.teshehui.swing.bean.Proxys;
import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.bean.SkuBean;
import com.cjh.teshehui.swing.bean.UserBean;
import com.cjh.teshehui.swing.service.TeshehuiService;
import com.cjh.teshehui.swing.session.TeshehuiSession;
import com.google.common.collect.Lists;

@Service
public class TeshehuiServiceImpl implements TeshehuiService {

	@Autowired
	private TeshehuiSession teshehuiSession;

	Logger log = LoggerFactory.getLogger(TeshehuiService.class);

	private int noBodyCount = 0;

	private int useSessionCount = 0;

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
						userBean.setNickName(jsonObject.getString("nickName"));
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

	@Override
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

	@Override
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
		if (noBodyCount < 3) {
			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
					.setDefaultHeaders(headerList).build();
			System.out.println("匿名用户测试库存");
			noBodyCount++;
			useSessionCount = 0;
		} else {
			CookieStore cookieStore = teshehuiSession.getCookieStore();
			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
					.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).setDefaultHeaders(headerList)
					.build();
			String webguid = "";
			for (Cookie cookie : cookieStore.getCookies()) {
				if (cookie.getName().equals("webguid")) {
					webguid = cookie.getValue();
				}
			}
			System.out.println("会话" + teshehuiSession.nowSessionIndex + ", webguid = " + webguid + ",用户测试库存");
			useSessionCount++;
			if (useSessionCount == teshehuiSession.sessionNum) {
				noBodyCount = 0;
			}
		}

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

	@Override
	public ReturnResultBean createOrder(SkuBean bean) {
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
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
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
		params.add(new BasicNameValuePair("buyType", "2"));
		params.add(new BasicNameValuePair("deliveryType", "2"));
		params.add(new BasicNameValuePair("orderPayAmount", bean.getMemberPrice()));
		params.add(new BasicNameValuePair("payPoint", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][freeAmount]", "0"));
		params.add(new BasicNameValuePair("scheduleOrderList[0][freightAmount]", bean.getFreightMoney() + ""));
		params.add(
				new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][costPrice]", bean.getMemberPrice()));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][costTB]", "0"));
		Date now = new Date();
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][createTime]", now.getTime() + ""));
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][isPresent]", "0"));
		params.add(
				new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][payAmount]", bean.getMemberPrice()));
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
		params.add(new BasicNameValuePair("scheduleOrderList[0][productOrderList][0][userCouponCode]", ""));
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
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
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
							resultBean.setReturnObj(jsonObject.getJSONObject("data").getJSONObject("productInfo")
									.getInteger("freightMoney"));
							resultBean.setResultCode(0);
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

}
