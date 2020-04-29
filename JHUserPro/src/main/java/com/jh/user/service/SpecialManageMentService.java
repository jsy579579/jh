package com.jh.user.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.business.SpecialManageMentBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRoleResourceBusiness;
import com.jh.user.pojo.SpecialManageMent;
import com.jh.user.pojo.User;
import com.jh.user.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class SpecialManageMentService {

	private static final Logger LOG = LoggerFactory.getLogger(SpecialManageMentService.class);

	@Autowired
	Util util;

	@Autowired
	private SpecialManageMentBusiness  specialManageMentBusiness;
	
	@Autowired
	private UserRoleResourceBusiness userRoleResourceBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;
	
	

	// 根据brandId查询特殊权限的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/spcialmanagement/getby/brandid")
	public @ResponseBody Object getByBrandId(HttpServletRequest request, 
			@RequestParam(value = "brandId") String brandId) {
		
		SpecialManageMent specialManageMentByBrandId = specialManageMentBusiness.getSpecialManageMentByBrandId(brandId);
		
		if(specialManageMentByBrandId == null) {
			SpecialManageMent specialManageMent = new SpecialManageMent();
			specialManageMent.setBrandId(brandId);
			specialManageMent.setData("0");
			
			specialManageMentBusiness.createSpecialManageMent(specialManageMent);
			
			specialManageMentByBrandId = specialManageMentBusiness.getSpecialManageMentByBrandId(brandId);
		}
		
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", specialManageMentByBrandId);
	}

	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/spcialmanagement/add/data")
	public @ResponseBody Object addData(HttpServletRequest request, 
			@RequestParam(value = "brandId") String brandId,
			@RequestParam(value = "data") String data
			) {
		
		SpecialManageMent specialManageMentByBrandId = specialManageMentBusiness.getSpecialManageMentByBrandId(brandId);
		
		try {
			if(specialManageMentByBrandId != null) {
				
				specialManageMentByBrandId.setData(data);
				
				specialManageMentBusiness.createSpecialManageMent(specialManageMentByBrandId);
			}else {
				
				SpecialManageMent specialManageMent = new SpecialManageMent();
				specialManageMent.setBrandId(brandId);
				specialManageMent.setData(data);
				
				specialManageMentBusiness.createSpecialManageMent(specialManageMent);

			}
		} catch (Exception e) {
			LOG.info("保存信息有误======",e);
			
			return ResultWrap.init(CommonConstants.FALIED, "网络暂时失去了连接,请稍后重试!");
		}
		
		return ResultWrap.init(CommonConstants.SUCCESS, "添加/修改成功!");
	}
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/spcialmanagement/getby/userid")
	public @ResponseBody Object getByUserId(HttpServletRequest request, 
			@RequestParam(value = "userId") String userId) {
		
		User user = userLoginRegisterBusiness.queryUserById(Long.parseLong(userId));
		
		long brandId = user.getBrandId();
		
		SpecialManageMent specialManageMentByBrandId = specialManageMentBusiness.getSpecialManageMentByBrandId(brandId + "");
		
		if(specialManageMentByBrandId == null) {
			
			SpecialManageMent specialManageMent = new SpecialManageMent();
			specialManageMent.setBrandId(brandId + "");
			specialManageMent.setData("0");
			
			specialManageMentBusiness.createSpecialManageMent(specialManageMent);
			
			specialManageMentByBrandId = specialManageMentBusiness.getSpecialManageMentByBrandId(brandId + "");
		}
		
		List<Object> findRoleResourceIdByuserId = userRoleResourceBusiness.findRoleResourceIdByuserId(Long.parseLong(userId));
		
		
		String roleId = "0";
		JSONObject jsonObject = new JSONObject();
		if(findRoleResourceIdByuserId != null && findRoleResourceIdByuserId.size()>0) {
			
			Object object = findRoleResourceIdByuserId.get(0);
			
			jsonObject.put("roleId", object);
			jsonObject.put("data", specialManageMentByBrandId.getData());
		}else {
			
			jsonObject.put("roleId", roleId);
			jsonObject.put("data", specialManageMentByBrandId.getData());
		}
		
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", jsonObject);
	}
	
	/**
	 * 20190509更新用户升级新接口
	 * @param request
	 * @param userId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/spcialmanagement/getby/useridNew")
	public @ResponseBody Object getByUserIdNew(HttpServletRequest request, 
			@RequestParam(value = "userId") String userId) {
		
		User user = userLoginRegisterBusiness.queryUserById(Long.parseLong(userId));
		
		long brandId = user.getBrandId();
		
		SpecialManageMent specialManageMentByBrandId = specialManageMentBusiness.getSpecialManageMentByBrandId(brandId + "");
		
		if(specialManageMentByBrandId == null) {
			
			SpecialManageMent specialManageMent = new SpecialManageMent();
			specialManageMent.setBrandId(brandId + "");
			specialManageMent.setData("0");
			
			specialManageMentBusiness.createSpecialManageMent(specialManageMent);
			
			specialManageMentByBrandId = specialManageMentBusiness.getSpecialManageMentByBrandId(brandId + "");
		}
		
		List<Object> findRoleResourceIdByuserId = userRoleResourceBusiness.findRoleResourceIdByuserId(Long.parseLong(userId));
		
		String roleId = "0";
		JSONObject jsonObject = new JSONObject();
		if(findRoleResourceIdByuserId != null && findRoleResourceIdByuserId.size()>0) {
			
			Object object = findRoleResourceIdByuserId.get(0);
			
			jsonObject.put("roleId", object);
			jsonObject.put("data", specialManageMentByBrandId.getData());
		}else {
			
			jsonObject.put("roleId", roleId);
			jsonObject.put("data", specialManageMentByBrandId.getData());
		}
		
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", jsonObject);
	}
	
	
}
