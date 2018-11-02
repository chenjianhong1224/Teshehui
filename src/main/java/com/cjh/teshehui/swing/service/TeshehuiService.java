package com.cjh.teshehui.swing.service;

import com.cjh.teshehui.swing.bean.ReturnResultBean;

public interface TeshehuiService {
	
	ReturnResultBean getLoginSmsCode(String phoneNo, String verifyImgCode);
	
	ReturnResultBean login(String phoneNo, String smsCode);
	
	ReturnResultBean getAddress();
	
	ReturnResultBean getUserInfo();

}
