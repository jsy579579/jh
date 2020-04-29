package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;


/**分销返佣记录*/
@Entity
@Table(name="t_distribution_record_copy")
public class DistributionRecordCopy implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id;
	
	/**对应于payment_order 的 order_code*/
	@Column(name="order_code")
	private String ordercode;
	
	/**交易金额*/
	@Column(name="amount")
	private BigDecimal  amount;
	
	@Column(name="ori_user_id")
	private long oriuserid;
	
	@Column(name="ori_phone")
	private String oriphone;
	
	@Column(name="ori_grade")
	private String oriGrade;
	
	@Column(name="ori_user_name")
	private String oriUserName;
	
	@Column(name="acq_user_id")
	private long acquserid;
	
	@Column(name="acq_phone")
	private String acqphone;
	
	
	/**获取人的返佣比率*/
	@Column(name="acq_ratio")
	private BigDecimal acqratio;
	
	@Column(name="acq_grade")
	private String acqGrade;
	
	@Column(name="acq_user_name")
	private String acqUserName;
	
	/**分润金额*/
	@Column(name="acq_amount")
	private BigDecimal acqAmount;
	
	@Column(name="remark")
	private String remark;
	
	@Column(name="level")
	private long level;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public long getOriuserid() {
		return oriuserid;
	}

	public void setOriuserid(long oriuserid) {
		this.oriuserid = oriuserid;
	}

	public String getOriphone() {
		return oriphone;
	}

	public void setOriphone(String oriphone) {
		this.oriphone = oriphone;
	}

	public long getAcquserid() {
		return acquserid;
	}

	public void setAcquserid(long acquserid) {
		this.acquserid = acquserid;
	}

	public String getAcqphone() {
		return acqphone;
	}

	public void setAcqphone(String acqphone) {
		this.acqphone = acqphone;
	}

	public BigDecimal getAcqratio() {
		return acqratio;
	}

	public void setAcqratio(BigDecimal acqratio) {
		this.acqratio = acqratio;
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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getOriGrade() {
		return oriGrade;
	}

	public void setOriGrade(String oriGrade) {
		this.oriGrade = oriGrade;
	}

	public String getAcqGrade() {
		return acqGrade;
	}

	public void setAcqGrade(String acqGrade) {
		this.acqGrade = acqGrade;
	}

	public long getLevel() {
		return level;
	}

	public void setLevel(long level) {
		this.level = level;
	}

	public String getOriUserName() {
		return oriUserName;
	}

	public void setOriUserName(String oriUserName) {
		this.oriUserName = oriUserName;
	}

	public String getAcqUserName() {
		return acqUserName;
	}

	public void setAcqUserName(String acqUserName) {
		this.acqUserName = acqUserName;
	}

	@Override
	public String toString() {
		return "DistributionRecord [id=" + id + ", ordercode=" + ordercode + ", amount=" + amount + ", oriuserid="
				+ oriuserid + ", oriphone=" + oriphone + ", oriGrade=" + oriGrade + ", oriUserName=" + oriUserName
				+ ", acquserid=" + acquserid + ", acqphone=" + acqphone + ", acqratio=" + acqratio + ", acqGrade="
				+ acqGrade + ", acqUserName=" + acqUserName + ", acqAmount=" + acqAmount + ", remark=" + remark
				+ ", level=" + level + ", createTime=" + createTime + "]";
	}

	

	
	
}
