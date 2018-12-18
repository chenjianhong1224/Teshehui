package com.cjh.teshehui.swing.bean;

import java.io.Serializable;

public class Coupon implements Serializable{
	
	private String couponCode;
	
	private String couponBatchCode;
	
	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public String getCouponBatchCode() {
		return couponBatchCode;
	}

	public void setCouponBatchCode(String couponBatchCode) {
		this.couponBatchCode = couponBatchCode;
	}

	public boolean isUseFlag() {
		return useFlag;
	}

	public void setUseFlag(boolean useFlag) {
		this.useFlag = useFlag;
	}

	private boolean useFlag;

}
