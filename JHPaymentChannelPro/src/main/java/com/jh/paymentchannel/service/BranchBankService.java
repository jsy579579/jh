package com.jh.paymentchannel.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class BranchBankService {
 
	
	private static final Logger log = LoggerFactory.getLogger(BranchBankService.class);
	
	@Autowired
	private BranchbankBussiness branchbankBussiness;
	
	@Autowired
	private Util util;
	
	
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/paymentchannel/pay/query/branchbank/bybankname")
	public Object queryBranchBankByBankname(HttpServletRequest request,
			@RequestParam(value = "bankName") String bankName
			) {
		
		Map map = new HashMap();
		
		BranchNo findByBankName = branchbankBussiness.findByBankName(bankName);
		
		if(findByBankName != null) {
			//String bankNo = findByBankName.getBankNo();
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, findByBankName);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无该银行数据");
			return map;
		}
		
	}
	
	
	
	
}
