package com.jh.channel.pojo;


import java.io.Serializable;


public class OutMerchantPayRequest implements Serializable {

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
    //同步回调URL
    private String returnURL;
    //异步回调
    private String notifyURL;
    //签名方式 MD5
    private String signType;
    //签名
    private String sign;
   //商户订单号
    private String tradeNo;
    //订单描述
    private String body;
    //额外分组
    private String extraParam;
    //订单金额
    private String totalFee;
    //币种
    private String feeType;
    //商户Ip
    private String merchantIP;
    //真是姓名
    private String userName;
    //借记卡名称
    private String debitCardName;
    //借记卡身份证号
    private String debitCardIdentityNo;
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
    

    private String isPublic;
    //信用卡名称
    private String creditCardName;
    //信用卡身份证号
    private String creditCardIdentityNo;
    //信用卡卡号
    private String creditCardNo;
    //信用卡绑定手机号
    private String creditCardMobile;
    //信用卡有效期
    private String creditCardExpireDate;
    //信用卡安全码
    private String creditCardCvn;
    //交易手机号
    private String tradeMobile;
    //验证码
    private String smsCode;
    //交易IP
    private String merchantIp;

    private String transactionId;


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


    public String getTradeNo() {
        return tradeNo;
    }


    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }


    public String getBody() {
        return body;
    }


    public void setBody(String body) {
        this.body = body;
    }


    public String getExtraParam() {
        return extraParam;
    }


    public void setExtraParam(String extraParam) {
        this.extraParam = extraParam;
    }


    public String getTotalFee() {
        return totalFee;
    }


    public void setTotalFee(String totalFee) {
        this.totalFee = totalFee;
    }


    public String getFeeType() {
        return feeType;
    }


    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }


    public String getMerchantIP() {
        return merchantIP;
    }


    public void setMerchantIP(String merchantIP) {
        this.merchantIP = merchantIP;
    }


    public String getDebitCardName() {
        return debitCardName;
    }


    public void setDebitCardName(String debitCardName) {
        this.debitCardName = debitCardName;
    }


    public String getDebitCardIdentityNo() {
        return debitCardIdentityNo;
    }


    public void setDebitCardIdentityNo(String debitCardIdentityNo) {
        this.debitCardIdentityNo = debitCardIdentityNo;
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


    public String getIsPublic() {
        return isPublic;
    }


    public void setIsPublic(String isPublic) {
        this.isPublic = isPublic;
    }


    public String getCreditCardName() {
        return creditCardName;
    }


    public void setCreditCardName(String creditCardName) {
        this.creditCardName = creditCardName;
    }


    public String getCreditCardIdentityNo() {
        return creditCardIdentityNo;
    }


    public void setCreditCardIdentityNo(String creditCardIdentityNo) {
        this.creditCardIdentityNo = creditCardIdentityNo;
    }


    public String getCreditCardNo() {
        return creditCardNo;
    }


    public void setCreditCardNo(String creditCardNo) {
        this.creditCardNo = creditCardNo;
    }


    public String getCreditCardMobile() {
        return creditCardMobile;
    }


    public void setCreditCardMobile(String creditCardMobile) {
        this.creditCardMobile = creditCardMobile;
    }


    public String getCreditCardExpireDate() {
        return creditCardExpireDate;
    }


    public void setCreditCardExpireDate(String creditCardExpireDate) {
        this.creditCardExpireDate = creditCardExpireDate;
    }


    public String getCreditCardCvn() {
        return creditCardCvn;
    }


    public void setCreditCardCvn(String creditCardCvn) {
        this.creditCardCvn = creditCardCvn;
    }


    public String getTradeMobile() {
        return tradeMobile;
    }


    public void setTradeMobile(String tradeMobile) {
        this.tradeMobile = tradeMobile;
    }


    public String getSmsCode() {
        return smsCode;
    }


    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }


    public String getMerchantIp() {
        return merchantIp;
    }


    public void setMerchantIp(String merchantIp) {
        this.merchantIp = merchantIp;
    }


    public String getTransactionId() {
        return transactionId;
    }


    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}
    
    
}
