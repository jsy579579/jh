package com.cardmanager.pro.service;

import cn.jh.common.utils.AuthorizationHandle;
import cn.jh.common.utils.CommonConstants;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.util.RestTemplateUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@EnableAutoConfiguration
public class CreditCardManagerConfigService {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	@Autowired
	private RestTemplateUtil util;

	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/set/cardmanager/config")
	public @ResponseBody Object addCreditCardManagerConfig(HttpServletRequest request,
			@RequestParam(value="channelId")String channelId,
			@RequestParam(value="paySingleLimitMoney",required=false,defaultValue="20")int paySingleLimitMoney,
			@RequestParam(value="paySingleLimitCount",required=false,defaultValue="2")int paySingleLimitCount,
			@RequestParam(value="conSingleLimitMoney",required=false,defaultValue="10")int conSingleLimitMoney,
			@RequestParam(value="conSingleLimitCount",required=false,defaultValue="2")int conSingleLimitCount,
			@RequestParam(value="firstMoney",required=false,defaultValue="10")int firstMoney,
			@RequestParam(value="version",required=false,defaultValue="1")String version
			){
		Map<String,Object> map = new HashMap<>();
		Map<String, Object> verifyStringFiledIsNull = AuthorizationHandle.verifyStringFiledIsNull(channelId,paySingleLimitMoney+"",paySingleLimitCount+"",firstMoney+"");
		if(!CommonConstants.SUCCESS.equals(verifyStringFiledIsNull.get(CommonConstants.RESP_CODE))){
			return verifyStringFiledIsNull;
		}
		
		CreditCardManagerConfig model = creditCardManagerConfigBusiness.findByVersion(version);
		if(model==null){
			model = new CreditCardManagerConfig();
		}
		
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("channelId", channelId);
		Map<String, Object> restTemplateDoPost = util.restTemplateDoPost("user", "/v1.0/user/channel/find/by/channelid", requestEntity);
		if(!CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))){
			return restTemplateDoPost;
		}
		JSONObject resultObj = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);
//		LOG.info("======/v1.0/user/channel/find/by/channelid:" + resultObj);
		
		
		model.setChannelId(channelId);
		model.setFirstMoney(firstMoney);
		model.setChannelTag(resultObj.getString("channelTag"));
		model.setPaySingleLimitCount(paySingleLimitCount);
		model.setPaySingleLimitMoney(new BigDecimal(paySingleLimitMoney));
		model.setConSingleLimitCount(conSingleLimitCount);
		model.setConSingleLimitMoney(new BigDecimal(conSingleLimitMoney));
		model = creditCardManagerConfigBusiness.save(model);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "添加成功");
		map.put(CommonConstants.RESULT, model);
		return map;
	}
}
