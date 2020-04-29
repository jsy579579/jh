package com.jh.paymentchannel.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.PassVerificationBusiness;
import com.jh.paymentchannel.business.PassVerificationCountBusiness;
import com.jh.paymentchannel.business.RealnameAuthBusiness;
import com.jh.paymentchannel.pojo.PassVerification;
import com.jh.paymentchannel.pojo.PassVerificationCount;
import com.jh.paymentchannel.pojo.RealNameAuth;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.TokenUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class PassVerificationService {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	PassVerificationCountBusiness passVerificationCountBusiness;

	@Autowired
	PassVerificationBusiness passVerificationBusiness;

	@Autowired
	private RealnameAuthBusiness  realnameAuthBusiness;
	@Autowired
	Util util;

	/**
	 * 生成信用卡管家激活码接口
	 * 
	 * @param request
	 * @param brandId 		贴牌id			必传
	 * @param size 			生成激活码个数	非必传,默认为500,最大为1000
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/pass/verification/add/{token}")
	public @ResponseBody Object addKey(HttpServletRequest request,
			//登录token
			@PathVariable("token") String token,
			//生成数量1~1000
			@RequestParam(value = "size", required = false, defaultValue = "500") int size) {
		Map<String, Object> map = new HashMap<String, Object>();
		//获取当前归属信息
		long userId;
		long brandId;
		String phone;
		try {
			userId = TokenUtil.getUserId(token);
			brandId=TokenUtil.getBrandid(token);
			phone=TokenUtil.getUserPhone(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		
		}
		
		String result = "";
		String respCode = "";
		String respMsg = "";
		JSONObject jsonObject;
		JSONObject brandObjb;
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() +  "/v1.0/user/brand/query/id?brand_id=" + brandId;
		RestTemplate restTemplate = new RestTemplate();
		try {
			result = restTemplate.getForObject(url, String.class);
			jsonObject = JSONObject.fromObject(result);
			brandObjb = jsonObject.getJSONObject("result");
			respCode = jsonObject.getString(CommonConstants.RESP_CODE);
			respMsg = jsonObject.getString(CommonConstants.RESP_MESSAGE);
		} catch (Exception e) {
			LOG.error("查询用户数量异常==========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "生成激活码失败,请重试");
			return map;
		}
		
		if(brandObjb.getLong("manageid")!=userId) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您无生成激活码权限！！！");
			return map;
		}
		
		int activeKeyCount = 0;
		int userCount = 0;
		activeKeyCount = passVerificationBusiness.queryCountByBrandId(brandId+"");
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/query/brand/count";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("brandId", brandId+"");
		restTemplate = new RestTemplate();
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			jsonObject = JSONObject.fromObject(result);
			respCode = jsonObject.getString(CommonConstants.RESP_CODE);
			respMsg = jsonObject.getString(CommonConstants.RESP_MESSAGE);
		} catch (Exception e) {
			LOG.error("查询用户数量异常==========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "生成激活码失败,请重试");
			return map;
		}
		
		if(!CommonConstants.SUCCESS.equals(respCode)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE,respMsg);
			return map;
		}
		
		userCount = jsonObject.getInt(CommonConstants.RESULT);
		
		if(userCount >= 1000){
			userCount = userCount * 100;
		}else{
			userCount = 100000;
		}
		if(activeKeyCount > userCount){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "生成激活码失败,已生成至最大数量!");
			return map;
		}
		
		if (size > 1000 || size < 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "个数只能输入大于0并且小于1000");
			return map;
		}

		Integer batchNo = null;

		try {
			batchNo = passVerificationBusiness.findLastBatchNoByBrandId(brandId+"");
		} catch (Exception e1) {
			LOG.error("查询激活码批次异常" + e1);
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "操作失败");
		}
		if (batchNo == null) {
			batchNo = new Integer(0);
		}
		batchNo = batchNo + 1;
		try {
			List<PassVerification> passList = new ArrayList<PassVerification>();
			String key = "";
			PassVerification model = null;
			for (int i = 0; i < size; i++) {
				model = new PassVerification(brandId+"", batchNo);
				key = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
				model.setPasskey(key);
				model.setDependenceUserId(userId);
				model.setDependencePhone(phone);
				model.setUserName(brandObjb.getString("name"));
				model = passVerificationBusiness.save(model);
				passList.add(model);
			}
			this.passVerificationCountAdd(userId, brandObjb.getString("name"), phone, brandId, size);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, passList);
			map.put(CommonConstants.RESP_MESSAGE, "操作成功");
		} catch (Exception e) {
			LOG.error("生成激活码异常" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "操作失败");
		}
		return map;
	}

	/**
	 * 验证用户帐号是否激活信用卡管家功能
	 * 
	 * @param request
	 * @param brandId 	贴牌id				必传
	 * @param userId 	需要激活用户的id		必传
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/pass/verification/isactive")
	public @ResponseBody Object isActive(HttpServletRequest request, @RequestParam(value = "brandId") String brandId,
			@RequestParam(value = "userId") String userId) {
		Map<String, Object> map = new HashMap<String, Object>();
		PassVerification model = null;
		try {
			model = passVerificationBusiness.findPassByUserIdAndBrandId(userId.trim(), brandId.trim());
		} catch (Exception e2) {
			LOG.error("查询信用卡管家激活码异常==========" + e2);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "验证失败,请确定帐号是否正确后再重试");
			return map;
		}

		if (model == null) {
			map.put(CommonConstants.RESP_CODE, "false");
			map.put(CommonConstants.RESP_MESSAGE, "该帐户未激活信用卡管家功能");
			return map;
		} else {
			map.put(CommonConstants.RESP_CODE, "true");
			map.put(CommonConstants.RESP_MESSAGE, "该帐户已激活信用卡管家功能");
			map.put(CommonConstants.RESULT, model);
			return map;
		}
	}

	/** 根据key修改userid,实现激活码与userId绑定 */
	/**
	 * 使用激活码的用户必须时未激活的用户
	 * @param request
	 * @param key			激活码		必传
	 * @param userId		用户userId	必传
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/pass/verification/active")
	public @ResponseBody Object updatePassBykey(HttpServletRequest request, @RequestParam(value = "passkey") String key,
			@RequestParam(value = "userId") String userId) {
		Map<String, Object> map = new HashMap<String, Object>();
		key = key.trim();
		userId = userId.trim();
		PassVerification model = null;
		try {
			model = passVerificationBusiness.findPassByUserId(userId);
		} catch (Exception e2) {
			LOG.error("查询信用卡管家激活码异常==========" + e2);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "激活失败,请确定帐号是否正确后再重试");
			return map;
		}

		String result = "";
		String respCode = "";
		String respMsg = "";
		JSONObject jsonObject;
		URI uri;
		String url;
		MultiValueMap<String, String> requestEntity;
		RestTemplate restTemplate;
		if (model != null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您已激活过,无需再进行激活！");
			return map;
		}
		
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/find/by/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", userId);
		restTemplate = new RestTemplate();
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			jsonObject = JSONObject.fromObject(result);
			respCode = jsonObject.getString(CommonConstants.RESP_CODE);
			respMsg = jsonObject.getString(CommonConstants.RESP_MESSAGE);
		} catch (Exception e) {
			LOG.error("查询用户异常==========");
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "激活失败,请重试");
			return map;
		}
		
		if(!CommonConstants.SUCCESS.equals(respCode)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE,respMsg);
			return map;
		}
		jsonObject = jsonObject.getJSONObject(CommonConstants.RESULT);
		String brandId = jsonObject.getString("brandId");
		String phone=jsonObject.getString("phone");
		try {
			model = passVerificationBusiness.findByPasskeyAndStatusAndBrandId(key,"0",brandId);
		} catch (Exception e1) {
			LOG.error("查询信用卡管家激活码异常==========" + e1);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "激活失败,请确定激活码是否正确后再重试");
			return map;
		}
		if (model == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "激活失败,该激活码无效,请输入新激活码");
			return map;
		}
		RealNameAuth RealNameAuth=realnameAuthBusiness.findRealNamesAuthByUsserId(Long.valueOf(userId.trim()));
		if(RealNameAuth!=null) {
			model.setUserName(RealNameAuth.getRealname());
		}
		model.setUserPhone(phone);
		model.setStatus("1");
		model.setUserId(userId);
		
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/active/bankmanager";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", userId.trim());
		restTemplate = new RestTemplate();
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			jsonObject = JSONObject.fromObject(result);
			respCode = jsonObject.getString(CommonConstants.RESP_CODE);
			respMsg = jsonObject.getString(CommonConstants.RESP_MESSAGE);
		} catch (Exception e) {
			LOG.error("设置user激活信用卡管家字段异常==========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "激活失败,未知错误,请重试");
			return map;
		}
		
		if(!CommonConstants.SUCCESS.equals(respCode)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE,respMsg);
			return map;
		}

		try {
			model = passVerificationBusiness.save(model);
		} catch (Exception e) {
			LOG.error("保存激活码异常==========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "激活失败,未知错误,请重试");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "恭喜您!激活成功!");
		map.put(CommonConstants.RESULT, model);
		return map;

	}
	
	/** 根据key修改userid,实现激活码与userId绑定 */
	/**
	 * 判断激活码的有效性
	 * @url /v1.0/paymentchannel/pass/verification/find
	 * @param request
	 * @param passkey			激活码		必传
	 * @param brand_id		用户userId	必传
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/pass/verification/find")
	public @ResponseBody Object getPassBykey(HttpServletRequest request, 
			@RequestParam(value = "passkey") String key,
			@RequestParam(value = "brand_id") String brandId) {
		Map<String, Object> map = new HashMap<String, Object>();
		PassVerification model = null;
		key = key.trim();
		brandId = brandId.trim();
		try {
			model = passVerificationBusiness.findByPasskeyAndStatusAndBrandId(key,"0",brandId);
			if(model==null) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "该激活码已失效");
				map.put(CommonConstants.RESULT, "");
			}else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "有效激活码");
				map.put(CommonConstants.RESULT, model);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
        	ExceptionUtil.errInfo(e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "该激活码已失效");
			map.put(CommonConstants.RESULT, "");
		}
		
		
		return map ;
		
	}

	/**
	 * 给代理商用的查询可用激活码接口,根据brandId查询出当前可用的激活码
	 * @param request
	 * @param brandId 		贴牌id			必传
	 * @param page  		页数				非必传,默认0
	 * @param size  		一页的记录数		非必传,默认20
	 * @param direction 	 desc:升序		非必传,默认升序
	 * @param sortProperty 	创建时间排序		非必传
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/pass/verification/queryavailable")
	public @ResponseBody Object queryByAvailable(HttpServletRequest request,
			@RequestParam(value = "brandId") String brandId,
//			status:0 为可用 1: 为已使用 2: 为全部
			@RequestParam(value="status",required=false,defaultValue="0")String status,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Map<String, Object> map = new HashMap<String, Object>();
		if ("".equals(brandId) || brandId == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "brandId不能为空");
			return map;
		}
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<PassVerification> models = null;
		try {
			if("0".equals(status)){
				models = passVerificationBusiness.findByBrandIdAndStatus(brandId, "0", pageable);
			}else if("1".equals(status)){
				models = passVerificationBusiness.findByBrandIdAndStatus(brandId, "1", pageable);
			}else if("2".equals(status)){
				models = passVerificationBusiness.findByBrandId(brandId, pageable);
			}
		} catch (Exception e) {
			LOG.error("查询可用激活码异常===============" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询可用激活码失败,请联系管理员");
			return map;
		}
		if (models == null || models.getSize() == 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无激活码,请先生成激活码");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询激活码成功");
		map.put(CommonConstants.RESULT, models);
		return map;
	}
	
	/**
	 * 反激活接口,使用该接口的用户必须是已激活
	 * @param reuqest
	 * @param userId		反激活的用户userId		必传
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pass/verification/unactive")
	public @ResponseBody Object cancelActive(HttpServletRequest reuqest,
			@RequestParam(value="userId")String userId
			){
		Map<String,Object> map = new HashMap<String,Object>();
		
		URI uri = util.getServiceUrl("user", "error url request");
		String url = uri.toString() + "/v1.0/user/unactive/bankmanager";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", userId);
		RestTemplate restTemplate = new RestTemplate();
		String respString = "";
		String respCode = "";
		String respMsg = "";
		JSONObject respJSON = null;
		try {
			respString = restTemplate.postForObject(url, requestEntity, String.class);
			respJSON = JSONObject.fromObject(respString);
			respCode = respJSON.getString(CommonConstants.RESP_CODE);
		} catch (RestClientException e) {
			LOG.error("取消用户激活状态异常===============" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "取消用户激活状态失败,请重试");
			return map;
		}
		if(!CommonConstants.SUCCESS.equals(respCode)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, respMsg);
			return map;
		}
		PassVerification model = null;
		try {
			model = passVerificationBusiness.findPassByUserId(userId);
		} catch (Exception e) {
			LOG.error("2取消用户激活状态异常===============" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "取消用户激活状态失败,请重试");
			return map;
		}
		if(model==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无该用户激活记录,无需再反激活");
			return map;
		}
		
		model.setUserId(null);
		model.setStatus("0");
		try {
			passVerificationBusiness.save(model);
		} catch (Exception e) {
			LOG.error("保存激活码实体异常=====================" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "取消用户激活状态失败,请重试");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "反激活成功,可重新再次激活");
		return map;
	}
	
	
	/**
	 * 设置若干批次激活码的归属
	 * @param request
	 * @param dependencePhone  归属人的手机号	必传
	 * @param dependenceName   归属人的姓名		必传
	 * @param batchNos		        批次号			必传,可传数组
	 * @param brandId		        贴牌号			必传
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pass/verification/set/dependence")
	public @ResponseBody Object setDependenceUser(HttpServletRequest request,
//			激活码管理员的手机号
			@RequestParam(value="dependencePhone")String dependencePhone,
//			激活码管理员的姓名
			@RequestParam(value="dependenceName")String dependenceName,
//			激活码管理员管理的激活码批次
			@RequestParam(value="batchNos")String[] batchNos,
//			管理员所在贴牌
			@RequestParam(value="brandId")String brandId
			){
		Map<String,Object> map = new HashMap<String,Object>();
		if("".equals(dependencePhone.trim())||"".equals(dependenceName.trim())|| batchNos.length==0 || "".equals(brandId.trim())){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的参数有误,请检查后重试!");
			return map;
		}
		
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/query/by/phone/and/brandid";
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		RestTemplate restTemplate = new RestTemplate();
		requestEntity.add("phone", dependencePhone);
		requestEntity.add("brandId", brandId);
		String responseStr;
		JSONObject responseJson;
		try {
			responseStr = restTemplate.postForObject(url, requestEntity, String.class);
			responseJson = JSONObject.fromObject(responseStr);
		} catch (Exception e) {
			LOG.error("获取用户信息异常:参数dependencePhone="+dependencePhone+",brandId"+brandId+","+e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,请稍后重试!");
			return map;
		}
		if(!CommonConstants.SUCCESS.equalsIgnoreCase(responseJson.getString(CommonConstants.RESP_CODE))){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, responseJson.containsKey(CommonConstants.RESP_MESSAGE)?responseJson.getString(CommonConstants.RESP_MESSAGE):"亲.网络出错了哦,请稍后重试!");
			return map;
		}
		JSONObject userJson = responseJson.getJSONObject(CommonConstants.RESULT);
		Long dependenceUserId = userJson.getLong("id");
		List<Integer> successBatchNos = new ArrayList<Integer>();
		if(batchNos.length >= 1){
			int batchNo;
			for(int i = 0; i < batchNos.length;i++){
				String strBatchNo = batchNos[i];
				try {
					batchNo = Integer.valueOf(strBatchNo);
				} catch (NumberFormatException e) {
					LOG.error("输入的批次号batchNo有误: 错误参数为"+strBatchNo+"," + e);
					continue;
				}
				try {
					passVerificationBusiness.setDependence(dependenceUserId,dependencePhone,dependenceName,batchNo,brandId);
				} catch (Exception e) {
					LOG.error("设置激活码管理员异常: " + e);
					continue;
				}
				successBatchNos.add(batchNo);
			}
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "设置成功");
		map.put(CommonConstants.RESULT, successBatchNos);
		return map;
	}
	
	
	/**
	 * 查询批次分配情况
	 * @param request
	 * @param brandId				贴牌号									必传
	 * @param dependenceStatus		查询的分配状态: 0:未分配,1:已分配,2:全部	非必传,默认为2
	 * @param page					页数										非必传,默认为0
	 * @param size					一页的记录数								非必传,默认为20条
	 * @param direction				升序										非必传,默认为升序
	 * @param sortProperty			排序依据									非必传,默认为根据创建时间
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pass/verification/query/batchno/dependence")
	public @ResponseBody Object queryByDependence(HttpServletRequest request,
			@RequestParam(value="brandId")String brandId,
//			是否分配给管理员,否:0 ,是:1 ,2:全部
			@RequestParam(value="dependenceStatus",required=false,defaultValue="2")String dependenceStatus,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Map<String,Object> map = new HashMap<String,Object>();
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<PassVerification> models = null;
		try {
			if("".equals(brandId.trim())||"".equals(dependenceStatus.trim())||(Integer.valueOf(dependenceStatus)!=0&&Integer.valueOf(dependenceStatus)!=1&&Integer.valueOf(dependenceStatus)!=2)){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的参数有误,请检查后重试!");
				return map;
			}
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的参数有误,请检查后重试!");
			return map;
		}
		try {
			if(Integer.valueOf(dependenceStatus)==0){
//				获得未分配批次
				models = passVerificationBusiness.queryByDependence(brandId,pageable);
			}else if(Integer.valueOf(dependenceStatus)==1){
//				获得已分配批次
				models = passVerificationBusiness.queryByDependenced(brandId,pageable);
			}else{
//				获得全部批次
				models = passVerificationBusiness.queryAllByDependenced(brandId,pageable);
			}
		} catch (Exception e) {
			LOG.error("查询管理批次batch异常==============参数"+brandId+"===============" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.发生未知错误,请稍后重试!");
			return map;
		}
		if(models == null || models.getSize() == 0){
			map.put(CommonConstants.RESP_MESSAGE, "查询成功,但无激活码数据!");
		}else{
			map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
			map.put(CommonConstants.RESULT, models);
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pass/verification/query/batchno/count")
	public @ResponseBody Object queryByBatchNoAndStatus(HttpServletRequest request,
			@RequestParam(value="batchNo")String sbatchNo,
			@RequestParam(value="brandId")String brandId
			){
		Map<String,Object>map = new HashMap<String,Object>();
		Integer batchNo = null;
		try {
			batchNo = Integer.valueOf(sbatchNo);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,输入的参数有误,请检查后重试!");
			return map;
		}
		
		int totalCount;
		int activeCount;
		int unactiveCount;
		
		totalCount = passVerificationBusiness.queryCountByBatchNoAndBrandIdAndStatus(batchNo,brandId);
		activeCount = passVerificationBusiness.queryActiveCountByBatchNo(batchNo,brandId,"1");
		unactiveCount = passVerificationBusiness.queryActiveCountByBatchNo(batchNo,brandId,"0");
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put("totalCount", totalCount);
		map.put("activeCount", activeCount);
		map.put("unactiveCount", unactiveCount);
		return map;
	}
	
	/**
	 * 激活码管理使用接口,可查看自己名下的激活码
	 * @param request
	 * @param token				管理员登陆时获得的token
	 * @param activeStatus		查看指定状态下的激活码,0:未激活的  1:已激活的  2:全部的
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pass/verification/query/by/user/{token}")
	public @ResponseBody Object queryPassByDependenceUser(HttpServletRequest request,
			@PathVariable("token")String token,
			@RequestParam(value="activeStatus",required=false,defaultValue="2")String activeStatus,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Map<String,Object> map = new HashMap<String,Object>();
		long userId = 833566829l;
		long brandId = 25;
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<PassVerification> models = null;
		try {
			userId = TokenUtil.getUserId(token);
			brandId = TokenUtil.getBrandid(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效，查询激活码失败!");
			return map;
		}
		
		try {
			if("".equals(activeStatus)||(Integer.valueOf(activeStatus)!=0&&Integer.valueOf(activeStatus)!=1&&Integer.valueOf(activeStatus)!=2)){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的参数有误,请检查后重试!");
				return map;
			}
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的参数有误,请检查后重试!");
			return map;
		}
		if(Integer.valueOf(activeStatus) == 0){
//			未激活的激活码
			models = passVerificationBusiness.queryByDependenceUserIdAndBrandId(userId,brandId+"","0",pageable);
		}else if(Integer.valueOf(activeStatus) == 1){
//			已激活的激活码
			models = passVerificationBusiness.queryByDependenceUserIdAndBrandId(userId,brandId+"","1",pageable);
		}else{
//			全部激活码
			models = passVerificationBusiness.queryByDependenceUserIdAndBrandId(userId,brandId+"",pageable);
		}
		
		if(models==null || models.getSize() == 0){
			map.put(CommonConstants.RESP_MESSAGE, "查询成功,但无激活码数据!");
		}else{
			map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
			map.put(CommonConstants.RESULT, models);
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		return map;
	}
	
	
	/**
	 * 激活码下拨
	 * @param request
	 * @param dependencePhone  归属人的手机号	必传
	 * @param dependenceName   归属人的姓名		必传
	 * @param number		 	  数量			必传,可传数组
	 * @param brandId		        贴牌号			必传
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pass/verification/allocated/{token}")
	public @ResponseBody Object verificationAllocated(HttpServletRequest request,
			//登录token
			@PathVariable("token") String token,
//			激活码管理员的手机号
			@RequestParam(value="dependencephone")String dependencePhone,
//			激活码管理员的姓名
			@RequestParam(value="dependencename")String dependenceName,
//			激活码管理员管理的激活码批次
			@RequestParam(value="number")   int number
			){
		Map<String,Object> map = new HashMap<String,Object>();
		//获取当前归属信息
		long userId;
		long brandId;
		try {
			userId = TokenUtil.getUserId(token);
			brandId=TokenUtil.getBrandid(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		
		}
		int userCount=passVerificationBusiness.findCountByBrandId(brandId+"", userId, 0+"", "");
		if("".equals(dependencePhone.trim())||"".equals(dependenceName.trim())|| number>userCount|| number==0){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的参数有误,请检查后重试!");
			return map;
		}
		
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/query/by/phone/and/brandid";
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		RestTemplate restTemplate = new RestTemplate();
		requestEntity.add("phone", dependencePhone);
		requestEntity.add("brandId", brandId+"");
		String responseStr;
		JSONObject responseJson;
		try {
			responseStr = restTemplate.postForObject(url, requestEntity, String.class);
			responseJson = JSONObject.fromObject(responseStr);
		} catch (Exception e) {
			LOG.error("获取用户信息异常:参数dependencePhone="+dependencePhone+",brandId"+brandId+","+e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,请稍后重试!");
			return map;
		}
		if(!CommonConstants.SUCCESS.equalsIgnoreCase(responseJson.getString(CommonConstants.RESP_CODE))){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, responseJson.containsKey(CommonConstants.RESP_MESSAGE)?responseJson.getString(CommonConstants.RESP_MESSAGE):"亲.网络出错了哦,请稍后重试!");
			return map;
		}
		JSONObject userJson = responseJson.getJSONObject(CommonConstants.RESULT);
		Long dependenceUserId = userJson.getLong("id");
		int updteCount=passVerificationBusiness.updateCountByBrandId(userId+"", dependenceUserId, dependencePhone, dependenceName, number);
		this.passVerificationCountAdd(dependenceUserId, dependenceName, dependencePhone, brandId, updteCount);
		this.passVerificationCountSubtract(userId,  brandId, updteCount);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "设置成功");
		map.put(CommonConstants.RESULT, updteCount);
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/query/verification/by/passkey")
	public @ResponseBody Object getByPasskey(
			@RequestParam(value="passkey")String passkey
			){
		PassVerification passVerification = passVerificationBusiness.findByPasskey(passkey);
		if(passVerification == null){
			return ResultWrap.init(CommonConstants.FALIED, "无该激活码数据");
		}else{
			return ResultWrap.init(CommonConstants.SUCCESS,"查询成功",passVerification);
		}
	}
	
	
	private void passVerificationCountAdd(long userId,String userName,String phone ,long brandid ,int ownSize) {
		
		PassVerificationCount pvc =passVerificationCountBusiness.findPassByUserId(userId);
		if(pvc==null) {
			pvc=new PassVerificationCount();
			pvc.setUserId(userId);
			pvc.setUserName(userName);
			pvc.setUserPhone(phone);
			pvc.setBrandId(brandid);
			pvc.setIssuePasskeys(0);
			pvc.setOwnPasskeys(ownSize);
			pvc.setCreateTime(new Date());
			pvc.setUpdateTime(new Date());
		}else {
			pvc.setUpdateTime(new Date());
			pvc.setOwnPasskeys(pvc.getOwnPasskeys()+ownSize);
		}
		passVerificationCountBusiness.save(pvc);
		
	}
	
private void passVerificationCountSubtract(long userId,long brandid ,int issueSize) {
		
		PassVerificationCount pvc =passVerificationCountBusiness.findPassByUserId(userId);
		if(pvc==null) {
			pvc=new PassVerificationCount();
			pvc.setUserId(userId);
			pvc.setUserName("");
			pvc.setUserPhone("");
			pvc.setIssuePasskeys(0);
			pvc.setBrandId(brandid);
			pvc.setOwnPasskeys(issueSize);
			pvc.setCreateTime(new Date());
			pvc.setUpdateTime(new Date());
		}else {
			pvc.setUpdateTime(new Date());
			pvc.setOwnPasskeys(pvc.getOwnPasskeys()-issueSize);
			pvc.setIssuePasskeys(pvc.getIssuePasskeys()+issueSize);
		}
		passVerificationCountBusiness.save(pvc);
		
	}

}
