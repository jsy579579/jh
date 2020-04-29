package cn.jh.clearing.pojo;

import java.math.BigDecimal;
import java.util.Date;

public class ProfitRecordExcel {
	
	private Date createTime;

	private String oriphone;

	private String ordercode;

	private BigDecimal  amount;

	private BigDecimal orirate;

	private String acqphone;
	
	private BigDecimal acqrate;

	private BigDecimal acqAmount;

	private String remark;

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getOriphone() {
		return oriphone;
	}

	public void setOriphone(String oriphone) {
		this.oriphone = oriphone;
	}

	public String getOrdercode() {
		return ordercode;
	}

	public void setOrdercode(String ordercode) {
		this.ordercode = ordercode;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getOrirate() {
		return orirate;
	}

	public void setOrirate(BigDecimal orirate) {
		this.orirate = orirate;
	}

	public String getAcqphone() {
		return acqphone;
	}

	public void setAcqphone(String acqphone) {
		this.acqphone = acqphone;
	}

	public BigDecimal getAcqrate() {
		return acqrate;
	}

	public void setAcqrate(BigDecimal acqrate) {
		this.acqrate = acqrate;
	}

	public BigDecimal getAcqAmount() {
		return acqAmount;
	}

	public void setAcqAmount(BigDecimal acqAmount) {
		this.acqAmount = acqAmount;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
