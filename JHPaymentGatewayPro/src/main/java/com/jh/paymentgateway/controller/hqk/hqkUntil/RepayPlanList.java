package com.jh.paymentgateway.controller.hqk.hqkUntil;


public class RepayPlanList {

	private String tradeTime;
	private String transferTime;
	private String tradeMoney;
	private String transferMoney;
	private String rateMoney;
	private String repayOrderId;
	private String transferRepayOrderId;
	private String repayOrderFlag;
	
	
    private String trade_time;
 
    private String transfer_time; 

    private String trade_amount;

    private String transfer_amount;

    private String fee;
    
//    标识代还记录是扣款还是还款，代还模式为2时必填，标识此代还记录是扣款还是还款：1-扣款， 2-还款
    private String repay_item_type;
//    合作商每笔代扣代还的订单号 
    private String cooperator_item_id;
//    扣款记录对应还款记录的订单号，代还模式为2时必填，如果是扣款记录则填此扣款记录对应的还款记录的合作商单笔订单号，如果是还款记录则填此还款记录的单笔订单号  
    private String cooperator_transfer_item_id;

    
	public String getRepay_item_type() {
		return repay_item_type;
	}

	public void setRepay_item_type(String repay_item_type) {
		this.repay_item_type = repay_item_type;
	}

	public String getCooperator_item_id() {
		return cooperator_item_id;
	}

	public void setCooperator_item_id(String cooperator_item_id) {
		this.cooperator_item_id = cooperator_item_id;
	}

	public String getCooperator_transfer_item_id() {
		return cooperator_transfer_item_id;
	}

	public void setCooperator_transfer_item_id(String cooperator_transfer_item_id) {
		this.cooperator_transfer_item_id = cooperator_transfer_item_id;
	}

	public String getTrade_time() {
		return trade_time;
	}

	public void setTrade_time(String trade_time) {
		this.trade_time = trade_time;
	}

	public String getTransfer_time() {
		return transfer_time;
	}

	public void setTransfer_time(String transfer_time) {
		this.transfer_time = transfer_time;
	}

	
	public String getTrade_amount() {
		return trade_amount;
	}

	public void setTrade_amount(String trade_amount) {
		this.trade_amount = trade_amount;
	}

	public String getTransfer_amount() {
		return transfer_amount;
	}

	public void setTransfer_amount(String transfer_amount) {
		this.transfer_amount = transfer_amount;
	}

	public String getFee() {
		return fee;
	}

	public void setFee(String fee) {
		this.fee = fee;
	}

	public String getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(String tradeTime) {
		this.tradeTime = tradeTime;
	}

	public String getTransferTime() {
		return transferTime;
	}

	public void setTransferTime(String transferTime) {
		this.transferTime = transferTime;
	}

	public String getTradeMoney() {
		return tradeMoney;
	}

	public void setTradeMoney(String tradeMoney) {
		this.tradeMoney = tradeMoney;
	}

	public String getTransferMoney() {
		return transferMoney;
	}

	public void setTransferMoney(String transferMoney) {
		this.transferMoney = transferMoney;
	}

	public String getRateMoney() {
		return rateMoney;
	}

	public void setRateMoney(String rateMoney) {
		this.rateMoney = rateMoney;
	}

	public String getRepayOrderId() {
		return repayOrderId;
	}

	public void setRepayOrderId(String repayOrderId) {
		this.repayOrderId = repayOrderId;
	}

	public String getTransferRepayOrderId() {
		return transferRepayOrderId;
	}

	public void setTransferRepayOrderId(String transferRepayOrderId) {
		this.transferRepayOrderId = transferRepayOrderId;
	}

	public String getRepayOrderFlag() {
		return repayOrderFlag;
	}

	public void setRepayOrderFlag(String repayOrderFlag) {
		this.repayOrderFlag = repayOrderFlag;
	}

}
