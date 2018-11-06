package com.cjh.teshehui.swing.bean;

import java.io.Serializable;

public class SkuBean implements Serializable {

	private String supplierId;
	
	private String storeId;
	
	private Integer freightMoney; 

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public Integer getSkuId() {
		return skuId;
	}

	public void setSkuId(Integer skuId) {
		this.skuId = skuId;
	}

	public String getSkuCode() {
		return skuCode;
	}

	public void setSkuCode(String skuCode) {
		this.skuCode = skuCode;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}

	public String getMemberPrice() {
		return memberPrice;
	}

	public void setMemberPrice(String memberPrice) {
		this.memberPrice = memberPrice;
	}

	public Integer getSkuStock() {
		return skuStock;
	}

	public void setSkuStock(Integer skuStock) {
		this.skuStock = skuStock;
	}

	public Integer getLimitNum() {
		return limitNum;
	}

	public void setLimitNum(Integer limitNum) {
		this.limitNum = limitNum;
	}

	private String productCode;

	private String productName;
	
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public Integer getFreightMoney() {
		return freightMoney;
	}

	public void setFreightMoney(Integer freightMoney) {
		this.freightMoney = freightMoney;
	}

	private Integer skuId;

	private String skuCode;
	
	private String attrValue;
	
	private String memberPrice;
	
	private Integer skuStock;
	
	private Integer limitNum;

}
