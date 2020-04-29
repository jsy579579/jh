package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author 作者:zxj
 */
@Entity
@Table(name = "t_xt_order")
public class XTOrderCode implements Serializable {

	private static final long serialVersionUID = 6864095834529982858L;
	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "order_code")
	private String orderCode;

	@Column(name = "status")
	private String status;

	@Column(name = "sub_merch_id")
	private String subMerchId;

	public String getSubMerchId() {
		return subMerchId;
	}

	public void setSubMerchId(String subMerchId) {
		this.subMerchId = subMerchId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
