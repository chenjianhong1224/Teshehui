package com.cjh.teshehui.swing.bean;

import java.io.Serializable;
import java.util.Date;

public class ViewMsgBean implements Serializable {
	
	private String msg;
	
	private Date time;
	
	private String phone;
	
	private int row;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

}
