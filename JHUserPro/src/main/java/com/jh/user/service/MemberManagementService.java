package com.jh.user.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.business.MemberManagementBusiness;

import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class MemberManagementService {
	private static final Logger LOG = LoggerFactory.getLogger(MemberManagementService.class);

	@Autowired
	MemberManagementBusiness memberManagementBusiness;

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/queryAll")
	public @ResponseBody Object pageAllUserQuery(@RequestParam(value = "brand_id", required = false) String brandId,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "user_name", required = false) String userName,
			@RequestParam(value = "real_status", required = false) String realStatus,
			@RequestParam(value = "grade", required = false) String grade,
			@RequestParam(value = "role", required = false) String role,
			@RequestParam(value = "current_page", defaultValue = "0", required = false) int currentPage,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "profit_desc", required = false) String profitDesc,
			@RequestParam(value = "referrals_desc",required = false) String referralsDesc) {
		LOG.info("条件筛选：===贴牌：" + brandId + "===手机号：" + phone + "===姓名：" + userName + "===实名状态：" + realStatus + "===等级："
				+ grade + "===代理商：" + role);
		if (size > 1000) {
			size = 1000;
		}
		Pageable pageAble = new PageRequest(currentPage, size);
        
		Map<String, Object> list = memberManagementBusiness.queryAlluser(brandId, phone, userName, grade, realStatus,
				role, profitDesc, referralsDesc, pageAble);
		Map<String, Object> maps = new HashMap<>();
		maps.put(CommonConstants.RESULT, list);
		return maps;
	}

}
