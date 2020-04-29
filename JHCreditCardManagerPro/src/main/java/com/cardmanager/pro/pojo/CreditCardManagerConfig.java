package com.cardmanager.pro.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name="t_credit_card_manager_config",indexes= {
		@Index(columnList="version",name="idx_version"),
		@Index(columnList="create_on_off",name="idx_create_on_off")
})
public class CreditCardManagerConfig implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4477373731913105481L;
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="version")
	private String version;
	@Column(name="channel_id")
	private String channelId;
	@Column(name="channel_tag")
	private String channelTag;
	@Column(name="channel_name")
	private String channelName;
	@Column(name="pay_single_limit_money",scale=2)
	private BigDecimal paySingleLimitMoney = BigDecimal.ZERO;
	@Column(name="pay_single_max_money",scale=2)
	private BigDecimal paySingleMaxMoney = BigDecimal.ZERO;
	@Column(name="pay_single_limit_count")
	private int paySingleLimitCount = 2;
	@Column(name="con_single_limit_money",scale=2)
	private BigDecimal conSingleLimitMoney = BigDecimal.ZERO;
	@Column(name="con_single_max_money",scale=2)
	private BigDecimal conSingleMaxMoney = BigDecimal.ZERO;
	@Column(name="con_single_limit_count")
	private int conSingleLimitCount = 2;
	@Column(name="first_money")
	private int firstMoney = 10;
//	扫描任务开关
	@Column(name="scan_on_off")
	private int scanOnOff = 0;
//	消费任务开关
	@Column(name="consume_on_off")
	private int consumeOnOff = 0;
//	还款任务开关
	@Column(name="repayment_on_off")
	private int repaymentOnOff = 0;
//	生成任务开关
	@Column(name="create_on_off")
	private int createOnOff = 0;
	@Column(name="no_support_bank")
	private String noSupportBank="0";
	@Column(name="bean_name")
	private String beanName;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getChannelTag() {
		return channelTag;
	}
	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public int getFirstMoney() {
		return firstMoney;
	}
	public void setFirstMoney(int firstMoney) {
		this.firstMoney = firstMoney;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public int getPaySingleLimitCount() {
		return paySingleLimitCount;
	}
	public void setPaySingleLimitCount(int paySingleLimitCount) {
		this.paySingleLimitCount = paySingleLimitCount;
	}
	public BigDecimal getPaySingleLimitMoney() {
		return paySingleLimitMoney;
	}
	public void setPaySingleLimitMoney(BigDecimal paySingleLimitMoney) {
		this.paySingleLimitMoney = paySingleLimitMoney;
	}
	public BigDecimal getConSingleLimitMoney() {
		return conSingleLimitMoney;
	}
	public void setConSingleLimitMoney(BigDecimal conSingleLimitMoney) {
		this.conSingleLimitMoney = conSingleLimitMoney;
	}
	public int getConSingleLimitCount() {
		return conSingleLimitCount;
	}
	public void setConSingleLimitCount(int conSingleLimitCount) {
		this.conSingleLimitCount = conSingleLimitCount;
	}
	public int getScanOnOff() {
		return scanOnOff;
	}
	public void setScanOnOff(int scanOnOff) {
		this.scanOnOff = scanOnOff;
	}
	public int getCreateOnOff() {
		return createOnOff;
	}
	public void setCreateOnOff(int createOnOff) {
		this.createOnOff = createOnOff;
	}
	public String getNoSupportBank() {
		return noSupportBank;
	}
	public void setNoSupportBank(String noSupportBank) {
		this.noSupportBank = noSupportBank;
	}
	public int getConsumeOnOff() {
		return consumeOnOff;
	}
	public void setConsumeOnOff(int consumeOnOff) {
		this.consumeOnOff = consumeOnOff;
	}
	public int getRepaymentOnOff() {
		return repaymentOnOff;
	}
	public void setRepaymentOnOff(int repaymentOnOff) {
		this.repaymentOnOff = repaymentOnOff;
	}
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public BigDecimal getPaySingleMaxMoney() {
		return paySingleMaxMoney;
	}
	public void setPaySingleMaxMoney(BigDecimal paySingleMaxMoney) {
		this.paySingleMaxMoney = paySingleMaxMoney;
	}
	public BigDecimal getConSingleMaxMoney() {
		return conSingleMaxMoney;
	}
	public void setConSingleMaxMoney(BigDecimal conSingleMaxMoney) {
		this.conSingleMaxMoney = conSingleMaxMoney;
	}
	public String getBeanName() {
		return beanName;
	}
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
}
