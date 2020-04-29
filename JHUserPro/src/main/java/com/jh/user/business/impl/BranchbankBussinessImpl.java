package com.jh.user.business.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.user.business.BranchbankBussiness;
import com.jh.user.pojo.Branchbank;
import com.jh.user.repository.BranchbankRepository;

@Service
public class BranchbankBussinessImpl implements BranchbankBussiness{
	
	@Autowired
	BranchbankRepository bbr;
	
	@Override
	public List<Branchbank> queryInfoBranch(String province, String city, String bankBranchname) {
		// TODO Auto-generated method stub
		return bbr.queryInfoBranch(province, city, bankBranchname);
	}

}
