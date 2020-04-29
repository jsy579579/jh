package com.jh.mircomall.bean;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable{
	
	static final long serialVersionUID = 1L;

	private Integer id;

    private String phone;

    private String password;

    private String payPassword;

    private String nickName;

    private String fullname;

    private String origcode;

    private String signcode;

    private String address;

    private String contactname;

    private String zipcode;

    private String userHeadUrl;

    private String province;

    private String city;

    private String county;

    private String email;

    private String sex;

    private String profession;

    private Date birthday;

    private String openid;

    private String unionid;

    private String grade;

    private String inviteCode;

    private Integer brandId;

    private String remarks;

    private String brandName;

    private Integer preUserId;

    private String preUserPhone;

    private Integer validStatus;

    private String realNameStatus;

    private String verifyStatus;

    private String vdynastType;

    private Integer encourageNum;

    private Integer bankCardManagerStatus;

    private String shopsStatus;

    private Date createTime;    


	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword == null ? null : payPassword.trim();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname == null ? null : fullname.trim();
    }

    public String getOrigcode() {
        return origcode;
    }

    public void setOrigcode(String origcode) {
        this.origcode = origcode == null ? null : origcode.trim();
    }

    public String getSigncode() {
        return signcode;
    }

    public void setSigncode(String signcode) {
        this.signcode = signcode == null ? null : signcode.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getContactname() {
        return contactname;
    }

    public void setContactname(String contactname) {
        this.contactname = contactname == null ? null : contactname.trim();
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode == null ? null : zipcode.trim();
    }

    public String getUserHeadUrl() {
        return userHeadUrl;
    }

    public void setUserHeadUrl(String userHeadUrl) {
        this.userHeadUrl = userHeadUrl == null ? null : userHeadUrl.trim();
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county == null ? null : county.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex == null ? null : sex.trim();
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession == null ? null : profession.trim();
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid == null ? null : unionid.trim();
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade == null ? null : grade.trim();
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode == null ? null : inviteCode.trim();
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? null : remarks.trim();
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName == null ? null : brandName.trim();
    }

    public Integer getPreUserId() {
        return preUserId;
    }

    public void setPreUserId(Integer preUserId) {
        this.preUserId = preUserId;
    }

    public String getPreUserPhone() {
        return preUserPhone;
    }

    public void setPreUserPhone(String preUserPhone) {
        this.preUserPhone = preUserPhone == null ? null : preUserPhone.trim();
    }

    public Integer getValidStatus() {
        return validStatus;
    }

    public void setValidStatus(Integer validStatus) {
        this.validStatus = validStatus;
    }

    public String getRealNameStatus() {
        return realNameStatus;
    }

    public void setRealNameStatus(String realNameStatus) {
        this.realNameStatus = realNameStatus == null ? null : realNameStatus.trim();
    }

    public String getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(String verifyStatus) {
        this.verifyStatus = verifyStatus == null ? null : verifyStatus.trim();
    }

    public String getVdynastType() {
        return vdynastType;
    }

    public void setVdynastType(String vdynastType) {
        this.vdynastType = vdynastType == null ? null : vdynastType.trim();
    }

    public Integer getEncourageNum() {
        return encourageNum;
    }

    public void setEncourageNum(Integer encourageNum) {
        this.encourageNum = encourageNum;
    }

    public Integer getBankCardManagerStatus() {
        return bankCardManagerStatus;
    }

    public void setBankCardManagerStatus(Integer bankCardManagerStatus) {
        this.bankCardManagerStatus = bankCardManagerStatus;
    }

    public String getShopsStatus() {
        return shopsStatus;
    }

    public void setShopsStatus(String shopsStatus) {
        this.shopsStatus = shopsStatus == null ? null : shopsStatus.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}