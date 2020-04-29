package com.jh.paymentgateway.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_order_parameter")
public class OrderParameter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="order_code")
	private String orderCode;
	@Column(name="order_json")
	private String orderJson;
	@Column(name="whether_to_request")
	private int whetherToRequest = 0;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public String getOrderJson() {
		return orderJson;
	}
	public void setOrderJson(String orderJson) {
		this.orderJson = orderJson;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public int getWhetherToRequest() {
		return whetherToRequest;
	}
	public void setWhetherToRequest(int whetherToRequest) {
		this.whetherToRequest = whetherToRequest;
	}

	
}
