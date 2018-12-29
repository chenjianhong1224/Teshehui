package com.cjh.teshehui.swing.session;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
@Scope("singleton")
public class TeshehuiSessionManager {

	Map<String, TeshehuiSession> sessionMap = Maps.newHashMap();

	public void addSession(String userName, TeshehuiSession session) {
		sessionMap.put(userName, session);
	}

	public TeshehuiSession getSession(String userName) {
		return sessionMap.get(userName);
	}

	public Map<String, TeshehuiSession> getAllSession() {
		return sessionMap;
	}

	public void clear(){
		sessionMap.clear();
	}
}
