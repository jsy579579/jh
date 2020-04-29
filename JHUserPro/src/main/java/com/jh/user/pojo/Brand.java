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
@Table(name = "t_brand")
public class Brand implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "name")
	private String name;

	@Column(name = "level_name")
	private String levelName = "普通用户";

	/** 品牌编号（8位） */
	@Column(name = "number")
	private String number;

	/** 品牌类型（默认贴牌，1主品牌） */
	@Column(name = "brand_type")
	private String brandType;

	/*** android版本号 */
	@Column(name = "version_android")
	private String androidVersion;

	/** 安卓更新描述 */
	@Column(name = "android_content")
	private String androidContent;

	/*** android下载地址 */
	@Column(name = "download_android")
	private String androidDownload;
	
	/*** android下载地址 */
	@Column(name = "download_android_url")
	private String androidDownloadUrl;

	/** ios版本号 */
	@Column(name = "version_ios")
	private String iosVersion;

	/** iOS更新描述 */
	@Column(name = "ios_content")
	private String iosContent;

	/** ios下载地址 */
	@Column(name = "download_ios")
	private String iosDownload;

	@Column(name = "repayment_url")
	private String repaymentUrl;
	
	/** 分享主题 */
	@Column(name = "share_title")
	private String shareTitle;

	/** 分享logo图片地址 */
	@Column(name = "share_logo_address")
	private String shareLogoAddress;

	/** 分享图片地址 */
	@Column(name = "share_main_address")
	private String shareMainAddress;

	/** 分享内容 */
	@Column(name = "share_content")
	private String shareContent;
	// 自动返利开关: 0:关,1:开,默认为0
	@Column(name = "auto_rebate_config_on_off")
	private Integer autoRebateConfigOnOff;

	/** 自动升级 0 表示不开通自动升级功能 1表示开通自动升级 2标识开通自动升级二只按人头*/
	@Column(name = "auto_upgrade")
	private String autoUpgrade;

	/** 自动升级需要达到的人数 */
	@Column(name = "auto_upgrade_people")
	private int autoUpgradePeople;

	/** 极光APP秘钥 */
	@Column(name = "jd_appkey")
	private String appkey;

	/** 极光APP推送秘钥 */
	@Column(name = "jd_mastersecret")
	private String mastersecret;

	/** 聚合数据手机充值KEY */
	@Column(name = "juhe_bill_key")
	private String juhekey;

	/** 聚合数据违章缴费KEY */
	@Column(name = "juhe_wzdj_key")
	private String juheWzdjKey;

	/** 聚合数据OpenId */
	@Column(name = "juhe_openid")
	private String juheOpenid;

	/**
	 * 400电话
	 */
	@Column(name = "brand_phone")
	private String brandPhone;
	/**
	 * QQ
	 */
	@Column(name = "brand_qq")
	private String brandQQ;
	/**
	 * 微信
	 */
	@Column(name = "brand_weixin")
	private String brandWeiXin;

	/** 短信发送码 */
	@Column(name = "tpl_id")
	private String tplid;

	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/** 品牌的管理人 */
	@Column(name = "manage_id")
	private long manageid;

	/** 同级分润比率 */
	@Column(name = "equal_rebate_rate")
	private BigDecimal equalRebateRate;
	// 返佣次数
	@Column(name = "rebate_count")
	private int rebateCount = 3;
	
	@Column(name="is_new_rebate")
	private int isNewRebate = 1;
	
	@Column(name="brand_description")
	private String brandDescription;
	
	@Column(name="car_query_price")
	private String carQueryPrice;
	
	/**激活升级  0：不升级 ；1开启激活升级**/
	@Column(name="activate_the_upgrade")
	private String activateTheUpgrade = "0";
	
	@Column(name="we_chat_name")
	private String weChatName = null;
	
	@Column(name="we_chat_url")
	private String weChatUrl = null;

	@Column(name="maid_proportion")
	private String maidProportion = null;
	
	public String getBrandDescription() {
		return brandDescription;
	}

	public void setBrandDescription(String brandDescription) {
		this.brandDescription = brandDescription;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getBrandType() {
		return brandType;
	}

	public void setBrandType(String brandType) {
		this.brandType = brandType;
	}

	public String getAndroidVersion() {
		return androidVersion;
	}

	public void setAndroidVersion(String androidVersion) {
		this.androidVersion = androidVersion;
	}

	public String getAndroidDownload() {
		return androidDownload;
	}

	public void setAndroidDownload(String androidDownload) {
		this.androidDownload = androidDownload;
	}

	public String getIosVersion() {
		return iosVersion;
	}

	public void setIosVersion(String iosVersion) {
		this.iosVersion = iosVersion;
	}

	public String getIosDownload() {
		return iosDownload;
	}

	public void setIosDownload(String iosDownload) {
		this.iosDownload = iosDownload;
	}

	public String getShareTitle() {
		return shareTitle;
	}

	public void setShareTitle(String shareTitle) {
		this.shareTitle = shareTitle;
	}

	public String getShareLogoAddress() {
		return shareLogoAddress;
	}

	public void setShareLogoAddress(String shareLogoAddress) {
		this.shareLogoAddress = shareLogoAddress;
	}

	public String getShareMainAddress() {
		return shareMainAddress;
	}

	public void setShareMainAddress(String shareMainAddress) {
		this.shareMainAddress = shareMainAddress;
	}

	public String getShareContent() {
		return shareContent;
	}

	public void setShareContent(String shareContent) {
		this.shareContent = shareContent;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public long getManageid() {
		return manageid;
	}

	public void setManageid(long manageid) {
		this.manageid = manageid;
	}

	public String getAppkey() {
		return appkey;
	}

	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}

	public String getMastersecret() {
		return mastersecret;
	}

	public void setMastersecret(String mastersecret) {
		this.mastersecret = mastersecret;
	}

	public String getTplid() {
		return tplid;
	}

	public void setTplid(String tplid) {
		this.tplid = tplid;
	}

	public BigDecimal getEqualRebateRate() {
		return equalRebateRate;
	}

	public void setEqualRebateRate(BigDecimal equalRebateRate) {
		this.equalRebateRate = equalRebateRate;
	}

	public String getJuhekey() {
		return juhekey;
	}

	public void setJuhekey(String juhekey) {
		this.juhekey = juhekey;
	}

	public String getJuheOpenid() {
		return juheOpenid;
	}

	public void setJuheOpenid(String juheOpenid) {
		this.juheOpenid = juheOpenid;
	}

	public String getJuheWzdjKey() {
		return juheWzdjKey;
	}

	public void setJuheWzdjKey(String juheWzdjKey) {
		this.juheWzdjKey = juheWzdjKey;
	}

	public String getBrandPhone() {
		return brandPhone;
	}

	public void setBrandPhone(String brandPhone) {
		this.brandPhone = brandPhone;
	}

	public String getBrandQQ() {
		return brandQQ;
	}

	public void setBrandQQ(String brandQQ) {
		this.brandQQ = brandQQ;
	}

	public String getBrandWeiXin() {
		return brandWeiXin;
	}

	public void setBrandWeiXin(String brandWeiXin) {
		this.brandWeiXin = brandWeiXin;
	}

	public String getAndroidContent() {
		return androidContent;
	}

	public void setAndroidContent(String androidContent) {
		this.androidContent = androidContent;
	}

	public String getIosContent() {
		return iosContent;
	}

	public void setIosContent(String iosContent) {
		this.iosContent = iosContent;
	}

	public Integer getAutoRebateConfigOnOff() {
		return autoRebateConfigOnOff;
	}

	public void setAutoRebateConfigOnOff(Integer autoRebateConfigOnOff) {
		this.autoRebateConfigOnOff = autoRebateConfigOnOff;
	}

	public int getRebateCount() {
		return rebateCount;
	}

	public void setRebateCount(int rebateCount) {
		this.rebateCount = rebateCount;
	}

	public String getActivateTheUpgrade() {
		return activateTheUpgrade;
	}

	public void setActivateTheUpgrade(String activateTheUpgrade) {
		this.activateTheUpgrade = activateTheUpgrade;
	}

	public int getIsNewRebate() {
		return isNewRebate;
	}

	public void setIsNewRebate(int isNewRebate) {
		this.isNewRebate = isNewRebate;
	}

	public String getWeChatName() {
		return weChatName;
	}

	public void setWeChatName(String weChatName) {
		this.weChatName = weChatName;
	}

	public String getWeChatUrl() {
		return weChatUrl;
	}

	public void setWeChatUrl(String weChatUrl) {
		this.weChatUrl = weChatUrl;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getMaidProportion() {
		return maidProportion;
	}

	public void setMaidProportion(String maidProportion) {
		this.maidProportion = maidProportion;
	}

	public String getLevelName() {
		return levelName;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	public String getAutoUpgrade() {
		return autoUpgrade;
	}

	public void setAutoUpgrade(String autoUpgrade) {
		this.autoUpgrade = autoUpgrade;
	}

	public int getAutoUpgradePeople() {
		return autoUpgradePeople;
	}

	public void setAutoUpgradePeople(int autoUpgradePeople) {
		this.autoUpgradePeople = autoUpgradePeople;
	}

	public String getAndroidDownloadUrl() {
		return androidDownloadUrl;
	}

	public void setAndroidDownloadUrl(String androidDownloadUrl) {
		this.androidDownloadUrl = androidDownloadUrl;
	}

	public String getRepaymentUrl() {
		return repaymentUrl;
	}

	public void setRepaymentUrl(String repaymentUrl) {
		this.repaymentUrl = repaymentUrl;
	}

	

	public String getCarQueryPrice() {
		return carQueryPrice;
	}

	public void setCarQueryPrice(String carQueryPrice) {
		this.carQueryPrice = carQueryPrice;
	}

	@Override
	public String toString() {
		return "Brand [id=" + id + ", name=" + name + ", levelName=" + levelName + ", number=" + number + ", brandType="
				+ brandType + ", androidVersion=" + androidVersion + ", androidContent=" + androidContent
				+ ", androidDownload=" + androidDownload + ", androidDownloadUrl=" + androidDownloadUrl
				+ ", iosVersion=" + iosVersion + ", iosContent=" + iosContent + ", iosDownload=" + iosDownload
				+ ", repaymentUrl=" + repaymentUrl + ", shareTitle=" + shareTitle + ", shareLogoAddress="
				+ shareLogoAddress + ", shareMainAddress=" + shareMainAddress + ", shareContent=" + shareContent
				+ ", autoRebateConfigOnOff=" + autoRebateConfigOnOff + ", autoUpgrade=" + autoUpgrade
				+ ", autoUpgradePeople=" + autoUpgradePeople + ", appkey=" + appkey + ", mastersecret=" + mastersecret
				+ ", juhekey=" + juhekey + ", juheWzdjKey=" + juheWzdjKey + ", juheOpenid=" + juheOpenid
				+ ", brandPhone=" + brandPhone + ", brandQQ=" + brandQQ + ", brandWeiXin=" + brandWeiXin + ", tplid="
				+ tplid + ", createTime=" + createTime + ", manageid=" + manageid + ", equalRebateRate="
				+ equalRebateRate + ", rebateCount=" + rebateCount + ", isNewRebate=" + isNewRebate
				+ ", brandDescription=" + brandDescription + ", carQueryPrice=" + carQueryPrice
				+ ", activateTheUpgrade=" + activateTheUpgrade + ", weChatName=" + weChatName + ", weChatUrl="
				+ weChatUrl + ", maidProportion=" + maidProportion + "]";
	}

	

	

	
	
}
