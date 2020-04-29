package cn.jh.clearing.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jh.clearing.business.AutoRebateConfigBusiness;
import cn.jh.clearing.pojo.AutoRebateConfig;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.TokenUtil;

@EnableAutoConfiguration
@Controller
public class AutoRebateConfigService {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private AutoRebateConfigBusiness autoRebateConfigBusiness;

	/**
	 * 增加一条自动返利配置
	 * @param request
	 * @param token 用户的token
	 * @param sonOff 开关: 0:关 1:开
	 * @param slimitAmount 返利金额条件
	 * @param srebate 返利金额
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/rebateconfig/add/{token}")
	public @ResponseBody Object createNewRebateConfig(HttpServletRequest request, @PathVariable("token") String token,
			@RequestParam(value = "onOff", required = false, defaultValue = "0") String sonOff,
			@RequestParam(value = "limitAmount") String slimitAmount, @RequestParam(value = "rebate") String srebate) {
		Map<String, Object> map = new HashMap<String, Object>();
		long userId = 1;
		long brandId = 1;
		try {
			userId = TokenUtil.getUserId(token);
			brandId = TokenUtil.getBrandid(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效，添加返利配置失败!");
			return map;
		}
		BigDecimal limitAmount = null;
		BigDecimal rebate = null;
		try {
			limitAmount = BigDecimal.valueOf(Long.valueOf(slimitAmount)).setScale(2, BigDecimal.ROUND_HALF_UP);
			rebate = BigDecimal.valueOf(Long.valueOf(srebate)).setScale(2, BigDecimal.ROUND_HALF_UP);
		} catch (Exception e) {
			LOG.error("用户输入异常,已处理,异常参数为======================:" + sonOff);
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "添加返利配置失败,您输入的金额有误,请检查后重试!");
			return map;
		}

		int onOff = 0;
		try {
			onOff = Integer.valueOf(sonOff);
		} catch (NumberFormatException e) {
			LOG.error("用户金额输入异常,已处理,异常参数为======================:" + slimitAmount + ";" + srebate);
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "添加返利配置失败,您输入的参数有误,请检查后重试!");
			return map;
		}
		if (onOff != 0 && onOff != 1) {
			onOff = 0;
		}
		AutoRebateConfig model = new AutoRebateConfig();
		model.setBrandId(brandId);
		model.setCreateUserId(userId);
		model.setLimitAmount(limitAmount);
		model.setRebate(rebate);
		model.setOnOff(onOff);

		try {
			model = autoRebateConfigBusiness.createNewConfig(model);
		} catch (Exception e) {
			LOG.error("保存返利配置异常,异常原因======================:");
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加返利配置失败,未知错误,请检查后重试!");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "添加配置成功");
		map.put(CommonConstants.RESULT, model);
		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/rebateconfig/query")
	public @ResponseBody Object queryRebateConfig(HttpServletRequest request,
			// 要查询配置的brandId,不传为查询所有brandId配置
			@RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
			// 查询指定onOff状态的配置,不传为查询所有onOff配置
			@RequestParam(value = "onOff", required = false, defaultValue = "-1") String sonOff) {
		Map<String, Object> map = new HashMap<String, Object>();
		if ("".equals(sbrandId) || "null".equals(sbrandId) || null == sbrandId) {
			LOG.error("用户传入brandId参数错误,错误参数为:======================brandId:" + sbrandId);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,您输入的brandId有误,请检查后重试!");
			return map;
		}

		if ("".equals(sonOff) || "null".equals(sonOff) || null == sonOff) {
			LOG.error("用户传入onOff参数错误,错误参数为:======================sonOff:" + sonOff);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,您输入的onOff有误,请检查后重试!");
			return map;
		}

		long brandId = -1;
		int onOff = -1;
		try {
			brandId = Long.valueOf(sbrandId);
			onOff = Integer.valueOf(sonOff);
		} catch (NumberFormatException e) {
			LOG.error("用户传入参数错误,错误参数为:======================onOff:" + sonOff + ";brandId" + sbrandId);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,您输入的条件有误,请检查后重试!");
			return map;
		}
		
		List<AutoRebateConfig> models;
		try {
			if (brandId == -1) {
				if (onOff == -1) {
					models = autoRebateConfigBusiness.findAllConfig();
				}else{
					models = autoRebateConfigBusiness.findByOnOff(onOff);
				}
			} else {
				if (onOff == -1) {
					models = autoRebateConfigBusiness.findByBrandId(brandId);
				}else{
					models = autoRebateConfigBusiness.findByBrandIdAndOnOff(brandId,onOff);
				}
			}
		} catch (Exception e) {
			LOG.error("查询返利配置异常,异常原因为======================:");
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,未知错误,请稍后重试!");
			return map;
		}
		
		if(models == null || models.size() == 0){
			map.put(CommonConstants.RESP_MESSAGE,"查询成功,无配置!");
		}else{
			map.put(CommonConstants.RESP_MESSAGE,"查询成功!");

		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, models);
		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/rebateconfig/onoff")
	public @ResponseBody Object updateRebateConfigOnOff(HttpServletRequest request,
			@RequestParam(value = "configId") String sconfigId,
			@RequestParam(value = "onOff") String sOnOff
			) {
		Map<String, Object> map = new HashMap<String, Object>();
		long configId;
		int onOff;
		try {
			configId = Long.valueOf(sconfigId);
			onOff = Integer.valueOf(sOnOff);
		} catch (NumberFormatException e) {
			LOG.error("用户传入onOff,configId参数错误,错误参数为:======================configId:" + sconfigId + "," + sOnOff);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,您输入的configId或者onOff有误,请检查后重试!");
			return map;
		}
		
		if(onOff != 0 && onOff != 1){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,您输入的onOff有误,只能时0或1,请检查后重试!");
			return map;
		}
		
		AutoRebateConfig model = null;
		try {
			model = autoRebateConfigBusiness.findById(configId);
		} catch (Exception e) {
			LOG.error("查询返利配置异常,异常原因为======================:");
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,未知错误,请稍后重试!");
			return map;
		}

		if (model == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无相应配置,无法更改状态,请检查参数后重试!");
			return map;
		}
		model.setOnOff(onOff);
		try {
			model = autoRebateConfigBusiness.updateOnOff(model);
		} catch (Exception e) {
			LOG.error("保存返利配置异常,异常原因======================:");
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加返利配置失败,未知错误,请检查后重试!");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "更改配置状态成功!");
		map.put(CommonConstants.RESULT, model);
		return map;

	}

}
