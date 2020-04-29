package cn.jh.clearing.business.impl;

import java.net.URI;
import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.WithDrawDealBusiness;
import cn.jh.clearing.pojo.PaymentOrder;
import cn.jh.clearing.repository.PaymentOrderRepository;
import cn.jh.clearing.util.Util;
import cn.jh.common.utils.CommonConstants;

@Service
public class WithDrawDealBusinessImpl implements WithDrawDealBusiness{

	@Autowired
    private PaymentOrderRepository paymentOrderRepository;
	
	@Autowired
	Util util;

	@Override
	public void deal() {
		 List<PaymentOrder> paymentOrders = paymentOrderRepository.findWaitWithdrawOrder();
		 for(PaymentOrder paymentorder  : paymentOrders){
			 withdrawDeal(paymentorder);
		 }
	}

	@Async("myAsync")
	@Transactional
	public void withdrawDeal(PaymentOrder paymentOrder){
		
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		URI  uri = util.getServiceUrl("paymentchannel", "error url request!");
		String url = uri.toString() + "/v1.0/paymentchannel/pay/query";
		
		/**根据的用户手机号码查询用户的基本信息*/
		requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("ordercode", paymentOrder.getThirdOrdercode());
		requestEntity.add("brandcode", paymentOrder.getBrandid()+"");
		requestEntity.add("channel_type", "2");
		requestEntity.add("channel_tag", paymentOrder.getChannelTag());
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		JSONObject resultObj  =  jsonObject.getJSONObject("result");
		String respcode =  resultObj.getString("rescode");
		String reqcode  =  resultObj.getString("reqcode");
		
		String status = "0";
		
		if(reqcode.equalsIgnoreCase(CommonConstants.SUCCESS)){ 		
			if(respcode.equalsIgnoreCase(CommonConstants.SUCCESS)){
				/**回调商家的交易处理页面*/
				status = "1";
			}else if(respcode.equalsIgnoreCase(CommonConstants.WAIT_CHECK)){
				status = "3";
			}else{
				status = "2";
			}			
		}else{
			status = "2";
		}
		
		if(status.equalsIgnoreCase("1")){
			/**表示成功, 先解冻 */
			uri = util.getServiceUrl("user", "error url request!");
			url = uri.toString() + "/v1.0/user/account/freeze";
			requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("order_code", paymentOrder.getOrdercode());
			requestEntity.add("user_id", paymentOrder.getUserid()+"");
			requestEntity.add("amount", paymentOrder.getAmount().toString());
			requestEntity.add("add_or_sub", "1");
			result = restTemplate.postForObject(url, requestEntity, String.class);
		}

		uri = util.getServiceUrl("transactionclear", "error url request!");
		url = uri.toString() + "/v1.0/transactionclear/payment/update";

		/**根据的用户手机号码查询用户的基本信息*/
		requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", paymentOrder.getOrdercode());
		requestEntity.add("third_code", paymentOrder.getThirdOrdercode());
		requestEntity.add("status", status);
		result = restTemplate.postForObject(url, requestEntity, String.class);
			
	}
		
	
}
