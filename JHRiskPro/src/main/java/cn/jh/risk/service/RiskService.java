package cn.jh.risk.service;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.risk.business.BlackWhiteListBusiness;
import cn.jh.risk.pojo.BlackWhiteList;
import cn.jh.risk.util.Util;


@Controller
@EnableAutoConfiguration
public class RiskService {
	
	private static final Logger LOG = LoggerFactory.getLogger(RiskService.class);
	
	@Autowired
	private BlackWhiteListBusiness  blackWhiteListBusiness;
	
	
	 @Autowired
	 Util util;
	
	 //operation_type 0登录 1交易 ，移除黑名单
	 @RequestMapping(method=RequestMethod.POST,value="/v1.0/risk/blackwhite/del/phone")
		public @ResponseBody Object delBlackWhiteByPhone(HttpServletRequest request,   
				@RequestParam(value = "phone") String phone,
				@RequestParam(value = "operation_type", defaultValue = "", required=false) String operationtype
				){
		 
		 blackWhiteListBusiness.delBlackWhiteAndOperation(phone, operationtype);		 
		 Map map = new HashMap();				
		 map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		 map.put(CommonConstants.RESP_MESSAGE, "成功");	
		 return map;
		 		
	 }
	 
	 //查询单个黑名单	
	 @RequestMapping(method=RequestMethod.POST,value="/v1.0/risk/blackwhite/query/phone")
		public @ResponseBody Object queryBlackWhiteByPhone(HttpServletRequest request,   
				@RequestParam(value = "phone") String phone,
				@RequestParam(value = "operation_type") String operationtype
				){
		 
		 BlackWhiteList blackWhite =  blackWhiteListBusiness.findBlackWhiteByPhone(phone, operationtype);		 
		 Map map = new HashMap();				
		 if(blackWhite == null){
			 
			 map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			 map.put(CommonConstants.RESP_MESSAGE, "用户不在黑名单里面");
				
		 }else{
			 
			 map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_USER_BLACK);
			 map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单了里面");
			 
		 }
		 
		 return map;
		 		
	 }
		 
	//查询单个黑名单	
		 @RequestMapping(method=RequestMethod.POST,value="/v1.0/risk/blackwhite/query/userid")
			public @ResponseBody Object queryBlackWhiteByUserId(HttpServletRequest request,   
					@RequestParam(value = "user_id") long userid,
					@RequestParam(value = "operation_type") String operationtype
					){
			 
			 BlackWhiteList blackWhite =  blackWhiteListBusiness.findBlackWhiteByUserId(userid, operationtype);		 
			 Map map = new HashMap();				
			 if(blackWhite == null){
				 
				 map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
				 map.put(CommonConstants.RESP_MESSAGE, "用户不在黑名单里面");
					
			 }else{
				 
				 map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_USER_BLACK);
				 map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单了里面");
				 
			 }
			 
			 return map;
			 		
		 }
	 
	 
	 
	 //添加黑名单
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/risk/blackwhite/add")
	public @ResponseBody Object addBlackWhite(HttpServletRequest request,   
			@RequestParam(value = "phone") String phone,
			@RequestParam(value="brandId",required=false,defaultValue="-1")String brandId,
			@RequestParam(value = "operation_type", defaultValue = "0", required=false) String operationtype
			){
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/query/phone";
		
		/**根据的用户手机号码查询用户的基本信息*/
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		requestEntity.add("brandId", brandId);
		RestTemplate restTemplate=new RestTemplate();
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		JSONObject resultObj  =  jsonObject.getJSONObject("result");
		if(resultObj.has("id")){
			long userid  = resultObj.getLong("id");
			long  brandid =  resultObj.getLong("brandId");
			BlackWhiteList blackWhiteList = new BlackWhiteList();
			blackWhiteList.setBrandid(brandid);		
			blackWhiteList.setCreateTime(new Date());
			blackWhiteList.setOperationType(operationtype);
			blackWhiteList.setPhone(phone);
			blackWhiteList.setUserid(userid);
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, blackWhiteListBusiness.merge(blackWhiteList));
			return map;
		
		}else{
			
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
			//map.put(CommonConstants.RESULT, profitRecordBusiness.findProfitByUserid(userId, StartTimeDate, endTimeDate,  pageable));
			return map;
			
		}

	}
	
	
	/**黑名单查询**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/risk/blackwhite/query/all")
	public @ResponseBody Object pageAllBlackWhiteQuery(HttpServletRequest request, 
			 @RequestParam(value = "phone", defaultValue = "", required = false) String phone,
			 @RequestParam(value = "brandid", defaultValue = "", required = false) String brandid,
			 @RequestParam(value = "start_time",  required = false) String  startTime,
			 @RequestParam(value = "end_time",  required = false) String endTime,
			 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
			 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
			 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty			
			){
		Map map = new HashMap();
		
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Date StartTimeDate = null;
		if(startTime != null  && !startTime.equalsIgnoreCase("")){
			StartTimeDate = DateUtil.getDateFromStr(startTime);
		}
		Date endTimeDate = null;
		
		if(endTime != null  && !endTime.equalsIgnoreCase("")){
			endTimeDate = DateUtil.getDateFromStr(endTime);
		}

		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, blackWhiteListBusiness.findBlackWhiteList(phone, brandid, StartTimeDate, endTimeDate, pageable));
		return map;
	}
}
