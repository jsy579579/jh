package cn.jh.clearing.pojo;

import java.io.Serializable;

public class BrandWithdrawRebate  implements Serializable{

	
	private static final long serialVersionUID = 1L;


	private  long brandId;
	
	
	private  String  orderCode;


	public long getBrandId() {
		return brandId;
	}


	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}


	public String getOrderCode() {
		return orderCode;
	}


	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	
	
}
