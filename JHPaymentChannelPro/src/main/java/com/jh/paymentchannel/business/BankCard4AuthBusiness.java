package com.jh.paymentchannel.business;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.jh.paymentchannel.pojo.BankCard4Auth;
import com.jh.paymentchannel.pojo.BankCardLocation;

public interface BankCard4AuthBusiness {

	
	public List<BankCard4Auth>  findAllBankCard4s(Pageable pageable);
	
	public Object bankCard4Auth(String mobile, String bankCard, String idcard, String realname);
	
	public List<BankCard4Auth>  findAllSuccessBankCard4s(Pageable pageable);
	
	public BankCard4Auth  findBankCard4AuthByCard(String bankcard);
	
	
	public BankCardLocation  findCardLocation(String cardid);
	
	public BankCard4Auth  findBankCard4AuthByMobile(String mobile);


    Object bankCard4AuthBackstage(String mobile, String bankcard, String idcard, String realname);
}
