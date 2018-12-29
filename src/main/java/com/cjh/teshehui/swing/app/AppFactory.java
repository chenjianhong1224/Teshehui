package com.cjh.teshehui.swing.app;

import java.util.Random;

public class AppFactory {

	public AppContext getNewApp() {
		AppContext ctx = new AppContext();
		ctx.setXuid(getNewXuid(20));
		return ctx;
	}

	private static String getNewXuid(int paramInt) {
		StringBuilder localStringBuilder = new StringBuilder();
		Random localRandom = new Random();
		for (int i = 0; i < paramInt; i++) {
			switch (localRandom.nextInt(4)) {
			case 1:
				localStringBuilder.append((char) (localRandom.nextInt(26) + 97));
				break;
			case 2:
				localStringBuilder.append((char) (localRandom.nextInt(26) + 65));
				break;
			case 3:
				localStringBuilder.append((char) (localRandom.nextInt(10) + 48));
			}
		}
		return localStringBuilder.toString();
	}
}
