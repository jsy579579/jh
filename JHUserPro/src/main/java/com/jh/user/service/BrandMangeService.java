package com.jh.user.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.RandomUtils;
import cn.jh.common.utils.TokenUtil;
import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.ChannelRateBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.*;
import com.jh.user.util.AliOSSUtil;
import com.jh.user.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class BrandMangeService {

	private static final Logger LOG = LoggerFactory.getLogger(BrandMangeService.class);

	@Autowired
	private BrandManageBusiness brandMangeBusiness;

	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;

	@Autowired
	private ChannelRateBusiness channelRateBusiness;
	
	@Autowired
	private UserRelationService userRelationService;
	
	@Autowired
	private UserJpushService userJpushService;
	
	@Autowired
	private RestTemplate resttemplate;
	
	@Autowired
	private AliOSSUtil aliOSSUtil;

	@Autowired
	Util util;

	@Value("${user.app.uploadpath}")
	private String realnamePic;

	@Value("${user.app.downloadpath}")
	private String downloadPath;
	
	@Value("${user.app.dowanloadurl}")
	private String downloadUrl;

	/** 删除一个资源 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/brand/resource/del")
	public @ResponseBody Object delReource(HttpServletRequest request,
			@RequestParam(value = "resource_id") long resource) {

		brandMangeBusiness.delResource(resource);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "删除成功");
		return map;
	}

	/**
	 * 修改等级名称 李梦珂
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/update/updatebylevelname")
	public Object updateByLevelName(HttpServletRequest request, @RequestParam(value = "id") long id,
			@RequestParam(value = "level_name", defaultValue = "普通用户") String levelName) {

		Map map = new HashMap();
		Brand brand = brandMangeBusiness.findBrandById(id);

		if (brand == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE, "该贴牌不存在");
			return map;

		}
		brand.setLevelName(levelName);
		brand = brandMangeBusiness.mergeBrand(brand);

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brand);
		map.put(CommonConstants.RESP_MESSAGE, "修改成功");
		return map;

	}

	/** 新增一个资源 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/resource/add")
	public @ResponseBody Object addReource(HttpServletRequest request,
			@RequestParam(value = "resource_name") String resourcename,
			@RequestParam(value = "resource_url") String resourceurl) {
		Resource resource = new Resource();
		resource.setCreateTime(new Date());
		resource.setResourceName(resourcename);
		resource.setUrl(resourceurl);
		resource.setStatus(CommonConstants.STATUS_VALID);
		resource.setResourceNo(RandomUtils.generateString(6));

		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brandMangeBusiness.mergeResource(resource));
		return map;
	}

	/** 删除一个品牌权限 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/brand/brandresource/del")
	public @ResponseBody Object delBrandReource(HttpServletRequest request,
			@RequestParam(value = "resource_id") long resource, @RequestParam(value = "brand_id") long brandid) {

		brandMangeBusiness.delResourceByBrand(brandid, resource);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "删除成功");
		return map;
	}

	/** 新增一个品牌权限 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/brandresource/add")
	public @ResponseBody Object addBrandReource(HttpServletRequest request,
			@RequestParam(value = "resource_id") long resourceid, @RequestParam(value = "brand_id") long brandid) {
		BrandResource brandresource = new BrandResource();
		brandresource.setBrandid(brandid);
		brandresource.setResourceid(resourceid);
		brandresource.setStatus(CommonConstants.STATUS_VALID);
		brandresource.setCreateTime(new Date());

		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brandMangeBusiness.saveBrandResource(brandresource));

		return map;
	}

	/** 修改品牌的平级奖励比率 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/equalRebateRate/update")
	public @ResponseBody Object updateBrandEqualRebateRate(HttpServletRequest request,
			@RequestParam(value = "equalRebateRate") BigDecimal equalRebateRate,
			@RequestParam(value = "brand_id") long brandid) {

		Brand brand = brandMangeBusiness.findBrandById(brandid);
		Map map = new HashMap();
		if (brand == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE, "贴牌不存在");
			return map;

		}
		brand.setEqualRebateRate(equalRebateRate);

		brand = brandMangeBusiness.mergeBrand(brand);

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brand);
		return map;
	}

	/** 获取一个品牌 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/brand/query/id")
	public @ResponseBody Object queryBrandById(HttpServletRequest request,
			@RequestParam(value = "brand_id") long brandid) {

		Brand brand = brandMangeBusiness.findBrandById(brandid);
		Map<String,Object> map = new HashMap<String,Object>();
		if(brand!=null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		}else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE,"贴牌不存在");
		}
		
		map.put(CommonConstants.RESULT, brand);
		return map;
	}

	/** 获取所有品牌 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/brand/query/all")
	public @ResponseBody Object queryAllBrand(HttpServletRequest request) {
		Map<String,Object> map = new HashMap<String,Object>();
		List<Brand> brands = brandMangeBusiness.findAllBrand();
		List<Map> list = new ArrayList<Map>();

		for (Brand brand : brands) {
			Map<String,Object> obje = new HashMap<String,Object>();
			obje.put("id", brand.getId());
			obje.put("name", brand.getName());
			obje.put("autoRebateConfigOnOff", brand.getAutoRebateConfigOnOff());
			obje.put("manageId", brand.getManageid());
			list.add(obje);
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		return map;
	}

	/** 新增一个品牌 */
	@Deprecated
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/add")
	public @ResponseBody Object addBrand(HttpServletRequest request, 
			@RequestParam(value = "name") String name,
			@RequestParam(value ="oriBrandId",required=false,defaultValue="-1")String soriBrandId,
			@RequestParam(value = "copy_brandid", defaultValue = "2", required = false) long copyBrandId,
			@RequestParam(value = "coin_ratio", defaultValue = "1.00", required = false) BigDecimal coinRatio,
			@RequestParam(value = "auto_upgrade", defaultValue = "2", required = false) String autoUpgrade,
			@RequestParam(value = "manager_phone") String managerPhone) {

		Map<String,Object> map = new HashMap<String,Object>();
		Brand brand = new Brand();
		Brand brand_a = null;
		if (managerPhone != null && !managerPhone.equalsIgnoreCase("")) {
			long oriBrandId = -1;
			User user;
			try {
				oriBrandId = Long.valueOf(soriBrandId);
			} catch (NumberFormatException e) {
				oriBrandId = -1;
			}
			if(oriBrandId==-1){
				user = userLoginRegisterBusiness.queryUserByPhone(managerPhone);
			}else{
				if("6".equals(brandMangeBusiness.findBrandById(oriBrandId).getBrandType())){
					user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(managerPhone, oriBrandId);
				}else{
					user = userLoginRegisterBusiness.queryUserByPhone(managerPhone);
				}
			}
			if (user == null) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
				map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
				return map;
			}
			brand.setManageid(user.getId());
			brand_a = brandMangeBusiness.findBrandById(user.getId());
			if (brand_a != null) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
				map.put(CommonConstants.RESP_MESSAGE, "不可建立多个贴牌");
				return map;
			}
		}
		brand.setBrandType(CommonConstants.BRAND_OTHER);
		brand.setCreateTime(new Date());
		brand.setName(name);
		brand.setAutoUpgrade(autoUpgrade);
		brand.setNumber(RandomUtils.generateString(8));
		brand.setAutoRebateConfigOnOff(0);
		brand = brandMangeBusiness.mergeBrand(brand);
		brand = brandMangeBusiness.mergeBrandNumber(brand.getNumber());
		if (brand != null) {
			User user = userLoginRegisterBusiness.queryUserById(brand.getManageid());
			List<User> users = userLoginRegisterBusiness.findAfterUsers(brand.getManageid());
			if ((users == null || users.size() == 0) && user != null) {
				user.setPreUserPhone("");
				user.setPreUserId(0);
				user.setBrandId(brand.getId());
				user.setBrandname(brand.getName());
				user.setVdynastType("1");
				user.setVerifyStatus("1");        //   已购买状态:开启
				user.setBankCardManagerStatus(1); //信用卡管家的信用卡状态:开启
				userLoginRegisterBusiness.saveUser(user);
				userRelationService.updateUserToBrandUser(user.getPhone(), brand.getId()+"");
			}
			// 自动添加积分比率
			BrandCoin Brandcoin = new BrandCoin();
			Brandcoin.setBrandId(brand.getId());
			Brandcoin.setRatio(coinRatio);
			Brandcoin.setCreateTime(new Date());
			brandMangeBusiness.addBrandCoin(Brandcoin);
			// 自动添加贴牌低价，和普通用户刷卡费率
			/**** 查询复制贴牌低价配置 ***/
			List<BrandRate> copyBrandRates = brandMangeBusiness.findRateByBrand(copyBrandId);
			/*** 循环配置低价 **/
			for (BrandRate copyBrandRate : copyBrandRates) {
				// 配置贴牌费率
				BrandRate pasteBrandRate = brandMangeBusiness.findRateByBrandAndChannel(brand.getId(),
						copyBrandRate.getChannelId());
				if (pasteBrandRate == null) {
					pasteBrandRate = new BrandRate();
				} else {
					pasteBrandRate.setId(0);
				}
				pasteBrandRate.setBrandId(brand.getId());
				pasteBrandRate.setExtraFee(copyBrandRate.getExtraFee());
				pasteBrandRate.setChannelId(copyBrandRate.getChannelId());
				pasteBrandRate.setMinrate(copyBrandRate.getMinrate());
				pasteBrandRate.setRate(copyBrandRate.getRate());
				pasteBrandRate.setStatus(copyBrandRate.getStatus());
				pasteBrandRate.setWithdrawFee(copyBrandRate.getWithdrawFee());
				pasteBrandRate.setCreateTime(new Date());
				pasteBrandRate = brandMangeBusiness.mergeBrandRate(pasteBrandRate);
				// 配置贴牌所属人的费率
				ChannelRate channelrate = channelRateBusiness.findChannelRateByUserid(brand.getManageid(),pasteBrandRate.getChannelId(),brand.getId());
				if (channelrate == null) {
					channelrate = new ChannelRate();
				}
				channelrate.setId(0);
				channelrate.setBrandId(brand.getId());
				channelrate.setChannelId(pasteBrandRate.getChannelId());
				channelrate.setExtraFee(pasteBrandRate.getExtraFee());
				channelrate.setCreateTime(new Date());
				channelrate.setRate(pasteBrandRate.getRate());
				channelrate.setUserId(brand.getManageid());
				channelrate.setWithdrawFee(pasteBrandRate.getWithdrawFee());
				channelrate = channelRateBusiness.mergeChannelRate(channelrate);
			}
			try {
				/*** 修改路由数据 ***/
				RestTemplate restTemplate = new RestTemplate();
				URI uri = util.getServiceUrl("paymentchannel", "error url request!");
				String url = uri.toString() + "/v1.0/paymentchannel/channelroute/config";
				/** 根据的用户手机号码查询用户的基本信息 */
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("brand_code", brand.getId() + "");
				requestEntity.add("brand_code1", copyBrandId + "");
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				
				//初始化校验次数
				url = "http://paymentchannel/v1.0/paymentchannel/create/bankcard/auth/count";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("brandId", brand.getId()+"");
				result = resttemplate.postForObject(url, requestEntity, String.class);
				LOG.info("/v1.0/paymentchannel/create/bankcard/auth/count==========" + result);
				//初始化校验次数
				url = "http://notice/v1.0/notice/sms/creat/brandcount";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("brandId", brand.getId()+"");
				result = resttemplate.postForObject(url, requestEntity, String.class);
				LOG.info("/v1.0/notice/sms/creat/brandcount==========" + result);
			} catch (RestClientException e) {
				e.printStackTrace();
			}
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brand);
		return map;
	}

	/** 修改一个品牌 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/update/id")
	public @ResponseBody Object updateBrandById(HttpServletRequest request,

			@RequestParam(value = "brand_id") long brandid,
			/** 自动升级 0 表示不开通自动升级功能 1表示开通自动升级 */
			@RequestParam(value = "autoUpgrade") String autoUpgrade, 
			/** 自动升级需要达到的人数 */
			@RequestParam(value = "autoUpgradePeople") int autoUpgradePeople) {
		Map map = new HashMap();
		Brand brand = brandMangeBusiness.findBrandById(brandid);
		if (brand == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
			return map;
		}
		brand.setAutoUpgradePeople(autoUpgradePeople);
		brand.setAutoUpgrade(autoUpgrade);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brandMangeBusiness.mergeBrand(brand));
		return map;
	}
	/**激活升级*/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/update/activatetheupgrade/id")
	public @ResponseBody Object updateBrandActivateTheUpgradeById(HttpServletRequest request,

			@RequestParam(value = "brand_id") long brandid,
			/** 激活升级  0 表示不开通;1表示开通 */
			@RequestParam(value = "activate_the_upgrade") String activateTheUpgrade) {
		Map map = new HashMap();
		Brand brand = brandMangeBusiness.findBrandById(brandid);
		if (brand == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE, "贴牌不存在");
			return map;
		}
		brand.setActivateTheUpgrade(activateTheUpgrade);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brandMangeBusiness.mergeBrand(brand));
		return map;
	}
	/** 修改一个品牌 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/phone/update/id")
	public @ResponseBody Object updateBrandById(HttpServletRequest request,

			@RequestParam(value = "brand_id") long brandid, /** QQ */
			@RequestParam(value = "brand_qq", required = false) String brandQQ,

			/** 微信 */
			@RequestParam(value = "brand_weixin", required = false) String brandWeiXin, /** 电话 */
			@RequestParam(value = "brand_phone", required = false) String brandphone,
			@RequestParam(value = "share_title", required = false) String shareTitle,
			@RequestParam(value = "share_content", required = false) String shareContent
			) {
		Map map = new HashMap();
		Brand brand = brandMangeBusiness.findBrandById(brandid);
		if (brand == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE, "贴牌不存在");
			return map;
		}
		if (brandphone != null) {
			brand.setBrandPhone(brandphone);
		}
		if (brandWeiXin != null) {
			brand.setBrandWeiXin(brandWeiXin);
		}
		if (brandQQ != null) {
			brand.setBrandQQ(brandQQ);
		}
		brand.setShareContent((shareContent==null||"".equals(shareContent))?brand.getShareContent():shareContent);
		brand.setShareTitle((shareTitle==null||"".equals(shareTitle))?brand.getShareContent():shareTitle);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brandMangeBusiness.mergeBrand(brand));
		map.put(CommonConstants.RESP_MESSAGE, "修改成功");
		return map;
	}

	/** 根据品牌名字获取品牌 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/brand/query/name")
	public @ResponseBody Object queryBrandByName(HttpServletRequest request,
			@RequestParam(value = "brand_name") String name) {
		Map<String,Object> map = new HashMap<String,Object>();
		List<Brand> brands = brandMangeBusiness.findBrandByName(name);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brands);
		return map;

	}

	/** 管理者的Ip获取品牌 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/brand/query/managerid")
	public @ResponseBody Object queryBrandByManagerId(HttpServletRequest request,
			@RequestParam(value = "manager_id") long manageid) {
		Map<String,Object> map = new HashMap<String,Object>();
		Brand brand = brandMangeBusiness.findBrandByManageid(manageid);
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brand);
		return map;
	}

	/** 新建一个品牌费率 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandrate/add")
	public @ResponseBody Object addBrandRate(HttpServletRequest request, @RequestParam(value = "brand_id") long brandid,
			@RequestParam(value = "channel_id") long channelid,
			@RequestParam(value = "rate") String srate,
			@RequestParam(value = "extra_fee", defaultValue = "0.00", required = false) String sextraFee,
			@RequestParam(value = "status", defaultValue = "2", required = false) String status) {
		Map<String,Object> map = new HashMap<String,Object>();
		Double drate ;
		BigDecimal rate;
		try {
			if("".equals(srate.trim())){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "设置品牌费率失败,参数rate不能为'"+srate+"',请检查后重试!");
				return map;
			}
			rate =new  BigDecimal(srate).setScale(4, BigDecimal.ROUND_HALF_DOWN);
		} catch (NumberFormatException e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "设置品牌费率失败,参数rate不能为'"+srate+"',请检查后重试!");
			return map;
		}
		
		BigDecimal extraFee;
		try {
			if("".equals(sextraFee.trim())){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "设置品牌费率失败,参数extraFee不能为'"+sextraFee+"',请检查后重试!");
				return map;
			}
			extraFee =new  BigDecimal(sextraFee).setScale(2, BigDecimal.ROUND_HALF_DOWN);
		} catch (NumberFormatException e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "设置品牌费率失败,参数extraFee不能为'"+sextraFee+"',请检查后重试!");
			return map;
		}
		
		
		
		BrandRate brandRate = brandMangeBusiness.findRateByBrandAndChannel(brandid, channelid);
		if (brandRate == null) {
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "该通道不存在！！");
			return map;
		} else {
			if (brandRate.getMinrate() != null) {
				if (rate.doubleValue() < brandRate.getMinrate().doubleValue()) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "普通用户费率不能小于，平台底价" + brandRate.getMinrate());
					return map;

				}
				BigDecimal maxrate = new BigDecimal("0.02");
				if (maxrate.doubleValue() < rate.doubleValue()) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "普通用户费率不能大于2%");
					return map;
				}

			}

		}
		brandRate.setBrandId(brandid);
		brandRate.setChannelId(channelid);
		brandRate.setCreateTime(new Date());
		brandRate.setExtraFee(extraFee);
		brandRate.setRate(rate);
		brandRate.setStatus(status == null || status.length() == 0 ? brandRate.getStatus() : status);
		BrandRate brandrate = brandMangeBusiness.mergeBrandRate(brandRate);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brandrate);
		return map;
	}

	/** 根据品牌id, 渠道id 修改贴牌状态 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandrate/status/upload")
	public @ResponseBody Object queryBrandRateStatusByID(HttpServletRequest request,
			@RequestParam(value = "brand_id") long brandid, 
			@RequestParam(value = "channel_id") long channelid,
			// 0:开发中；1：正常使用；2：试运营
			@RequestParam(value = "status") String status) {
		Map map = new HashMap();
		BrandRate brandRate = null;
		try {
				brandRate = brandMangeBusiness.findRateByBrandAndChannel(brandid, channelid);
				brandRate.setStatus(status);
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESULT, brandMangeBusiness.mergeBrandRate(brandRate));
				map.put(CommonConstants.RESP_MESSAGE, "修改通道成功 ");

		} catch (Exception e) {
			LOG.info(e.getMessage());
		}
		return map;
	}

	/** 新建一个/多个品牌最低费率，同时将minrate的值赋给ChannelRate的rate */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandminrate/add")
	public @ResponseBody Object addBrandMinRate(HttpServletRequest request,
			@RequestParam(value = "brand_ids") Long[] brandidStr, 
			@RequestParam(value = "channel_id") long channelid,
			// 普通用户刷卡费率
			@RequestParam(value = "rate") BigDecimal rate,
			// 最低费率
			@RequestParam(value = "minrate") BigDecimal minrate,
			@RequestParam(value = "extra_fee", defaultValue = "0.0000", required = false) BigDecimal extraFee,
			@RequestParam(value = "with_draw_fee", defaultValue = "2.0000", required = false) BigDecimal withdrawFee,
			@RequestParam(value = "status", defaultValue = "2", required = false) String status) {
		Map<String,Object> map = new HashMap<>();
		BrandRate brandRate = null;
		//这里是为多个贴牌新增一条通道.	
		
		Channel channel=null;
		channel=channelRateBusiness.findChannelById(channelid);
		String costRateStr= channel.getCostRate();
		BigDecimal costRate=null;
		costRate=new BigDecimal(costRateStr);
		//lx 比较底价与他之间的问题
		if(costRate.doubleValue() > minrate.doubleValue() ) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE,"填写的最低费率低于通道底价费率" );
			return map;
		}else {
		
		long[] brandIds=new long[brandidStr.length];
		for(int i=0;i<brandIds.length;i++) {
			brandIds[i] = Long.valueOf(brandidStr[i]); 
		
		try {
				brandRate = brandMangeBusiness.findRateByBrandAndChannel(brandIds[i], channelid);
				if (brandRate == null) {
					brandRate = new BrandRate();
					brandRate.setBrandId(brandIds[i]);
					brandRate.setChannelId(channelid);
					brandRate.setCreateTime(new Date());
					brandRate.setRate(rate);
					brandRate.setMinrate(minrate);
					brandRate.setExtraFee(extraFee);
					brandRate.setWithdrawFee(withdrawFee);
					brandRate.setStatus(status);
				} else {
					
					brandRate.setCreateTime(new Date());
					brandRate.setRate(rate);
					brandRate.setMinrate(minrate);
					brandRate.setExtraFee(extraFee);
					brandRate.setWithdrawFee(withdrawFee);
					brandRate.setStatus(status);
				}
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESULT,  brandMangeBusiness.mergeBrandRate(brandRate));
				map.put(CommonConstants.RESP_MESSAGE, "修改通道成功 ");
				Brand brand =brandMangeBusiness.findBrandById(brandIds[i]);
				ChannelRate channelRate=channelRateBusiness.findChannelRateByUserid(brand.getManageid(), channelid,brand.getId());
				LOG.info("更改前的费率==========" + channelRate);
				if(channelRate==null) {
					channelRate=new ChannelRate();
				}
				channelRate.setUserId(brand.getManageid());
				channelRate.setRate(brandRate.getMinrate());
				channelRate.setBrandId(brandIds[i]);
				channelRate.setChannelId(channelid);
				channelRate.setExtraFee(brandRate.getExtraFee());
				channelRate.setWithdrawFee(brandRate.getWithdrawFee());
				channelRate.setCreateTime(new Date());
				channelRateBusiness.mergeChannelRate(channelRate);
				LOG.info("更改后的费率==========" + channelRate);

		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改通道失败 ");
			}
		
		}
	}
		return map;
}

	/** 传递通道id将所有平台信息和费率都取回来 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/rate/query/brandid")
	public @ResponseBody Object queryAllChannelByBrandId(HttpServletRequest request,
			@RequestParam("brandid") long brandid) {

		List<Channel> channels = channelRateBusiness.findAllChannel();

		List<Channel> result = new ArrayList<Channel>();
		for (Channel channel : channels) {
			Channel temp = channel;
			BrandRate brandRate = brandMangeBusiness.findRateByBrandAndChannel(brandid, channel.getId());
			if (brandRate == null) {
				temp.setRate(null);
			} else {
				temp.setRate(brandRate.getRate());
			}
			result.add(temp);
		}
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, result);
		return map;

	}
	
	/** 根据品牌id, 渠道id 获取改品牌的费率 老接口 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandrate/query")
	public @ResponseBody Object queryBrandRate(HttpServletRequest request,
			@RequestParam(value = "brand_id") long brandid, 
			@RequestParam(value = "channel_id") long channelid) {

		BrandRate brandRate = brandMangeBusiness.findRateByBrandAndChannel(brandid, channelid);
		if (brandRate == null) {
			return ResultWrap.init(CommonConstants.FALIED, "暂无该通道费率");
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "暂无该通道费率",brandRate);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandrate/queryby/brandid")
	public @ResponseBody Object queryBrandRate(HttpServletRequest request,
			@RequestParam(value = "brandId") long brandId) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<Object> list = new ArrayList<Object>();
		List<BrandRate> findRateByBrand = brandMangeBusiness.findRateByBrand(brandId);
		
		for(BrandRate brandRate : findRateByBrand) {
			
			long channelId = brandRate.getChannelId();
			
			Channel channel = channelRateBusiness.findByChannelId(channelId);
			if(channel == null || "0".equals(channel.getStatus())){
				continue;
			}
			
			BrandAndChannel bac = new BrandAndChannel();
			bac.setChannelId(channel.getId());
			bac.setChannelName(channel.getName());
			bac.setSingleMinLimit(channel.getSingleMinLimit());
			bac.setSingleMaxLimit(channel.getSingleMaxLimit());
			bac.setEveryDayMaxLimit(channel.getEveryDayMaxLimit());
			bac.setRemarks(channel.getRemarks());
			bac.setRate(brandRate.getRate());
			bac.setMinRate(brandRate.getMinrate());
			bac.setExtraFee(brandRate.getExtraFee());
			bac.setWithdrawFee(brandRate.getWithdrawFee());
			bac.setStatus(brandRate.getStatus());
			bac.setLog(channel.getLog());
			bac.setChannelParams(channel.getChannelParams());
			
			list.add(bac);
			
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		return map;

	}
	
	
	/** lx 根据品牌id, 渠道id 获取改品牌的费率 */
	//这里重新写一个,,只是String不同而已.
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandrate/queryRate")
	public @ResponseBody Object queryBrandRateByLx(HttpServletRequest request,
			@RequestParam(value = "brand_id") String brandId, 
			@RequestParam(value = "channel_id") long channelid) {
		String[] brandids=brandId.split(",");
		BrandRate brandRate = null;
		Map map = new HashMap();
		Brand brand=null;
		
		List<Map> bList=new ArrayList<Map>();
		for(int i=0;i<brandids.length;i++) {
			Map map1=new HashMap();
			long brandid=Long.parseLong(brandids[i]);
			brand=brandMangeBusiness.findBrandById(brandid);
			brandRate = brandMangeBusiness.findRateByBrandAndChannel(brandid, channelid);
			map1.put("brand", brand.getName());
			map1.put("brandRate", brandRate);
			bList.add(map1);
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, bList);
			
			return map;

	}
	
	/***** 积分----操作 ******/

	/** 根据品牌id, 获取用户的积分配置 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandcoin/query")
	public @ResponseBody Object queryBrandCoin(HttpServletRequest request,
			@RequestParam(value = "brand_id") long brandid) {

		BrandCoin brandCoin = brandMangeBusiness.findBrandCoin(brandid);

		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brandCoin);
		return map;

	}

	/** 根据品牌id, 获取用户的积分配置 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandcoin/save")
	public @ResponseBody Object addBrandCoin(HttpServletRequest request, @RequestParam(value = "brand_id") long brandid,
			@RequestParam(value = "ratio") BigDecimal ratio) {

		BrandCoin brandCoin = brandMangeBusiness.findBrandCoin(brandid);

		if (brandCoin == null) {
			brandCoin = new BrandCoin();
		}
		brandCoin.setRatio(ratio);
		brandCoin.setCreateTime(new Date());

		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brandMangeBusiness.addBrandCoin(brandCoin));
		return map;

	}

	/** 获取所有资源 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/brand/resource/all")
	public @ResponseBody Object queryAllReource(HttpServletRequest request) {

		List<Resource> resources = brandMangeBusiness.findAllResource();
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, resources);
		return map;
	}

	/***
	 * 查询指定用户的贴牌
	 **/
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/brand/user/{token}")
	public @ResponseBody Object queryUserBrandall(HttpServletRequest request,
			// token
			@PathVariable("token") String token) {
		Map map = new HashMap();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {

			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;

		}
		User use = new User();
		use = userLoginRegisterBusiness.queryUserById(userId);
		List<User> users = new ArrayList<User>();
		if (use != null) {
			users = userLoginRegisterBusiness.findAfterUsers(use.getId());
			users.add(use);
		}
		List<Brand> brands = new ArrayList<Brand>();
		String ids = "";
		for (User user : users) {
			ids += user.getId() + ",";
		}
		if (ids.length() > 0) {
			ids = ids.substring(0, ids.length() - 1);
			String[] str1 = ids.split(",");
			Long[] manageids = new Long[str1.length];
			for (int j = 0; j < str1.length; j++) {
				manageids[j] = Long.valueOf(str1[j]);
			}
			brands = brandMangeBusiness.findBrandByManageids(manageids);
		}
		brands.add(brandMangeBusiness.findBrandById(use.getBrandId()));
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brands);

		return map;
	}

	/*** =============版本和下载地址=========== ***/
	/***
	 * 
	 * 版本号查询
	 **/

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/versionnumber")
	public @ResponseBody Object Android_IOS_VersionNumber(HttpServletRequest request,
			@RequestParam(value = "brand_id", defaultValue = "1", required = false) String sbrandid) {
		Map<String, Object> android = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		if ("".equals(sbrandid.trim())) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的贴牌号有误,贴牌号不能为" + sbrandid + ",请检查后重试!!!");
			return map;
		}
		long brandid = 1;

		try {
			brandid = Long.valueOf(sbrandid.trim());
		} catch (NumberFormatException e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的贴牌号有误,贴牌号不能为" + sbrandid + ",请检查后重试!!!");
			return map;
		}

		Brand brand;
		try {
			brand = brandMangeBusiness.findBrandById(brandid);
		} catch (Exception e) {
			LOG.error("查询贴牌异常=========================");
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的贴牌号有误,请检查后重试");
			return map;
		}
		if (brand == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "新版上线，数据维护，预计时间为3天");
			return map;
		}

		Map ios = new HashMap();
		android.put("version", brand.getAndroidVersion());
		android.put("url", brand.getAndroidDownload());
		ios.put("version", brand.getIosVersion());
		ios.put("url", brand.getIosDownload());
		Map and = new HashMap();
		and.put("android", android);
		and.put("downloadAndroidUrl", brand.getAndroidDownloadUrl());
		and.put("iOS", ios);

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, and);
		return map;

	}

	/***
	 * 
	 * 版本号和地址修改
	 **/

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/version/update")
	public @ResponseBody Object updateBrandVersionByBrandId(HttpServletRequest request,
			@RequestParam(value = "brand_id") long brandid, /** 极光APP秘钥 */
			@RequestParam(value = "appkey", required = false) String appkey,

			/** 极光APP推送秘钥 */
			@RequestParam(value = "mastersecret", required = false) String mastersecret,

			/** 短信发送码 */
			@RequestParam(value = "tplid", required = false) String tplid,

			/** 400电话 */
			@RequestParam(value = "brand_phone", required = false) String brandphone,

			/** QQ */
			@RequestParam(value = "brand_qq", required = false) String brandQQ,

			/** 微信 */
			@RequestParam(value = "brand_weixin", required = false) String brandWeiXin,
//			微信公众号名称
			@RequestParam(value = "weChatName", required = false) String weChatName,
//			微信公众号链接
			@RequestParam(value = "weChatUrl", required = false) String weChatUrl,

			/** 安卓版本描述 */
			@RequestParam(value = "android_content", required = false) String androidContent,

			/** iOS版本描述 */
			@RequestParam(value = "ios_content", required = false) String iosContent,

			/** 分享地址 */
			@RequestParam(value = "share_main_address", required = false) String shareMainAddress,

			/** 分享标题 */
			@RequestParam(value = "share_title", required = false) String sharetitle,

			/** 分享内容 */
			@RequestParam(value = "share_content", required = false) String sharecontent,

			// android版本号
			@RequestParam(value = "androidVersion", required = false) String androidVersion,
			// android下载地址
			@RequestParam(value = "download_android", required = false) String downloadAndroid,
			// android微信下载地址
			@RequestParam(value = "download_android_url", required = false) String downloadAndroidUrl,
			// ios版本号
			@RequestParam(value = "iosVersion", required = false) String iosVersion,
			// ios下载地址
			@RequestParam(value = "download_ios", required = false) String downloadIos,
			//还款的链接
			@RequestParam(value = "repaymentUrl", required = false) String repaymentUrl
			) {
		Map map = new HashMap();

		Brand brand = brandMangeBusiness.findBrandById(brandid);
		if (brand == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESULT, brand);
			map.put(CommonConstants.RESP_MESSAGE, "贴牌不存在");
			return map;
		}
		if (brand.getAppkey() != null && !brand.getAppkey().equals("")) {
			if (androidVersion != null && !androidVersion.equals("")&& !androidVersion.equals(brand.getAndroidVersion())) {
				/**
				 * 推送信息 *URL：/v1.0/user/jpush/android/brand
				 **/
				String alert = "版本更新提示";
				String content = "发现新版本" + androidVersion + "，请下载新版本！";
				// 备注
				String btype = "androidVersion";
				String btypeval = androidVersion;
				/** 获取身份证实名信息 */
				userJpushService.setJpushtestall(request, brandid,  alert + "", content + "", btype + "", btypeval + "");
				brand.setAndroidVersion(androidVersion);
			}
		}

		brand.setAndroidVersion(brand.getAndroidVersion());

		brand.setAndroidDownload(downloadAndroid == null || downloadAndroid.equals("") ? brand.getAndroidDownload() : downloadAndroid);

		brand.setAndroidDownloadUrl(downloadAndroidUrl == null || downloadAndroidUrl.equals("") ? brand.getAndroidDownloadUrl() : downloadAndroidUrl);
		
		brand.setIosVersion(iosVersion == null || iosVersion.equals("") ? brand.getIosVersion() : iosVersion);

		brand.setIosDownload(downloadIos == null || downloadIos.equals("") ? brand.getIosDownload() : downloadIos);

		brand.setAppkey(appkey == null || appkey.equals("") ? brand.getAppkey() : appkey);

		brand.setMastersecret(mastersecret == null || mastersecret.equals("") ? brand.getMastersecret() : mastersecret);

		brand.setTplid(tplid == null || tplid.equals("") ? brand.getTplid() : tplid);

		brand.setBrandPhone(brandphone == null || brandphone.equals("") ? brand.getBrandPhone() : brandphone);

		brand.setBrandQQ(brandQQ == null || brandQQ.equals("") ? brand.getBrandQQ() : brandQQ);

		brand.setBrandWeiXin(brandWeiXin == null || brandWeiXin.equals("") ? brand.getBrandWeiXin() : brandWeiXin);

		brand.setAndroidContent(androidContent == null || androidContent.equals("") ? brand.getAndroidContent() : androidContent);

		brand.setIosContent(iosContent == null || iosContent.equals("") ? brand.getIosContent() : iosContent);

		brand.setShareTitle(sharetitle == null || sharetitle.equals("") ? brand.getShareTitle() : sharetitle);

		brand.setShareContent(sharecontent == null || sharecontent.equals("") ? brand.getShareContent() : sharecontent);

		brand.setShareMainAddress(shareMainAddress == null || shareMainAddress.equals("") ? brand.getShareMainAddress(): shareMainAddress);
		
		brand.setWeChatName(weChatName == null || "".equals(weChatName)?brand.getWeChatName():weChatName); 
		
		brand.setWeChatUrl(weChatUrl == null || "".equals(weChatUrl) ?brand.getWeChatUrl():weChatUrl);
		
		brand.setRepaymentUrl(repaymentUrl == null || "".equals(repaymentUrl) ? brand.getRepaymentUrl() : repaymentUrl);
		
		brand = brandMangeBusiness.mergeBrand(brand);

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, brand);
		map.put(CommonConstants.RESP_MESSAGE, "修改成功");
		return map;

	}

	/**
	 * android数据添加数据
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/androidapp/upload")
	public @ResponseBody Object uploadBrandApp(HttpServletRequest request, @RequestParam("android") MultipartFile file,
			@RequestParam(value = "brand_id") long brandid
			) {
		Map map = new HashMap();
		Brand brand = brandMangeBusiness.findBrandById(brandid);
		if (brand == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESULT, brand);
			map.put(CommonConstants.RESP_MESSAGE, "贴牌不存在");
			return map;
		}
		String src = brandid + "/android";

		File dir = new File(realnamePic + src);

		if (!dir.exists()) {
			dir.mkdirs();
		}
//		// 创建目录
//		if (dir.mkdirs()) {
//
//		} else {
//
//			File[] tempfiles = dir.listFiles();
//			for (File tfile : tempfiles) {
//				tfile.delete();
//			}
//
//			System.out.println("创建目录" + realnamePic + src + "失败！");
//		}
		
//		String ossObjectNamePrefix = AliOSSUtil.APP_ANDROID + "-" + brandid + "-";
//		List<String> listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
//		if (listFiles != null && listFiles.size()>0) {
//			for(String fileName:listFiles){
//				aliOSSUtil.deleteFileFromOss(fileName);
//			}
//		}
//
		String fileName = "";
		String ossObjectName = "";
		if (file != null) {
			fileName = file.getOriginalFilename();
			String prefix = fileName.substring(fileName.lastIndexOf("."));
			fileName = System.currentTimeMillis() + prefix;
			
//			ossObjectName = ossObjectNamePrefix + fileName;
			
//			try {
////				aliOSSUtil.uploadStreamToOss(ossObjectName,file.getInputStream());
//
//			} catch (IOException e1) {
//				e1.printStackTrace();LOG.error(ExceptionUtil.errInfo(e1));
//			}
			File dest = new File(dir + "/" + fileName);
			LOG.info("上传文件==============================="+dest);
			try {
				if (dest.exists()) {
					dest.delete();
				}
				file.transferTo(dest);
				Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
			} catch (Exception e) {
				LOG.error("保存文件出错啦======");
				e.printStackTrace();

				return ResultWrap.init(CommonConstants.FALIED, "保存文件失败!");

			}
			/*File dest = new File(realnamePic + src + "/" + fileName);
			try {
				file.transferTo(dest);
			} catch (IllegalStateException e) {
				e.printStackTrace();LOG.error("",e);
			} catch (IOException e) {
				e.printStackTrace();LOG.error("",e);
			}*/

		}
		
//		String fileUrl = aliOSSUtil.getFileUrl(ossObjectName);
		String fileUrl=downloadPath+src+"/"+fileName;
//		brand.setAndroidDownload(downloadPath + src + "/" + fileName);
		brand.setAndroidDownload(downloadUrl + "?url=" +fileUrl);
		brand.setAndroidDownloadUrl(downloadUrl + "?url=" +fileUrl);
		brand = brandMangeBusiness.mergeBrand(brand);

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, brand);
		return map;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/download/app")
	public @ResponseBody Object downloadApp(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(value="url")String url) throws IOException{
//		if (request.getHeader("user-agent").indexOf("MicroMessenger") > 0) {
//			String objectName = url.substring(url.lastIndexOf("/")+1, url.length());
//			aliOSSUtil.downloadStream(objectName,response);
//		}else {
			response.sendRedirect(url);
//		}
		return null;
	}
	
	/**
	 * ios数据添加数据
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brand/iosapp/upload")
	public @ResponseBody Object uploadBrandIOSApp(HttpServletRequest request,
			@RequestParam("android") MultipartFile file,
			@RequestParam(value = "brand_id") long brandid
			) {
		Map map = new HashMap();
		Brand brand = brandMangeBusiness.findBrandById(brandid);
		if (brand == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESULT, brand);
			map.put(CommonConstants.RESP_MESSAGE, "贴牌不存在");
			return map;
		}
		
//		String ossObjectNamePrefix = AliOSSUtil.APP_IOS + "-" + brandid + "-";
//		List<String> listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
//		if (listFiles != null && listFiles.size()>0) {
//			for(String fileName:listFiles){
//				aliOSSUtil.deleteFileFromOss(fileName);
//			}
//		}
		String src = brandid + "/ios";

		File dir = new File(realnamePic + src);

		if (!dir.exists()) {
			dir.mkdirs();
		}
		String fileName = "";
		String ossObjectName = "";
		if (file != null) {
			fileName = file.getOriginalFilename();
			String prefix = fileName.substring(fileName.lastIndexOf("."));
			fileName = System.currentTimeMillis() + prefix;

//			ossObjectName = ossObjectNamePrefix + fileName;

//			try {
////				aliOSSUtil.uploadStreamToOss(ossObjectName,file.getInputStream());
//
//			} catch (IOException e1) {
//				e1.printStackTrace();LOG.error(ExceptionUtil.errInfo(e1));
//			}
			File dest = new File(dir + "/" + fileName);
			LOG.info("上传文件==============================="+dest);
			try {
				if (dest.exists()) {
					dest.delete();
				}
				file.transferTo(dest);
				Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
			} catch (Exception e) {
				LOG.error("保存文件出错啦======");
				e.printStackTrace();

				return ResultWrap.init(CommonConstants.FALIED, "保存文件失败!");

			}
			/*File dest = new File(realnamePic + src + "/" + fileName);
			try {
				file.transferTo(dest);
			} catch (IllegalStateException e) {
				e.printStackTrace();LOG.error("",e);
			} catch (IOException e) {
				e.printStackTrace();LOG.error("",e);
			}*/

		}
		
//		String fileUrl = aliOSSUtil.getFileUrl(ossObjectName);
		String fileUrl=downloadPath+src+"/"+fileName;
		//brand.setAndroidDownload(downloadPath + src + "/" + fileName);
		brand.setIosDownload(fileUrl);
		brand = brandMangeBusiness.mergeBrand(brand);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, brand);
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/brand/update/autorebateonoff/by/brandid")
	public @ResponseBody Object updateAutoRebateConfigOnOffStatus(HttpServletRequest request,
			@RequestParam(value = "brandId") String sbrandId,
			@RequestParam(value="onOff")String sonOff){
		Map<String,Object>map = new HashMap<String,Object>();
		try {
			if("".equals(sbrandId.trim())||"".equals(sonOff.trim())||((Integer.valueOf(sonOff.trim())).intValue() !=0&&(Integer.valueOf(sonOff.trim())).intValue() != 1)){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的参数有误,请检查后重新输入!");
				return map;
			}
		} catch (Exception e1) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的参数有误,请检查后重新输入!");
			return map;
		}
		Brand model = null;
		try {
			model = brandMangeBusiness.findBrandById(Integer.valueOf(sbrandId));
		} catch (Exception e) {
			LOG.error("根据brandId查询贴牌异常=============================:");
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.您输入的贴牌号有误,请检查后重新输入!");
			return map;
		}
		if(model==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.查询不到您要的贴牌,请确认后重新查询!");
			return map;
		}
		model.setAutoRebateConfigOnOff(Integer.valueOf(sonOff));
		try {
			model = brandMangeBusiness.mergeBrand(model);
		} catch (Exception e) {
			LOG.error("保存贴牌异常=============================:");
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.发生位置错误了哦,请稍后重试!");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "修改自动返利开关状态成功");
		map.put(CommonConstants.RESULT,model);
		return map;
	}
	
	//修改贴牌描述接口
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/brand/update/branddescription")
	@ResponseBody
	public Object updateBrandDescription(HttpServletRequest request,
			@RequestParam(value = "id") long brandid,
			@RequestParam(value = "brandDescription",defaultValue="",required=false) String branddescription) {
		Map map = new HashMap();
		brandMangeBusiness.updateBrandMange(brandid, branddescription);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

}
