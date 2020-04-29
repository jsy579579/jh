package com.jh.user.business;

import java.util.List;

import com.jh.user.pojo.Branchbank;


public interface BranchbankBussiness {
	public List<Branchbank> queryInfoBranch(String province,String city,String bankBranchname);
}
