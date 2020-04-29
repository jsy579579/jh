package com.jh.mircomall.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Brand implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer id;

    private String name;

    private String number;

    private String brandType;

    private String versionAndroid;

    private String androidContent;

    private String downloadAndroid;

    private String versionIos;

    private String downloadIos;

    private String iosContent;

    private String weChatName;

    private String weChatUrl;

    private String shareTitle;

    private String shareLogoAddress;

    private String shareMainAddress;

    private String shareContent;

    private String remarks;

    private String autoUpgrade;

    private String activateTheUpgrade;

    private String jdAppkey;

    private String brandPhone;

    private String brandQq;

    private String brandWeixin;

    private String juheBillKey;

    private String juheOpenid;

    private String jdMastersecret;

    private String juheWzdjKey;

    private String tplId;

    private String levelName;

    private Integer rebateCount;

    private Integer isNewRebate;

    private BigDecimal equalRebateRate;

    private Integer autoRebateConfigOnOff;

    private Integer autoUpgradePeople;

    private Integer manageId;

    private String brandDescription;

    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number == null ? null : number.trim();
    }

    public String getBrandType() {
        return brandType;
    }

    public void setBrandType(String brandType) {
        this.brandType = brandType == null ? null : brandType.trim();
    }

    public String getVersionAndroid() {
        return versionAndroid;
    }

    public void setVersionAndroid(String versionAndroid) {
        this.versionAndroid = versionAndroid == null ? null : versionAndroid.trim();
    }

    public String getAndroidContent() {
        return androidContent;
    }

    public void setAndroidContent(String androidContent) {
        this.androidContent = androidContent == null ? null : androidContent.trim();
    }

    public String getDownloadAndroid() {
        return downloadAndroid;
    }

    public void setDownloadAndroid(String downloadAndroid) {
        this.downloadAndroid = downloadAndroid == null ? null : downloadAndroid.trim();
    }

    public String getVersionIos() {
        return versionIos;
    }

    public void setVersionIos(String versionIos) {
        this.versionIos = versionIos == null ? null : versionIos.trim();
    }

    public String getDownloadIos() {
        return downloadIos;
    }

    public void setDownloadIos(String downloadIos) {
        this.downloadIos = downloadIos == null ? null : downloadIos.trim();
    }

    public String getIosContent() {
        return iosContent;
    }

    public void setIosContent(String iosContent) {
        this.iosContent = iosContent == null ? null : iosContent.trim();
    }

    public String getWeChatName() {
        return weChatName;
    }

    public void setWeChatName(String weChatName) {
        this.weChatName = weChatName == null ? null : weChatName.trim();
    }

    public String getWeChatUrl() {
        return weChatUrl;
    }

    public void setWeChatUrl(String weChatUrl) {
        this.weChatUrl = weChatUrl == null ? null : weChatUrl.trim();
    }

    public String getShareTitle() {
        return shareTitle;
    }

    public void setShareTitle(String shareTitle) {
        this.shareTitle = shareTitle == null ? null : shareTitle.trim();
    }

    public String getShareLogoAddress() {
        return shareLogoAddress;
    }

    public void setShareLogoAddress(String shareLogoAddress) {
        this.shareLogoAddress = shareLogoAddress == null ? null : shareLogoAddress.trim();
    }

    public String getShareMainAddress() {
        return shareMainAddress;
    }

    public void setShareMainAddress(String shareMainAddress) {
        this.shareMainAddress = shareMainAddress == null ? null : shareMainAddress.trim();
    }

    public String getShareContent() {
        return shareContent;
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent == null ? null : shareContent.trim();
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? null : remarks.trim();
    }

    public String getAutoUpgrade() {
        return autoUpgrade;
    }

    public void setAutoUpgrade(String autoUpgrade) {
        this.autoUpgrade = autoUpgrade == null ? null : autoUpgrade.trim();
    }

    public String getActivateTheUpgrade() {
        return activateTheUpgrade;
    }

    public void setActivateTheUpgrade(String activateTheUpgrade) {
        this.activateTheUpgrade = activateTheUpgrade == null ? null : activateTheUpgrade.trim();
    }

    public String getJdAppkey() {
        return jdAppkey;
    }

    public void setJdAppkey(String jdAppkey) {
        this.jdAppkey = jdAppkey == null ? null : jdAppkey.trim();
    }

    public String getBrandPhone() {
        return brandPhone;
    }

    public void setBrandPhone(String brandPhone) {
        this.brandPhone = brandPhone == null ? null : brandPhone.trim();
    }

    public String getBrandQq() {
        return brandQq;
    }

    public void setBrandQq(String brandQq) {
        this.brandQq = brandQq == null ? null : brandQq.trim();
    }

    public String getBrandWeixin() {
        return brandWeixin;
    }

    public void setBrandWeixin(String brandWeixin) {
        this.brandWeixin = brandWeixin == null ? null : brandWeixin.trim();
    }

    public String getJuheBillKey() {
        return juheBillKey;
    }

    public void setJuheBillKey(String juheBillKey) {
        this.juheBillKey = juheBillKey == null ? null : juheBillKey.trim();
    }

    public String getJuheOpenid() {
        return juheOpenid;
    }

    public void setJuheOpenid(String juheOpenid) {
        this.juheOpenid = juheOpenid == null ? null : juheOpenid.trim();
    }

    public String getJdMastersecret() {
        return jdMastersecret;
    }

    public void setJdMastersecret(String jdMastersecret) {
        this.jdMastersecret = jdMastersecret == null ? null : jdMastersecret.trim();
    }

    public String getJuheWzdjKey() {
        return juheWzdjKey;
    }

    public void setJuheWzdjKey(String juheWzdjKey) {
        this.juheWzdjKey = juheWzdjKey == null ? null : juheWzdjKey.trim();
    }

    public String getTplId() {
        return tplId;
    }

    public void setTplId(String tplId) {
        this.tplId = tplId == null ? null : tplId.trim();
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName == null ? null : levelName.trim();
    }

    public Integer getRebateCount() {
        return rebateCount;
    }

    public void setRebateCount(Integer rebateCount) {
        this.rebateCount = rebateCount;
    }

    public Integer getIsNewRebate() {
        return isNewRebate;
    }

    public void setIsNewRebate(Integer isNewRebate) {
        this.isNewRebate = isNewRebate;
    }

    public BigDecimal getEqualRebateRate() {
        return equalRebateRate;
    }

    public void setEqualRebateRate(BigDecimal equalRebateRate) {
        this.equalRebateRate = equalRebateRate;
    }

    public Integer getAutoRebateConfigOnOff() {
        return autoRebateConfigOnOff;
    }

    public void setAutoRebateConfigOnOff(Integer autoRebateConfigOnOff) {
        this.autoRebateConfigOnOff = autoRebateConfigOnOff;
    }

    public Integer getAutoUpgradePeople() {
        return autoUpgradePeople;
    }

    public void setAutoUpgradePeople(Integer autoUpgradePeople) {
        this.autoUpgradePeople = autoUpgradePeople;
    }

    public Integer getManageId() {
        return manageId;
    }

    public void setManageId(Integer manageId) {
        this.manageId = manageId;
    }

    public String getBrandDescription() {
        return brandDescription;
    }

    public void setBrandDescription(String brandDescription) {
        this.brandDescription = brandDescription == null ? null : brandDescription.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}