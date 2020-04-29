package com.jh.user.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;

import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.ThirdLeveDistributionBusiness;
import com.jh.user.business.UserBankInfoBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserShopsBusiness;
import com.jh.user.business.impl.JuHeAPIPhoneBillService;
import com.jh.user.business.impl.JuHeAPIdBeccancyService;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.User;
import com.jh.user.util.MD5Util;
import com.jh.user.util.RC4Util;
import com.jh.user.util.Util;



@Controller
@EnableAutoConfiguration
/**增值服务**/
public class UserAddedService {

	private static final Logger LOG = LoggerFactory.getLogger(UserAddedService.class);
		
	@Autowired 
	private BrandManageBusiness brandMangeBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness  userLoginRegisterBusiness;
	
	
	
	private String[] tels ={"10","20","30","50","100","300"};
	private BigDecimal telrate=new BigDecimal("0.02");
	
	 @Autowired
	 Util util;
	 	/**获取手机号充值商品列表
	 	 * telquery
	 	 * **/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/phone/telquery")
		public @ResponseBody Object phoneTelquery(HttpServletRequest request,
				//贴牌号
				 @RequestParam(value = "brand_id") long brandId,
				 //手机号
				 @RequestParam(value = "phone") String phone
				){
			Map map = new HashMap();
			Brand brand =brandMangeBusiness.findBrandById(brandId);
			if(brand==null||brand.getJuhekey()==null||brand.getJuheOpenid()==null||brand.getJuheOpenid().equals("")||brand.getJuhekey().equals("")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
				return map;
			}
			List<Map<String, String>> maplist= new ArrayList<Map<String,String>>();
			for(String tel:tels){
				String result ="";
				Map<String,String> mapla=new HashMap<String, String>();
				result=JuHeAPIPhoneBillService.mobileTelquery(phone, tel, brand.getJuhekey());
				JSONObject jsonObject =  JSONObject.fromObject(result);
				String code=jsonObject.getString("error_code");
				JSONObject resObject  =  jsonObject.getJSONObject("result");
				if(code.equals("0")){
					//产品ID
					mapla.put("cardid", resObject.getString("cardid"));
					//产品名
					mapla.put("cardname", resObject.getString("cardname"));
					//购买价格
					BigDecimal inprice=new BigDecimal(tel);
					mapla.put("inprice", inprice.subtract(inprice.multiply(telrate)).setScale(2, BigDecimal.ROUND_DOWN).toString());
					//归属地
					mapla.put("game_area", resObject.getString("game_area"));
					//面值
					mapla.put("cardtel", tel);
					maplist.add(mapla);
				}else{
					map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString("reason"));
				}
				
			}
			if(tels.length!=maplist.size()){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, maplist);
			
			return map;
		}
		
		/**
		 * 手机直充接口
		 * 
		 * ***/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/phone/onlineorder")
		public @ResponseBody Object phoneOnlineorder(HttpServletRequest request,
				//贴牌号
				 @RequestParam(value = "brand_id") long brandId,
				 //手机号
				 @RequestParam(value = "phone") String phone,
				 //面值
				 @RequestParam(value = "cardnum") String cardnum,
				 //订单号
				 @RequestParam(value = "ordercode") String ordercode
				){
			Map map = new HashMap();
			Brand brand =brandMangeBusiness.findBrandById(brandId);
			if(brand==null||brand.getJuhekey()==null||brand.getJuheOpenid()==null||brand.getJuheOpenid().equals("")||brand.getJuhekey().equals("")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
				return map;
			}
			String result ="";
			result=JuHeAPIPhoneBillService.mobileOnlineorder(phone, cardnum, ordercode, brand.getJuhekey(), brand.getJuheOpenid());
			JSONObject jsonObject =  JSONObject.fromObject(result);
			String code=jsonObject.getString("error_code");
			JSONObject resObject  =  jsonObject.getJSONObject("result");
			Map remap = new HashMap();
			if(code.equals("0")){
				 /*充值的卡类ID*/
				remap.put("cardid", resObject.getString("cardid"));
				/*数量*/
				remap.put("cardnum", resObject.getString("cardnum"));
				 /*进货价格*/
				remap.put("ordercash", resObject.getString("ordercash"));
				/*充值名称*/
				remap.put("cardname", resObject.getString("cardname"));
				/*聚合订单号*/
				remap.put("sporder_id", resObject.getString("sporder_id"));
				/*商户自定的订单号*/    
				remap.put("uorderid", resObject.getString("uorderid"));
				/*充值的手机号码*/
				remap.put("game_userid", resObject.getString("game_userid"));
				/*充值状态:0充值中 1成功 9撤销，刚提交都返回0*/
				remap.put("game_state", resObject.getString("game_state"));
				/*查询提示*/
				remap.put("reason", jsonObject.getString("reason"));
				
				/*查询状态码0*/
				remap.put("error_code", jsonObject.getString("error_code"));
			}else{
				/*查询提示*/
				remap.put("reason", jsonObject.getString("reason"));
				/*查询状态码0*/
				remap.put("error_code", jsonObject.getString("error_code"));
				RestTemplate restTemplate=new RestTemplate();
 				URI uri = util.getServiceUrl("transactionclear", "error url request!");
 				String url = uri.toString() + "/v1.0/transactionclear/payment/type1/update";
 				MultiValueMap<String, String>  requestEntity  = new LinkedMultiValueMap<String, String>();
 				requestEntity.add("status", "2");
				requestEntity.add("order_code",  ordercode);
				requestEntity.add("third_code",  "");
				String resultt=	restTemplate.postForObject(url, requestEntity, String.class);
 				LOG.info("RESULT======sta=========="+resultt);
			}
			
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, remap);
			return map;
		}
		
		/**
		 * 订单状态查询
		 * 
		 * ***/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/phone/ordersta")
		public @ResponseBody Object phoneOrdersta(HttpServletRequest request,
				//贴牌号
				 @RequestParam(value = "brand_id") long brandId,
				 //订单号
				 @RequestParam(value = "ordercode") String ordercode
				){
			Map map = new HashMap();
			Brand brand =brandMangeBusiness.findBrandById(brandId);
			if(brand==null||brand.getJuhekey()==null||brand.getJuheOpenid()==null||brand.getJuheOpenid().equals("")||brand.getJuhekey().equals("")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
				return map;
			}
			String result ="";
			result=JuHeAPIPhoneBillService.mobileOrdersta(ordercode,  brand.getJuhekey());
			JSONObject jsonObject =  JSONObject.fromObject(result);
			String code=jsonObject.getString("error_code");
			JSONObject resObject  =  jsonObject.getJSONObject("result");
			Map remap = new HashMap();
			if(code.equals("0")){
				 /*订单扣除金额*/
				remap.put("uordercash", resObject.getString("uordercash"));
				/*聚合订单号*/
				remap.put("sporder_id", resObject.getString("sporder_id"));
				
				/*充值状态:0充值中 1成功 9撤销，刚提交都返回0*/
				remap.put("game_state", resObject.getString("game_state"));
				/*查询提示*/
				remap.put("reason", resObject.getString("reason"));
				/*查询状态码0*/
				remap.put("error_code", jsonObject.getString("error_code"));
			}else{
				/*查询提示*/
				remap.put("reason", jsonObject.getString("reason"));
				/*查询状态码0*/
				remap.put("error_code", jsonObject.getString("error_code"));
			}
			
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, remap);
			return map;
		}
		
		/**
		 * 订单状态回调
		 * /v1.0/paymentchannel/topup/ailong/notify_call
		 * @throws IOException 
		 * ***/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/phone/bill/notify_call")
		public @ResponseBody String phoneNotify_call(HttpServletRequest request, HttpServletResponse response,
				@RequestParam("sporder_id") String sporder_id,@RequestParam("orderid") String orderid,
		        @RequestParam("sta") String sta,@RequestParam("sign") String sign 
				){
			
			LOG.info("sporder_id================"+sporder_id+"orderid================"+orderid+"sta================"+sta+"sign================"+sign);
			String brandid ="";
			{
				RestTemplate restTemplate=new RestTemplate();
				URI uri = util.getServiceUrl("transactionclear", "error url request!");
				String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
				/**根据的用户手机号码查询用户的基本信息*/
				MultiValueMap<String, String>  requestEntity  = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code", orderid);
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================"+result);
				JSONObject jsonObject =  JSONObject.fromObject(result);
				JSONObject resultObj  =  jsonObject.getJSONObject("result");
				if(resultObj.containsKey("brandid")){
					brandid=resultObj.getString("brandid");
				}else{
					 return "fail"; 
				}
			}
			if(brandid==null||brandid.equals("")){
				 return "fail"; 
			}
			
			
			Brand brand =brandMangeBusiness.findBrandById(Long.parseLong(brandid));
			
			 String local_sign = MD5Util.strToMD5(brand.getJuhekey()+sporder_id+orderid);//本地sign校验值
		        if(sign.equals(local_sign)){
		                if(sta.equals("1")){
		                	
		                	RestTemplate restTemplate=new RestTemplate();
		    				URI uri = util.getServiceUrl("transactionclear", "error url request!");
		    				String url = uri.toString() + "/v1.0/transactionclear/payment/type1/update";
		    				
		    				/**根据的用户手机号码查询用户的基本信息*/
		    				MultiValueMap<String, String>  requestEntity  = new LinkedMultiValueMap<String, String>();
		    				requestEntity.add("status", "1");
							requestEntity.add("order_code",  orderid);
							requestEntity.add("third_code",  sporder_id);
		    				String result = restTemplate.postForObject(url, requestEntity, String.class);
		    				LOG.info("RESULT======sta===="+sta+"======"+result);
		    				return "success"; 
		                 }else if(sta.equals("9")){
		                	 RestTemplate restTemplate=new RestTemplate();
			    				URI uri = util.getServiceUrl("transactionclear", "error url request!");
			    				String url = uri.toString() + "/v1.0/transactionclear/payment/type1/update";
			    				MultiValueMap<String, String>  requestEntity  = new LinkedMultiValueMap<String, String>();
			    				requestEntity.add("status", "2");
								requestEntity.add("order_code",  orderid);
								requestEntity.add("third_code",  sporder_id);
			    				String result = restTemplate.postForObject(url, requestEntity, String.class);
			    				LOG.info("RESULT======sta===="+sta+"======"+result);
			    				return "success"; 
		                 }
		                return "fail"; 
		                
		         }else{
		        	 return "fail"; 
		         }
		}
	/***=====================================违章缴费===============================================***/	
		
		/**
		 * 城市列表
	 	 * citylist
	 	 * JuHeAPIdBeccancyService
	 	 * **/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/wzdj/citylist")
		public @ResponseBody Object wzdjCitylist(HttpServletRequest request,
				//贴牌号
				 @RequestParam(value = "brand_id") long brandId
				){
			Map map = new HashMap();
			Brand brand =brandMangeBusiness.findBrandById(brandId);
			if(brand==null||brand.getJuhekey()==null||brand.getJuheOpenid()==null||brand.getJuheOpenid().equals("")||brand.getJuhekey().equals("")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
				return map;
			}
			String result ="";
			result=JuHeAPIdBeccancyService.wzdjCitylist(brand.getJuheWzdjKey());
			JSONObject jsonObject =  JSONObject.fromObject(result);
			String code=jsonObject.getString("error_code");
			JSONArray resObject  = jsonObject.getJSONArray("result");
			if(code.equals("0")){
				map.put(CommonConstants.RESULT, resObject);
			}else{
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE,jsonObject.getString("reason"));
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		/**
		 * 违章查询
	 	 * querywz
	 	 * JuHeAPIdBeccancyService
	 	 * **/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/wzdj/querywz")
		public @ResponseBody Object wzdjQuerywz(HttpServletRequest request,
				//贴牌号
				 @RequestParam(value = "brand_id") long brandId,
				// 车牌号
				 @RequestParam(value = "carNo") String carNo,
				//车架号(根据城市列表的规则决定长度)
				 @RequestParam(value = "frameNo") String frameNo,
				//发动机号(根据城市列表的规则决定长度)
				 @RequestParam(value = "enginNo") String enginNo,
				// 车类型(默认02:小型车,暂时只支持小型车)
				 @RequestParam(value = "carType") String carType,
				 //省份id(当不指定的时候默认根据车前缀)
				 @RequestParam(value = "provinceid",  required=false, defaultValue = "") String provinceid,
				 //城市id(当不指定的时候默认根据车前缀)
				 @RequestParam(value = "cityid",  required=false, defaultValue = "") String cityid
				 
				){
			Map map = new HashMap();
			Brand brand =brandMangeBusiness.findBrandById(brandId);
			if(brand==null||brand.getJuhekey()==null||brand.getJuheOpenid()==null||brand.getJuheOpenid().equals("")||brand.getJuhekey().equals("")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
				return map;
			}
			String result ="";
			result=JuHeAPIdBeccancyService.wzdjQuerywz(carNo, frameNo, enginNo, brand.getJuheWzdjKey(), carType,provinceid,cityid);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			String code=jsonObject.getString("error_code");
			JSONObject resObject  =  jsonObject.getJSONObject("result");
			if(code.equals("0")){
				map.put(CommonConstants.RESULT, resObject);
			}else{
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE,jsonObject.getString("reason"));
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		/**
		 * 提交订单
	 	 * submitOrder
	 	 * JuHeAPIdBeccancyService
	 	 * **/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/wzdj/submitOrder")
		public @ResponseBody Object wzdjSubmitOrder(HttpServletRequest request,
				//贴牌号
				 @RequestParam(value = "brand_id") long brandId,
				// 违章记录,多个用英文逗号分隔,如recordIds=12345,87342
				 @RequestParam(value = "recordIds") String recordIds,
				// 	车牌号
				 @RequestParam(value = "carNo") String carNo,
				// 	 联系人(如果是测试订单,请写"测试")
				 @RequestParam(value = "contactName") String contactName,
				// 联系人电话
				 @RequestParam(value = "tel") String tel,
				// 商户订单号
				 @RequestParam(value = "userOrderId") String userOrderId
				){
			Map map = new HashMap();
			Brand brand =brandMangeBusiness.findBrandById(brandId);
			if(brand==null||brand.getJuhekey()==null||brand.getJuheOpenid()==null||brand.getJuheOpenid().equals("")||brand.getJuhekey().equals("")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
				return map;
			}
			String result ="";
			result=JuHeAPIdBeccancyService.wzdjSubmitOrder(recordIds, carNo, contactName, tel, userOrderId, brand.getJuheWzdjKey());
			JSONObject jsonObject =  JSONObject.fromObject(result);
			String code=jsonObject.getString("error_code");
			JSONObject resObject  =  jsonObject.getJSONObject("result");
			if(code.equals("0")){
				map.put(CommonConstants.RESULT, resObject);
			}else{
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE,jsonObject.getString("reason"));
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		/**
		 * 证件上传
	 	 * upload
	 	 * JuHeAPIdBeccancyService
		 * @throws IOException 
	 	 * **/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/wzdj/upload")
		public @ResponseBody Object wzdjUpload(HttpServletRequest request,
				//贴牌号
				 @RequestParam(value = "brand_id") long brandId,
				// 用户自定义订单号
				 @RequestParam(value = "userOrderId") String userOrderId,
				// 	车主名称
				 @RequestParam(value = "ownerName") String ownerName
				 
				) throws IOException{
			Map map = new HashMap();
			Brand brand =brandMangeBusiness.findBrandById(brandId);
			if(brand==null||brand.getJuhekey()==null||brand.getJuheOpenid()==null||brand.getJuheOpenid().equals("")||brand.getJuhekey().equals("")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
				return map;
			}
			
		    Map<String, String> textMap = new HashMap<String, String>();  
		    textMap.put("userOrderId", userOrderId);  
		    textMap.put("ownerName", ownerName);    
		    Map<String, MultipartFile> fileMap = new HashMap<String, MultipartFile>(); 
		    MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;  
			List<MultipartFile> files = multipartRequest.getFiles("image");
			for(MultipartFile file : files){
				String fileName=file.getOriginalFilename();
				String prefix=fileName.substring(fileName.lastIndexOf("."));//如果想获得不带点的后缀，变为fileName.lastIndexOf(".")+1  
				int num=prefix.length();//得到后缀名长度  
			    String fileOtherName=fileName.substring(0, fileName.length()-num);//得到文件名。去掉了后缀  
				fileMap.put(fileOtherName, file);     
			}
	         
			String result ="";
			result=JuHeAPIdBeccancyService.formUpload(brand.getJuheWzdjKey(), textMap, fileMap); 
			JSONObject jsonObject =  JSONObject.fromObject(result);
			String code=jsonObject.getString("error_code");
			JSONObject resObject  =  jsonObject.getJSONObject("result");
			if(code.equals("0")){
				map.put(CommonConstants.RESULT, resObject);
				
			}else{
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE,jsonObject.getString("reason"));
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		
		
		/**
		 * 订单支付
	 	 * payOrder
	 	 * JuHeAPIdBeccancyService
	 	 * **/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/wzdj/payOrder")
		public @ResponseBody Object wzdjPayOrder(HttpServletRequest request,
				//贴牌号
				 @RequestParam(value = "brand_id") long brandId,
				//  	用户自定义订单号
				 @RequestParam(value = "userOrderId") String userOrderId
				){
			Map map = new HashMap();
			Brand brand =brandMangeBusiness.findBrandById(brandId);
			if(brand==null||brand.getJuhekey()==null||brand.getJuheOpenid()==null||brand.getJuheOpenid().equals("")||brand.getJuhekey().equals("")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
				return map;
			}
			String result ="";
			result=JuHeAPIdBeccancyService.wzdjPayOrder(userOrderId,  brand.getJuheWzdjKey());
			JSONObject jsonObject =  JSONObject.fromObject(result);
			String code=jsonObject.getString("error_code");
			JSONObject resObject  =  jsonObject.getJSONObject("result");
			if(code.equals("0")){
				map.put(CommonConstants.RESULT, resObject);
			}else{
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE,jsonObject.getString("reason"));
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		
		
		/**
		 * 订单详情
	 	 * orderDetail
	 	 * JuHeAPIdBeccancyService
	 	 * **/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/wzdj/orderDetail")
		public @ResponseBody Object wzdjOrderDetail(HttpServletRequest request,
				//贴牌号
				 @RequestParam(value = "brand_id") long brandId,
				//  	用户自定义订单号
				 @RequestParam(value = "userOrderId") String userOrderId
				){
			Map map = new HashMap();
			Brand brand =brandMangeBusiness.findBrandById(brandId);
			if(brand==null||brand.getJuhekey()==null||brand.getJuheOpenid()==null||brand.getJuheOpenid().equals("")||brand.getJuhekey().equals("")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
				return map;
			}
			String result ="";
			result=JuHeAPIdBeccancyService.wzdjOrderDetail(userOrderId,  brand.getJuheWzdjKey());
			JSONObject jsonObject =  JSONObject.fromObject(result);
			String code=jsonObject.getString("error_code");
			JSONObject resObject  =  jsonObject.getJSONObject("result");
			if(code.equals("0")){
				map.put(CommonConstants.RESULT, resObject);
			}else{
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE,jsonObject.getString("reason"));
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}	
		
		/**
		 * 订单详情
	 	 * callbackConfig
	 	 * JuHeAPIdBeccancyService
	 	 * **/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/wzdj/callbackConfig")
		public @ResponseBody Object wzdjCallbackConfig(HttpServletRequest request,
				//贴牌号
				 @RequestParam(value = "brand_id") long brandId,
				//  	用户自定义订单号
				 @RequestParam(value = "callbackurl") String callbackurl
				){
			Map map = new HashMap();
			Brand brand =brandMangeBusiness.findBrandById(brandId);
			if(brand==null||brand.getJuhekey()==null||brand.getJuheOpenid()==null||brand.getJuheOpenid().equals("")||brand.getJuhekey().equals("")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
				return map;
			}
			String result ="";
			result=JuHeAPIdBeccancyService.wzdjCallbackConfig(callbackurl,   brand.getJuheWzdjKey());
			JSONObject jsonObject =  JSONObject.fromObject(result);
			String code=jsonObject.getString("error_code");
			JSONObject resObject  =  jsonObject.getJSONObject("result");
			if(code.equals("0")){
				map.put(CommonConstants.RESULT, resObject);
			}else{
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE,jsonObject.getString("reason"));
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}	
		/**
		 * 订单详情
	 	 * callbackConfig
	 	 * JuHeAPIdBeccancyService
	 	 * **/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/wzdj/callback")
		public @ResponseBody Object wzdjCallback(HttpServletRequest request,
				//用户订单号
				 @RequestParam(value = "userOrderId") String userOrderId,
				//  	备注信息
				 @RequestParam(value = "remark") String remark,
				 //			  	订单状态
				 @RequestParam(value = "state") String state
				){
			if(state.equals("1")){
            	
            	RestTemplate restTemplate=new RestTemplate();
				URI uri = util.getServiceUrl("transactionclear", "error url request!");
				String url = uri.toString() + "/v1.0/transactionclear/payment/type1/update";
				
				/**根据的用户手机号码查询用户的基本信息*/
				MultiValueMap<String, String>  requestEntity  = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code",  userOrderId);
//				requestEntity.add("third_code",  sporder_id);
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT======state===="+state+"======"+result);
				return "success"; 
             }else if(state.equals("9")){
            	 RestTemplate restTemplate=new RestTemplate();
    				URI uri = util.getServiceUrl("transactionclear", "error url request!");
    				String url = uri.toString() + "/v1.0/transactionclear/payment/type1/update";
    				MultiValueMap<String, String>  requestEntity  = new LinkedMultiValueMap<String, String>();
    				requestEntity.add("status", "2");
					requestEntity.add("order_code",  userOrderId);
//					requestEntity.add("third_code",  sporder_id);
    				String result = restTemplate.postForObject(url, requestEntity, String.class);
    				LOG.info("RESULT======state===="+state+"======"+result);
    				return "success"; 
             }
			Map map = new HashMap();
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}	
		
		
		/****保险购买****/
		String callback="http://ds.jiepaypal.cn/v1.0/facade/added/zhongan/callback";
		String key="open20160501";
		private String FormUrl="https://ztg.zhongan.com/promote/showcase/landingH5.htm?promoteType=2&promotionCode=INST170926063033&redirectType=h5";
		
		/**
		 * 
		 *保险购买
	 	 * callbackConfig
	 	 * JuHeAPIdBeccancyService
	 	 * **/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/added/zhongan/{token}")
		public @ResponseBody Object zhonganCall(HttpServletRequest request,
				@PathVariable("token") String token
				){
			
			Map map = new HashMap();
			long userId;
			try {
				userId = TokenUtil.getUserId(token);
			} catch (Exception e) {
		
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
				map.put(CommonConstants.RESP_MESSAGE, "token无效"); //return "redirect:"+redirect_url;
				return map;
			}
			
//			FormUrl
			String data = "&payChannel=alipay,unionpay";
			data+="&returnUrl="+callback;
			
			String bizContent="{'extraInfo': {'userid': '"+userId
								+"' }";
			User user =userLoginRegisterBusiness.queryUserById(userId);
			if (user.getRealnameStatus().equals("1")){
				/**获取身份证实名信息*/
				URI uri = util.getServiceUrl("paymentchannel", "error url request!");
				String url = uri.toString() + "/v1.0/paymentchannel/realname/userid";
				MultiValueMap<String, Long> requestEntity  = new LinkedMultiValueMap<String, Long>();
				requestEntity.add("userid", user.getId());
				RestTemplate restTemplate=new RestTemplate();
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================/v1.0/paymentchannel/realname/userid" + result);
				JSONObject jsonObject =  JSONObject.fromObject(result);
				JSONObject authObject  =  jsonObject.getJSONObject("realname");
				bizContent+=",'policyHolderCertiNo': '"+authObject.getString("idcard")
							+"','policyHolderCertiType': 'I','policyHolderPhone': '"+user.getPhone()
							+"','policyHolderUserName': '"+authObject.getString("realname")+"'";
			}
			bizContent+="}";
			LOG.info("bizContent="+bizContent);
			
			data+="&bizContent="+RC4Util.encryRC4String(bizContent, key);
			LOG.info("data="+data);
			
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, FormUrl+data);
			LOG.info("FormUrl+data="+FormUrl+data);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}	
		
		
		
		
		
		
	}
		

