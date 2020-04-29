package com.jh.notice.service;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.jh.notice.business.BrandSMSCountBusiness;
import com.jh.notice.pojo.BrandSMSCount;
import com.jh.notice.pojo.BrandSMSCountHistory;
import com.jh.notice.repository.BrandSMSCountHistoryRepository;
import com.jh.notice.util.Util;
import cn.jh.common.utils.CommonConstants;




@Controller
@EnableAutoConfiguration
public class BrandSmsSendService {

	
	private static final Logger LOG = LoggerFactory.getLogger(BrandSmsSendService.class);
	
	private static final BigDecimal perSmsInFormPrice = BigDecimal.valueOf(0.045).setScale(4);
	
	@Autowired
	private BrandSMSCountBusiness brandSMSCountBusiness;
	
	@Autowired
	private BrandSMSCountHistoryRepository brandSMSCountHistoryRepository;
	
	@Autowired
	private Util util;
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/notice/sms/update/count")
	public @ResponseBody Object updateCardAuthCount(
			//type:smsInform 短信充值
			@RequestParam(value="type")String type,
			@RequestParam(value="amount")String amount,
			@RequestParam(value="brandId")String brandId
			){
		Map<String,Object> map = new HashMap<>();
		BrandSMSCount brandSMSCount = brandSMSCountBusiness.findByBrandId(brandId);
		BrandSMSCountHistory brandSMSCountHistory=new BrandSMSCountHistory();
		if(brandSMSCount == null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无该贴牌帐户,无法充值!");
			return map;			
		}
		if("smsInform".equals(type)){
			int counts = new BigDecimal(amount).divide(perSmsInFormPrice,0,BigDecimal.ROUND_HALF_UP).intValue();
			brandSMSCount.setSmsCount(brandSMSCount.getSmsCount()+Long.valueOf(counts));
			brandSMSCountHistory.setAccount( new BigDecimal(amount));
			brandSMSCountHistory.setSmsCount(Long.valueOf(counts));
			brandSMSCountHistory.setBrandId(brandId);
			brandSMSCountHistory.setCreateTime(new Date());
		}
		
		brandSMSCountHistoryRepository.save(brandSMSCountHistory);
		brandSMSCountBusiness.save(brandSMSCount);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "充值成功!");
		map.put(CommonConstants.RESULT, brandSMSCount);
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/notice/sms/sel/count")
	public @ResponseBody Object queryCountByBrandId(
			@RequestParam(value="brandId")String brandId
			){
		Map<String,Object> map = new HashMap<>();
		BrandSMSCount brandSMSCount = brandSMSCountBusiness.findByBrandId(brandId);
		if(brandSMSCount == null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无该贴牌帐户");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
		map.put(CommonConstants.RESULT, brandSMSCount);
		return map;
	}
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/notice/sms/selcreat/brandcount")
	public @ResponseBody Object selCreateNewBrandCardAuthCount(
			@RequestParam(value = "brandId")String brandId
			){
		Map<String,Object> map = new HashMap<>();
		BrandSMSCount brandSMSCount = brandSMSCountBusiness.findByBrandId(brandId);
		if(brandSMSCount != null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
			map.put(CommonConstants.RESULT, brandSMSCount);
			return map;
		}else{
			brandSMSCount = new BrandSMSCount();
			brandSMSCount.setBrandId(brandId);
			brandSMSCount.setSmsCount(2000L);
			brandSMSCount.setCreateTime(new Date());;
			brandSMSCount = brandSMSCountBusiness.save(brandSMSCount);
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "创建贴牌帐户成功!");
		map.put(CommonConstants.RESULT, brandSMSCount);
		return map;
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/notice/sms/creat/brandcount")
	public @ResponseBody Object createNewBrandCardAuthCount(
			@RequestParam(value = "brandId")String brandId
			){
		Map<String,Object> map = new HashMap<>();
		BrandSMSCount brandSMSCount = brandSMSCountBusiness.findByBrandId(brandId);
		if(brandSMSCount != null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "已创建过该贴牌帐户,无法再次创建!");
			return map;
		}else{
			brandSMSCount = new BrandSMSCount();
			brandSMSCount.setBrandId(brandId);
			brandSMSCount.setSmsCount(2000L);
			brandSMSCount.setCreateTime(new Date());;
			brandSMSCount = brandSMSCountBusiness.save(brandSMSCount);
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "创建贴牌帐户成功!");
		map.put(CommonConstants.RESULT, brandSMSCount);
		return map;
	}
}
