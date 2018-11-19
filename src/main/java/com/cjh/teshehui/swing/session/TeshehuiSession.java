package com.cjh.teshehui.swing.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.bean.UserBean;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
@Scope("singleton")
public class TeshehuiSession {

	String[] cookieName = { "cookie1", "cookie2", "cookie3", "cookie4", "cookie5", "cookie6", "cookie7", "cookie8",
			"cookie9", "cookie10" };

	public int sessionNum = 0;

	public int nowSessionIndex = 0;

	public int count = 0;

	public void cleanSession() {
		userBean = null;
		cookieStore = null;
		for (int i = 0; i < sessionNum; i++) {
			File file = new File(cookieName[i]);
			file.delete();
		}
		sessionNum = 0;
		nowSessionIndex = 0;
	}

	private CookieStore cookieStore;

	private String auth;

	private UserBean userBean;

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public CookieStore getCookieStore() {
		return getLocalCookieStore();
	}

	public void setCookieStore(CookieStore cookieStore) throws IOException {
		saveCookieStore(cookieStore);
		this.cookieStore = cookieStore;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	private CookieStore getLocalCookieStore() {
		cookieStore = null;
		try {
			nowSessionIndex = count % sessionNum;
			cookieStore = readCookieStore(cookieName[nowSessionIndex]);
			count++;
		} catch (Exception e) {
			e.printStackTrace();
			cookieStore = new BasicCookieStore();
		}
		if (cookieStore == null) {
			cookieStore = new BasicCookieStore();
		}
		return cookieStore;
	}

	private void saveCookieStore(CookieStore cookieStore) throws IOException {
		FileOutputStream fs = new FileOutputStream(cookieName[sessionNum]);
		ObjectOutputStream os = new ObjectOutputStream(fs);
		os.writeObject(cookieStore);
		os.close();
		sessionNum++;
	}

	// 读取Cookie的序列化文件，读取后可以直接使用
	private CookieStore readCookieStore(String savePath) throws IOException, ClassNotFoundException {
		FileInputStream fs = new FileInputStream(savePath);
		ObjectInputStream ois = new ObjectInputStream(fs);
		CookieStore cookieStore = (CookieStore) ois.readObject();
		ois.close();
		return cookieStore;
	}

	public boolean checkSession() throws ClassNotFoundException, IOException {
		for (int i = 0; i < cookieName.length && i < 1; i++) {
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
			CloseableHttpClient httpClient = null;
			try {
				httpClient = HttpClients.custom()
						.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
						.setDefaultHeaders(headerList).setDefaultCookieStore(readCookieStore(cookieName[i]))
						.setDefaultHeaders(headerList).build();
			} catch (Exception e) {
				if (sessionNum < 0) {
					return false;
				} else {
					break;
				}
			}
			String url = "https://m.teshehui.com/user/get_user_info";
			URI uri = null;
			try {
				uri = new URIBuilder(url).build();
			} catch (URISyntaxException e) {
				return false;
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
						if (jsonObject.getInteger("status") != 200) {
							if (sessionNum < 0) {
								return false;
							} else {
								break;
							}
						}
					}
					sessionNum++;
				}
				httpClient.close();
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}
}
