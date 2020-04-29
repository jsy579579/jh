package com.jh.paymentchannel.service;


import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.ChannelSupportBankBusiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.BankInfo;
import com.jh.paymentchannel.pojo.ChannelSupportBank;
import com.jh.paymentchannel.pojo.TopupPayChannelRoute;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class ChannelSupportBankService {
	
	private static final Logger log = LoggerFactory.getLogger(ChannelSupportBankService.class);
	@Autowired
	private ChannelSupportBankBusiness channelsupportbankbusiness;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Autowired
	private Util util;
	//根据通道标识，卡名称，卡类型 查询是否支持该发卡行 给前端
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pay/query/supportbankbytagnametype")
	public Object querySupportBankByTagAndNameAndType(HttpServletRequest request,
			@RequestParam(value = "channel_Tag")String channelTag,//通道标识
			@RequestParam(value = "supprort_bank_name")String supportBankName,//卡名称
			@RequestParam(value = "support_bank_type")String supportBankType//卡类型
			) {
			
			ChannelSupportBank channelSupportBank=null;
			Map map=new HashMap();
		
				channelSupportBank=channelsupportbankbusiness.getSupportBankByTagAndNameAndType(channelTag, supportBankName, supportBankType);
			
		
		
		if(channelSupportBank!=null) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, channelSupportBank);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			
		}else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "该通道不支持此银行卡！");
		}
		
		return map;
	}
	
	//根据通道标识查询银行支持列表     前端
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pay/querysupportbankbytag")
	public Object querySupportBankByTag(HttpServletRequest request,
			@RequestParam(value="channelTag")String channelTag
			){
			List<ChannelSupportBank> channelsupportbanklist=channelsupportbankbusiness.querySupportBankByTag(channelTag);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", channelsupportbanklist);
	}
	
	//根据用户的userId bankcard，cardtype 三个条件去查出该卡的名称 
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pay/querysupportbank")
	public Object queryBankNameByUserIdAndCardNo(
			HttpServletRequest request,
			@RequestParam(value="user_id")long userId,
			@RequestParam(value="card_no")String cardNo,
			@RequestParam(value="type")String type,
			@RequestParam(value="channel_tag")String channeltag) {
		
			Map map = new HashMap();
			RestTemplate restTemplate=new RestTemplate();
			URI uri = util.getServiceUrl("user", "error url request!");
			//根据用户信息查询出银行名称
			String url = uri.toString() + "/v1.0/user/payment/query/querysupportbanknamebyparams";
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("user_id", userId+"");//转换成String类型传进去
			requestEntity.add("card_no", cardNo);
			requestEntity.add("type", type);
			
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			log.info("RESULT================"+result);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			JSONObject resultObj  =  jsonObject.getJSONObject("result");
			String bankName = resultObj.getString("bankName");
			ChannelSupportBank supportBankOne = null;
			if(bankName.contains("浦发银行")) {
				bankName="浦东发展银行";
			} else if(bankName.contains("邮政银行") || bankName.contains("邮政储蓄") || bankName.contains("邮储银行")) {
				bankName="中国邮政储蓄银行";
				
			} else if(bankName.contains("广发银行")) {
				bankName="广东发展银行";
				
			}else if(bankName.contains("华夏银行(63040000)")) {
				bankName="华夏银行";
				
			}else if(bankName.contains("广州银行股份有限公司(64135810)")) {
				bankName="广州银行股份有限公司";
				
			}
			
			 supportBankOne = channelsupportbankbusiness.querySupportBankByTagAndNameAndType(channeltag, bankName);
			
			if(supportBankOne!=null) {
				map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
				map.put(CommonConstants.RESULT, supportBankOne);
			
				
			}else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "该通道不支持"+bankName+"的卡！");
			}
			
			return map;
	}
	
	
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pay/querysupportbank/{token}")
	public Object getSupportBankCard(HttpServletRequest request,
			@PathVariable("token") String token,
			@RequestParam(value = "channel_tag", required = false) String channelTag,
			@RequestParam(value = "type", defaultValue = "0", required = false) String type
			) {
		
		if("1".equals(type)||"1"==type){
			channelTag = "UMP_QUICK";
		}
		
		Map map = new HashMap();
		long userId;
		long brandId;
		try {
			userId = TokenUtil.getUserId(token);
			brandId = TokenUtil.getBrandid(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		
		}
		
		//处理前端传来的channel_tag
		if(null!=channelTag&&!"".equals(channelTag)){
			TopupPayChannelRoute topupChannelByBrandcode = topupPayChannelBusiness.getTopupChannelByBrandcode(brandId+"", type, channelTag);
			String targetChannelTag = topupChannelByBrandcode.getTargetChannelTag();
			
			
			List<ChannelSupportBank> querySupportBankByTag = channelsupportbankbusiness.querySupportBankByTag(targetChannelTag);
			
			System.out.println("querySupportBankByTag===="+querySupportBankByTag.toString());
			
			RestTemplate restTemplate=new RestTemplate();
			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/bank/query/useridandtype";
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("userId", userId+"");
			requestEntity.add("type", "0");
			JSONArray resultObj;
			try {
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				log.info("接口/v1.0/user/bank/query/useridandtype--RESULT================"+result);
				JSONObject jsonObject =  JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONArray("result");
			} catch (RestClientException e) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "失败");
				return map;
			}
			
			List<BankInfo> list = new ArrayList<BankInfo>();
			BankInfo bi = null;
			int a = 0;
			
			if(resultObj!=null&&resultObj.size()>0){
				for(int i = 0;i<resultObj.size();i++){
					
					JSONObject jsonObject2 = resultObj.getJSONObject(i);
					
					bi = new BankInfo();
					bi.setBankBranchName(jsonObject2.getString("bankBranchName"));
					bi.setBankBrand(jsonObject2.getString("bankBrand"));
					bi.setBankName(jsonObject2.getString("bankName"));
					bi.setCardNo(jsonObject2.getString("cardNo"));
					bi.setCardType(jsonObject2.getString("cardType"));
					bi.setCity(jsonObject2.getString("city"));
					bi.setIdcard(jsonObject2.getString("idcard"));
					bi.setExpiredTime(jsonObject2.getString("expiredTime"));
					bi.setIdDef(jsonObject2.getString("idDef"));
					bi.setLineNo(jsonObject2.getString("lineNo"));
					bi.setLogo(jsonObject2.getString("logo"));
					bi.setNature(jsonObject2.getString("nature"));
					bi.setPhone(jsonObject2.getString("phone"));
					bi.setPriOrPub(jsonObject2.getString("priOrPub"));
					bi.setProvince(jsonObject2.getString("province"));
					bi.setSecurityCode(jsonObject2.getString("securityCode"));
					bi.setState(jsonObject2.getString("state"));
					bi.setType(jsonObject2.getString("type"));
					bi.setUserId(Long.parseLong(jsonObject2.getString("userId")));
					bi.setUserName(jsonObject2.getString("userName"));
					bi.setUseState("0");
					list.add(bi);
					
					if(querySupportBankByTag!=null&&querySupportBankByTag.size()>0){
						for(ChannelSupportBank csb : querySupportBankByTag){
							
							String bankName = jsonObject2.getString("bankName");
							if(bankName.contains("浦发银行")) {
								bankName="浦东发展银行";
							} else if(bankName.contains("邮政银行") || bankName.contains("邮政储蓄") || bankName.contains("邮储银行")) {
								bankName="邮政储蓄银行";
								
							} else if(bankName.contains("广发银行")) {
								bankName="广东发展银行";
							}
							
							if(csb.getSupportBankName().contains(bankName)){
								
								bi.setUseState("1");
								a = 1;
								break;
							}
						}
					}
					
					if(a==1){
						continue;
					}
				}
			}
			
			if(list!=null&&list.size()>0){
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESULT, list);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "亲，没有绑定充值卡哦");
			}
			
			return map;	
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			return map;
		}
	
	}

	/**
	 * 2019.10.11 修改  增加了账单日 还款日 和  信用额度
	 * @param request
	 * @param userId
	 * @param brandId
	 * @param channelTag
	 * @param type
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pay/query/supportbank")
	public Object getSupportBankCard(HttpServletRequest request,
			@RequestParam(value = "user_id") String userId,
			@RequestParam(value = "brand_id") String brandId,
			@RequestParam(value = "channel_tag", required = false) String channelTag,
			@RequestParam(value = "type", defaultValue = "0", required = false) String type
			) {
		
		if("1".equals(type)||"1"==type){
			channelTag = "UMP_QUICK";
		}
		
		Map map = new HashMap();
		
		try {
			//处理前端传来的channel_tag
			if(null!=channelTag&&!"".equals(channelTag)){
				TopupPayChannelRoute topupChannelByBrandcode = topupPayChannelBusiness.getTopupChannelByBrandcode(brandId, type, channelTag);
				String targetChannelTag = topupChannelByBrandcode.getTargetChannelTag();
				
				
				List<ChannelSupportBank> querySupportBankByTag = channelsupportbankbusiness.querySupportBankByTag(targetChannelTag);
				
				System.out.println("querySupportBankByTag===="+querySupportBankByTag.toString());
				
				RestTemplate restTemplate=new RestTemplate();
				URI uri = util.getServiceUrl("user", "error url request!");
				String url = uri.toString() + "/v1.0/user/bank/query/useridandtype";
				MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
				requestEntity.add("userId", userId);
				requestEntity.add("type", "0");
				JSONArray resultObj;
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				log.info("接口/v1.0/user/bank/query/useridandtype--RESULT================" + result);
				JSONObject jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONArray("result");

				List<BankInfo> list = new ArrayList<BankInfo>();
				BankInfo bi = null;
				int a = 0;
				if(resultObj!=null&&resultObj.size()>0){
					for(int i = 0;i<resultObj.size();i++){
						
						JSONObject jsonObject2 = resultObj.getJSONObject(i);
						
						bi = new BankInfo();
						bi.setBankBranchName(jsonObject2.getString("bankBranchName"));
						bi.setBankBrand(jsonObject2.getString("bankBrand"));
						bi.setBankName(jsonObject2.getString("bankName"));
						bi.setCardNo(jsonObject2.getString("cardNo"));
						bi.setCardType(jsonObject2.getString("cardType"));
						bi.setCity(jsonObject2.getString("city"));
						bi.setIdcard(jsonObject2.getString("idcard"));
						bi.setExpiredTime(jsonObject2.getString("expiredTime"));
						bi.setIdDef(jsonObject2.getString("idDef"));
						bi.setLineNo(jsonObject2.getString("lineNo"));
						bi.setLogo(jsonObject2.getString("logo"));
						bi.setNature(jsonObject2.getString("nature"));
						bi.setPhone(jsonObject2.getString("phone"));
						bi.setPriOrPub(jsonObject2.getString("priOrPub"));
						bi.setProvince(jsonObject2.getString("province"));
						bi.setSecurityCode(jsonObject2.getString("securityCode"));
						bi.setState(jsonObject2.getString("state"));
						bi.setType(jsonObject2.getString("type"));
						bi.setUserId(Long.parseLong(jsonObject2.getString("userId")));
						bi.setUserName(jsonObject2.getString("userName"));
						bi.setUseState("0");
						bi.setBillDay(jsonObject2.getInt("billDay"));
						bi.setRepaymentDay(jsonObject2.getInt("repaymentDay"));
						bi.setCreditBlance(new BigDecimal(jsonObject2.getString("creditBlance")));
						list.add(bi);
						
						if(querySupportBankByTag!=null&&querySupportBankByTag.size()>0){
							for(ChannelSupportBank csb : querySupportBankByTag){
								
								String bankName = jsonObject2.getString("bankName");
								if(bankName.contains("浦发银行")) {
									bankName="浦东发展银行";
								} else if(bankName.contains("邮政银行") || bankName.contains("邮政储蓄") || bankName.contains("邮储银行")) {
									bankName="邮政储蓄银行";
									
								} else if(bankName.contains("广发银行")) {
									bankName="广东发展银行";
								}
								
								if(csb.getSupportBankName().contains(bankName)){
									
									bi.setUseState("1");
									a = 1;
									break;
								}
							}
						}
						
						if(a==1){
							continue;
						}
					}
				}
				
				if(list!=null&&list.size()>0){
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESULT, list);
					map.put(CommonConstants.RESP_MESSAGE, "成功");
				}else{
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "亲,您绑定的卡暂不支持该通道哦");
				}
				
				return map;	
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "亲，您没有绑定银行卡哦");
				return map;
			}
		} catch (Exception e) {
			log.error("选择通道支持的银行卡出现空值======");
			e.printStackTrace();
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲，您没有绑定银行卡哦");
			return map;
		}
	
	}
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/querysupportbank/byname")
	public @ResponseBody Object querySupportBankByName(HttpServletRequest request,
			@RequestParam(value = "bankName")String bankName,
			@RequestParam(value = "type")String type,
			@RequestParam(value = "channelTag")String[] channelTag
			) {
		
		List<ChannelSupportBank> querySupportBankByName = null;
		try {
			querySupportBankByName = channelsupportbankbusiness.querySupportBankByName(bankName, type, channelTag);
		} catch (Exception e) {
			log.error("查询结果有误=====");
			
			return ResultWrap.init(CommonConstants.FALIED, "查询银行支持列表有误!");
		}
		
		if(querySupportBankByName != null && querySupportBankByName.size() > 0) {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", querySupportBankByName);
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, "暂无银行支持列表的数据!");
		}
		
	}
	
	
}
