package com.jh.user.service;

import java.util.HashMap;
import java.util.List;
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

import com.jh.user.business.BranchbankBussiness;
import com.jh.user.pojo.Branchbank;

import cn.jh.common.utils.CommonConstants;  

@Controller
@EnableAutoConfiguration
public class BranchbankService {
	private static final Logger log = LoggerFactory.getLogger(BranchbankService.class);
	
	@Autowired
	private BranchbankBussiness branchbankBussiness;
	
	
	/**查询支行信息**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/branchbank/query")
	public @ResponseBody Object bandcard4Auth(HttpServletRequest request, 
			@RequestParam(value = "province") String province,
			@RequestParam(value = "city") String city,
			@RequestParam(value = "bankBranchname") String bankBranchname){
		
		Map map = new HashMap();
		List<Branchbank> list = branchbankBussiness.queryInfoBranch(province, city, bankBranchname);
		if(list.size()>0) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, list);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
		}else {
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "暂不支持该银行卡");
		}
		return map;
	}
}
