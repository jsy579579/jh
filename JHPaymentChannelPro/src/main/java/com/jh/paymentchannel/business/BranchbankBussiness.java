package com.jh.paymentchannel.business;

import java.util.List;

import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.pojo.Branchbank;

public interface BranchbankBussiness {
	public List<Branchbank> queryInfoBranch(String province,String city,String bankBranchname);

	public BranchNo findByBankName(String bankName);

	public String getNumByName(String bankBranchName);
	
	public BranchNo getJFMBankNoByBankName(String bankName);

}
