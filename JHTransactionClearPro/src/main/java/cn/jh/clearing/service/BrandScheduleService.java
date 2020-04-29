package cn.jh.clearing.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.PaymentOrderBusiness;
import cn.jh.clearing.business.ProfitRecordBusiness;
import cn.jh.clearing.pojo.PaymentOrder;
import cn.jh.clearing.pojo.ProfitRecord;
import cn.jh.clearing.util.Util;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.RandomUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@EnableScheduling
@Configuration
@Controller
public class BrandScheduleService {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private PaymentOrderBusiness paymentOrderBusiness;

	@Autowired
	Util util;

	@Autowired
	private ProfitRecordBusiness profitRecordBusiness;

	@Value("${schedule-task.on-off}")
	private String scheduleTaskOnOff;
	
	@Autowired
	RestTemplate restTemplate;

	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/weekWithdrawClearing/test")
	public @ResponseBody Object weekWithdrawClearingTest(String endDate ,String startDate,String http) throws ParseException {
		//获取品牌列表
		RestTemplate restTemplate = new RestTemplate();
		String httpurl=http+"/v1.0/user/brand/query/all";
		String result1 = restTemplate.getForObject(httpurl, String.class);
		JSONObject jsonObject1 = JSONObject.fromObject(result1);
		JSONArray jsonArray = jsonObject1.getJSONArray(CommonConstants.RESULT);
		for (int i = 0; i < jsonArray.size(); i++) {
			long brandId=Long.parseLong(jsonArray.optJSONObject(i).optString("id"));
			brandCleing(endDate, startDate, brandId, http);
		}
		return "OK";
	}

	/**
	 * 周结算提现费 周一 2:10
	 */
	@Scheduled(cron = "0 10 2 ? * MON")
	public void weekWithdrawClearing() {
		/*if ("true".equals(scheduleTaskOnOff)) {

			LOG.info("周提现分润结算");

			Date todayDate = new Date();

			SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");

			Calendar date = Calendar.getInstance();
			date.setTime(todayDate);
			date.set(Calendar.DATE, date.get(Calendar.DATE) - 7);

			Date endDate = null;
			try {
				endDate = dft.parse(dft.format(date.getTime()));
			} catch (ParseException e) {
				e.printStackTrace();LOG.error("",e);
			}
			//提现返利
			List<PaymentOrder> brandWithdrawRebates = paymentOrderBusiness.queryWeekBrandWithdrawRebate(endDate,todayDate);
			
			if(brandWithdrawRebates!=null)
			for (PaymentOrder withdrawRebate : brandWithdrawRebates) {
				*//*** 获取平台的管理员账号 *//*
				long brandid = withdrawRebate.getBrandid();
				String ordercode = withdrawRebate.getOrdercode();
				RestTemplate restTemplate = new RestTemplate();
				URI uri = util.getServiceUrl("user", "error url request!");
				String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandid;
				String result = restTemplate.getForObject(url, String.class);
				*//** 根据的渠道标识或去渠道的相关信息 *//*
				LOG.info("RESULT================" + result);
				JSONObject jsonObject = JSONObject.fromObject(result);
				if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
					LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============brand_id==="+brandid);
					continue;
				}
				JSONObject resultObj = 	jsonObject.getJSONObject("result");
				String manageid = resultObj.getString("manageid");

				*//** 根据manageid获取用户 *//*
				uri = util.getServiceUrl("user", "error url request!");
				url = uri.toString() + "/v1.0/user/query/id";
				*//** 根据的渠道标识或去渠道的相关信息 *//*
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("id", manageid);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
					LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============manageid==="+manageid);
					continue;
				}
				resultObj = jsonObject.getJSONObject("result");
				String phone = resultObj.getString("phone");

				*//** 根据订单号获取订单 *//*
				PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);

				ProfitRecord profitRecord = profitRecordBusiness.queryProfitRecordByordercode(ordercode, "1");
				if (profitRecord != null) {
					continue;
				} else {
					*//** 存贮分润记录明细 *//*
					profitRecord = new ProfitRecord();
					profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
					profitRecord.setAcqAmount(new BigDecimal("1"));
					profitRecord.setAcqphone(phone);
					profitRecord.setBrandId(withdrawRebate.getBrandid()+"");
					profitRecord.setAcquserid(Long.parseLong(manageid));
					profitRecord.setAmount(paymentOrder.getAmount());
					profitRecord.setCreateTime(new Date());
					profitRecord.setOrdercode(ordercode);
					profitRecord.setOriphone(paymentOrder.getPhone());
					profitRecord.setOrirate(paymentOrder.getRate());
					profitRecord.setOriuserid(paymentOrder.getUserid());
					profitRecord.setRemark("品牌提现分润");
					profitRecord.setType("1");
					profitRecord.setScale(BigDecimal.ONE);
					profitRecordBusiness.merge(profitRecord);

					*//** 存储 用户的分润记录 *//*
					restTemplate = new RestTemplate();
					uri = util.getServiceUrl("user", "error url request!");
					url = uri.toString() + "/v1.0/user/rebate/update";
					*//** 根据的渠道标识或去渠道的相关信息 *//*
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("rebate_amount", "1");
					requestEntity.add("user_id", manageid);
					requestEntity.add("order_code", ordercode);
					result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("订单====" + withdrawRebate.getOrdercode() + ",提现返利成功!");
				}
			}
			//自清充值返利
			List<PaymentOrder> brandWithdrawClearRebates = paymentOrderBusiness.queryWeekBrandWithdrawClearRebate(endDate,todayDate);
			if(brandWithdrawClearRebates!=null) {
				LOG.info("RESULT===============日期="+endDate+"=======清算数量=" + brandWithdrawClearRebates.size());
				for (PaymentOrder withdrawRebate : brandWithdrawClearRebates) {
					
					ProfitRecord profitRecord=profitRecordBusiness.queryProfitRecordByordercode(withdrawRebate.getOrdercode(), 1+"");
					if(profitRecord!=null)
						continue;
					try {
						*//*** 获取平台的管理员账号 *//*
						long brandid = withdrawRebate.getBrandid();
						String ordercode = withdrawRebate.getOrdercode();
						RestTemplate restTemplate = new RestTemplate();
						URI uri = util.getServiceUrl("user", "error url request!");
						String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandid;
						String result = restTemplate.getForObject(url, String.class);
						*//** 根据的渠道标识或去渠道的相关信息 *//*
						LOG.info("RESULT================" + result);
						JSONObject jsonObject = JSONObject.fromObject(result);
						if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
							LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============brand_id==="+brandid);
							continue;
						}
						JSONObject resultObj = jsonObject.getJSONObject("result");
						String manageid = resultObj.getString("manageid");
						*//** 根据manageid获取用户 *//*
						uri = util.getServiceUrl("user", "error url request!");
						url = uri.toString() + "/v1.0/user/query/id";
						*//** 根据的渠道标识或去渠道的相关信息 *//*
						MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
						requestEntity = new LinkedMultiValueMap<String, String>();
						requestEntity.add("id", manageid);
						result = restTemplate.postForObject(url, requestEntity, String.class);
						LOG.info("RESULT================" + result);
						jsonObject = JSONObject.fromObject(result);
						if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
							LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============manageid==="+manageid);
							continue;
						}
						resultObj = jsonObject.getJSONObject("result");
						String phone = resultObj.getString("phone");
						*//** 根据订单号获取订单 *//*
						PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);
						profitRecord = profitRecordBusiness.queryProfitRecordByordercode(ordercode, "1");
						if (profitRecord != null) {
							continue;
						} else {
							BigDecimal AcqAmount=new BigDecimal("0.00").setScale(2, BigDecimal.ROUND_DOWN) ;
							try {
								AcqAmount=paymentOrder.getExtraFee().subtract(paymentOrder.getCostfee()).setScale(2, BigDecimal.ROUND_DOWN);
							} catch (Exception e) {
								LOG.info("RESULT=============getExtraFee===" + paymentOrder.getExtraFee()+"====================getCostfee==="+paymentOrder.getCostfee());
								continue;
							}
							if(AcqAmount.compareTo(new BigDecimal("0.00"))<=0) {
								continue;
							}
							*//** 存贮分润记录明细 *//*
							profitRecord = new ProfitRecord();
							profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
							profitRecord.setAcqAmount(AcqAmount);
							profitRecord.setAcqphone(phone);
							profitRecord.setAcquserid(Long.parseLong(manageid));
							profitRecord.setAmount(paymentOrder.getAmount());
							profitRecord.setCreateTime(new Date());
							profitRecord.setOrdercode(ordercode);
							profitRecord.setOriphone(paymentOrder.getPhone());
							profitRecord.setOrirate(paymentOrder.getRate());
							profitRecord.setOriuserid(paymentOrder.getUserid());
							profitRecord.setRemark("品牌自清分润");
							profitRecord.setType("1");
							profitRecord.setScale(BigDecimal.ONE);
							profitRecordBusiness.merge(profitRecord);
							*//** 存储 用户的分润记录 *//* 
							try {
								restTemplate = new RestTemplate();
								uri = util.getServiceUrl("user", "error url request!");
								url = uri.toString() + "/v1.0/user/rebate/update";
								*//** 根据的渠道标识或去渠道的相关信息 *//*
								requestEntity = new LinkedMultiValueMap<String, String>();
								requestEntity.add("rebate_amount", AcqAmount.toString());
								requestEntity.add("user_id", manageid);
								requestEntity.add("order_code", ordercode);
								result = restTemplate.postForObject(url, requestEntity, String.class);
								LOG.info("订单====" + withdrawRebate.getOrdercode() + ",充值返利成功!");
							}catch (Exception e) {
								LOG.info("RESULT===============返利疑似失败=====================order_code==="+ordercode);
								e.printStackTrace();LOG.error("",e);
								continue;
							}
							
						}
					}catch (Exception e) {
						LOG.info("RESULT===============返利疑似失败=====================order_code==="+withdrawRebate.getOrdercode());
						e.printStackTrace();LOG.error("",e);
						continue;
					}
					
				}
			}
		}*/
	}

	
	public  void brandClearing(PaymentOrder paymentOrder){
		
		if(paymentOrder.getType().equals("2")){
			PaymentOrder withdrawRebate=paymentOrder;

			/*** 获取平台的管理员账号 */
			long brandid = withdrawRebate.getBrandid();
			String ordercode = withdrawRebate.getOrdercode();
			String url ="http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
			String result = restTemplate.getForObject(url, String.class);
			/** 根据的渠道标识或去渠道的相关信息 */
			LOG.info("RESULT================" + result);
			JSONObject jsonObject = JSONObject.fromObject(result);
			if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
				LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============brand_id==="+brandid);
				return;
			}
			JSONObject resultObj = 	jsonObject.getJSONObject("result");
			String manageid = resultObj.getString("manageid");

			/** 根据manageid获取用户 */
			url = "http://user/v1.0/user/query/id";
			/** 根据的渠道标识或去渠道的相关信息 */
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("id", manageid);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
				LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============manageid==="+manageid);
				return;
			}
			resultObj = jsonObject.getJSONObject("result");
			String phone = resultObj.getString("phone");

			/** 根据订单号获取订单 */
			paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);

			ProfitRecord profitRecord = profitRecordBusiness.queryProfitRecordByordercode(ordercode, "1");
			if (profitRecord != null) {
				return;
			} else {
				/** 存贮分润记录明细 */
				profitRecord = new ProfitRecord();
				profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
				profitRecord.setAcqAmount(new BigDecimal("1"));
				profitRecord.setAcqphone(phone);
				profitRecord.setBrandId(withdrawRebate.getBrandid()+"");
				profitRecord.setAcquserid(Long.parseLong(manageid));
				profitRecord.setAmount(paymentOrder.getAmount());
				profitRecord.setCreateTime(new Date());
				profitRecord.setOrdercode(ordercode);
				profitRecord.setOriphone(paymentOrder.getPhone());
				profitRecord.setOrirate(paymentOrder.getRate());
				profitRecord.setOriuserid(paymentOrder.getUserid());
				profitRecord.setRemark("品牌提现分润");
				profitRecord.setType("1");
				profitRecord.setScale(BigDecimal.ONE);
				profitRecordBusiness.merge(profitRecord);
				/** 存储 用户的分润记录 */
				url ="http://user/v1.0/user/rebate/update";
				/** 根据的渠道标识或去渠道的相关信息 */
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("rebate_amount", "1");
				requestEntity.add("user_id", manageid);
				requestEntity.add("order_code", ordercode);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("订单====" + withdrawRebate.getOrdercode() + ",提现返利成功!");
			}
			
		}else {
			PaymentOrder withdrawRebate=paymentOrder;
			ProfitRecord profitRecord=profitRecordBusiness.queryProfitRecordByordercode(withdrawRebate.getOrdercode(), 1+"");
			if(profitRecord!=null)
				return;
			try {
				/*** 获取平台的管理员账号 */
				long brandid = withdrawRebate.getBrandid();
				String ordercode = withdrawRebate.getOrdercode();
				String url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
				String result = restTemplate.getForObject(url, String.class);
				/** 根据的渠道标识或去渠道的相关信息 */
				LOG.info("RESULT================" + result);
				JSONObject jsonObject = JSONObject.fromObject(result);
				if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
					LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============brand_id==="+brandid);
					return;
				}
				JSONObject resultObj = jsonObject.getJSONObject("result");
				String manageid = resultObj.getString("manageid");
				/** 根据manageid获取用户 */
				url = "http://user/v1.0/user/query/id";
				/** 根据的渠道标识或去渠道的相关信息 */
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("id", manageid);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
					LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============manageid==="+manageid);
					return;
				}
				resultObj = jsonObject.getJSONObject("result");
				String phone = resultObj.getString("phone");
				/** 根据订单号获取订单 */
				 paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);
				profitRecord = profitRecordBusiness.queryProfitRecordByordercode(ordercode, "1");
				if (profitRecord != null) {
					return;
				} else {
					BigDecimal AcqAmount=new BigDecimal("0.00").setScale(2, BigDecimal.ROUND_DOWN) ;
					try {
						AcqAmount=paymentOrder.getExtraFee().subtract(paymentOrder.getCostfee()).setScale(2, BigDecimal.ROUND_DOWN);
					} catch (Exception e) {
						LOG.info("RESULT=============getExtraFee===" + paymentOrder.getExtraFee()+"====================getCostfee==="+paymentOrder.getCostfee());
						return;
					}
					if(AcqAmount.compareTo(new BigDecimal("0.00"))<=0) {
						return;
					}
					/** 存贮分润记录明细 */
					profitRecord = new ProfitRecord();
					profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
					profitRecord.setAcqAmount(AcqAmount);
					profitRecord.setAcqphone(phone);
					profitRecord.setAcquserid(Long.parseLong(manageid));
					profitRecord.setAmount(paymentOrder.getAmount());
					profitRecord.setCreateTime(new Date());
					profitRecord.setOrdercode(ordercode);
					profitRecord.setOriphone(paymentOrder.getPhone());
					profitRecord.setOrirate(paymentOrder.getRate());
					profitRecord.setOriuserid(paymentOrder.getUserid());
					profitRecord.setRemark("品牌自清分润");
					profitRecord.setType("1");
					profitRecord.setScale(BigDecimal.ONE);
					profitRecordBusiness.merge(profitRecord);
					/** 存储 用户的分润记录 */ 
					try {
						url = "http://user/v1.0/user/rebate/update";
						/** 根据的渠道标识或去渠道的相关信息 */
						requestEntity = new LinkedMultiValueMap<String, String>();
						requestEntity.add("rebate_amount", AcqAmount.toString());
						requestEntity.add("user_id", manageid);
						requestEntity.add("order_code", ordercode);
						result = restTemplate.postForObject(url, requestEntity, String.class);
						LOG.info("订单====" + withdrawRebate.getOrdercode() + ",充值返利成功!");
					}catch (Exception e) {
						LOG.info("RESULT===============返利疑似失败=====================order_code==="+ordercode);
						e.printStackTrace();LOG.error("",e);
						return;
					}
					
				}
			}catch (Exception e) {
				LOG.info("RESULT===============返利疑似失败=====================order_code==="+withdrawRebate.getOrdercode());
				e.printStackTrace();LOG.error("",e);
				return;
			}
			
		}
		
	}
	
	//平台清算
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/payment/query")
	public  Object  brandCleing(String endDate,String startDate,long brandId,String http) throws ParseException{
		if(http==null||http.trim().length()==0) {
			http="http://user";
		}
		SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
		Date endIime=dft.parse(endDate);
		Date startTime=dft.parse(startDate);
		//RestTemplate restTemplate = new RestTemplate();
		
		//提现返利
		List<PaymentOrder> brandWithdrawRebates = paymentOrderBusiness.queryWeekBrandWithdrawRebate(startTime,endIime,brandId);
		if(brandWithdrawRebates!=null)
		for (PaymentOrder withdrawRebate : brandWithdrawRebates) {
			/*** 获取平台的管理员账号 */
			long brandid = withdrawRebate.getBrandid();
			String ordercode = withdrawRebate.getOrdercode();
			String url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
			String result = restTemplate.getForObject(url, String.class);
			/** 根据的渠道标识或去渠道的相关信息 */
			LOG.info("RESULT================" + result);
			JSONObject jsonObject = JSONObject.fromObject(result);
			if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
				LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============brand_id==="+brandid);
				continue;
			}
			JSONObject resultObj = 	jsonObject.getJSONObject("result");
			String manageid = resultObj.getString("manageid");

			/** 根据manageid获取用户 */
			url = "http://user/v1.0/user/query/id";
			/** 根据的渠道标识或去渠道的相关信息 */
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("id", manageid);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
				LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============manageid==="+manageid);
				continue;
			}
			resultObj = jsonObject.getJSONObject("result");
			String phone = resultObj.getString("phone");

			/** 根据订单号获取订单 */
			PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);

			ProfitRecord profitRecord = profitRecordBusiness.queryProfitRecordByordercode(ordercode, "1");
			if (profitRecord != null) {
				continue;
			} else {
				/** 存贮分润记录明细 */
				profitRecord = new ProfitRecord();
				profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
				profitRecord.setAcqAmount(new BigDecimal("1"));
				profitRecord.setAcqphone(phone);
				profitRecord.setBrandId(withdrawRebate.getBrandid()+"");
				profitRecord.setAcquserid(Long.parseLong(manageid));
				profitRecord.setAmount(paymentOrder.getAmount());
				profitRecord.setCreateTime(new Date());
				profitRecord.setOrdercode(ordercode);
				profitRecord.setOriphone(paymentOrder.getPhone());
				profitRecord.setOrirate(paymentOrder.getRate());
				profitRecord.setOriuserid(paymentOrder.getUserid());
				profitRecord.setRemark("品牌提现分润");
				profitRecord.setType("1");
				profitRecord.setScale(BigDecimal.ONE);
				profitRecordBusiness.merge(profitRecord);

				/** 存储 用户的分润记录 */
				//url = http + "/v1.0/user/rebate/update";
				url="http://user/v1.0/user/rebate/update";
				/** 根据的渠道标识或去渠道的相关信息 */
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("rebate_amount", "1");
				requestEntity.add("user_id", manageid);
				requestEntity.add("order_code", ordercode);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("订单====" + withdrawRebate.getOrdercode() + ",提现返利成功!");
			}
		}
		//自清充值返利
		List<PaymentOrder> brandWithdrawClearRebates = paymentOrderBusiness.queryWeekBrandWithdrawClearRebate(startTime,endIime,brandId);
		if(brandWithdrawClearRebates!=null) {
			LOG.info("RESULT===============日期="+endDate+"=======清算数量=" + brandWithdrawClearRebates.size());
			for (PaymentOrder withdrawRebate : brandWithdrawClearRebates) {
				
				ProfitRecord profitRecord=profitRecordBusiness.queryProfitRecordByordercode(withdrawRebate.getOrdercode(), 1+"");
				if(profitRecord!=null)
					continue;
				try {
					/*** 获取平台的管理员账号 */
					long brandid = withdrawRebate.getBrandid();
					String ordercode = withdrawRebate.getOrdercode();
					String url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
					String result = restTemplate.getForObject(url, String.class);
					/** 根据的渠道标识或去渠道的相关信息 */
					LOG.info("RESULT================" + result);
					JSONObject jsonObject = JSONObject.fromObject(result);
					if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
						LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============brand_id==="+brandid);
						continue;
					}
					JSONObject resultObj = jsonObject.getJSONObject("result");
					String manageid = resultObj.getString("manageid");
					/** 根据manageid获取用户 */
					//url = http + "/v1.0/user/query/id";
					url = "http://user/v1.0/user/query/id";
					/** 根据的渠道标识或去渠道的相关信息 */
					MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("id", manageid);
					result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("RESULT================" + result);
					jsonObject = JSONObject.fromObject(result);
					if(!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
						LOG.info("失败返利订单================" + withdrawRebate.getOrdercode()+"============manageid==="+manageid);
						continue;
					}
					resultObj = jsonObject.getJSONObject("result");
					String phone = resultObj.getString("phone");
					/** 根据订单号获取订单 */
					PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);
					profitRecord = profitRecordBusiness.queryProfitRecordByordercode(ordercode, "1");
					if (profitRecord != null) {
						continue;
					} else {
						BigDecimal AcqAmount=new BigDecimal("0.00").setScale(2, BigDecimal.ROUND_DOWN) ;
						try {
							AcqAmount=paymentOrder.getExtraFee().subtract(paymentOrder.getCostfee()).setScale(2, BigDecimal.ROUND_DOWN);
						} catch (Exception e) {
							LOG.info("RESULT=============getExtraFee===" + paymentOrder.getExtraFee()+"====================getCostfee==="+paymentOrder.getCostfee());
							continue;
						}
						if(AcqAmount.compareTo(new BigDecimal("0.00"))<=0) {
							continue;
						}
						/** 存贮分润记录明细 */
						profitRecord = new ProfitRecord();
						profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
						profitRecord.setAcqAmount(AcqAmount);
						profitRecord.setAcqphone(phone);
						profitRecord.setAcquserid(Long.parseLong(manageid));
						profitRecord.setAmount(paymentOrder.getAmount());
						profitRecord.setCreateTime(new Date());
						profitRecord.setOrdercode(ordercode);
						profitRecord.setOriphone(paymentOrder.getPhone());
						profitRecord.setOrirate(paymentOrder.getRate());
						profitRecord.setOriuserid(paymentOrder.getUserid());
						profitRecord.setRemark("品牌自清分润");
						profitRecord.setType("1");
						profitRecord.setScale(BigDecimal.ONE);
						profitRecordBusiness.merge(profitRecord);
						/** 存储 用户的分润记录 */
						try {
							url = "http://user/v1.0/user/rebate/update";
							/** 根据的渠道标识或去渠道的相关信息 */
							requestEntity = new LinkedMultiValueMap<String, String>();
							requestEntity.add("rebate_amount", AcqAmount.toString());
							requestEntity.add("user_id", manageid);
							requestEntity.add("order_code", ordercode);
							result = restTemplate.postForObject(url, requestEntity, String.class);
							LOG.info("订单====" + withdrawRebate.getOrdercode() + ",充值返利成功!");
						}catch (Exception e) {
							LOG.info("RESULT===============返利疑似失败=====================order_code==="+ordercode);
							e.printStackTrace();LOG.error(ExceptionUtil.errInfo(e));
							continue;
						}
						
					}
				}catch (Exception e) {
					LOG.info("RESULT===============返利疑似失败=====================order_code==="+withdrawRebate.getOrdercode());
					e.printStackTrace();LOG.error(ExceptionUtil.errInfo(e));
					continue;
				}
				
			}
		}
		return "";
	}
	
	
}
