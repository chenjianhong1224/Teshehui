package com.cjh.teshehui.swing.service;

import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.bean.SkuBean;

public interface TeshehuiService {
	
	ReturnResultBean getLoginSmsCode(String phoneNo, String verifyImgCode);
	
	ReturnResultBean login(String phoneNo, String smsCode);
	
	ReturnResultBean getAddress();
	
	ReturnResultBean getUserInfo();
	
	ReturnResultBean getProductStockInfo(String url);
	
	ReturnResultBean createOrder(SkuBean bean);
	
	ReturnResultBean getCheckCode();

	ReturnResultBean checkCode(String verifyImgCode);
	
	ReturnResultBean getCoupon();
	
	ReturnResultBean getMyCoupon();
}
