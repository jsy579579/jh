package cn.jh.clearing.service;

import java.util.HashMap;
import java.util.Map;

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

import cn.jh.clearing.business.AutoRebateHistoryBusiness;
import cn.jh.clearing.pojo.AutoRebateHistory;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@EnableAutoConfiguration
@Controller
public class AutoRebateHistoryService {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private AutoRebateHistoryBusiness autoRebateHistoryBusiness;
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/rebatehistory/add")
	public @ResponseBody Object addAutoRebateHistory(HttpServletRequest request,
			@RequestParam(value="userId")String userId,
			@RequestParam(value="configId")String configId
			){
		Map<String,Object> map = new HashMap<String,Object>();
		AutoRebateHistory model = new AutoRebateHistory();
		model.setUserId(Long.valueOf(userId));
		model.setRebateConfigId(Long.valueOf(configId));
		
		try {
			model = autoRebateHistoryBusiness.createNewHistory(model);
		} catch (Exception e) {
			LOG.error("保存返利配置异常,异常原因======================:" + ";参数为: " + model);
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加返利历史失败,未知错误,请检查后重试!");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "保存返利历史记录成功!");
		map.put(CommonConstants.RESULT, model);
		return map;
		
	}
}
