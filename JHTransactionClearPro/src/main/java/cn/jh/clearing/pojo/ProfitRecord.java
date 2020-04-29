package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;



@Entity
@Table(name="t_profit_record")
public class ProfitRecord implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id;
	
	/**对应于payment_order 的 order_code*/
	@Column(name="order_code")
	private String ordercode;
	
	@Column(name="brand_id")
	private String brandId;
	
	/**交易金额*/
	@Column(name="amount")
	private BigDecimal  amount;
	
	/**套现前一级的用户*/
	@Column(name="ori_user_id")
	private long oriuserid;
	
	/**套现前一级的用户手机号码*/
	@Column(name="ori_phone")
	private String oriphone;
	
	/**套现前一的费率*/
	@Column(name="ori_rate")
	private BigDecimal orirate;
	
	@Column(name="ori_user_name")
	private String oriUserName;
	
	/**获取人的手机号码*/
	@Column(name="acq_user_id")
	private long acquserid;
	
	@Column(name="acq_phone")
	private String acqphone;
	
	/**获取人的结算费率*/
	@Column(name="acq_rate")
	private BigDecimal acqrate;
	
	@Column(name="acq_user_name")
	private String acqUserName;
	
	/**分润比例*/
	@Column(name="scale")
	private BigDecimal scale = new BigDecimal("1");
	
	/**分润金额*/
	@Column(name="acq_amount")
	private BigDecimal acqAmount;
	
	@Column(name="remark")
	private String remark;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	
	/**来源类型   0表示用户充值费率分润  1表示品牌的提现分润   2表示品牌的产品分润  3表示平台商的平级分润*/
	@Column(name="profit_type")
	private String type;

	@Column(name="level")
	private long level;
	
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


	public BigDecimal getOrirate() {
		return orirate;
	}


	public void setOrirate(BigDecimal orirate) {
		this.orirate = orirate;
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


	public BigDecimal getAcqrate() {
		return acqrate;
	}


	public void setAcqrate(BigDecimal acqrate) {
		this.acqrate = acqrate;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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


	public BigDecimal getScale() {
		return scale;
	}


	public void setScale(BigDecimal scale) {
		this.scale = scale;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getBrandId() {
		return brandId;
	}


	public void setBrandId(String brandId) {
		this.brandId = brandId;
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
		return "ProfitRecord [id=" + id + ", ordercode=" + ordercode + ", brandId=" + brandId + ", amount=" + amount
				+ ", oriuserid=" + oriuserid + ", oriphone=" + oriphone + ", orirate=" + orirate + ", oriUserName="
				+ oriUserName + ", acquserid=" + acquserid + ", acqphone=" + acqphone + ", acqrate=" + acqrate
				+ ", acqUserName=" + acqUserName + ", scale=" + scale + ", acqAmount=" + acqAmount + ", remark="
				+ remark + ", createTime=" + createTime + ", type=" + type + ", level=" + level + "]";
	}




	

	
	
}
