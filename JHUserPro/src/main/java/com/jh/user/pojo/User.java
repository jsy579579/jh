package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_user")
public class User implements Serializable {

	private static final long serialversionuid = 1L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "phone")
	private String phone;// 手机号

	@Column(name = "password")
	private String password;// 密码

	@Column(name = "pay_password")
	private String paypass;// 支付提现密码

	@Column(name = "nick_name")
	private String nickName;// 昵称/简称

	/** 全称 */
	@Column(name = "fullname")
	private String fullname;
	
	@Transient
	private String realname;

	/** 组织机构码 */
	@Column(name = "origcode")
	private String origcode;

	/** 登记码 */
	@Column(name = "signcode")
	private String signcode;

	/** 地址 */
	@Column(name = "address")
	private String address;

	/** 联系人 */
	@Column(name = "contactname")
	private String contactname;

	@Column(name = "zipcode")
	private String zipcode;

	@Column(name = "user_head_url")
	private String userHeadUrl;// 用户头像

	@Column(name = "province")
	private String province;// 省份

	@Column(name = "city")
	private String city;// 城市

	@Column(name = "county")
	private String county;// 县/区

	@Column(name = "email")
	private String email;// 邮箱

	@Column(name = "sex")
	private String sex;// 性别

	@Column(name = "profession")
	private String profession;// 职业

	@Column(name = "birthday")
	private Date birthday;// 生日

	@Column(name = "openid")
	private String openid;// 微信中针对一个公众号的用户唯一标志，公众号获取和微信登录获取不一样

	@Column(name = "unionid")
	private String unionid;// 微信中各处针对用户唯一标志，公众号获取和微信登录获取一样的

	@Transient
	private String userToken;
	// 信用卡管家的激活状态: 0 未激活,1已激活
	@Column(name = "bank_card_manager_status")
	private Integer bankCardManagerStatus=0;

	@Column(name = "valid_status")
	private Integer validStatus;// 0无效，1有效encourage

	@Column(name = "encourage_num")
	private int encourageNum;// 抽奖次数

	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;// 创建日期

	/** 邀请码 */
	@Column(name = "invite_code")
	private String inviteCode;

	/** 所属品牌 */
	@Column(name = "brand_id")
	private long brandId;

	/** 所属品牌 */
	@Column(name = "brand_name")
	private String brandname;

	/** 用户等级 三级分销 3是最高级 2是中级 1是低级 0不具备三级分销等级的 */
	@Column(name = "grade")
	private String grade = "0";

	@Column(name="older")
	private int older= 0;

	/** 邀请自己的代理商 */
	@Column(name = "pre_user_id")
	private long preUserId;

	@Column(name = "pre_user_phone")
	private String preUserPhone;

	/** 0表示审核中 1表示审核通过 2表示审核拒绝 */
	@Column(name = "real_name_status")
	private String realnameStatus = "3";

	/** 0表示审核中 1表示审核通过 2表示审核拒绝 3表示未提交 */
	@Column(name = "shops_status")
	private String shopsStatus = "3";

	/** 是否购买过 */
	@Column(name = "verify_status")
	private String verifyStatus = "0";

	/**是否是统治者
	 * 
	 * 0会员；1；品牌商；2机构商
	 * */
	@Column(name = "vdynast_type")
	private String vdynastType = "0";
	
	/** 备注 */
	@Column(name = "remarks")
	private String remarks;


	public int getOlder() {
		return older;
	}

	public void setOlder(int older) {
		this.older = older;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getProfession() {
		return profession;
	}

	public void setProfession(String profession) {
		this.profession = profession;
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
		this.openid = openid;
	}

	public String getUnionid() {
		return unionid;
	}

	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}

	public String getUserToken() {
		return userToken;
	}

	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUserHeadUrl() {
		return userHeadUrl;
	}

	public void setUserHeadUrl(String userHeadUrl) {
		this.userHeadUrl = userHeadUrl;
	}

	public Integer getValidStatus() {
		return validStatus;
	}

	public void setValidStatus(Integer validStatus) {
		this.validStatus = validStatus;
	}

	public String getVdynastType() {
		return vdynastType;
	}

	public void setVdynastType(String vdynastType) {
		this.vdynastType = vdynastType;
	}

	public String getPaypass() {
		return paypass;
	}

	public void setPaypass(String paypass) {
		this.paypass = paypass;
	}

	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public long getBrandId() {
		return brandId;
	}

	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}

	public long getPreUserId() {
		return preUserId;
	}

	public void setPreUserId(long preUserId) {
		this.preUserId = preUserId;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getPreUserPhone() {
		return preUserPhone;
	}

	public void setPreUserPhone(String preUserPhone) {
		this.preUserPhone = preUserPhone;
	}

	public String getBrandname() {
		return brandname;
	}

	public void setBrandname(String brandname) {
		this.brandname = brandname;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getOrigcode() {
		return origcode;
	}

	public void setOrigcode(String origcode) {
		this.origcode = origcode;
	}

	public String getSigncode() {
		return signcode;
	}

	public void setSigncode(String signcode) {
		this.signcode = signcode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContactname() {
		return contactname;
	}

	public void setContactname(String contactname) {
		this.contactname = contactname;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getRealnameStatus() {
		return realnameStatus;
	}

	public void setRealnameStatus(String realnameStatus) {
		this.realnameStatus = realnameStatus;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getVerifyStatus() {
		return verifyStatus;
	}

	public void setVerifyStatus(String verifyStatus) {
		this.verifyStatus = verifyStatus;
	}

	public String getShopsStatus() {
		return shopsStatus;
	}

	public void setShopsStatus(String shopsStatus) {
		this.shopsStatus = shopsStatus;
	}

	public int getEncourageNum() {
		return encourageNum;
	}

	public void setEncourageNum(int encourageNum) {
		this.encourageNum = encourageNum;
	}

	public Integer getBankCardManagerStatus() {
		return bankCardManagerStatus;
	}

	public void setBankCardManagerStatus(Integer bankCardManagerStatus) {
		this.bankCardManagerStatus = bankCardManagerStatus;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", phone='" + phone + '\'' +
				", password='" + password + '\'' +
				", paypass='" + paypass + '\'' +
				", nickName='" + nickName + '\'' +
				", fullname='" + fullname + '\'' +
				", realname='" + realname + '\'' +
				", origcode='" + origcode + '\'' +
				", signcode='" + signcode + '\'' +
				", address='" + address + '\'' +
				", contactname='" + contactname + '\'' +
				", zipcode='" + zipcode + '\'' +
				", userHeadUrl='" + userHeadUrl + '\'' +
				", province='" + province + '\'' +
				", city='" + city + '\'' +
				", county='" + county + '\'' +
				", email='" + email + '\'' +
				", sex='" + sex + '\'' +
				", profession='" + profession + '\'' +
				", birthday=" + birthday +
				", openid='" + openid + '\'' +
				", unionid='" + unionid + '\'' +
				", userToken='" + userToken + '\'' +
				", bankCardManagerStatus=" + bankCardManagerStatus +
				", validStatus=" + validStatus +
				", encourageNum=" + encourageNum +
				", createTime=" + createTime +
				", inviteCode='" + inviteCode + '\'' +
				", brandId=" + brandId +
				", brandname='" + brandname + '\'' +
				", grade='" + grade + '\'' +
				", older=" + older +
				", preUserId=" + preUserId +
				", preUserPhone='" + preUserPhone + '\'' +
				", realnameStatus='" + realnameStatus + '\'' +
				", shopsStatus='" + shopsStatus + '\'' +
				", verifyStatus='" + verifyStatus + '\'' +
				", vdynastType='" + vdynastType + '\'' +
				", remarks='" + remarks + '\'' +
				'}';
	}

	public static long getSerialversionuid() {
		return serialversionuid;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

}
