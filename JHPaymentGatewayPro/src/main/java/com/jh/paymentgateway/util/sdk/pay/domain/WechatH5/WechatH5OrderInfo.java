package com.jh.paymentgateway.util.sdk.pay.domain.WechatH5;

import java.util.ArrayList;
import java.util.List;

public class WechatH5OrderInfo {
	private String id;
	private String businessType;
	private List<WxH5OrderGoods> goodsList = new ArrayList<WxH5OrderGoods>(10);

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public List<WxH5OrderGoods> getGoodsList() {
		return goodsList;
	}

	public void setGoodsList(List<WxH5OrderGoods> list) {
		this.goodsList = list;
	}

	public void addGood(WxH5OrderGoods good) {
		this.goodsList.add(good);
	}
}
