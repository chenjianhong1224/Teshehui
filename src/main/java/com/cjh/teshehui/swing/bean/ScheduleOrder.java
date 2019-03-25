package com.cjh.teshehui.swing.bean;

import java.io.Serializable;
import java.util.List;

public class ScheduleOrder implements Serializable {
	
	public String getFreeAmount() {
		return freeAmount;
	}

	public void setFreeAmount(String freeAmount) {
		this.freeAmount = freeAmount;
	}

	public String getFreightAmount() {
		return freightAmount;
	}

	public void setFreightAmount(String freightAmount) {
		this.freightAmount = freightAmount;
	}

	public List<ProductOrder> getProductOrderList() {
		return productOrderList;
	}

	public void setProductOrderList(List<ProductOrder> productOrderList) {
		this.productOrderList = productOrderList;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	private String freeAmount;
	
	private String freightAmount;
	
	private List<ProductOrder> productOrderList;
	
	private String storeId;

}
