package com.juhe.creditcardapplyfor.async;

import com.alibaba.fastjson.JSONObject;
import com.juhe.creditcardapplyfor.entity.CardOrderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Lazy(true)
public class AsyncMethod {

	private final Logger LOG = LoggerFactory.getLogger(getClass());


	private RestTemplate restTemplate;

	@Value("${creditCard.ipAddress}")
	private String ip;

	@Async
	public void updatePaymentOrderByOrderCode(CardOrderEntity cardOrderEntity){

		restTemplate = new RestTemplate();
		String clientNo=cardOrderEntity.getClientNo();
		//根据手机号查询用户id
		String userInfoUrl=ip+"/v1.0/user/query/phone";
		MultiValueMap requestEntity=new LinkedMultiValueMap<>();
		requestEntity.add("phone",cardOrderEntity.getUserPhone());
		String userInfoResult=restTemplate.postForObject(userInfoUrl,requestEntity,String.class);
		JSONObject jsonObject1=JSONObject.parseObject(userInfoResult);
		JSONObject userInfo=jsonObject1.getJSONObject("result");
		String grade=userInfo.getString("grade");

		//查询当前通道channelId
		String channelTag="WQT_QUICK";
		String url=ip+"/v1.0/user/channel/query";
		requestEntity = new LinkedMultiValueMap<>();
		requestEntity.add("channel_tag",channelTag);
		String result=restTemplate.postForObject(url,requestEntity,String.class);
		JSONObject jsonObject=JSONObject.parseObject(result);
		JSONObject channelJson=jsonObject.getJSONObject("result");
		String channelId=channelJson.getString("id");


		//发放用户返佣
		String sendDisUrl=ip+"/v1.0/transactionclear/thirdDistribution/send";
		requestEntity = new LinkedMultiValueMap<>();
		requestEntity.add("ordercode",clientNo);
		requestEntity.add("userId",cardOrderEntity.getUseId());
		requestEntity.add("channelId",channelId);
		requestEntity.add("phone",cardOrderEntity.getUserPhone());
		requestEntity.add("amount",cardOrderEntity.getRebatePrice());
		requestEntity.add("brandId",cardOrderEntity.getBrandId());
		requestEntity.add("grade",grade);
		LOG.info("请求参数========="+requestEntity);
		try {
			restTemplate.postForObject(sendDisUrl,requestEntity,JSONObject.class);
			LOG.info("===============" + clientNo + "===============信用卡返佣执行完毕");
		} catch (RestClientException e) {
			e.printStackTrace();
			LOG.info("===============" + clientNo + "===============信用卡返佣执行失败");
		}
	}

}
