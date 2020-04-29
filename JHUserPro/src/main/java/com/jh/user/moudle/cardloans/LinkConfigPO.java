package com.jh.user.moudle.cardloans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_link_config")
public class LinkConfigPO implements Serializable {

	/**
	 * <p>Description: </p>
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	@Column(name="brand_id")
	private String brandId;
	@Column(name="link_type")
	private String linkType;
	@Column(name="link_classify")
	private String linkClassify;
	@Column(name="link_title")
	private String linkTitle;
	@Column(name="link_address",length=1000)
	private String linkAddress;
	@Column(name="link_pucture",length=1000)
	private String linkPicture;
	@Column(name="detail_pucture",length=1000)
	private String detailPicture;
	@Column(name="on_off")
	private String onOff = "1";
	@Column(name="link_hits")
	private Integer linkHits = 0;
	@Column(name="min_limit")
	private BigDecimal minLimit = BigDecimal.ZERO;
	@Column(name="max_limit")
	private BigDecimal maxLimit = BigDecimal.ZERO;
	@Column(name="link_remark",length=1000)
	private String linkRemark;
	@Column(name="link_publisher")
	private String linkPublisher;
//	结算方式
	@Column(name="clearing_form")
	private String clearingForm;
//	数据形式
	@Column(name="data_form")
	private String dataForm;
//	结算标准
	@Column(name="settlement_standard")
	private String settlementStandard;
//	可得分润
	@Column(name="rebate")
	private BigDecimal rebate = BigDecimal.ZERO;
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	public String getLinkType() {
		return linkType;
	}

	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	public String getLinkClassify() {
		return linkClassify;
	}

	public void setLinkClassify(String linkClassify) {
		this.linkClassify = linkClassify;
	}

	public String getLinkTitle() {
		return linkTitle;
	}

	public void setLinkTitle(String linkTitle) {
		this.linkTitle = linkTitle;
	}

	public String getLinkAddress() {
		return linkAddress;
	}

	public void setLinkAddress(String linkAddress) {
		this.linkAddress = linkAddress;
	}

	public String getLinkPicture() {
		return linkPicture;
	}

	public void setLinkPicture(String linkPicture) {
		this.linkPicture = linkPicture;
	}

	public String getOnOff() {
		return onOff;
	}

	public void setOnOff(String onOff) {
		this.onOff = onOff;
	}

	public Integer getLinkHits() {
		return linkHits;
	}

	public void setLinkHits(Integer linkHits) {
		this.linkHits = linkHits;
	}

	public String getLinkRemark() {
		return linkRemark;
	}

	public void setLinkRemark(String linkRemark) {
		this.linkRemark = linkRemark;
	}

	public String getLinkPublisher() {
		return linkPublisher;
	}

	public void setLinkPublisher(String linkPublisher) {
		this.linkPublisher = linkPublisher;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public BigDecimal getMinLimit() {
		return minLimit;
	}

	public void setMinLimit(BigDecimal minLimit) {
		this.minLimit = minLimit;
	}

	public BigDecimal getMaxLimit() {
		return maxLimit;
	}

	public void setMaxLimit(BigDecimal maxLimit) {
		this.maxLimit = maxLimit;
	}

	public String getDetailPicture() {
		return detailPicture;
	}

	public void setDetailPicture(String detailPicture) {
		this.detailPicture = detailPicture;
	}

	public String getClearingForm() {
		return clearingForm;
	}

	public void setClearingForm(String clearingForm) {
		this.clearingForm = clearingForm;
	}

	public String getDataForm() {
		return dataForm;
	}

	public void setDataForm(String dataForm) {
		this.dataForm = dataForm;
	}

	public String getSettlementStandard() {
		return settlementStandard;
	}

	public void setSettlementStandard(String settlementStandard) {
		this.settlementStandard = settlementStandard;
	}

	public BigDecimal getRebate() {
		return rebate;
	}

	public void setRebate(BigDecimal rebate) {
		this.rebate = rebate;
	}

}
