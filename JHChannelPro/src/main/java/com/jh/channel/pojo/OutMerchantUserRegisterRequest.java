package com.jh.channel.pojo;


import java.io.Serializable;

/****
 * 
 * 用户进件表格
 * 
 * **/
public class OutMerchantUserRegisterRequest implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    //接口——通道标识
    private String service;
    //版本号
    private String version;
    //编码
    private String charset;
    //商户号
    private String merchantId;
    //商注册手机号
    private String registerPhone;
    //姓名
    private String realname;
    //身份证
    private String idcard;
    
    //同步回调URL
    private String returnURL;
    //异步回调
    private String notifyURL;
    //签名方式 MD5
    private String signType;
    //签名
    private String sign;
    //借记卡名称
    private String debitCardName;
    //借记卡卡号
    private String debitCardNo;
    //借记卡绑定手机号
    private String debitCardMobile;
    
    /**开户行所在省份*/
    private String debitProvince;
    
    /**开户行所在市*/
    private String debitCity;
    
    /**详细地址*/
    private String debitAddress;
    
    
    /**联行行号*/
    private String debitBankBranchId;
    
    
    /**开户支行名称*/
    private String debitBranchName;
    

    //子商户编号
    private String premerchantid;

    /**刷卡费率**/
    private String premerchantRate;

    public String getService() {
        return service;
    }


    public void setService(String service) {
        this.service = service;
    }


    public String getVersion() {
        return version;
    }


    public void setVersion(String version) {
        this.version = version;
    }


    public String getCharset() {
        return charset;
    }


    public void setCharset(String charset) {
        this.charset = charset;
    }


    public String getMerchantId() {
        return merchantId;
    }


    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }


    public String getRegisterPhone() {
		return registerPhone;
	}


	public void setRegisterPhone(String registerPhone) {
		this.registerPhone = registerPhone;
	}


	public String getRealname() {
		return realname;
	}


	public void setRealname(String realname) {
		this.realname = realname;
	}


	public String getIdcard() {
		return idcard;
	}


	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}


	public String getReturnURL() {
        return returnURL;
    }


    public void setReturnURL(String returnURL) {
        this.returnURL = returnURL;
    }


    public String getNotifyURL() {
        return notifyURL;
    }


    public void setNotifyURL(String notifyURL) {
        this.notifyURL = notifyURL;
    }


    public String getSignType() {
        return signType;
    }


    public void setSignType(String signType) {
        this.signType = signType;
    }


    public String getSign() {
        return sign;
    }


    public void setSign(String sign) {
        this.sign = sign;
    }



    public String getDebitCardName() {
        return debitCardName;
    }


    public void setDebitCardName(String debitCardName) {
        this.debitCardName = debitCardName;
    }


    public String getDebitCardNo() {
        return debitCardNo;
    }


    public void setDebitCardNo(String debitCardNo) {
        this.debitCardNo = debitCardNo;
    }


    public String getDebitCardMobile() {
        return debitCardMobile;
    }


    public void setDebitCardMobile(String debitCardMobile) {
        this.debitCardMobile = debitCardMobile;
    }

	public String getDebitProvince() {
		return debitProvince;
	}


	public void setDebitProvince(String debitProvince) {
		this.debitProvince = debitProvince;
	}


	public String getDebitCity() {
		return debitCity;
	}


	public void setDebitCity(String debitCity) {
		this.debitCity = debitCity;
	}


	public String getDebitAddress() {
		return debitAddress;
	}


	public void setDebitAddress(String debitAddress) {
		this.debitAddress = debitAddress;
	}


	public String getDebitBankBranchId() {
		return debitBankBranchId;
	}


	public void setDebitBankBranchId(String debitBankBranchId) {
		this.debitBankBranchId = debitBankBranchId;
	}


	public String getDebitBranchName() {
		return debitBranchName;
	}


	public void setDebitBranchName(String debitBranchName) {
		this.debitBranchName = debitBranchName;
	}


	public String getPremerchantid() {
		return premerchantid;
	}


	public void setPremerchantid(String premerchantid) {
		this.premerchantid = premerchantid;
	}


	public String getPremerchantRate() {
		return premerchantRate;
	}


	public void setPremerchantRate(String premerchantRate) {
		this.premerchantRate = premerchantRate;
	}
    
    
    
    

}
