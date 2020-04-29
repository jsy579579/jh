package com.jh.paymentchannel.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.pojo.BranchNo;

import cn.jh.common.utils.CommonConstants;
@Controller
@EnableAutoConfiguration
public class BranchNoService {
	@Autowired
	private BranchbankBussiness branchbankBussiness;
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/branchno/findbybankname")
	public @ResponseBody Object findByBankName(HttpServletRequest request,
			@RequestParam(value = "bankName") String bankName) {

		Map map = new HashMap();
		BranchNo branchNo = branchbankBussiness.findByBankName(bankName);

		if (branchNo == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "根据银行名称查询不到银行代码");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, branchNo);
		return map;
	}
}
