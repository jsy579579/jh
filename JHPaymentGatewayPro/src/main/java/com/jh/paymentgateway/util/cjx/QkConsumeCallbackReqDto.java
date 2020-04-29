package com.jh.paymentgateway.util.cjx;

@SuppressWarnings("deprecation")
public class QkConsumeCallbackReqDto {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private String accUsrNo;		//接入用户编号
	private String trxChnNo;		//交易通道编号
	private String mrchntNo;		//交易商户编号
	private String trxTyp;			//交易类型代码
	private String trxId;			//交易流水号
	private Integer trxStatus;		//交易状态
	private String rpCd;			//交易应答码
	private String rpDesc;			//交易应答描述
	
	/**
	 * 接入用户编号
	 * @return
	 */
	public String getAccUsrNo() {
		return accUsrNo;
	}
	/**
	 * 接入用户编号
	 * @param accUsrNo
	 */
	public void setAccUsrNo(String accUsrNo) {
		this.accUsrNo = accUsrNo;
	}
	/**
	 * 交易通道编号
	 * @return
	 */
	public String getTrxChnNo() {
		return trxChnNo;
	}
	/**
	 * 交易通道编号
	 * @param trxChnNo
	 */
	public void setTrxChnNo(String trxChnNo) {
		this.trxChnNo = trxChnNo;
	}
	/**
	 * 交易商户编号
	 * @return
	 */
	public String getMrchntNo() {
		return mrchntNo;
	}
	/**
	 * 交易商户编号
	 * @param mrchntNo
	 */
	public void setMrchntNo(String mrchntNo) {
		this.mrchntNo = mrchntNo;
	}
	/**
	 * 交易类型代码
	 * @return
	 */
	public String getTrxTyp() {
		return trxTyp;
	}
	/**
	 * 交易类型代码
	 * @param trxTyp
	 */
	public void setTrxTyp(String trxTyp) {
		this.trxTyp = trxTyp;
	}
	/**
	 * 交易流水号
	 * @return
	 */
	public String getTrxId() {
		return trxId;
	}
	/**
	 * 交易流水号
	 * @param trxId
	 */
	public void setTrxId(String trxId) {
		this.trxId = trxId;
	}
	/**
	 * 交易状态
	 * @return
	 */
	public Integer getTrxStatus() {
		return trxStatus;
	}
	/**
	 * 交易状态
	 * @param trxStatus
	 */
	public void setTrxStatus(Integer trxStatus) {
		this.trxStatus = trxStatus;
	}
	/**
	 * 交易应答码
	 * @return
	 */
	public String getRpCd() {
		return rpCd;
	}
	/**
	 * 交易应答码
	 * @param rpCd
	 */
	public void setRpCd(String rpCd) {
		this.rpCd = rpCd;
	}
	/**
	 * 交易应答码描述
	 * @return
	 */
	public String getRpDesc() {
		return rpDesc;
	}
	/**
	 * 交易应答码描述
	 * @param rpDesc
	 */
	public void setRpDesc(String rpDesc) {
		this.rpDesc = rpDesc;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("QkConsumeCallbackReqDto [accUsrNo=");
		builder.append(accUsrNo);
		builder.append(", trxChnNo=");
		builder.append(trxChnNo);
		builder.append(", mrchntNo=");
		builder.append(mrchntNo);
		builder.append(", trxTyp=");
		builder.append(trxTyp);
		builder.append(", trxId=");
		builder.append(trxId);
		builder.append(", trxStatus=");
		builder.append(trxStatus);
		builder.append(", rpCd=");
		builder.append(rpCd);
		builder.append(", rpDesc=");
		builder.append(rpDesc);
		builder.append("]");
		return builder.toString();
	}
	
}
