package com.cjh.teshehui.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.google.common.collect.Lists;

@SpringBootTest
public class TeshehuiSessionTest {

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
