package com.jh.paymentgateway.util.hqb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepayItemVO {

    private String trade_time;
 
    private String transfer_time;

    private Long trade_amount;

    private Long transfer_amount;

    private Long fee;

    private String cooperator_item_id;
    
    private String mcc;
	
	
    /**
     * 扣款记录对应还款记录的订单号，代还模式为2时必填，如果是扣款记录则填此扣款记录对应的还款记录的订单号，如果是还款记录则填此还款记录的订单号
     */
    private String cooperator_transfer_item_id;

    /**
     * 标识代还记录是扣款还是还款，代还模式为2时必填，标识此代还记录是扣款还是还款：1-扣款， 2-还款
     */
    private Byte repay_item_type;
    
	public String getCooperator_transfer_item_id() {
		return cooperator_transfer_item_id;
	}

	public void setCooperator_transfer_item_id(String cooperator_transfer_item_id) {
		this.cooperator_transfer_item_id = cooperator_transfer_item_id;
	}

	public Byte getRepay_item_type() {
		return repay_item_type;
	}

	public void setRepay_item_type(Byte repay_item_type) {
		this.repay_item_type = repay_item_type;
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

	public Long getTrade_amount() {
		return trade_amount;
	}

	public void setTrade_amount(Long trade_amount) {
		this.trade_amount = trade_amount;
	} 
 
	public Long getTransfer_amount() {
		return transfer_amount;
	}

	public void setTransfer_amount(Long transfer_amount) {
		this.transfer_amount = transfer_amount;
	}

	public Long getFee() {
		return fee;
	}

	public void setFee(Long fee) {
		this.fee = fee;
	}

	public String getCooperator_item_id() {
		return cooperator_item_id;
	}

	public void setCooperator_item_id(String cooperator_item_id) {
		this.cooperator_item_id = cooperator_item_id;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	@Override
	public String toString() {
		return "RepayItemVO [trade_time=" + trade_time + ",transfer_time="
				+ transfer_time + ",trade_amount=" + trade_amount
				+ ",transfer_amount=" + transfer_amount + ",fee=" + fee
				+ ",cooperator_item_id=" + cooperator_item_id + ",mcc=" + mcc
				+ ",cooperator_transfer_item_id="
				+ cooperator_transfer_item_id + ",repay_item_type="
				+ repay_item_type + "]";
	}
 
	/*@Override
	public String toString() {
		return "RepayItemVO [trade_time=" + trade_time + ", transfer_time="
				+ transfer_time + ", trade_amount=" + trade_amount
				+ ", transfer_amount=" + transfer_amount + ", fee=" + fee
				+ ", cooperator_item_id=" + cooperator_item_id + ", mcc=" + mcc
				+ ", cooperator_transfer_item_id="
				+ cooperator_transfer_item_id + ", repay_item_type="
				+ repay_item_type + "]";
	}*/
	
	

}
