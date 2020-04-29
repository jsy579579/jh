package com.jh.paymentchannel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.HLJCRegister;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;

import net.sf.json.JSONObject;

@Service
public class HLJCTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger log = LoggerFactory.getLogger(HLJCTopupPage.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Autowired
	private HLJCpageRequest hljcpageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Override
	public Map<String, String> topupRequest(Map<String,Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		
		Map<String, String> map = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");
		String rate = resultObj.getString("rate");
		String extraFee = resultObj.getString("extraFee");
		String realAmount = resultObj.getString("realAmount");
		String orderType = resultObj.getString("type");

		HLJCRegister hljcRegister = topupPayChannelBusiness.getHLJCRegisterByBankCard(bankCard);

		/*Map<String,String> maps = new HashMap<String, String>();
		List<Object> list = new ArrayList<Object>();
		
		maps.put("01", "虚拟产品");
		maps.put("02", "公共事业缴费");
		maps.put("03", "手机充值");
		maps.put("04", "公益事业");
		maps.put("05", "实物电商/综合业务");
		maps.put("06", "彩票业务");
		maps.put("07", "行政教育");
		maps.put("08", "线下服务业");
		maps.put("09", "微信实物电商");
		maps.put("10", "微信虚拟电商");
		maps.put("11", "保险行业");
		maps.put("12", "基金行业");
		maps.put("13", "电子票务");
		maps.put("14", "金融投资");
		maps.put("15", "大额支付");
		maps.put("16", "其他");
		maps.put("17", "旅游机票");
		maps.put("18", "畅付D");
		
		Set<String> keySet = maps.keySet();
		
		Iterator<String> it = keySet.iterator();
		while(it.hasNext()) {
			String next = it.next();
			list.add(next);
		}
		
		String key = (String) list.get(new Random().nextInt(18));
		String value = maps.get(key);*/
		
		if("10".equals(orderType)) {
			log.info("根据判断进入消费任务======");
			
			if (!rate.equals(hljcRegister.getRate()) || !extraFee.equals(hljcRegister.getExtraFee())) {

				map = (Map<String, String>) hljcpageRequest.hljcUpdateMerchant(request, ordercode, "R");
				Object respCode = map.get("resp_code");
				Object respMessage = map.get("resp_message");
				log.info("respCode====="+respCode);
				
				if("000000".equals(respCode)) {
					
					map = (Map<String, String>) hljcpageRequest.hljcFastPay(request, ordercode);

				}

			} else {
				
				map = (Map<String, String>) hljcpageRequest.hljcFastPay(request, ordercode);
				
			}
			
		}
		
		
		if("11".equals(orderType)) {
			
			map = (Map<String, String>) hljcpageRequest.hljcTransfer(request, ordercode);
			
		}
		

		return map;

	}

}
