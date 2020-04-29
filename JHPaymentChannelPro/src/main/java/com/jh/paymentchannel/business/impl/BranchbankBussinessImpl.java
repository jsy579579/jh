package com.jh.paymentchannel.business.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.pojo.Branchbank;
import com.jh.paymentchannel.repository.BatchNoRepository;
import com.jh.paymentchannel.repository.BranchNoRepository;
import com.jh.paymentchannel.repository.BranchbankRepository;
@Service
public class BranchbankBussinessImpl implements BranchbankBussiness{
	
	@Autowired
	BranchbankRepository bbr;
	@Autowired
	BranchNoRepository BranchNoRepository;
	@Autowired
	BatchNoRepository batchNoRepository;
	
	@Override
	public List<Branchbank> queryInfoBranch(String province, String city, String bankBranchname) {
		return bbr.queryInfoBranch(province, city, bankBranchname);
	}

	@Override
	public BranchNo findByBankName(String bankName) {
		return BranchNoRepository.findBankNoByBankName(bankName);
	}

	@Override
	public String getNumByName(String bankBranchName) {
		return bbr.getNumByName(bankBranchName);
	}

	@Override
	public BranchNo getJFMBankNoByBankName(String bankName) {
		return batchNoRepository.getJFMBankNoByBankName(bankName);
	}
}
