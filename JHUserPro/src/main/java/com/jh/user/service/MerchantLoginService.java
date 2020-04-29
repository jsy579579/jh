package com.jh.user.service;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.user.business.MerchantLoginBusiness;
import com.jh.user.pojo.Merchant;
import com.jh.user.pojo.User;
import com.jh.user.util.Util;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.Md5Util;
import cn.jh.common.utils.TokenUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class MerchantLoginService {
	private static final Logger LOG = LoggerFactory.getLogger(MerchantLoginService.class);

	@Autowired
	private MerchantLoginBusiness merchantLoginBusiness;

	@Autowired
	Util util;

	/** 商户登陆 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/merchant/login")
	public @ResponseBody Object MerchantLogin(HttpServletRequest request,
			@RequestParam(value = "preMchId") String preMchId, 
			@RequestParam(value = "password") String password) {
		Map map = new HashMap();
		String passwd = Md5Util.getMD5(password);
		try {
			Merchant merchant = merchantLoginBusiness.isLoginMerchant(preMchId, passwd);

			if (merchant != null && merchant.getId() > 0) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");

			} else {

				map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PASS_ERROR);
				map.put(CommonConstants.RESP_MESSAGE, "密码错误或商户不存在");

			}
		} catch (Exception e) {
			LOG.info(e.getMessage());
		}
		return map;
	}

	/** 商户注册 **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/merchant/register")
	public @ResponseBody Object registerUser(HttpServletRequest request,
			@RequestParam(value = "preMchId") String preMchId, 
			@RequestParam(value = "phone", required=false) String phone,
			@RequestParam(value = "password", defaultValue = "123456", required = false) String password,
			@RequestParam(value = "mchId", required = false) String mchId,
			@RequestParam(value = "premchkey") String premchkey,
			@RequestParam(value = "fullname", required = false) String fullname) {
		Map map = new HashMap();
		
		try {
			   Merchant merchant = new Merchant();
			   merchant = merchantLoginBusiness.findAllByPreMchId(preMchId);
			   
			   if(merchant!=null&&merchant.getId()!=0){
				   map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
				   map.put(CommonConstants.RESP_MESSAGE, "注册失败,商户号已存在!");
			   }else{
				     merchant = new Merchant();			    
				     merchant.setPreMchId(preMchId);
				     merchant.setPhone(phone);
				     merchant.setPremchkey(premchkey);
				     merchant.setMchId(mchId);
				     merchant.setFullName(fullname);
				     String pass = Md5Util.getMD5(password);
				     merchant.setPassword(pass);		
				     
					 try {
						merchant = merchantLoginBusiness.saveMerchant(merchant);
						 map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						 map.put(CommonConstants.RESULT, merchant);						
						 map.put(CommonConstants.RESP_MESSAGE, "注册成功");
					} catch (Exception e) {
						LOG.info(e.getMessage());
						LOG.info("未成功注册商户");
					}			    
			   }
		} catch (Exception e) {
			LOG.info(e.getMessage());
			LOG.info("查询商户发生错误");
			}
		return map;
	}
	
	
	     /**查询所有商户*/
	    @RequestMapping(method=RequestMethod.POST,value="/v1.0/user/merchant/query/all")
		public @ResponseBody Object queryAll(HttpServletRequest request){
			Map map = new HashMap();
			try {
				List<Merchant> merchant = merchantLoginBusiness.findAllMerchant();
				if(merchant!=null&&!merchant.equals("")){
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESULT, merchant);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PASS_ERROR);
				map.put(CommonConstants.RESP_MESSAGE, "没有商户信息");
			 }
		 } catch (Exception e) {
				LOG.info(e.getMessage());
				LOG.info("未找到商户");
		}
			return map;
}
	    
	    
	    /**更新商户的登陆密码*/
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/merchant/password/update")
		public @ResponseBody Object updateMerchantPassword(HttpServletRequest request,   
				@RequestParam(value = "preMchId") String preMchId,
				@RequestParam(value = "password") String password
				){
			
			Map map = new HashMap();	
			try {
				
				 Merchant merchant = merchantLoginBusiness.findMerchantByMchId(preMchId);
		            if(merchant == null){
		            	map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_USER_NO_REGISTER);
		    			map.put(CommonConstants.RESP_MESSAGE, "商户未注册");
		        		return map;
		            }
		            try {
						merchant.setPassword(Md5Util.getMD5(password));
						   merchantLoginBusiness.saveMerchant(merchant);          
				    	   map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
				    	   map.put(CommonConstants.RESP_MESSAGE, "成功");
				    	   return map;	
					} catch (Exception e) {
						  LOG.info(e.getMessage());
					}				
			} catch (Exception e) {
				LOG.info(e.getMessage());
			}
          return map;
	}
}
