package com.jh.paymentchannel.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentchannel.business.BatchNoBusiness;
import com.jh.paymentchannel.pojo.BatchNo;

@Controller
@EnableAutoConfiguration
public class BatchNoService {
	@Autowired
	private BatchNoBusiness bnbi;
	
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/paymentchannel/batchno/add")
	public @ResponseBody Object add(@RequestParam(value="bindId") String bindId,
			@RequestParam(value="userId") String userId,
			@RequestParam(value="amount") String amount,
			@RequestParam(value="money") String money,
			@RequestParam(value="times") String times,
			@RequestParam(value="status") String status,
			@RequestParam(value="batchNo") String batchNo) {
		Map map = new HashMap();
		try {
			BatchNo bn = new BatchNo();
			bn.setAmount(new BigDecimal(amount.trim()));
			bn.setBatchNo(batchNo.trim());
			bn.setBindId(bindId.trim());
			bn.setMoney(new BigDecimal(money.trim()));
			bn.setUserId(userId.trim());
			bn.setTimes(times.trim());
			bn.setStatus("0");
			BatchNo batchno = bnbi.save(bn);
			if(null==batchno||"".equals(batchno)) {
				map.put("resp_code", "999999");
				map.put("resp_message", "添加失败");
			}else {
				map.put("resp_code", "000000");
				map.put("resp_message","添加成功");
				map.put("result", batchno);
			}
		} catch (Exception e) {
			map.put("resp_code", "999999");
			map.put("resp_message", e.getMessage());
		}
		return map;
	}

	@RequestMapping(method=RequestMethod.POST, value="/v1.0/paymentchannel/batchno/find")
	public @ResponseBody Object find(
			@RequestParam(value="bindId") String bindId
			) {
		Map map = new HashMap();
		try {
			BatchNo batchno = bnbi.findByBindId(bindId);
			if(null==batchno||"".equals(batchno)) {
				map.put("resp_code", "999999");
				map.put("resp_message", "查询失败");
			}else {
				map.put("resp_code", "000000");
				map.put("resp_message","查询成功");
				map.put("result", batchno);
			}
		} catch (Exception e) {
			map.put("resp_code", "999999");
			map.put("resp_message", e.getMessage());
		}
		return map;
	}
}
