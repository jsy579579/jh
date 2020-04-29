package com.jh.paymentgateway.controller.yxe.domain;

public class JiFuPayMsg {
	private String merchantCode; //注册商户号
	private String openCardId;   //开通支付卡返回绑卡ID
	private String feeRate;      //支付费率
	private String settFee;      //结算手续费
    private String userId;       //用户id

	private String debitRate;                  //借记卡费率
	private String creditRate;                 //信用卡费率
	private String withdrawDepositRateD0;      //D0提现费率
	private String withdrawDepositSingleFeeD0; //D0提现手续费
	private String withdrawDepositRateT1;      //T1提现费率(非必填)
	private String withdrawDepositSingleFeeT1; //T1提现手续费(非必填)

	public String getDebitRate() {
		return debitRate;
	}

	public void setDebitRate(String debitRate) {
		this.debitRate = debitRate;
	}

	public String getCreditRate() {
		return creditRate;
	}

	public void setCreditRate(String creditRate) {
		this.creditRate = creditRate;
	}

	public String getWithdrawDepositRateD0() {
		return withdrawDepositRateD0;
	}

	public void setWithdrawDepositRateD0(String withdrawDepositRateD0) {
		this.withdrawDepositRateD0 = withdrawDepositRateD0;
	}

	public String getWithdrawDepositSingleFeeD0() {
		return withdrawDepositSingleFeeD0;
	}

	public void setWithdrawDepositSingleFeeD0(String withdrawDepositSingleFeeD0) {
		this.withdrawDepositSingleFeeD0 = withdrawDepositSingleFeeD0;
	}

	public String getWithdrawDepositRateT1() {
		return withdrawDepositRateT1;
	}

	public void setWithdrawDepositRateT1(String withdrawDepositRateT1) {
		this.withdrawDepositRateT1 = withdrawDepositRateT1;
	}

	public String getWithdrawDepositSingleFeeT1() {
		return withdrawDepositSingleFeeT1;
	}

	public void setWithdrawDepositSingleFeeT1(String withdrawDepositSingleFeeT1) {
		this.withdrawDepositSingleFeeT1 = withdrawDepositSingleFeeT1;
	}

	public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMerchantCode() {
		return merchantCode;
	}
	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}
	public String getOpenCardId() {
		return openCardId;
	}
	public void setOpenCardId(String openCardId) {
		this.openCardId = openCardId;
	}
	public String getFeeRate() {
		return feeRate;
	}
	public void setFeeRate(String feeRate) {
		this.feeRate = feeRate;
	}
	public String getSettFee() {
		return settFee;
	}
	public void setSettFee(String settFee) {
		this.settFee = settFee;
	}
	
}
