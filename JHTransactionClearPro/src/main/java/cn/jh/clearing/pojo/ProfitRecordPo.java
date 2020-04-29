package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;


public class ProfitRecordPo implements Serializable{
	
	private String oriphone;
	
	private BigDecimal acqAmount;
	
	private String remark;
	
	private String createTime;

	public String getOriphone() {
		return oriphone;
	}

	public void setOriphone(String oriphone) {
		this.oriphone = oriphone;
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

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
