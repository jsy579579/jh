package com.jh.user.service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.ThirdLeveDistributionBusiness;
import com.jh.user.business.UserAutoUpgradeBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.BrandAutoUpdateConfig;
import com.jh.user.pojo.BrandAutoUpgrade;
import com.jh.user.pojo.BrandRate;
import com.jh.user.pojo.ThirdLevelDistribution;
import com.jh.user.pojo.ThirdLevelRate;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Configuration
@Controller
@EnableScheduling
public class UserAutoUpgradeService {
	private static final Logger LOG = LoggerFactory.getLogger(UserAutoUpgradeService.class);
	
	@Autowired
	private UserAutoUpgradeBusiness userAutoUpgradeBusiness;
	
	@Autowired 
	private BrandManageBusiness brandMangeBusiness;

	@Autowired
	private ThirdLeveDistributionBusiness  thirdLevelBusiness;
	
	@Value("${schedule-task.on-off}")
	private String scheduleOnOff;
	
	

	@Scheduled(cron = "0 0/10 * * * ?")
	public void scheduler() {
		if("true".equals(scheduleOnOff)){
			LOG.info("自动升级开始了...........................");
			userAutoUpgradeBusiness.getNeedAutoGradeUser();
		}
    }
	
	/**生成一个自动升级通道费率*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/auto/upgrade/add")
	public @ResponseBody Object addBrandAutoUpgrade(HttpServletRequest request,   
			@RequestParam(value = "brand_id") long brandId,
			@RequestParam(value = "channel_id") long channelId,
			@RequestParam(value = "rate") BigDecimal rate){
		BrandRate brandRate=brandMangeBusiness.findRateByBrandAndChannel(brandId,channelId);
		if(brandRate!=null){
			if(rate.doubleValue()<brandRate.getMinrate().doubleValue()){
				Map map = new HashMap();
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的设置的费率小于该通道的最低费率"+brandRate.getMinrate());
				return map;		
			}
			if(rate.doubleValue()>brandRate.getRate().doubleValue()){
				Map map = new HashMap();
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的设置的费率大于该通道的普通会员的费率"+brandRate.getRate());
				return map;		
			}
			
		}else{
			Map map = new HashMap();
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的贴牌未设置初始费率");
			return map;		
			
		}
		
		ThirdLevelDistribution thirdLevelDistribution=thirdLevelBusiness.getThirdLevelByBrandidandgrade(brandId,  1);
		List<ThirdLevelDistribution> tlds=new ArrayList<ThirdLevelDistribution>();
		if(thirdLevelDistribution!=null){
			tlds=thirdLevelBusiness.getAllThirdLevelPrd(thirdLevelDistribution.getBrandId());
			if(tlds.size()>0&&tlds.size()>thirdLevelDistribution.getGrade()){
				ThirdLevelDistribution thirdLevelDistribution2=thirdLevelBusiness.getThirdLevelByBrandidandgrade(thirdLevelDistribution.getBrandId(), thirdLevelDistribution.getGrade()+1);
				if(thirdLevelDistribution2!=null){
					ThirdLevelRate tlr2 =thirdLevelBusiness.findAllThirdLevelRatesBylevelidAndChannelId(thirdLevelDistribution2.getId(), channelId);
					if(tlr2!=null){
						if(tlr2.getRate().doubleValue()>rate.doubleValue()){
							Map map = new HashMap();
							map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
							map.put(CommonConstants.RESP_MESSAGE, "添加失败,你设置的费率小于你的上一等级产品个费率");
							return map;		
						}
					}else{
						Map map = new HashMap();
						map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "添加失败,您还未添加产品通道费率，请先添加");
						return map;	
						
					}
					if(thirdLevelDistribution.getGrade()>1){
						ThirdLevelDistribution thirdLevelDistribution3=thirdLevelBusiness.getThirdLevelByBrandidandgrade(thirdLevelDistribution.getBrandId(), thirdLevelDistribution.getGrade()-1);
						if(thirdLevelDistribution3!=null){
							ThirdLevelRate tlr3=thirdLevelBusiness.findAllThirdLevelRatesBylevelidAndChannelId(thirdLevelDistribution3.getId(), channelId);
							if(tlr3!=null){
								if(tlr3.getRate().doubleValue()<rate.doubleValue()){
									Map map = new HashMap();
									map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
									map.put(CommonConstants.RESP_MESSAGE, "添加失败,你设置的费率大于你的下一等级产品个费率");
									return map;		
								}
							}
						}
					}
				}
			}
		
		}else{
			Map map = new HashMap();
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加失败,你设置的费率大于你的下一等级产品个费率");
			return map;		
			
			
		}
		
		BrandAutoUpgrade brandAutoUpgrade = null;
		
		brandAutoUpgrade = userAutoUpgradeBusiness.findBrandAutoUpgradeBybrandidAndchannelId(brandId, channelId);
		
		if(brandAutoUpgrade == null){
			brandAutoUpgrade = new BrandAutoUpgrade();
		}
		brandAutoUpgrade.setChannelId(channelId);
		brandAutoUpgrade.setCreateTime(new Date());
		brandAutoUpgrade.setRate(rate);
		brandAutoUpgrade.setBrandId(brandId);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userAutoUpgradeBusiness.saveBrandAutoUpgrade(brandAutoUpgrade));
		return map;
	}
	
	/**查询当前品牌的自动升级的通道费率*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/auto/upgrade/query")
	public @ResponseBody Object queryBrandAutoUpgradeByBrandId(HttpServletRequest request,   
			@RequestParam(value = "brand_id") long brandId){
		List<BrandAutoUpgrade> brandAutoUpgrades = null;
		brandAutoUpgrades = userAutoUpgradeBusiness.findBrandAutoUpgradeByBrandid(brandId);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, brandAutoUpgrades);
		return map;
	}
	
	/**查询当前品牌的自动升级的通道费率*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/auto/channel/upgrade/query")
	public @ResponseBody Object queryBrandAutoUpgradeChannelByBrandId(HttpServletRequest request,   
			@RequestParam(value = "brand_id") long brandId){
		List<BrandRate>  BrandRates =brandMangeBusiness.findRateByBrand(brandId);
		List<BrandAutoUpgrade> baus = new ArrayList<BrandAutoUpgrade>();
		if(BrandRates!=null){
			List<BrandAutoUpgrade> brandAutoUpgrades = null;
			brandAutoUpgrades = userAutoUpgradeBusiness.findBrandAutoUpgradeByBrandid(brandId);
			for(BrandRate brandRate:BrandRates){
				String start="0";
				for(BrandAutoUpgrade brandAutoUpgrade:brandAutoUpgrades){
					if(brandAutoUpgrade.getChannelId()==brandRate.getChannelId()){
						baus.add(brandAutoUpgrade);
						start="1";
						break ;
					}
				}
				if(start.equals("1")){
					continue;
				}
				BrandAutoUpgrade brandAutoUpgrade=new BrandAutoUpgrade();
				brandAutoUpgrade.setBrandId(brandId);
				brandAutoUpgrade.setChannelId(brandRate.getChannelId());
				brandAutoUpgrade.setRate(new BigDecimal("0.00"));
				brandAutoUpgrade.setCreateTime(new Date());
				baus.add(brandAutoUpgrade);
			}
		}else{
			Map map = new HashMap();
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败，你还未开通支付通道");
			return map;
		}
		
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, baus);
		return map;
	}
	
	/**查询指定自动升级的通道费率*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/auto/upgrade/channel/query")
	public @ResponseBody Object addBrandAutoUpgradeByBrandIdAndChannelId(HttpServletRequest request,   
			@RequestParam(value = "brand_id") long brandId,
			@RequestParam(value = "channel_id") long channelId){
		
		BrandAutoUpgrade brandAutoUpgrade = null;
		
		brandAutoUpgrade = userAutoUpgradeBusiness.findBrandAutoUpgradeBybrandidAndchannelId(brandId, channelId);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, brandAutoUpgrade);
		return map;
	}
	
	/**添加自动升级配置*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/auto/upgrade/Config/add")
	public @ResponseBody Object saveBrandAutoUpdateConfig(HttpServletRequest request,  
			/**贴牌Id***/
			@RequestParam(value = "brand_id") long brandId,
			/***级别***/
			@RequestParam(value = "grade") long grade,
			/***直推判定级别***/
			@RequestParam(value = "pursuant_grade", defaultValue = "0", required = false) long pursuantGrade,
			/***团队判定级别***/
			@RequestParam(value = "team_grade", defaultValue = "0", required = false) int teamGrade,
			/***人数**/
			@RequestParam(value = "people_num", defaultValue = "0", required = false) int peoplenum,
			/***团队人数**/
			@RequestParam(value = "team_people_num" , defaultValue = "0", required = false) int teamPeopleNum,
			/***模式1，直推；2团队；3：直推+团队**/
			@RequestParam(value = "auto_update_type" , defaultValue = "1", required = false) int autoUpdateType,
			/***状态**/
			@RequestParam(value = "status") String  status){
		Map<String,Object> map = new HashMap<String,Object>();
		if(grade==0||"".equals(brandId)||"".equals(grade)||"".equals(peoplenum)){
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "产品等级出错请检查");
			return map;
		}
		BrandAutoUpdateConfig brandAutoUpgrade = userAutoUpgradeBusiness.getBrandAutoByBrandidAndGradeNostutas(brandId, grade);
		
		if(brandAutoUpgrade==null){
			brandAutoUpgrade=new BrandAutoUpdateConfig();
		}
		if(autoUpdateType==1) {
			teamGrade=0;
			teamPeopleNum=0;
		}
		if(autoUpdateType==2) {
			peoplenum=0;
			pursuantGrade=0;
		}
		Brand brand=brandMangeBusiness.findBrandById(brandId);
		if(brand!=null) {
			brand.setAutoUpgrade("3");
		}else {
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "品牌传输有误请确认");
			return map;
		}
		brandAutoUpgrade.setBrandId(brandId);
		brandAutoUpgrade.setGrade(grade);
		brandAutoUpgrade.setPeopleNum(peoplenum);
		brandAutoUpgrade.setPursuantGrade(pursuantGrade);
		brandAutoUpgrade.setTeamGrade(teamGrade);
		brandAutoUpgrade.setTeamPeopleNum(teamPeopleNum);
		brandAutoUpgrade.setAutoUpdateType(autoUpdateType);
		brandAutoUpgrade.setStatus(status);
		brandAutoUpgrade.setCreateTime(new Date());
		brandAutoUpgrade = userAutoUpgradeBusiness.saveBrandAutoUpdateConfig(brandAutoUpgrade);
		brandMangeBusiness.mergeBrand(brand);
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, brandAutoUpgrade);
		return map;
	}
	/**查询贴牌配置*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/auto/upgrade/Config/brand/query")
	public @ResponseBody Object saveBrandAutoUpdateConfig(HttpServletRequest request,  
			/**贴牌Id***/
			@RequestParam(value = "brand_id") long brandId){
		List<BrandAutoUpdateConfig>  brandAutoUpgrade = userAutoUpgradeBusiness.getBrandAutoConfigByBrandidNostutas(brandId);
		for (BrandAutoUpdateConfig brandAutoUpdateConfig : brandAutoUpgrade) {
			try {
				ThirdLevelDistribution thirdLevelDistribution = thirdLevelBusiness.getThirdLevelByBrandidandgrade(brandId, Long.valueOf(brandAutoUpdateConfig.getGrade()).intValue());
				brandAutoUpdateConfig.setProductName(thirdLevelDistribution.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "成功",brandAutoUpgrade);
	}
	
	/**开关*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/auto/upgrade/Config/status/update")
	public @ResponseBody Object saveBrandAutoUpdateConfig(HttpServletRequest request,  
			/**贴牌Id***/
			@RequestParam(value = "brand_id") long brandId,
			/***级别***/
			@RequestParam(value = "grade") long grade,
			/***状态**/
			@RequestParam(value = "status") String  status){
		Map map = new HashMap();
		BrandAutoUpdateConfig brandAutoUpgrade = userAutoUpgradeBusiness.getBrandAutoByBrandidAndGrade(brandId, grade);
		
		if(brandAutoUpgrade==null){
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "不存在");
			return map;
		}
		brandAutoUpgrade.setStatus(status);
		brandAutoUpgrade.setCreateTime(new Date());
		brandAutoUpgrade = userAutoUpgradeBusiness.saveBrandAutoUpdateConfig(brandAutoUpgrade);
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, brandAutoUpgrade);
		return map;
	}
	
	/**开关*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/auto/upgrade/Config/status/test")
	public @ResponseBody Object test(HttpServletRequest request){
		this.scheduler();
		return scheduleOnOff;
	}
	
}
