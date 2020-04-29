package com.jh.user.pojo;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_merchant")
public class Merchant implements Serializable{
	
	private static final long serialVersionUID = 1L;
   
	@Id
    @Column(name="id")
	private long id;
    
    /**商户号*/
    @Column(name="mch_id")
    private String mchId;
     
    /**登陆密码*/
    @Column(name="password")
    private String password;
    
    /**密钥*/
    @Column(name="pre_key")
    private String premchkey;
    
    /**名称*/
    @Column(name="fullname")
    private String fullName;
    
    /**上级商户号*/
    @Column(name="pre_mch_id")
    private String preMchId;
    
    /**手机号*/
    @Column(name = "phone")
    private String phone;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMchId() {
		return mchId;
	}

	public void setMchId(String mchId) {
		this.mchId = mchId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPremchkey() {
		return premchkey;
	}

	public void setPremchkey(String premchkey) {
		this.premchkey = premchkey;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPreMchId() {
		return preMchId;
	}

	public void setPreMchId(String preMchId) {
		this.preMchId = preMchId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
    
}
