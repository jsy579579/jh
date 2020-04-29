package cn.jh.clearing.service;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.SwiftpassOrderBusiness;
import cn.jh.clearing.pojo.SwiftpassOrder;
import cn.jh.clearing.util.Util;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Configuration
@Controller
@EnableScheduling
public class AutoDownloadOrderService {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	@Autowired
	SwiftpassOrderBusiness swiftpassOrderBusiness;
	
	@Value("${schedule-task.on-off}")
	private String scheduleTaskOnOff;

	@Autowired
	Util util;
	/***00:20执行***/
	@Scheduled(cron = "0 20 0 * * ?")
	public void scheduleDownloadOrder() {
		if("true".equals(scheduleTaskOnOff)){
			executeDownloadOrder();
		}
	}
	/***每6个小时**/
	@Scheduled(cron = "0 0 0/6 * * ?")
	public void scheduleDownloadOrderAuto() {
		if("true".equals(scheduleTaskOnOff)){
			executeDownloadOrder();
		}
	}
	/*3个小时执行一次***/
	@Scheduled(cron = "0 0 3 * * ?")
	public void scheduleDeleteRepeatingData(){
		if("true".equals(scheduleTaskOnOff)){
			deleteRepeatingData();
		}
	}
	
	
	public void executeDownloadOrder() {
		boolean isExecute = false;

		try {
			isExecute = isDownloanYesterdayOrders(getYeterday("0000-00-00"));
		} catch (Exception e1) {
			LOG.error(e1.getMessage());
		}
		JSONArray resultObjArray = null;
		if (!isExecute) {
			LOG.info("***************开始执行定时下载账单任务***************");

			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/merchant/query/all";

			/**
			 * 拿到所有商户的资质
			 */
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			RestTemplate restTemplate = new RestTemplate();
			String resultObjx = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("接口/v1.0/user/merchant/query/all--RESULT================" + resultObjx);
			JSONObject jsonObject = JSONObject.fromObject(resultObjx);
			resultObjArray = jsonObject.getJSONArray("result");
			String billDate = null;
			try {
				billDate = getYeterday("00000000");
			} catch (Exception e1) {
				LOG.error("获取时间异常");
			}
			List<SwiftpassOrder> models = null;
			if (resultObjArray.size() > 0) {
				for (int i = 0; i < resultObjArray.size(); i++) {
					jsonObject = resultObjArray.getJSONObject(i);
					String mchId = jsonObject.getString("preMchId");
					String key = jsonObject.getString("premchkey");
					try {
						// 账单下载
						models = swiftpassOrderBusiness.downloadSwiftpass(billDate, mchId, key);
						LOG.info("下载账单的记录数:****"+models.size()+"****");
					} catch (Exception e) {
						LOG.error(e.getMessage());
					}
				}
			}
			LOG.info("***************账单下载成功***************");
		} else {
			LOG.info("***************账单下载成功***************账单已存在");
		}
	}
	
	
	@RequestMapping(method = RequestMethod.POST,value = "/v1.0/transactionclear/swiftorder/delete")
	public @ResponseBody Object deleteRepeatingData(HttpServletRequest request){
		Map map = new HashMap();
		try {
			deleteRepeatingData();
		} catch (Exception e) {
			LOG.error(e.getMessage());
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "删除重复数据失败");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "	删除成功");
		return map;
		
	}
	

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/swiftorder/download")
	public @ResponseBody Object executeDownloadOrder(HttpServletRequest request,
//			需要查询账单的日期
			@RequestParam(name="queryDate" ,defaultValue="",required=false)String squeryDate,
//			商户帐号
			@RequestParam(name="merchantId" ,defaultValue="",required=false)String merchanId,
//			商户密钥
			@RequestParam(name="merchantKey" ,defaultValue="",required=false)String merchantKey
//			三个参数填了才进行指定商户账单下载
	) {
		Map map = new HashMap();
		if(!"".equals(squeryDate)&&!"".equals(merchanId)&&!"".equals(merchantKey)){
			if(!"".equals(squeryDate)){
				Date today = null;
				Date queryDate = null;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				
				try {
					today = sdf.parse(getYeterday("00000000"));
					queryDate = sdf.parse(squeryDate);
				} catch (Exception e2) {
					LOG.error(e2.getMessage());
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "亲,日期输入有误");
					return map;
				}
				
				if(queryDate.getTime()>today.getTime()){
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "亲,请选择今天以前的日期哦");
					return map;
				}else{
					squeryDate = sdf.format(queryDate);
				}
			}
			List<SwiftpassOrder> models = null;
			try {
				// 账单下载
				models = swiftpassOrderBusiness.downloadSwiftpass(squeryDate, merchanId,  merchantKey);
				LOG.info("下载账单的记录数:****"+models.size()+"****");
			} catch (Exception e) {
				LOG.error(e.getMessage());
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "亲,出错了哦,请重试!");
				return map;
			}
			if(models==null||models.size()==0){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "亲,当前日期无账单哦!");
				return map;
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "下载成功");
			map.put(CommonConstants.RESULT, models);
			return map;
		}
//		如果参数输入不足三个,执行下面下载所有商户昨天的账单
		boolean isExecute = false;
		try {
			isExecute = isDownloanYesterdayOrders(getYeterday("0000-00-00"));
		} catch (Exception e1) {
			LOG.error(e1.getMessage());
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲,服务器出错了哟");
			return map;
		}
		
		JSONArray resultObjArray = null;
		if (!isExecute) {
			LOG.info("***************开始执行定时下载账单任务***************");

			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/merchant/query/all";

			/**
			 * 拿到所有商户的资质帐号和密钥
			 */
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			RestTemplate restTemplate = new RestTemplate();
			String resultObjx = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("接口/v1.0/user/merchant/query/all--RESULT================" + resultObjx);
			JSONObject jsonObject = JSONObject.fromObject(resultObjx);
			resultObjArray = jsonObject.getJSONArray("result");
			String billDate = null;
			try {
				billDate = getYeterday("00000000");
			} catch (Exception e1) {
				LOG.error("获取时间异常" + e1.getMessage());
			}
			List<SwiftpassOrder> models = null;
			if (resultObjArray.size() > 0) {
				for (int i = 0; i < resultObjArray.size(); i++) {
					jsonObject = resultObjArray.getJSONObject(i);
					String mchId = jsonObject.getString("preMchId");
					String key = jsonObject.getString("premchkey");
					try {
						// 账单下载
						models = swiftpassOrderBusiness.downloadSwiftpass(billDate, mchId,  key);
						LOG.info("下载账单的记录数:****"+models.size()+"****");
					} catch (Exception e) {
						LOG.error(e.getMessage());
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "亲,出错了哦,请重试!");
						return map;
					}
				}
			}

			LOG.info("***************账单下载成功***************");
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "下载账单成功");
			return map;
		} else {
			LOG.info("***************账单下载成功***************");
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "账单已存在");
			return map;
		}
	}
	
	
	public void deleteRepeatingData(){
		String createTime = null;
		try {
			createTime = getYeterday("0000-00-00");
		} catch (Exception e2) {
			LOG.error("获取昨天日期异常====");
		}
		List<SwiftpassOrder> models2 = null;
		List<SwiftpassOrder> models= null;
		try {
			models = swiftpassOrderBusiness.findByCreateTime(createTime);
		} catch (Exception e1) {
			LOG.error(e1.getMessage());
		}
		try {
			if (models != null && models.size() > 0) {
				for (SwiftpassOrder model : models) {
					models2 = swiftpassOrderBusiness.findByOrderCode(model.getOrderCode());
					if (models2.size() > 1) {
						for (int i = 1; i < models2.size(); i++) {
							swiftpassOrderBusiness.delete(models2.get(i));
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		LOG.info("删除重复数据成功");
	}

	// 获取昨天的日期
	public String getYeterday(String fomat) throws Exception {
		Calendar cld = Calendar.getInstance();
		// System.out.println(cld);
		// System.out.println(cld.get(Calendar.YEAR) + "年");
		// System.out.println(cld.get(Calendar.MONTH) + "月");
		// System.out.println(cld.get(Calendar.DAY_OF_MONTH) + "日");
		// cld.set(Calendar.DAY_OF_MONTH, 1);
		// cld.set(Calendar.MONTH, 3);
		// cld.set(Calendar.YEAR, 2020);

		int year;
		int month;
		int dayOfMonth;
		// 拿到当前年月日
		year = cld.get(Calendar.YEAR);
		month = cld.get(Calendar.MONTH) + 1;
		dayOfMonth = cld.get(Calendar.DAY_OF_MONTH);

		// 判断当前日期是否为1日,是则月份减1,否则不减
		if (dayOfMonth == 1) {
			month = month - 1;
			// 判断减完之后是否为0,是则改为12月,年份减1,否则不改
			if (month == 0) {
				month = 12;
				year = year - 1;
			}

			// 判断当前年份是否是闰年
			Calendar cld2 = Calendar.getInstance();
			cld2.set(Calendar.YEAR, year);
			cld2.set(Calendar.MONTH, 11);
			cld2.set(Calendar.DAY_OF_MONTH, 31);
			if (cld2.get(Calendar.DAY_OF_YEAR) == 366) {
				// 是闰年
				if (month == 2) {
					dayOfMonth = 29;
				} else if (month == 4 || month == 6 || month == 9 || month == 11) {
					dayOfMonth = 30;
				} else {
					dayOfMonth = 31;
				}
			} else {
				// 不是闰年
				if (month == 2) {
					dayOfMonth = 28;
				} else if (month == 4 || month == 6 || month == 9 || month == 11) {
					dayOfMonth = 30;
				} else {
					dayOfMonth = 31;
				}
			}

		} else {
			dayOfMonth = dayOfMonth - 1;
		}
		LOG.info("前一天日期是:" + year + "年" + month + "月" + dayOfMonth + "日");

		if ("00000000".equals(fomat)) {
			return (year + "" + month + "" + dayOfMonth + "");
		} else if ("0000-00-00".equals(fomat)) {
			return (year + "-" + month + "-" + dayOfMonth);
		} else {
			throw new Exception("格式化异常");
		}
	}

	// @RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/swiftorder/test")
	public boolean isDownloanYesterdayOrders(String createTime) throws Exception {
		int counts;
		try {
			counts = swiftpassOrderBusiness.findCountByCreateTime(createTime);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw e;
		}
		if (counts == 0) {
			return false;
		} else {
			return true;
		}
	}
}
