package com.cjh.teshehui.swing.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cjh.teshehui.swing.bean.UserBean;
import com.google.common.collect.Maps;

@Component
@Scope("singleton")
public class TeshehuiSession {

	public void cleanSession() {
		userBean = null;
		cookieStore = null;
		File file = new File("cookie");
		file.delete();
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
			cookieStore = readCookieStore("cookie");
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
		FileOutputStream fs = new FileOutputStream("cookie");
		ObjectOutputStream os = new ObjectOutputStream(fs);
		os.writeObject(cookieStore);
		os.close();
	}

	// 读取Cookie的序列化文件，读取后可以直接使用
	private CookieStore readCookieStore(String savePath) throws IOException, ClassNotFoundException {
		FileInputStream fs = new FileInputStream(savePath);
		ObjectInputStream ois = new ObjectInputStream(fs);
		CookieStore cookieStore = (CookieStore) ois.readObject();
		ois.close();
		return cookieStore;
	}
}
