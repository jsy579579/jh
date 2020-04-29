package cn.jh.clearing.service;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.CoinRecordBusiness;
import cn.jh.clearing.pojo.CoinRecord;
import cn.jh.clearing.util.Util;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.TokenUtil;




@Controller
@EnableAutoConfiguration
public class CoinService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CoinService.class);
	
	@Autowired
	private CoinRecordBusiness  coinRecordBusiness;
	
	@Autowired
	Util util;
	
	
	/**获取用户的积分流水**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/coin/query/{token}")
	public @ResponseBody Object pageCoinQueryByUserId(HttpServletRequest request,  
			 @PathVariable("token") String token,
			 @RequestParam(value = "start_time",  required = false) String  startTime,
			 @RequestParam(value = "end_time",  required = false) String endTime,
			 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
			 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
			 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
			){
		Map map = new HashMap();
		long userId;
		try{
			userId = TokenUtil.getUserId(token);
		}catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;		
		}
		
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Date StartTimeDate = null;
		if(startTime != null  && !startTime.equalsIgnoreCase("")){
			StartTimeDate = DateUtil.getDateFromStr(startTime);
		}
		Date endTimeDate = null;
		
		if(endTime != null  && !endTime.equalsIgnoreCase("")){
			endTimeDate = DateUtil.getDateFromStr(endTime);
		}
		String grade=null;
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, coinRecordBusiness.findCoinRecordByUserid(userId+"",grade,StartTimeDate ,endTimeDate ,pageable));
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/coin/query/all")
	public @ResponseBody Object pageCoinQuery(HttpServletRequest request,  
			 @RequestParam(value = "phone", defaultValue = "", required = false) String phone,
			//用户编号
			 @RequestParam(value = "userid", defaultValue = "0", required = false) long userid,
			 //贴牌id
			 @RequestParam(value = "brand_id",defaultValue = "-1", required = false) String  brandid,
			//等级
			@RequestParam(value = "grade",  required = false) String grade,
			//身份证号
			@RequestParam(value = "idcard", required=false) String idcard,
			
			//银行卡号
			@RequestParam(value = "cardNo", required=false) String cardNo,
			 @RequestParam(value = "start_time",  required = false) String  startTime,
			 @RequestParam(value = "end_time",  required = false) String endTime,
			 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
			 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
			 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty			
			){

		Map<String,Object> map = new HashMap<String, Object>();
		
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Date StartTimeDate = null;
		if(startTime != null  && !startTime.equalsIgnoreCase("")){
			StartTimeDate = DateUtil.getDateFromStr(startTime);
		}
		Date endTimeDate = null;
		
		if(endTime != null  && !endTime.equalsIgnoreCase("")){
			endTimeDate = DateUtil.getDateFromStr(endTime);
		}
			
		String userId = "";
		if(phone!=null&&!phone.equals("")){
			long brandId = -1;
			try {
				brandId = Long.valueOf(brandid);
			} catch (NumberFormatException e) {
				brandId = -1;
			}
			
			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/query/phone";
			/**根据的用户手机号码查询用户的基本信息*/
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("phone", phone);
			requestEntity.add("brandId", brandId+"");
			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);

			LOG.info("RESULT================"+result);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			JSONObject resultObj  =  jsonObject.getJSONObject("result");
			
			if(resultObj.containsKey("id")){
				userId  = resultObj.getString("id");
			}else{
				userId="0";
			}	
		}
		
		if(userid!=0){
			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/query/id";
			/**根据的用户userid查询用户的基本信息*/
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("id", userid+"");
			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================"+result);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			JSONObject resultObj  =  jsonObject.getJSONObject("result");
			
			if(resultObj.containsKey("id")){
				userId  = resultObj.getString("id");
			}else{
				userId="0";
			}	
		}
		/***
		 * 身份证不为空判定
		 * **/
		if(idcard!=null&&!idcard.equals("")){
			
			/**获取身份证实名信息*/
			URI uri = util.getServiceUrl("paymentchannel", "error url request!");
			String url = uri.toString() + "/v1.0/paymentchannel/realname/idcard";
			
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("idcard", idcard);
			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			if(result==null){
				userId="0";
			}else{
				userId=jsonObject.getString("userId");
			}
		}
		/***
		 * 银行卡不为空判定
		 * **/
		if(idcard!=null&&!idcard.equals("")){
			
			/**获取银行卡信息*/
			URI uri = util.getServiceUrl("paymentchannel", "error url request!");
			String url = uri.toString() + "/v1.0/user/bank/default/cardno";
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("cardno", cardNo);
			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			if(result==null){
				userId="0";
			}else{
				userId=jsonObject.getString("userId");
			}
			
		}
		Page<CoinRecord> coinRecord=null;
		if("-1".equals(brandid)){
			coinRecord=coinRecordBusiness.findCoinRecordByUserid(userId,grade, StartTimeDate, endTimeDate, pageable);
		}else{
			coinRecord=coinRecordBusiness.findCoinRecordByBrandid(userId,Long.valueOf(brandid),grade, StartTimeDate, endTimeDate, pageable);
		}
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, coinRecord);
		return map;
	}
	
}
