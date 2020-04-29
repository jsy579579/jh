package cn.jh.clearing.service;

import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.jh.clearing.business.ProfitRecordBusiness;
import cn.jh.clearing.pojo.ProfitRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.ChannelCostRateBusiness;
import cn.jh.clearing.business.PaymentOrderBusiness;
import cn.jh.clearing.pojo.BrandProfit;
import cn.jh.clearing.pojo.ChannelCostRate;
import cn.jh.clearing.util.Util;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;




@Controller
@EnableAutoConfiguration
public class TransactionService {

	private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);

	@Autowired
	private PaymentOrderBusiness  paymentOrderBusiness;

	@Autowired
	private ChannelCostRateBusiness  channelCostRateBusiness;

	@Autowired
	private ProfitRecordBusiness profitRecordBusiness;

	@Autowired
	Util util;

	@Value("${schedule-task.on-off}")
	private String scheduleTaskOnOff;

	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/transaction/querybytime")
	public @ResponseBody Object queryTransactionByTime(HttpServletRequest request,
													   @RequestParam(value = "channelTag", required = false) String channelTag,
													   //@RequestParam(value = "type", required = false, defaultValue = "0") String type,
													   @RequestParam(value = "brandId", required = false) String brandId,
													   @RequestParam(value = "startTime", required = false) String startTime,
													   @RequestParam(value = "endTime", required = false) String endTime
	){

		List<ChannelCostRate> allChannelCostRate = channelCostRateBusiness.getAllChannelCostRate();

		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for(ChannelCostRate cc : allChannelCostRate) {
			
			/*if(cc.getIsBankRate() == 0) {
				0continue;
			}*/

			if("10".equals(cc.getChannelType())) {
				continue;
			}

			Map map = paymentOrderBusiness.getTransactionByChannelTagAndTypeAndBrandIdAndDate(cc.getChannelTag(), cc.getBrandMinRate(), cc.getBrandExtraFee(),cc.getCostRate(), cc.getCostExtraFee(), cc.getChannelType(), cc.getIsBankRate(), brandId, startTime, endTime);

			LOG.info("map======" + map + "&&&& channelTag======" + cc.getChannelTag());

			Long count = (Long) map.get("count");
			BigDecimal sumAmount = (BigDecimal) map.get("sumAmount");
			BigDecimal sumRealAmount = (BigDecimal) map.get("sumRealAmount");
			BigDecimal profit = (BigDecimal) map.get("profit");


			jsonObject.put("channelRealName", cc.getChannelRealName());
			jsonObject.put("channelName", cc.getChannelName());
			jsonObject.put("channelTag", cc.getChannelTag());
			jsonObject.put("count", map.get("count"));
			jsonObject.put("sumAmount", map.get("sumAmount"));
			jsonObject.put("sumRealAmount", map.get("sumRealAmount"));
			jsonObject.put("brandProfit", map.get("brandProfit"));
			jsonObject.put("costProfit", map.get("costProfit"));

			jsonArray.add(jsonObject);

		}

		return jsonArray;

	}


	@Scheduled(cron = "0 00 03 * * ?")
	public void scheduleQueryGHTQuickTransferOrder() {
		if ("true".equals(scheduleTaskOnOff)) {
			AutoCreateTransaction();
		}
	}

	//@Scheduled(cron = "0 0/2 * * * ?")
	public void AutoCreateTransaction() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar calendar = Calendar.getInstance();

		calendar.add(Calendar.DATE, -1);

		String format = sdf.format(calendar.getTime());

		createTransaction(format, format);

	}


	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/transaction/createbrandprofit")
	public @ResponseBody Object createTransaction(@RequestParam(value = "startTime", required = false) String startTime,
												  @RequestParam(value = "endTime", required = false) String endTime
	){

		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/all";
		RestTemplate restTemplate = new RestTemplate();
		String result;
		JSONObject jsonObject;
		JSONArray jsonArray;
		try {
			result = restTemplate.getForObject(url, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			jsonArray = jsonObject.getJSONArray("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/brand/query/id 查询贴牌信息异常===========", e);

			return ResultWrap.init(CommonConstants.FALIED, "查询贴牌信息异常");
		}

		LOG.info("开始执行保存分润信息======");
		for(int i = 0; i<jsonArray.size(); i++) {
			jsonObject = (JSONObject) jsonArray.get(i);

			String brandId = jsonObject.getString("id");

			List<ChannelCostRate> allChannelCostRate = channelCostRateBusiness.getAllChannelCostRate();

			for(ChannelCostRate ccr : allChannelCostRate) {

				Map<String, Object> sumProfit = paymentOrderBusiness.getSumProfitByStartTimeAndEndTimeAndBrandIdAndChannelTag(startTime, endTime, Long.parseLong(brandId), ccr.getChannelTag());

				LOG.info("sumProfit======" + sumProfit);

				BrandProfit brandProfit = new BrandProfit();

				brandProfit.setBrandId(Long.parseLong(brandId));
				brandProfit.setChannelName(ccr.getChannelName());
				brandProfit.setChannelRealName(ccr.getChannelRealName());
				brandProfit.setChannelTag(ccr.getChannelTag());
				brandProfit.setChannelType(ccr.getChannelType());

				try {
					if(sumProfit.get("sum(phone_bill)") != null) {
						brandProfit.setBrandProfit(new BigDecimal(sumProfit.get("sum(phone_bill)").toString()).setScale(2, BigDecimal.ROUND_DOWN));
					}else {
						brandProfit.setBrandProfit(BigDecimal.ZERO);
					}
					if(sumProfit.get("sum(car_no)") != null) {
						brandProfit.setCostProfit(new BigDecimal(sumProfit.get("sum(car_no)").toString()).setScale(2, BigDecimal.ROUND_DOWN));
					}else {
						brandProfit.setCostProfit(BigDecimal.ZERO);
					}

					if("0".equals(ccr.getChannelType())) {
						if(sumProfit.get("sum(amount)") != null) {
							brandProfit.setSumAmount(new BigDecimal(sumProfit.get("sum(amount)").toString()).setScale(2, BigDecimal.ROUND_DOWN));
						}else {
							brandProfit.setSumAmount(BigDecimal.ZERO);
						}
						if(sumProfit.get("sum(real_amount)") != null) {
							brandProfit.setSumRealAmount(new BigDecimal(sumProfit.get("sum(real_amount)").toString()).setScale(2, BigDecimal.ROUND_DOWN));
						}else {
							brandProfit.setSumRealAmount(BigDecimal.ZERO);
						}
					}

					if("10".equals(ccr.getChannelType())) {
						if(sumProfit.get("sum(amount)") != null) {
							brandProfit.setSumRealAmount(new BigDecimal(sumProfit.get("sum(amount)").toString()).setScale(2, BigDecimal.ROUND_DOWN));
						}else {
							brandProfit.setSumRealAmount(BigDecimal.ZERO);
						}
						if(sumProfit.get("sum(real_amount)") != null) {
							brandProfit.setSumAmount(new BigDecimal(sumProfit.get("sum(real_amount)").toString()).setScale(2, BigDecimal.ROUND_DOWN));
						}else {
							brandProfit.setSumAmount(BigDecimal.ZERO);
						}
					}

				} catch (Exception e) {
					LOG.error("保存分润信息有误======",e);

					brandProfit.setBrandProfit(BigDecimal.ZERO);
					brandProfit.setCostProfit(BigDecimal.ZERO);
					brandProfit.setSumAmount(BigDecimal.ZERO);
					brandProfit.setSumRealAmount(BigDecimal.ZERO);

				}
				//笔数分润/v1.0/transactionclear/transaction/queryprofitbybrandid
				BigDecimal extraFeeProfit=ccr.getBrandExtraFee().subtract(ccr.getCostExtraFee());
				if(extraFeeProfit.compareTo((BigDecimal.ZERO))>0){
					BigDecimal countProfit=new BigDecimal(sumProfit.get("count(*)").toString()).multiply(extraFeeProfit);
					brandProfit.setCountProfit(countProfit);
				}
				brandProfit.setNumber(Long.parseLong(sumProfit.get("count(*)").toString()));
				brandProfit.setTradeTime(startTime);

				paymentOrderBusiness.createBrandProfit(brandProfit);

			}

	}

		LOG.info("执行保存分润信息完成======");

		return ResultWrap.init(CommonConstants.SUCCESS, "成功!");
	}



	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/transaction/queryprofitbybrandid")
	public @ResponseBody Object queryTransactionByBrandId(HttpServletRequest request,
														  @RequestParam(value = "brandId", required = false, defaultValue = "-1") long brandId,
														  @RequestParam(value = "page", defaultValue = "0", required = false) int page,
														  @RequestParam(value = "size", defaultValue = "20", required = false) int size,
														  @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
														  @RequestParam(value = "sort", defaultValue = "tradeTime", required = false) String sortProperty
	){

		Pageable pageAble = new PageRequest(page, size, new Sort(direction, sortProperty));

		Map<String, Object> brandProfitByBrandId = paymentOrderBusiness.getBrandProfitByBrandId(brandId, pageAble);

		LOG.info("brandProfitByBrandId======" + brandProfitByBrandId);

		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", brandProfitByBrandId);

	}

	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/transaction/queryprofitbybrandid/andtradetime")
	public @ResponseBody Object queryTransactionByBrandIdAndTradeTime(HttpServletRequest request,
																	  @RequestParam(value = "brandId", required = false, defaultValue = "-1") long brandId,
																	  @RequestParam(value = "tradeTime") String tradeTime,
																	  @RequestParam(value = "page", defaultValue = "0", required = false) int page,
																	  @RequestParam(value = "size", defaultValue = "20", required = false) int size,
																	  @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
																	  @RequestParam(value = "sort", defaultValue = "tradeTime", required = false) String sortProperty
	){

		Pageable pageAble = new PageRequest(page, size, new Sort(direction, sortProperty));

		Page<BrandProfit> brandProfit = null;
		if(brandId == -1) {

			Map<String, Object> brandProfitByTradeTime = paymentOrderBusiness.getBrandProfitByTradeTime(tradeTime, pageAble);

			LOG.info("brandProfitByTradeTime======" + brandProfitByTradeTime);

			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", brandProfitByTradeTime);
		}else {

			brandProfit = paymentOrderBusiness.getBrandProfitByBrandIdAndTradeTime(brandId, tradeTime, pageAble);

			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", brandProfit);
		}

	}

	/**
	 * 后台根据条件查询贴牌分润总和
	 * @param brandId
	 * @param createTime
	 * @param endTime
	 * @param type
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/transaction/queryProfitByBrandIdAndTradeTime")
	@ResponseBody
	public Object queryProfitByBrandIdAndTradeTimeAndType(
			@RequestParam("brand_id") String brandId,
			@RequestParam(value = "create_time" ,required = false)String createTime,
			@RequestParam(value = "end_time",required = false) String endTime,
			@RequestParam(value="type",required=false)String type,
			@RequestParam(value="page",defaultValue = "0",required = false) int page,
			@RequestParam(value="size",defaultValue = "20",required = false)int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
	) throws ParseException {
		Map map=new HashMap<String,Object>();
//		createTime=createTime.trim();
//		endTime=endTime.trim();
//		type=type.trim();
		if("".equals(createTime)){
			createTime=null;
		}
		if("".equals(endTime)){
			endTime=null;
		}
		if("".equals(type)){
			type=null;
		}
		Pageable pageAble = new PageRequest(page, size, new Sort(direction, sortProperty));
		if(createTime==null&&endTime==null&&type==null){
			Page<ProfitRecord> result=profitRecordBusiness.findBrandProfitByBrandId(brandId,pageAble);
			Object obj = profitRecordBusiness.queryProfitAllByBrandId(brandId);
			if(obj==null){
				obj=0;
			}
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put("result",result);
			map.put("brandProfitSum",obj);
			return map;
		}else if(type==null){
			endTime=endTime+" 23:59:59";
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat format1=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date startDate=format.parse(createTime);
			Date endDate=format1.parse(endTime);
			if(startDate.compareTo(endDate)>0){
				map.put(CommonConstants.RESP_MESSAGE,"开始日期大于结束日期，请重新选择起始时间");
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				return map;
			}
			Page<ProfitRecord> result=profitRecordBusiness.findProfitByBrandIdAndTime(brandId,startDate,endDate,pageAble);
			Object obj = profitRecordBusiness.queryProfitAllByBrandIdAndTime(brandId,startDate,endDate);
			if(obj==null){
				obj=0;
			}
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put("result",result);
			map.put("brandProfitSum",obj);
			return map;
		}else if(createTime==null&&endTime==null){
			Page<ProfitRecord> result=profitRecordBusiness.findBrandProfitByBrandIdAndType(brandId,type,pageAble);
			Object obj = profitRecordBusiness.queryProfitAllByBrandIdAndType(brandId,type);
			if(obj==null){
				obj=0;
			}
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put("result",result);
			map.put("brandProfitSum",obj);
			return map;
		}else{
			endTime=endTime+" 23:59:59";
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat format1=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date startDate=format.parse(createTime);
			Date endDate=format1.parse(endTime);
			if(startDate.compareTo(endDate)>0){
				map.put(CommonConstants.RESP_MESSAGE,"开始日期大于结束日期，请重新选择起始时间");
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				return map;
			}
			Page<ProfitRecord> result=profitRecordBusiness.findBrandProfitByBrandIdAndTypeAndTime(brandId,type,startDate,endDate,pageAble);
			Object obj = profitRecordBusiness.queryProfitAllByBrandIdAndTypeAndTime(brandId,type,startDate,endDate);
			if(obj==null){
				obj=0;
			}
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put("result",result);
			map.put("brandProfitSum",obj);
			return map;
		}
	}

	/**
	 * 查询分润类别
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/transaction/queryRebateType")
	@ResponseBody
	public Object queryRebateType(){
		Map map=new HashMap<>();
		List list=new ArrayList<>();
		list=profitRecordBusiness.queryRebateType();
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put("result",list);
		return map;
	}
}
