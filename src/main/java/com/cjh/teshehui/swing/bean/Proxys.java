package com.cjh.teshehui.swing.bean;

public class Proxys {

	private static Proxys instance;

	int index = 0;

	private Proxys() {
	}

	public static synchronized Proxys getInstance() {
		if (instance == null) {
			instance = new Proxys();
		}
		return instance;
	}

	private String[] proxys = { "221.7.255.167:80", "120.76.112.196:3128", "119.27.177.169:80", "116.62.204.38:9999",
			"221.7.255.168:80", "140.143.96.216:80", "120.92.74.237:3128", "113.200.56.13:8010", "221.7.255.167:8080",
			"218.60.8.99:3129", "120.92.74.189:3128", "120.83.49.90:9000", "218.60.8.98:3129", "113.200.214.164:9999",
			"218.60.8.83:3129", "117.35.51.77:53281", "183.129.207.82:12134", "220.171.96.155:55705",
			"61.128.208.94:3128", "114.112.84.197:43007" };

	public String getProxys() {
		String proxy = proxys[index];
		index = (++index) % proxys.length;
		return proxy;
	}
}
