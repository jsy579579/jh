package com.jh.paymentchannel.util.jp;

public class BingCardMessageParam {
	public String no;
	public String bankCode;
	public String bankName;
	public String bankNo;
	public String cardName;
	public String cardNo;
	public String cardPhone;
	public String cardIdentity;
	public String validCode;
	public String validDates;
	
	@Override
	public String toString() {
		return "BingCardMessageParam [no=" + no + ", bankCode=" + bankCode + ", bankName=" + bankName + ", bankNo="
				+ bankNo + ", cardName=" + cardName + ", cardNo=" + cardNo + ", cardPhone=" + cardPhone
				+ ", cardIdentity=" + cardIdentity + ", validCode=" + validCode + ", validDates=" + validDates + "]";
	}
	
	
}
