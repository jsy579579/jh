package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_loan_application")
public class LoanApplication implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "brand_id")
	private int brandId;

	// 分类
	@Column(name = "classifi_cation")
	private String classifiCation;

	// 标题
	@Column(name = "title")
	private String title;

	// 内容
	@Column(name = "content")
	private String content;

	// 预览图id
	@Column(name = "low_source_id")
	private String lowSourceId;
	
	// 预览图
	@Column(name = "low_source")
	private String lowSource;

	// 开关
	@Column(name = "on_off")
	private int onOff;

	// 摘要
	@Column(name = "remark")
	private String remark;

	// 预览人数
	@Column(name = "preview_number")
	private String previewNumber;

	// 发布人
	@Column(name = "publisher")
	private String publisher;

	// 贷款额度
	@Column(name = "loan_limit")
	private BigDecimal loanLimit;

	// 日息
	@Column(name = "daily_interest")
	private String dailyInterest;

	// 标签1
	@Column(name = "label1")
	private String label1;

	// 标签2
	@Column(name = "label2")
	private String label2;

	// 标签3
	@Column(name = "label3")
	private String label3;

	// 到卡时间描述
	@Column(name = "account_arr_time_desc")
	private String accountArrTimeDesc;

	// 推广返佣说明图片id
	@Column(name = "desc_picture_id")
	private String descPictureId;
	
	// 推广返佣说明图片
	@Column(name = "desc_picture")
	private String descPicture;

	// 贷款成功奖励金额
	@Column(name = "reward_amount")
	private BigDecimal rewardAmount;

	// 结算周期 文字描述
	@Column(name = "settlement_cycle")
	private String settlementCycle;

	// 结算规则 文字描述
	@Column(name = "settlement_rules")
	private String settlementRules;

	// 备用字段1
	@Column(name = "spare1")
	private String spare1;

	// 备用字段2
	@Column(name = "spare2")
	private String spare2;

	// 备用字段3
	@Column(name = "spare3")
	private String spare3;
	
	//开关
	@Column(name = "status")
	private int status;

	// 更新时间
	@Column(name = "update_time")
	private String updateTime;
	
	@Column(name = "delete_time")
	private String deleteTime;

	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getBrandId() {
		return brandId;
	}

	public void setBrandId(int brandId) {
		this.brandId = brandId;
	}

	public String getClassifiCation() {
		return classifiCation;
	}

	public void setClassifiCation(String classifiCation) {
		this.classifiCation = classifiCation;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLowSource() {
		return lowSource;
	}

	public void setLowSource(String lowSource) {
		this.lowSource = lowSource;
	}

	public int getOnOff() {
		return onOff;
	}

	public void setOnOff(int onOff) {
		this.onOff = onOff;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getPreviewNumber() {
		return previewNumber;
	}

	public void setPreviewNumber(String previewNumber) {
		this.previewNumber = previewNumber;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public BigDecimal getLoanLimit() {
		return loanLimit;
	}

	public void setLoanLimit(BigDecimal loanLimit) {
		this.loanLimit = loanLimit;
	}

	public String getDailyInterest() {
		return dailyInterest;
	}

	public void setDailyInterest(String dailyInterest) {
		this.dailyInterest = dailyInterest;
	}

	public String getLabel1() {
		return label1;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public String getLabel2() {
		return label2;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	public String getLabel3() {
		return label3;
	}

	public void setLabel3(String label3) {
		this.label3 = label3;
	}

	public String getAccountArrTimeDesc() {
		return accountArrTimeDesc;
	}

	public void setAccountArrTimeDesc(String accountArrTimeDesc) {
		this.accountArrTimeDesc = accountArrTimeDesc;
	}

	public String getDescPicture() {
		return descPicture;
	}

	public void setDescPicture(String descPicture) {
		this.descPicture = descPicture;
	}

	public BigDecimal getRewardAmount() {
		return rewardAmount;
	}

	public void setRewardAmount(BigDecimal rewardAmount) {
		this.rewardAmount = rewardAmount;
	}

	public String getSettlementCycle() {
		return settlementCycle;
	}

	public void setSettlementCycle(String settlementCycle) {
		this.settlementCycle = settlementCycle;
	}

	public String getSettlementRules() {
		return settlementRules;
	}

	public void setSettlementRules(String settlementRules) {
		this.settlementRules = settlementRules;
	}

	public String getSpare1() {
		return spare1;
	}

	public void setSpare1(String spare1) {
		this.spare1 = spare1;
	}

	public String getSpare2() {
		return spare2;
	}

	public void setSpare2(String spare2) {
		this.spare2 = spare2;
	}

	public String getSpare3() {
		return spare3;
	}

	public void setSpare3(String spare3) {
		this.spare3 = spare3;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getLowSourceId() {
		return lowSourceId;
	}

	public void setLowSourceId(String lowSourceId) {
		this.lowSourceId = lowSourceId;
	}

	public String getDescPictureId() {
		return descPictureId;
	}

	public void setDescPictureId(String descPictureId) {
		this.descPictureId = descPictureId;
	}

	public String getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(String deleteTime) {
		this.deleteTime = deleteTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "LoanApplication [id=" + id + ", brandId=" + brandId + ", classifiCation=" + classifiCation + ", title="
				+ title + ", content=" + content + ", lowSourceId=" + lowSourceId + ", lowSource=" + lowSource
				+ ", onOff=" + onOff + ", remark=" + remark + ", previewNumber=" + previewNumber + ", publisher="
				+ publisher + ", loanLimit=" + loanLimit + ", dailyInterest=" + dailyInterest + ", label1=" + label1
				+ ", label2=" + label2 + ", label3=" + label3 + ", accountArrTimeDesc=" + accountArrTimeDesc
				+ ", descPictureId=" + descPictureId + ", descPicture=" + descPicture + ", rewardAmount=" + rewardAmount
				+ ", settlementCycle=" + settlementCycle + ", settlementRules=" + settlementRules + ", spare1=" + spare1
				+ ", spare2=" + spare2 + ", spare3=" + spare3 + ", status=" + status + ", updateTime=" + updateTime
				+ ", deleteTime=" + deleteTime + ", createTime=" + createTime + "]";
	}

}
