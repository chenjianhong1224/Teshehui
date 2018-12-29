package com.cjh.teshehui.test;

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
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjh.teshehui.swing.app.AppContext;
import com.cjh.teshehui.swing.app.AppFactory;
import com.cjh.teshehui.swing.bean.PasswdLoginBean;
import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.bean.UserBean;
import com.google.common.collect.Lists;

@SpringBootTest
public class TeshehuiSessionTest {

	@Test
	public void LogoinTest() {
		String number = "18577787720";
		String passwd = "824682k11";
		PasswdLoginBean bean = new PasswdLoginBean();
		bean.setNumber(number);
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
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("登录失败");
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
						cookie.setPath("/");
						cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "teshehui.com");
						cookie.setAttribute(ClientCookie.PATH_ATTR, "/");
						cookie.setAttribute("skey", token);
					} else {
					}
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("登录失败 " + e.getMessage());
		}
	}

	@Test
	public void sessionTest() {
		ReturnResultBean resultBean = new ReturnResultBean();
		resultBean.setResultCode(-1);
		resultBean.setReturnMsg("获取session失败");
		List<Header> headerList = Lists.newArrayList();
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
		headerList.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE,
				"zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
		headerList.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		headerList.add(new BasicHeader(HttpHeaders.HOST, "m.teshehui.com"));
		headerList.add(new BasicHeader(HttpHeaders.REFERER, "https://m.teshehui.com/user/setting"));
		headerList.add(new BasicHeader("TE", "Trailers"));
		headerList.add(new BasicHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0"));
		headerList.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
		CookieStore cookieStore = new BasicCookieStore();
		HttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultHeaders(headerList).setDefaultCookieStore(cookieStore).build();
		String url = "https://m.teshehui.com/user/login?normal=true";
		URI uri = null;
		try {
			uri = new URIBuilder(url).build();
		} catch (URISyntaxException e) {
			resultBean.setResultCode(-1);
			resultBean.setReturnMsg("获取session失败 " + e.getMessage());
			return;
		}
		HttpUriRequest httpUriRequest = RequestBuilder.get().setUri(uri).build();
		HttpClientContext httpClientContext = HttpClientContext.create();
		try {
			HttpResponse response = httpClient.execute(httpUriRequest, httpClientContext);
			if (response.getStatusLine().getStatusCode() == 200) {
				List<Cookie> cookies = cookieStore.getCookies();
				for (Cookie cookie : cookies) {
					System.out.println(String.format("domain={%s}, path={%s}, name={%s}, value={%s}",
							cookie.getDomain(), cookie.getPath(), cookie.getName(), cookie.getValue()));
				}
			}
		} catch (Exception e) {
			resultBean.setReturnMsg("获取session失败 " + e.getMessage());
		}
		return;
	}

}
