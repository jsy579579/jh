package com.jh.user.business;

import java.util.Map;

import org.springframework.data.domain.Pageable;


public interface MemberManagementBusiness {
	public Map<String, Object> queryAlluser(String brandId, String phone, String userName, String grade, String realStatus,
			String role, String profitDesc, String referralsDesc,Pageable pageAble);
}
