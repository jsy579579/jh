package com.jh.user.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.ChannelRateBusiness;
import com.jh.user.business.ThirdLeveDistributionBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.BrandRate;
import com.jh.user.pojo.Channel;
import com.jh.user.pojo.ChannelRate;
import com.jh.user.pojo.ThirdLevelDistribution;
import com.jh.user.pojo.ThirdLevelRate;
import com.jh.user.pojo.ThirdLevelRebateRatio;
import com.jh.user.pojo.ThirdLevelRebateRatioNew;
import com.jh.user.pojo.ThirdLevelRebateRatioNew2;
import com.jh.user.pojo.User;
import com.jh.user.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.web.client.RestTemplate;


@Controller
@EnableAutoConfiguration
public class ThirdLevelDistriService {

	private static final Logger LOG = LoggerFactory.getLogger(ChannelService.class);
	
	@Autowired 
	private BrandManageBusiness brandMangeBusiness;

	@Autowired
	private ThirdLeveDistributionBusiness  thirdLevelBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness  userLoginRegisterBusiness;
	
	@Autowired
	private ChannelRateBusiness  channelRateBusiness;

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	Util util;
	
	/**新增一个三级分销产品*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/prod/add")
	public @ResponseBody Object addThirdLevelProd(HttpServletRequest request,   
			@RequestParam(value = "brand_id") long  brandid,
			@RequestParam(value = "grade" ,defaultValue = "-1", required=false )  int   grade,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "money") String smoney,
			@RequestParam(value = "discount") String sdiscount,
			
			//如何升级描述
			@RequestParam(value = "upgrade_state", required=false) String upgradestate,
			
			//是否线上购买
			@RequestParam(value = "true_false_buy",   defaultValue = "0", required=false) String trueFalseBuy,
			
			//享受收益描述TrueFalseBuy
			@RequestParam(value = "earnings_state", required=false) String earningsState,
			@RequestParam(value = "remark", required=false) String remark
						){
		
		Map<String,Object> map = new HashMap<String,Object>();
		long ldiscount;
		BigDecimal discount = null;
		
		try {
			ldiscount = Long.valueOf(sdiscount);
			discount = BigDecimal.valueOf(ldiscount,4);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加失败,参数discount不能为'"+ sdiscount +"',请检查后重试!");
			return map;
		}
		
		long lmoney;
		BigDecimal money = null;
		try {
			lmoney = Long.valueOf(smoney);
			money = BigDecimal.valueOf(lmoney);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加失败,参数money不能为'"+ smoney +"',请检查后重试!");
			return map;
		}
		
		ThirdLevelDistribution  distribution  =new ThirdLevelDistribution();
		if(grade==-1){
			distribution=null;
		}else{
			distribution= thirdLevelBusiness.getThirdLevelByBrandidandgradeNoStatus(brandid, grade);	
		}
		
		if(distribution == null){
			List<ThirdLevelDistribution> tlds=new ArrayList<ThirdLevelDistribution>();
			tlds=thirdLevelBusiness.getAllThirdLevelPrd(brandid);
//			if(tlds.size()>0){
//				grade=tlds.get(tlds.size()-1).getGrade()+1;
//			}else{
//				grade=tlds.size()+1;
//			}
			
			if(tlds.size() > 9) {
				
				return ResultWrap.init(CommonConstants.FALIED, "抱歉,最多可以添加10个会员产品!");
			}
			
			distribution =  new ThirdLevelDistribution();
			distribution.setBrandId(brandid);
			distribution.setCreateTime(new Date());
			distribution.setDiscount(discount);
			distribution.setGrade(tlds.size()+1);
			distribution.setMoney(money);
			distribution.setName(name);
			distribution.setRemark(remark);
			distribution.setTrueFalseBuy(trueFalseBuy);
			distribution.setUpgradestate(upgradestate==null?"":upgradestate);
			distribution.setEarningsState(earningsState==null?"":earningsState);
			distribution.setStatus("0");
		}else{
			distribution.setCreateTime(new Date());
			distribution.setDiscount(discount);
			distribution.setGrade(grade);
			distribution.setMoney(money);
			distribution.setName(name);
			distribution.setRemark(remark);
			distribution.setUpgradestate(upgradestate==null?"":upgradestate);
			distribution.setEarningsState(earningsState==null?"":earningsState);
			distribution.setTrueFalseBuy(trueFalseBuy);
			distribution.setStatus("0");
		}
		
		distribution = thirdLevelBusiness.mergeThirdDistribution(distribution);
		
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, distribution);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
		
	
	}
	
	

	/**新增一个三级分销产品*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/prod/truefalsebuy/update")
	public @ResponseBody Object addThirdLevelProd(HttpServletRequest request, 
			
			//三级产品
			@RequestParam(value = "id") long id,
			
			//是否线上购买
			@RequestParam(value = "true_false_buy",   defaultValue = "0", required=false) String trueFalseBuy
						){
		Map map = new HashMap();
		ThirdLevelDistribution  distribution  =thirdLevelBusiness.queryThirdLevelDistri(id);
		
		if(distribution == null){
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败");
			return map;
		}else{
			distribution.setTrueFalseBuy(trueFalseBuy);
		}
		
		distribution = thirdLevelBusiness.mergeThirdDistribution(distribution);
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, distribution);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
		
	
	}
	
	
	/**新增品牌上工一个三级分销比率*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/ratio/add")
	public @ResponseBody Object addThirdLevelRatio(HttpServletRequest request,   
			@RequestParam(value = "brand_id") String  sbrandid,
			@RequestParam(value = "ratio") BigDecimal ratio,
			@RequestParam(value = "pre_level") String prelevel){
		Map<String,Object> map = new HashMap<String,Object>();
		if("".equals(sbrandid)){
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.更新失败,贴牌号不能为:"+ sbrandid +",请检查后重试!!!");
			return map;
		}
		
		long brandid = 1;
		try {
			brandid = Long.valueOf(sbrandid);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.更新失败,贴牌号不能为:"+ sbrandid +",请检查后重试!!!");
			return map;
		}
		
		List<ThirdLevelRebateRatio>  levelRebateRatios=thirdLevelBusiness.getAllThirdRatio(brandid);
		ThirdLevelRebateRatio levelRebateRatio = thirdLevelBusiness.getThirdRatioByBrandidandprelevel(brandid, prelevel);
		if( ratio.doubleValue()>0.33){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "更新失败,总费率大于1");
				return map;		
			 }
		
		if(levelRebateRatio == null){
			levelRebateRatio =  new ThirdLevelRebateRatio();
			levelRebateRatio.setBrandId(brandid);
			levelRebateRatio.setRatio(ratio);
			levelRebateRatio.setPreLevel(prelevel);
			levelRebateRatio.setCreateTime(new Date());
		}else{
			
			levelRebateRatio.setRatio(ratio);
			levelRebateRatio.setCreateTime(new Date());
		}
		
		levelRebateRatio = thirdLevelBusiness.mergeThirdLevelRebateRatio(levelRebateRatio);
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, levelRebateRatio);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	
	
	/**根据三级分销的id获取相信信息*/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/user/thirdlevel/prod/query/{id}")
	public @ResponseBody Object queryThirdLevelProByid(HttpServletRequest request,   
			@PathVariable("id") long id){
		Map map = new HashMap();
		ThirdLevelDistribution tdb =thirdLevelBusiness.queryThirdLevelDistri(id);
		if(tdb!=null) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
		}else {
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败");
		}
		map.put(CommonConstants.RESULT, tdb);
		
		return map;
	}
	
	
	/**按品牌查询所有三级分销产品*/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/user/thirdlevel/prod/brand/{brand}")
	public @ResponseBody Object queryThirdLevelPro(HttpServletRequest request,
			@RequestParam(value = "access_type",defaultValue="1", required=false) long accesstype,
			@PathVariable("brand") long brandid){
		Map<String,Object> map = new HashMap<String,Object>();
		if("".equals(accesstype+"")||"".equals(brandid+"")){
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询产品失败,设置条件不能为空!!!");
			return map;
		}
		
		
		List<ThirdLevelDistribution> tld=thirdLevelBusiness.getAllThirdLevelPrd(brandid);
		if(accesstype!=1) {
			if (tld == null) {
				tld = new ArrayList<>();
			}
			ThirdLevelDistribution ThirdLevelDistribution=new ThirdLevelDistribution();
			ThirdLevelDistribution.setBrandId(brandid);
			ThirdLevelDistribution.setCreateTime(new Date());
			ThirdLevelDistribution.setId(0);
			ThirdLevelDistribution.setGrade(0);
			Brand  brand=brandMangeBusiness.findBrandById(brandid);
			ThirdLevelDistribution.setName(brand!=null?brand.getLevelName():"");
			ThirdLevelDistribution.setMoney(new BigDecimal("0.0"));
			ThirdLevelDistribution.setDeposit(new BigDecimal("0.0"));
			ThirdLevelDistribution.setDeposit(new BigDecimal("0.0"));
			ThirdLevelDistribution.setStatus("0");
			ThirdLevelDistribution.setTrueFalseBuy("0");
			tld.add(ThirdLevelDistribution);
		}
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, tld);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	/**按品牌查询所有三级分销费率 */
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/user/thirdlevel/prod/rate/{levelid}")
	public @ResponseBody Object queryThirdLevelRates(HttpServletRequest request,   
			@PathVariable("levelid") long levelid){
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, thirdLevelBusiness.findAllThirdLevelRates(levelid));
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		
		return map;
	}
	
	/**按品牌查询所有三级分销费率 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/channel/rate")
	public @ResponseBody Object queryThirdLevelRatesbylevelidAndChannelId(HttpServletRequest request,  
			@RequestParam("thirdLevelId") long thirdLevelId,
			@RequestParam("channelId") long channelId){
		Map<String, Serializable> map = new HashMap<String, Serializable>();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, thirdLevelBusiness.findAllThirdLevelRatesBylevelidAndChannelId(thirdLevelId, channelId));
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		
		return map;
	}
	
	/**按品牌添加三级分销费率 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/prod/rate/add")
	public @ResponseBody Object addThirdLevelRates(HttpServletRequest request,  
			@RequestParam("thirdLevelId") long thirdLevelId,
			@RequestParam("channelId") long channelId,
			@RequestParam("rate") String srate,
			@RequestParam(value="brandId",required=false) String brandId
//			@RequestParam("extraFee") BigDecimal extraFee
	) {
		Map<String, Object> map = new HashMap<String, Object>();
		BigDecimal rate = null;
		try {
			rate = new BigDecimal(Double.valueOf(srate)).setScale(4,BigDecimal.ROUND_HALF_DOWN);
		} catch (NumberFormatException e) {
			// e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加失败,您设置的费率有误,请检查后重试!");
			return map;
		}

		ThirdLevelRate tlr = thirdLevelBusiness.findAllThirdLevelRatesBylevelidAndChannelId(thirdLevelId,channelId);
		ThirdLevelDistribution thirdLevelDistribution = thirdLevelBusiness.queryThirdLevelDistri(thirdLevelId);

		List<ThirdLevelDistribution> tlds = new ArrayList<ThirdLevelDistribution>();
		BrandRate brandRate = null;
		if (thirdLevelDistribution != null) {

			brandRate = brandMangeBusiness.findRateByBrandAndChannel(thirdLevelDistribution.getBrandId(), channelId);
			if (brandRate != null) {
				if (rate.doubleValue() < brandRate.getMinrate().doubleValue()) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的设置的费率小于该通道的最低费率" + brandRate.getMinrate());
					return map;
				}
				if (rate.doubleValue() > brandRate.getRate().doubleValue()) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的设置的费率大于该通道的普通会员的费率" + brandRate.getRate());
					return map;
				}
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的贴牌未设置初始费率");
				return map;
			}

			tlds = thirdLevelBusiness.getAllThirdLevelPrd(thirdLevelDistribution.getBrandId());
			if (tlds.size() > 0 && tlds.size() > thirdLevelDistribution.getGrade()) {
				ThirdLevelDistribution thirdLevelDistribution2 = thirdLevelBusiness.getThirdLevelByBrandidandgrade(thirdLevelDistribution.getBrandId(),thirdLevelDistribution.getGrade() + 1);
				if (thirdLevelDistribution2 != null) {
					ThirdLevelRate tlr2 = thirdLevelBusiness.findAllThirdLevelRatesBylevelidAndChannelId(thirdLevelDistribution2.getId(), channelId);
					if (tlr2 != null) {
						if (tlr2.getRate().doubleValue() > rate.doubleValue()) {
							map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
							map.put(CommonConstants.RESP_MESSAGE,"添加失败,你设置的费率小于你的上一等级产品个费率");
							return map;
						}
					} else {
						map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE,"添加失败,你的最高级别产品的费率还没设置");
						return map;
					}
					if (thirdLevelDistribution.getGrade() > 1) {
						ThirdLevelDistribution thirdLevelDistribution3 = thirdLevelBusiness.getThirdLevelByBrandidandgrade(thirdLevelDistribution.getBrandId(),thirdLevelDistribution.getGrade() - 1);
						if (thirdLevelDistribution3 != null) {
							ThirdLevelRate tlr3 = thirdLevelBusiness.findAllThirdLevelRatesBylevelidAndChannelId(thirdLevelDistribution3.getId(),channelId);
							if (tlr3 != null) {
								if (tlr3.getRate().doubleValue() < rate.doubleValue()) {
									map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
									map.put(CommonConstants.RESP_MESSAGE,"添加失败,你设置的费率大于你的下一等级产品个费率");
									return map;
								}
							}
						}

					}
				}
			}
		} else {
			if (brandId == null) {
				return ResultWrap.init(CommonConstants.FALIED, "贴牌号不能为空");
			}
			brandRate = brandMangeBusiness.findRateByBrandAndChannel(Long.valueOf(brandId), channelId);
			if (brandRate == null) {
				return ResultWrap.init(CommonConstants.FALIED, "该通道费率不存在！！");
			} else {
				if (brandRate.getMinrate() != null) {
					if (rate.compareTo(brandRate.getMinrate()) < 0) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "普通用户费率不能小于，平台底价" + brandRate.getMinrate());
						return map;

					}
					BigDecimal maxrate = new BigDecimal("0.02");
					if (maxrate.compareTo(rate) <= 0) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "普通用户费率不能大于2%");
						return map;
					}

				}

			}
			brandRate.setRate(rate);
			BrandRate brandrate = brandMangeBusiness.mergeBrandRate(brandRate);
			return ResultWrap.init(CommonConstants.SUCCESS, "修改成功", brandrate);
		}

		if (tlr == null) {
			tlr = new ThirdLevelRate();
		}
		tlr.setThirdLevelId(thirdLevelId);
		tlr.setChannelId(channelId);
		tlr.setRate(rate);
		tlr.setExtraFee(brandRate.getExtraFee());
		tlr.setCreateTime(new Date());
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT,thirdLevelBusiness.addThirdLevelRates(tlr));
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		/****
		 * 替换用户等级
		 ***/
		{
			/***
			 * 查询该等级的用户
			 ***/
			Long[] userIds = userLoginRegisterBusiness.queryUserIdByGradeAndBrandId(thirdLevelDistribution.getBrandId(),thirdLevelDistribution.getGrade() + "");
			Brand brand = brandMangeBusiness.findBrandById(thirdLevelDistribution.getBrandId());
			/***
			 * 进行替换
			 ***/
			if (userIds != null && userIds.length > 0) {
				for (Long userid : userIds) {
					if (userid == brand.getManageid()) {
						continue;
					}
					ChannelRate channelRate = channelRateBusiness.findChannelRateByUserid(userid, channelId);
					if (channelRate == null) {
						channelRate = new ChannelRate();
						channelRate.setUserId(userid);
						channelRate.setBrandId(thirdLevelDistribution.getBrandId());
						channelRate.setChannelId(tlr.getChannelId());
						channelRate.setRate(tlr.getRate());
						channelRate.setExtraFee(brandRate.getExtraFee());
						channelRate.setWithdrawFee(brandRate.getWithdrawFee());
						channelRate.setCreateTime(new Date());
					} else {
						channelRate.setRate(rate);
						channelRate.setExtraFee(brandRate.getExtraFee());
						channelRate.setWithdrawFee(brandRate.getWithdrawFee());
						channelRate.setCreateTime(new Date());
					}
					channelRateBusiness.mergeChannelRate(channelRate);
				}
			}
		}
		return map;
	}
	
	/**按品牌查询所有三级分销比率*/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/user/thirdlevel/ratio/query/{brand}")
	public @ResponseBody Object queryThirdLevelRatio(HttpServletRequest request,   
			@PathVariable("brand") long brandid
			){
		Map<String,Object> map = new HashMap<String,Object>();
		List<ThirdLevelRebateRatio> models = null;
		models = thirdLevelBusiness.getAllThirdRatio(brandid);
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, models);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	/**按品牌查询所有三级分销比率*/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/user/thirdlevel/prod/ratio/query/{brand}")
	public @ResponseBody Object queryThirdLevelProdRatioBy(HttpServletRequest request,   
			@PathVariable("brand") long brandid){
		Map map = new HashMap();
		List<ThirdLevelDistribution> thirdLevelDistributions= thirdLevelBusiness.getAllThirdLevelPrd(brandid);
		if(thirdLevelDistributions==null){
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, thirdLevelBusiness.getAllThirdRatio(brandid));
			map.put(CommonConstants.RESP_MESSAGE, "你还未添加产品，无法配置返佣费率");
			return map;
		}
		List<Map> maps=new ArrayList<Map>();
		
		List<ThirdLevelRebateRatio> thirdLevelRebateRatios =thirdLevelBusiness.getAllThirdRatio(brandid);
		for(ThirdLevelDistribution thirdLevelDistribution:thirdLevelDistributions){
			Map thirdLevelProdRatio=new HashMap();
			thirdLevelProdRatio.put("thirdLevelId", thirdLevelDistribution.getId());
			thirdLevelProdRatio.put("thirdLevelNmae", thirdLevelDistribution.getName());
			thirdLevelProdRatio.put("thirdLevelBrandId", thirdLevelDistribution.getBrandId());
			thirdLevelProdRatio.put("thirdLevelGrade", thirdLevelDistribution.getGrade());
			thirdLevelProdRatio.put("thirdLevelRatio", "");
			thirdLevelProdRatio.put("thirdLevelMoney", thirdLevelDistribution.getMoney());
			if(thirdLevelRebateRatios!=null){
				for(ThirdLevelRebateRatio thirdLevelRebateRatio:thirdLevelRebateRatios){
					if(Integer.parseInt(thirdLevelRebateRatio.getPreLevel())==thirdLevelDistribution.getGrade()){
						thirdLevelProdRatio.put("thirdLevelRatio", thirdLevelRebateRatio.getRatio());
					}
				}
			}
			maps.add(thirdLevelProdRatio);
		}
		
		
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, maps);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		
		return map;
	}
	
	/**删除一个三级分销产品
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/user/thirdlevel/del")
	public @ResponseBody Object delThirdLevelId(HttpServletRequest request,   
			@RequestParam(value = "id") long  id
						){
		
		 thirdLevelBusiness.delThirdLevelByid(id);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
		
	
	}*/
	/**传递产品id将所有平台信息和费率都取回来*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/rate/query/thirdlevelid")
	public @ResponseBody Object queryAllChannelByBrandId(HttpServletRequest request,
			@RequestParam("thirdLevelId") long thirdLevelId,
			@RequestParam(value = "access_type",defaultValue="1", required=false) long accesstype,
			@RequestParam(value="brand_id" , defaultValue = "0", required=false) long brandid
			){
		Map map = new HashMap();
		try {
			ThirdLevelDistribution thirdLevelDistribution= thirdLevelBusiness.queryThirdLevelDistri(thirdLevelId);
			List<Channel> channels;
			if(thirdLevelId!=0) {
				channels = channelRateBusiness.findAllChannelByBrandid(thirdLevelDistribution.getBrandId());
			}else {
				channels = channelRateBusiness.findAllChannelByBrandid(brandid);
			}
			List<Channel> result = new ArrayList<Channel>();
			if(thirdLevelId==0) {
				result=channels;
			}else {
				for(Channel channel : channels){
					Channel temp = channel;
					ThirdLevelRate tlr = thirdLevelBusiness.findAllThirdLevelRatesBylevelidAndChannelId(thirdLevelId,  channel.getId());
					BrandRate br = brandMangeBusiness.findRateByBrandAndChannel(thirdLevelDistribution.getBrandId(), channel.getId());
					if(tlr == null){
						temp.setRate(null);
					}else{
						temp.setRate(tlr.getRate());
						temp.setExtraFee(br.getExtraFee());
						temp.setWithdrawFee(br.getWithdrawFee());
					}
					result.add(temp);
				}
			}
			if(accesstype==1) {
				List<Channel> channels2=result;
				result = new ArrayList<Channel>();
				for(Channel channel : channels2) {
					if(!channel.getStatus().equals("1")) {
						channel.setChannelNo("4");
						result.add(channel);
					}else {
						result.add(channel);
					}
				}
			}
			map.put(CommonConstants.RESULT, result);
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			
		} catch (Exception e) {
			LOG.error("/v1.0/user/thirdlevel/rate/query/thirdlevelid:"+e);
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败，请稍后重试");
			map.put(CommonConstants.RESULT, null);
		}
		
		return map;
		
	}
	
	/**获取等级信息
	 * grade 等级
	 * brand_id 贴牌ID
	 * */
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/user/thirdlevel/prod/grade/query/{token}")
	public @ResponseBody Object queryThirdLevelDistriByBrandId(HttpServletRequest request,   
			@PathVariable("token") String token){
		Map map = new HashMap();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;		
		}	
		
		User user =userLoginRegisterBusiness.queryUserById(userId);
		
		ThirdLevelDistribution  distribution  =  thirdLevelBusiness.getThirdLevelByBrandidandgradeNoStatus(user.getBrandId(),Integer.parseInt(user.getGrade()) );
		if(distribution==null){
			distribution=new ThirdLevelDistribution();
			distribution.setGrade(0);
			distribution.setName("普通会员");
		}
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, distribution);
		return map;
	}
	
	
	//根据最高等级删除一个三级分销产品
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/delete/thirdlevelid")
	public @ResponseBody Object deleteThirdLevelDistributionByGrade(HttpServletRequest request,   
			@RequestParam("brand_id") long brandId){
		
		//查找到最高等级
		int ftdb = thirdLevelBusiness.findThirdLevelDistributionByBrandid(brandId);
		
		List<User> users = new ArrayList<User>();
		users = userLoginRegisterBusiness.queryUserByGrade(brandId, ftdb+"");
		Map map = new HashMap();
		if(users!=null&&users.size()>0){
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "该产品已被用户购买使用，无法删除该产品");
		}else{
			//根据最高等级删除产品
			thirdLevelBusiness.deleteThirdLevelDistributionByGrade(ftdb,brandId);

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "删除成功");
		}
		
		
		return map;
	}
	
	/**按品牌和产品id查询所有三级分销比率/新返佣逻辑使用接口 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/ratio/query/by/thirdlevelid")
	public @ResponseBody Object queryThirdLevelRatioByBrandIdAndThirdLevelId(HttpServletRequest request,   
			@RequestParam("brandId") String sbrandId,
			@RequestParam(value="thirdLevelId")String sthirdLevelId
			){
		Map<String,Object> map = new HashMap<String,Object>();
		long brandId;
		Integer thirdLevelId = null;
		try {
			brandId = Long.valueOf(sbrandId).longValue();
			thirdLevelId = Integer.valueOf(sthirdLevelId);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败,输入参数有误,请检查后重试!");
			return map;
		}
		List<ThirdLevelRebateRatioNew> models = null;
		models = thirdLevelBusiness.getAllThirdRatio(brandId,thirdLevelId);
		if(models == null || models.size() == 0){
			map.put(CommonConstants.RESP_MESSAGE, "查询成功,但没有数据!");
		}else{
			map.put(CommonConstants.RESULT, models);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
		}
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		return map;
	}
	
	/**按品牌和产品id查询所有三级分销比率/新返佣逻辑使用接口 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/ratio2/query/by/thirdlevelid")
	public @ResponseBody Object queryThirdLevelRatio2ByBrandIdAndThirdLevelId(HttpServletRequest request,   
			@RequestParam("brandId") String sbrandId,
			@RequestParam(value="thirdLevelId")String sthirdLevelId
			){
		Map<String,Object> map = new HashMap<String,Object>();
		long brandId;
		Integer thirdLevelId = null;
		try {
			brandId = Long.valueOf(sbrandId).longValue();
			thirdLevelId = Integer.valueOf(sthirdLevelId);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败,输入参数有误,请检查后重试!");
			return map;
		}
		List<ThirdLevelRebateRatioNew2> models = null;
		models = thirdLevelBusiness.getAllThirdRatio2(brandId,thirdLevelId);
		if(models == null || models.size() == 0){
			map.put(CommonConstants.RESP_MESSAGE, "查询成功,但没有数据!");
		}else{
			map.put(CommonConstants.RESULT, models);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
		}
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/ratio2/set/by/id")
	public @ResponseBody Object setThirdRatio2(HttpServletRequest request,
			@RequestParam(value="thirdRatioId")String sthirdRatioId,
			/** 固定获利*/
			@RequestParam(value="constant_return")String constantReturn,
			/** 直推收益*/
			@RequestParam(value="straight_return")String straightReturn,
			/** 间推收益*/
			@RequestParam(value="between_return")String betweenreturn
			){
		Map<String,Object>map = new HashMap<String,Object>();
		Long thirdRatioId = null;
		BigDecimal ratio = null;
		BigDecimal constantReturnAccount = null;
		BigDecimal straightReturnAccount = null;
		BigDecimal betweenreturnAccount  = null;
		if("".equals(sthirdRatioId.trim())||"".equals(constantReturn.trim())||"".equals(straightReturn.trim())||"".equals(betweenreturn.trim())){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败,输入参数有误,请检查后重试!");
			return map;
		}
		try {
			thirdRatioId = Long.valueOf(sthirdRatioId);
			constantReturnAccount = BigDecimal.valueOf(Double.valueOf(constantReturn)).setScale(2, BigDecimal.ROUND_HALF_UP);
			straightReturnAccount = BigDecimal.valueOf(Double.valueOf(straightReturn)).setScale(2, BigDecimal.ROUND_HALF_UP);
			betweenreturnAccount = BigDecimal.valueOf(Double.valueOf(betweenreturn)).setScale(2, BigDecimal.ROUND_HALF_UP);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败,输入参数有误,请检查后再试!");
			return map;
		}
		ThirdLevelRebateRatioNew2 model = null;
		model = thirdLevelBusiness.getThirdRatio2ById(thirdRatioId);
		if(model==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败,无需要修改的配置!");
			return map;
		}
		model.setConstantReturn(constantReturnAccount);
		model.setStraightReturn(straightReturnAccount);
		model.setBetweenreturn(betweenreturnAccount);
		model = thirdLevelBusiness.mergeThirdLevelRebateRatio2(model);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "修改成功!");
		map.put(CommonConstants.RESULT, model);
		return map;
	}
	
//	根据brandId查询出所有返佣比例设置,如果没有配置则进行初始化
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/ratio2/query/all")
	public @ResponseBody Object queryAllThirdLevelRebate2ByBrandId(HttpServletRequest request,
			@RequestParam(value="brandId") String sbrandId
			){
		Map<String,Object> map = new HashMap<String,Object>();
		Long brandId = null;
		if("".equals(brandId)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,输入参数有误,请检查后重试!");
			return map;
		}
		try {
			brandId = Long.valueOf(sbrandId);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,输入参数有误,请检查再重试!");
			return map;
		}
		Brand brand = brandMangeBusiness.findBrandById(brandId);
		if(brand ==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,无该贴牌!");
			return map;
		}
		List<ThirdLevelDistribution> proModels = null;
		List<ThirdLevelRebateRatioNew2> ratioModels = null;
		proModels = thirdLevelBusiness.getAllThirdLevelPrd(brandId);
		if(proModels==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,无产品配置!");
			return map;
		}
		for(int i = 0; i < proModels.size() ;i++){
			ratioModels = thirdLevelBusiness.getAllThirdRatio2(brandId,Integer.valueOf(proModels.get(i).getId()+""));
			if(ratioModels.size() != proModels.size()){
				for(int j = 0; j < proModels.size() ;j++){
					ThirdLevelRebateRatioNew2 ratioModel = thirdLevelBusiness.getThirdLevelRebateRatioNew2ByBrandIdAndPreLevelAndThirdLevelId(brandId,Integer.valueOf(proModels.get(i).getId()+""),j+1+"");
					if(ratioModel == null){
						ratioModel = new ThirdLevelRebateRatioNew2();
						ratioModel.setBrandId(brandId);
						ratioModel.setPreLevel(j+1+"");
						ratioModel.setThirdLevelId(Integer.valueOf(proModels.get(i).getId()+""));
						ratioModel.setConstantReturn(BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP));
						ratioModel.setStraightReturn(BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP));
						ratioModel.setBetweenreturn(BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP));
						ratioModel = thirdLevelBusiness.mergeThirdLevelRebateRatio2(ratioModel);
					}
				}
			}
		}
		ratioModels = thirdLevelBusiness.getAllThirdRatio2ByBrandId(brandId);
		map.put("brand", brand);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put("ThirdLevelDistribution", proModels);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, ratioModels);
		return map;
	}
	
//	根据brandId查询出所有返佣比例设置,如果没有配置则进行初始化
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/ratio/query/all")
	public @ResponseBody Object queryAllThirdLevelRebateByBrandId(HttpServletRequest request,
			@RequestParam(value="brandId") String sbrandId
			){
		Map<String,Object> map = new HashMap<String,Object>();
		Long brandId = null;
		if("".equals(brandId)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,输入参数有误,请检查后重试!");
			return map;
		}
		try {
			brandId = Long.valueOf(sbrandId);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,输入参数有误,请检查再重试!");
			return map;
		}
		Brand brand = brandMangeBusiness.findBrandById(brandId);
		if(brand ==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,无该贴牌!");
			return map;
		}
		List<ThirdLevelDistribution> proModels = null;
		List<ThirdLevelRebateRatioNew> ratioModels = null;
		proModels = thirdLevelBusiness.getAllThirdLevelPrd(brandId);
		if(proModels==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,无产品配置!");
			return map;
		}
		for(int i = 0; i < proModels.size() ;i++){
			ratioModels = thirdLevelBusiness.getAllThirdRatio(brandId,Integer.valueOf(proModels.get(i).getId()+""));
			if(ratioModels.size() != proModels.size()){
				for(int j = 0; j < proModels.size() ;j++){
					ThirdLevelRebateRatioNew ratioModel = thirdLevelBusiness.getByBrandIdAndPreLevelAndThirdLevelId(brandId,Integer.valueOf(proModels.get(i).getId()+""),j+1+"");
					if(ratioModel == null){
						ratioModel = new ThirdLevelRebateRatioNew();
						ratioModel.setBrandId(brandId);
						ratioModel.setPreLevel(j+1+"");
						ratioModel.setThirdLevelId(Integer.valueOf(proModels.get(i).getId()+""));
						ratioModel.setRatio(BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP));
						ratioModel = thirdLevelBusiness.mergeThirdLevelRebateRatio(ratioModel);
					}
				}
			}
		}
		ratioModels = thirdLevelBusiness.getAllThirdRatioByBrandId(brandId);
		for (ThirdLevelRebateRatioNew thirdLevelRebateRatioNew : ratioModels) {
			for (ThirdLevelDistribution thirdLevelDistribution :proModels) {
				if (thirdLevelRebateRatioNew.getThirdLevelId().intValue() == thirdLevelDistribution.getId()) {
					thirdLevelRebateRatioNew.setGrade(thirdLevelDistribution.getGrade());
				}
			}
		}
		
		map.put("brand", brand);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put("ThirdLevelDistribution", proModels);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, ratioModels);
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/ratio/set/by/id")
	public @ResponseBody Object setThirdRatio(HttpServletRequest request,
			@RequestParam(value="thirdRatioId")String sthirdRatioId,
			@RequestParam(value="ratio")String sratio
			){
		Map<String,Object>map = new HashMap<String,Object>();
		Long thirdRatioId = null;
		BigDecimal ratio = null;
		if("".equals(sthirdRatioId.trim())||"".equals(sratio.trim())){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败,输入参数有误,请检查后重试!");
			return map;
		}
		try {
			thirdRatioId = Long.valueOf(sthirdRatioId);
			ratio = BigDecimal.valueOf(Double.valueOf(sratio)).setScale(4, BigDecimal.ROUND_HALF_UP);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败,输入参数有误,请检查后再试!");
			return map;
		}
		if(ratio.compareTo(BigDecimal.ZERO) < 0 || ratio.compareTo(BigDecimal.ONE) > 0){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败,输入返佣比例有误,只能大于0小于1,请检查后再试!");
			return map;
		}
		
		ThirdLevelRebateRatioNew model = null;
		model = thirdLevelBusiness.getThirdRatioById(thirdRatioId);
		if(model==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败,无需要修改的配置!");
			return map;
		}
		model.setRatio(ratio);
		model = thirdLevelBusiness.mergeThirdLevelRebateRatio(model);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "修改成功!");
		map.put(CommonConstants.RESULT, model);
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/ratio/onoff")
	public @ResponseBody Object updateNewRebate(HttpServletRequest request,
			@RequestParam(value="brandId")String sbrandId,
			@RequestParam(value="status")String status
			){
		Map<String,Object>map = new HashMap<String,Object>();
		Brand model = null;
		model = brandMangeBusiness.findBrandById(Long.valueOf(sbrandId));
		if(model==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "设置失败,无该贴牌!");
			return map;
		}
		Integer newisNewRebate = null;
		Integer isNewRebate=model.getIsNewRebate();
		try {
			newisNewRebate = Integer.valueOf(status);
			if(newisNewRebate==isNewRebate){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "设置失败，模式无法重复设置!");
				return map;
			}
			if(0!=newisNewRebate.intValue() && 1!=newisNewRebate.intValue()&& 2!=newisNewRebate.intValue()){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "设置失败,设置参数有误,请检查后重试!");
				return map;
			}
			boolean size=true;
			if(isNewRebate.intValue()==0){
				List<ThirdLevelRebateRatio> ThirdLevelRebateRatios=thirdLevelBusiness.getAllThirdRatio(Long.valueOf(sbrandId));
				if(ThirdLevelRebateRatios!=null&&ThirdLevelRebateRatios.size()>0){
					size=false;
				}
			}else if(isNewRebate.intValue()==1){
				List<ThirdLevelRebateRatioNew>   ThirdLevelRebateRatioNews =thirdLevelBusiness.getAllThirdRatioByBrandId(Long.valueOf(sbrandId));
				if(ThirdLevelRebateRatioNews!=null&&ThirdLevelRebateRatioNews.size()>0){
					size=false;
				}
			}else if(isNewRebate.intValue()==2){
				List<ThirdLevelRebateRatioNew2>   ThirdLevelRebateRatioNew2s =thirdLevelBusiness.getAllThirdRatio2ByBrandId(Long.valueOf(sbrandId));
				if(ThirdLevelRebateRatioNew2s!=null&&ThirdLevelRebateRatioNew2s.size()>0){
					size=false;
				}
			}
			if(!size){
				String rebateName = null;
				String NewRebateName = null;
				if (isNewRebate.intValue()==0) {
					rebateName = "第一套";
				}else if(isNewRebate.intValue()==1){
					rebateName = "第二套";
				}else{
					rebateName = "第三套";
				}
				if (newisNewRebate.intValue()==0) {
					NewRebateName = "第一套";
				}else if(newisNewRebate.intValue()==1){
					NewRebateName = "第二套";
				}else{
					NewRebateName = "第三套";
				}
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "您当前正在使用"+ rebateName + "模式,无法修改成" + NewRebateName + "模式");
				return map;
			}
			
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "设置失败,设置参数非法,请检查后重试!");
			return map;
		}
		model.setIsNewRebate(newisNewRebate);
		model = brandMangeBusiness.mergeBrand(model);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "设置成功");
		map.put(CommonConstants.RESULT,model);
		return map;
	}
	
	/**
	 * 三级分销死循环删除数据功能接口
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/delete/bylevelidandchannelid/{token}")
	public @ResponseBody Object deleteThirdLevelRatesbylevelidAndChannelId(HttpServletRequest request,
			@PathVariable("token") String token,
			@RequestParam(value = "channel_id") long channelId){
		Map<String,Object>map = new HashMap<String,Object>();
		long brandid;
		try {
			brandid = TokenUtil.getBrandid(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}
		
		List<ThirdLevelDistribution> thirdLevelDistribution = thirdLevelBusiness.getAllThirdLevelPrd(brandid);
		if (thirdLevelDistribution == null || thirdLevelDistribution.isEmpty()) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "没有查询到该贴牌下三级分销产品信息！");
			return map;
		}
		long[] tldids = new long[thirdLevelDistribution.size()];
		for (int i = 0; i < tldids.length; i++) {
			tldids[i] = thirdLevelDistribution.get(i).getId();
		}
		List<ThirdLevelRate> thirdLevelRate = thirdLevelBusiness.queryThirdLevelRatesBylevelidAndChannelId(tldids, channelId);
		if (thirdLevelRate == null || thirdLevelRate.isEmpty()) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "没有查询到该通道三级分销费率！");
			return map;
		} else {
			thirdLevelBusiness.deleteThirdLevelThirdLevelRatesBylevelidAndChannelId(tldids, channelId);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "数据删除成功！");
			return map;
		}
	}

	/*
	 *@description:三级分销的VIP费率查询
	 *@author: ives
	 *@annotation:"制定计划的时候调用，提醒用户从会员提升到VIP会优惠多少钱"
	 *@data:2019年9月16日  18:00:32
	 *
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/thirdlevel/delete/bylevelidandchannelid/selectrate")
	public @ResponseBody Object selectThirdLevelVIPrate(@RequestParam(value = "brandId")String brandId,
														@RequestParam(value="amount")String amount,
														@RequestParam(value="version")String version,
														@RequestParam(value="userId")String userId){
		//查询用户当前等级
		String url="http://creditcardmanager/v1.0/creditcardmanager/repayment/zp/selectversion";
		Long userid=Long.valueOf(userId);
		Long brandid=Long.valueOf(brandId);
		User user=userLoginRegisterBusiness.queryUserById(userid);
		Brand brand=brandMangeBusiness.findBrandById(brandid);
		if(String.valueOf(brand.getManageid()).equals(userId)){return null;}
		String firstGrade=user.getGrade();
		if(!firstGrade.equals("0")){return null;}
		int preGrade=1;
		ThirdLevelDistribution thirdLevelDistribution=thirdLevelBusiness.getThirdLevelByBrandidandgrade(brandid,preGrade);
		Long preGradeId=thirdLevelDistribution.getId();
		MultiValueMap<String,Object> requestEntity=new LinkedMultiValueMap<>();
		requestEntity.add("version",version);
		String versionConfig=restTemplate.postForObject(url,requestEntity,String.class);
		JSONObject channelConfig=JSONObject.fromObject(versionConfig);
		String channelId=channelConfig.getString("channelId");
		Long channelid=Long.valueOf(channelId);
		ThirdLevelRate thirdLevelRate=thirdLevelBusiness.findAllThirdLevelRatesBylevelidAndChannelId(preGradeId,channelid);
		BigDecimal preRate=thirdLevelRate.getRate();
		BrandRate brandRate=brandMangeBusiness.findRateByBrandAndChannel(brandid,channelid);
		BigDecimal firstRate=brandRate.getRate();
		BigDecimal rate=firstRate.subtract(preRate);
		BigDecimal blance=new BigDecimal(amount).multiply(rate).setScale(2,BigDecimal.ROUND_DOWN);
		return ResultWrap.init(CommonConstants.SUCCESS,"如果您升级为VIP用户，您该账单将节省"+blance+"元");

	}

	// 根据价格查询产品id
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/user/thirdlevel/findByPrice")
	public @ResponseBody Object findByPrice(@RequestParam(value = "price")String price){
		ThirdLevelDistribution thirdLevelDistribution = thirdLevelBusiness.findByPrice(price);
		return ResultWrap.init(CommonConstants.SUCCESS,"查询成功",thirdLevelDistribution);
	}



}

