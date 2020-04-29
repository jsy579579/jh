package cn.jh.clearing.service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.AutoRebateConfigBusiness;
import cn.jh.clearing.business.AutoRebateHistoryBusiness;
import cn.jh.clearing.business.PaymentOrderBusiness;
import cn.jh.clearing.pojo.AutoRebateConfig;
import cn.jh.clearing.pojo.AutoRebateHistory;
import cn.jh.clearing.util.Util;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Configuration
@Controller
@EnableScheduling
public class AutoRebateExecuteTask {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private AutoRebateConfigBusiness autoRebateConfigBusiness;

	@Autowired
	private PaymentOrderBusiness paymentOrderBusiness;

	@Autowired
	private AutoRebateHistoryBusiness autoRebateHistoryBusiness;
	
	@Value("${schedule-task.on-off}")
	private String scheduleTaskOnOff;

	@Autowired
	Util util;
	/**30分钟执行一次**/
	@Scheduled(cron = "0 30 0 * * ?")
	private void scheduleAutoRebateTask(){
		if("true".equals(scheduleTaskOnOff)){
			executeAutoRebate();
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/test")
	public @ResponseBody Object testAutoRebate(HttpServletRequest request) {
		executeAutoRebate();
		return "Ok";
	}

	private void executeAutoRebate() {
		LOG.info("*******************开始执行自动返利*******************");

		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/all";

		// 拿到需要返利的贴牌号
		// MultiValueMap<String, String> requestEntity = new
		// LinkedMultiValueMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		String respString = "";
		JSONObject jsonObject = null;
		String respCode = "";
		MultiValueMap<String, String> requestEntity;
		try {
			respString = restTemplate.getForObject(url, String.class);
			jsonObject = JSONObject.fromObject(respString);
			respCode = jsonObject.getString(CommonConstants.RESP_CODE);
		} catch (RestClientException e) {
			LOG.error("用户返利异常=============================/v1.0/user/brand/query/all:");
			e.printStackTrace();LOG.error("",e);
		}

		if (CommonConstants.SUCCESS.equals(respCode)) {
			JSONArray brandArray = jsonObject.getJSONArray("result");
			if (brandArray != null && brandArray.size() > 0) {
				for (Object object : brandArray) {
					JSONObject brandJson = (JSONObject) object;
					Integer brandOnOff = brandJson.containsKey("autoRebateConfigOnOff")
							? brandJson.getInt("autoRebateConfigOnOff") : null;
					Long manageId = brandJson.containsKey("manageId") ? brandJson.getLong("manageId") : null;
					if (brandOnOff != null && brandOnOff == 1 && manageId != null) {
						List<AutoRebateConfig> autoRebateConfigs = autoRebateConfigBusiness
								.findByBrandIdAndOnOff(brandJson.getLong("id"), 1);
						if (autoRebateConfigs != null && autoRebateConfigs.size() > 0) {
							for (AutoRebateConfig autoRebateConfig : autoRebateConfigs) {
								Long autoRebateConfigId = autoRebateConfig.getId();
								Long brandId = autoRebateConfig.getBrandId();
								BigDecimal limitAmount = autoRebateConfig.getLimitAmount();
								BigDecimal rebate = autoRebateConfig.getRebate();
								List<Long> userIds = paymentOrderBusiness.queryUserIdsByAmount(brandId, limitAmount,
										autoRebateConfigId);
								LOG.info("符合条件的userId有:=============" + userIds);
								if(userIds!=null && userIds.size() >0){
									uri = util.getServiceUrl("facade", "error url request!");
									url = uri.toString() + "/v1.0/facade/amount2amount";
									AutoRebateHistory autoRebateHistoryModel;
									for(Long destinationUserId : userIds){
										if(0 == destinationUserId.compareTo(manageId)){
											continue;
										}
										requestEntity = new LinkedMultiValueMap<String, String>();
										restTemplate = new RestTemplate();
										requestEntity.add("sourceUserId", manageId+"");
										requestEntity.add("isVerify", "notVerify");
										requestEntity.add("destinationUserId",destinationUserId+"");
										requestEntity.add("amount", rebate+"");
										requestEntity.add("orderDesc", "充值返利");
										try {
											restTemplate.postForObject(url, requestEntity, String.class);
											jsonObject = JSONObject.fromObject(respString);
										} catch (RestClientException e) {
											LOG.error("用户返利异常=============================/v1.0/facade/amount2amount:返利userId" + destinationUserId + ",异常为:");
											e.printStackTrace();LOG.error("",e);
											continue;
										}
										if(!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))){
											LOG.error("用户返利失败=============================/v1.0/facade/amount2amount:返利userId" + destinationUserId + ",原因为:" +jsonObject.getString(CommonConstants.RESP_MESSAGE));
											if(CommonConstants.ERROR_AMOUNT_ERROR.equals(jsonObject.getString(CommonConstants.RESP_CODE))){
												uri = util.getServiceUrl("user", "error url request!");
												url = uri.toString() + "/v1.0/user/jpush/tset";
												requestEntity = new LinkedMultiValueMap<String, String>();
												restTemplate = new RestTemplate();
												requestEntity.add("userId", manageId+"");
												requestEntity.add("alert", "返利通知");
												requestEntity.add("content", "您的用户达到返利要求,但帐户余额不足,无法返利!请及时充值!");
												requestEntity.add("btype", "balanceadd");
												requestEntity.add("btypeval", "");
												restTemplate.postForObject(url, requestEntity, String.class);
												break;
											}
											continue;
										}
										autoRebateHistoryModel = new AutoRebateHistory();
										autoRebateHistoryModel.setUserId(Long.valueOf(destinationUserId));
										autoRebateHistoryModel.setRebateConfigId(autoRebateConfigId);
										autoRebateHistoryBusiness.createNewHistory(autoRebateHistoryModel);
										LOG.info("返利成功,用户userId为:" + destinationUserId);
									}
								}
							}
						}
					}
				}
			}
		}
	}



}
