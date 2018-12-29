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
import com.cjh.teshehui.swing.bean.Coupon;
import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.bean.UserBean;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
@Scope("singleton")
public class TeshehuiSession {

	private CookieStore cookieStore;

	private List<String> hadGotcouponBatchList = Lists.newArrayList();

	public void addHadGotcouponBatch(String batchCode) {
		hadGotcouponBatchList.add(batchCode);
	}

	public boolean hadGotVerify(String batchCode) {
		for (String code : hadGotcouponBatchList) {
			if (code.equals(batchCode)) {
				return true;
			}
		}
		return false;
	}

	private List<Coupon> couponList = Lists.newArrayList();

	private String auth;

	private UserBean userBean;

	public UserBean getUserBean() {
		return userBean;
	}

	public void addCoupon(Coupon coupon) {
		synchronized (TeshehuiSession.class) {
			for (Coupon myCoupon : couponList) {
				if (myCoupon.getCouponCode().equals(coupon.getCouponCode())) {
					return;
				}
			}
			couponList.add(coupon);
		}
	}

	public Coupon useCoupon() {
		synchronized (TeshehuiSession.class) {
			for (int i = 0; i < couponList.size(); i++) {
				if (couponList.get(i).isUseFlag() == false) {
					Coupon coupon = couponList.get(i);
					couponList.get(i).setUseFlag(true);
					couponList.remove(i);
					return coupon;
				}
			}
		}
		return null;
	}

	public void useCouponFail(Coupon coupon) {
		coupon.setUseFlag(false);
		couponList.add(coupon);
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public CookieStore getCookieStore() {
		return cookieStore;
	}

	public void setCookieStore(CookieStore cookieStore) throws IOException {
		this.cookieStore = cookieStore;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public List<Coupon> getCouponList() {
		return couponList;
	}

	public void setCouponList(List<Coupon> couponList) {
		this.couponList = couponList;
	}

	public List<String> getHadGotcouponBatchList() {
		return hadGotcouponBatchList;
	}

	public void setHadGotcouponBatchList(List<String> hadGotcouponBatchList) {
		this.hadGotcouponBatchList = hadGotcouponBatchList;
	}
}
