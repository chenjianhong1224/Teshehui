package com.cjh.teshehui.swing.session;

import java.io.IOException;

import org.apache.http.client.CookieStore;

public class BeidianSession {
	
	private CookieStore cookieStore;
	
	public CookieStore getCookieStore() {
		return cookieStore;
	}

	public void setCookieStore(CookieStore cookieStore) throws IOException {
		this.cookieStore = cookieStore;
	}

}
